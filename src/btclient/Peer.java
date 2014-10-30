package btclient;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Map;

public class Peer extends BTClient implements Runnable {
	
	private int port;
	private String ip = null;
	private Socket s; 
	private InputStream input; 
	private OutputStream output; 
	private DataOutputStream dataout; 
	private DataInputStream datain; 

	public Peer(Map<ByteBuffer,Object> peerinfo){
		port = (Integer) peerinfo.get(Constants.PORT);
			try {
				ip = new String ( ((ByteBuffer)peerinfo.get(Constants.IP)).array(), "ASCII" );
			} catch (UnsupportedEncodingException e) {}
	}
	
	public Peer(Socket con) throws IOException{
		output=con.getOutputStream();
		input=con.getInputStream();
		datain=new DataInputStream(input);
		dataout=new DataOutputStream(output);
		
	}
	
	public boolean openSocket(){
		try {
			s = new Socket (ip, port);
			input= s.getInputStream();
			output = s.getOutputStream(); 
			dataout= new DataOutputStream(output);
			datain=new DataInputStream(input);
		} catch (IOException e){
			// socket creation failed at this port 
			// retuning false. try another peer to open socket. 
			return false; 
		}
		return true; 
	}

	public void closeSocket(){
		try {
			s.close();
			input.close(); 
			output.close();
			datain.close();
			dataout.close();
		} catch (IOException e){}
	}


	public boolean shakeHands(Message message, TorrentInfo torrentinfo){
		byte[] fromShake = new byte[67];
		try {
			dataout.write(message.toShake);
			dataout.flush();
			s.setSoTimeout(1000);
		} catch (IOException e) {
			// handshake sending failed 
			return false; 
		}
		
		try {
			while(datain.readByte()!=19);
			datain.read(fromShake);
		} catch (IOException e) {
			// handshake failed receiving something
			return false; 
		}

		byte[] infohashpart = Arrays.copyOfRange(fromShake, 27, 47);

		if (Arrays.equals(infohashpart, torrentinfo.info_hash.array()) == false){
			return false; 
		}
		


		return true; 
	}
	
	public boolean uploadToPeer() throws IOException{
		byte[] fromShake=new byte[68];
		byte [] block; 
		datain.read(fromShake); 
		int i; 
		int index;
		int begin;
		int length;
		
		for(i=0; i<68; i++){
			if(fromShake[i]!=(byte)19){
				System.out.println("The connecting host is not Bit Torrent");
				return false; 
			}
		}
		//return back to them, but with our part of the handshake
		Message message = new Message(Constants.BITTORRENTPROTOCOL, Constants.PEERID,BTClient.torrentinfo); 
		dataout.write(message.toShake);
		dataout.flush();
		
		/*Start reading the bytes sent to us*/
		for(;;){
			int prefix= datain.readInt(); //Length-prefix
			if(prefix==0){
				continue; 
			}
			byte id=datain .readByte();//message ID
			
			if(id==Constants.HAVE_ID){ //Have 
				datain.readInt();
				continue; 
			}
			else if(id==Constants.REQUEST_ID){ //request, get the payload bits 
				index=datain.readInt();
				begin=datain.readInt();
				length=datain.readInt(); 
				block=new byte[length]; 
				
				
				if(this.checkIfAvailable(index)){
					System.arraycopy(BTClient.downloaded, begin, block, 0, length);
					Message piece = new Message(9+length, (byte)7,begin,id,block); 
					dataout.write(piece.upload);
					//Maybe add what has been already uploaded? like an array
				}
				
			}
			
			
			
		}
			
		
	}
	
	


// not sure about these methods
	public DataInputStream receive (){
		return datain;
	}

	public DataOutputStream send (){
		return dataout; 
	}
	
	public Socket modifysocket(){
		return s; 
	}

	public int getPort(){
		return port; 
	}
	
	public String getIP(){
		return ip; 
	}

	public void run() {
		try {
			uploadToPeer();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
	}
	
	
	
	
}
