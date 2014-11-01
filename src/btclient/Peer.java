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
	private Thread t = new Thread ();	
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
	
	
	public Object clone2(){  
	    try{  
	        return super.clone();  
	    }catch(Exception e){ 
	        return null; 
	    }
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
			e.printStackTrace();
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
			ToolKit.print(message.toShake);
			dataout.write(message.toShake);
			dataout.flush();
			
			s.setSoTimeout(1000);
		} catch (IOException e) {
			

			return false; 
		}
		
		try {
			while(datain.readByte()!=(byte)19);
			ToolKit.print(message.toShake);
			datain.readFully(fromShake);
			ToolKit.print(fromShake);
		} catch (IOException e) {
			System.out.println("made it through handshake fail");
			
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
		
		System.out.println("From peer handshake: ");
		ToolKit.print(fromShake);
		
		//return back to them, but with our part of the handshake
		Message message = new Message(Constants.BITTORRENTPROTOCOL, Constants.PEERID,BTClient.torrentinfo); 
		dataout.write(message.toShake);
		dataout.flush();
		
		/*Start reading the bytes sent to us*/
		/*have to code to start choking when cannot keep up*/
		for(int j=0;j<torrentinfo.piece_hashes.length*2+1;j++){/*limit this, so we dont get EOF*/
			
			int prefix= datain.readInt(); //Length-prefix
			System.out.println("datain prefix: " + prefix);
			if(prefix==0){
				continue; 
			}
			byte id=datain .readByte();//message ID
			System.out.println("id: "+id);

			if(id==Constants.INTERESTED_ID){//Interested, return interest 
			//	System.out.println("Here");
				byte[] unChoke=new byte[5]; 
				System.arraycopy(BTClient.toEndianArray(1), 0, unChoke, 0, 4);
				unChoke[4]= (byte)1; 
				dataout.write(unChoke);
			}
			
			if(id==Constants.HAVE_ID){ //Have 
				datain.readInt();
				continue; 
			}
			else if(id==Constants.REQUEST_ID){ //request, get the payload bits 
				index=datain.readInt();
				begin=datain.readInt();
				length=datain.readInt(); 
				block=new byte[length]; //
				
				/*if(begin==435){
					begin=16384;
				}*/
				
				if(this.checkIfAvailable(index)){
					BTClient.u=u+length; 
					System.out.println("Index: "+ index+ " begin: "+begin+" length: "+ length );
					System.arraycopy(BTClient.downloaded[index], begin, block, 0, length);
					//block=BTClient.downloaded[index];
					if(begin==435){
						begin=16384;
					}
					
					Message piece = new Message(9+length, (byte)7,begin,id,block); //maybe one bit off here???
					
					dataout.write(piece.upload);
					//Maybe add what has been already uploaded? like an array
				}
				else{/*Give time to catch up!!!*/
					try {
						Thread.sleep(9000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					BTClient.u=u+length; 
					System.out.println("Index: "+ index+ " begin: "+begin+" length: "+ length );
					System.arraycopy(BTClient.downloaded[index], begin, block, 0, length);
					if(begin==435){
						begin=16384;
					}
					
					Message piece = new Message(9+length, (byte)7,begin,id,block); 
					
					dataout.write(piece.upload);
					
				}
				
				
			}
			
		
			
		}
		try {
			Thread.sleep(9000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
			
		
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
