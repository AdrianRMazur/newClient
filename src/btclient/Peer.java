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
	
	
	public Peer(Socket con) throws IOException{
		output=con.getOutputStream();
		input=con.getInputStream();
		datain=new DataInputStream(input);
		dataout=new DataOutputStream(output);
		
	}
	
	
	
	public boolean uploadToPeer() {
		byte[] fromShake=new byte[68];
		byte [] block; 
		try {
			datain.read(fromShake);
		} catch (IOException e1) { 
			e1.printStackTrace();
			return false;
		} 
		int index;
		int begin;
		int length;
		
		/*System.out.println("From peer handshake: ");
		ToolKit.print(fromShake);*/
		
		//return back to them, but with our part of the handshake
		Message message = new Message(Constants.BITTORRENTPROTOCOL, Constants.PEERID,BTClient.torrentinfo); 
		try {
			dataout.write(message.toShake);
			dataout.flush();
		} catch (IOException e1) {
			
			return false;
		}
		
		
		/*Start reading the bytes sent to us*/
		/*have to code to start choking when cannot keep up*/
		for(int j=0;j<torrentinfo.piece_hashes.length*2+1;j++){/*limit this, so we dont get EOF*/
			
			int prefix;
			try {
				prefix = datain.readInt();
			} catch (IOException e1) {
				
				return false;
			} //Length-prefix
			/*System.out.println("datain prefix: " + prefix);*/
			if(prefix==0){
				continue; 
			}
			byte id;
			try {
				id = datain.readByte();
			} catch (IOException e1) {
				
				return false;
			}//message ID
		/*	System.out.println("id: "+id);*/

			if(id==Constants.INTERESTED_ID){//Interested, return interest 
			//	System.out.println("Here");
				byte[] unChoke=new byte[5]; 
				System.arraycopy(BTClient.toEndianArray(1), 0, unChoke, 0, 4);
				unChoke[4]= (byte)1; 
				try {
					dataout.write(unChoke);
				} catch (IOException e) {
					
					return false;
				}
			}
			
			if(id==Constants.HAVE_ID){ //Have 
				try {
					datain.readInt();
				} catch (IOException e) {
					
					return false;
				}
				continue; 
			}
			else if(id==Constants.REQUEST_ID){ //request, get the payload bits 
				try {
					index=datain.readInt();
					begin=datain.readInt();
					length=datain.readInt(); 
				} catch (IOException e1) {
				
					return false; 
				}

				block=new byte[length]; //
				
				/*if(begin==435){
					begin=16384;
				}*/
				
				if(this.checkIfAvailable(index)){
					BTClient.u=u+length; 
				/*	System.out.println("Index: "+ index+ " begin: "+begin+" length: "+ length );*/
					System.arraycopy(BTClient.downloaded[index], begin, block, 0, length);
					//block=BTClient.downloaded[index];
					if(begin==435){
						begin=16384;
					}
					
					Message piece = new Message(9+length, (byte)7,begin,id,block); //maybe one bit off here???
					
					try {
						dataout.write(piece.upload);
					} catch (IOException e) {
						
						return false; 
					}
					//Maybe add what has been already uploaded? like an array
				}
				else{/*Give time to catch up!!!*/
					try {
						Thread.sleep(9000);
					} catch (InterruptedException e) {	}
					BTClient.u=u+length; 
					System.out.println("Index: "+ index+ " begin: "+begin+" length: "+ length );
					System.arraycopy(BTClient.downloaded[index], begin, block, 0, length);
					if(begin==435){
						begin=16384;
					}
					
					Message piece = new Message(9+length, (byte)7,begin,id,block); 
					
					try {
						dataout.write(piece.upload);
					} catch (IOException e) {
						
						return false; 
					}
					
				}
			}
		}
		try {
			Thread.sleep(9000);
		} catch (InterruptedException e) {
			return false; 
		}
		return true;
			
		
	}
	
	

	
	public void run() {
		if(uploadToPeer()==false){
			System.out.println("Issue uploading to peer, please re-run the program. ");
		}
	}
	
	
	
}
