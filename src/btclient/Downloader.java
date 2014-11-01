package btclient;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;



public class Downloader extends BTClient implements Runnable{ 
	private static  int port;
	private static String ip = null;
	private static Socket s; 
	private static InputStream input; 
	private static OutputStream output; 
	private static DataOutputStream dataout; 
	private static DataInputStream datain; 

	
	
	public Downloader(Map<ByteBuffer,Object> peerinfo) {
		
		port = (Integer) peerinfo.get(Constants.PORT);
		
		try {
			ip = new String ( ((ByteBuffer)peerinfo.get(Constants.IP)).array(), "ASCII" );
		} catch (UnsupportedEncodingException e) {}
	}
	
	private boolean openSocket(){
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

	private  void closeSocket(){
		try {
			s.close();
			input.close(); 
			output.close();
			datain.close();
			dataout.close();
		} catch (IOException e){}
	}


	private  boolean shakeHands(Message message, TorrentInfo torrentinfo){
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



	//not sure about these methods
	private DataInputStream receive (){
		return datain;
	}

	private DataOutputStream send (){
		return dataout; 
	}

	private Socket modifysocket(){
		return s; 
	}

	public int getPort(){
		return port; 
	}

	public String getIP(){
		return ip; 
	}
	
	private static boolean unchokepeer(){
		boolean unChoke = false;
		
		unChoke = false;
		// lastPieceLength= torrentinfo.file_length - (torrentinfo.piece_length
		// * (torrentinfo.piece_hashes.length-1));
		byte str;

		while (unChoke == false) {
			byte[] interested = new byte[5];
			System.arraycopy(toEndianArray(1), 0, interested, 0, 4);
			interested[4] = (byte) 2;
			try {
				dataout.write(interested);
				dataout.flush();
				currpeer.modifysocket().setSoTimeout(1300000);
			} catch (IOException e) {
				return false; 
			}
			

			for (int c = 0; c < 5; c++) {
				if (c == 4) {
					try {
						str = currpeer.receive().readByte();
						if (str == 1) {
							unChoke = true;
							break;
						}
					} catch (IOException e) {
						return false ; 
					}
				}
				try {
					currpeer.receive().readByte();
				} catch (IOException e) {
					return false ; 
				}
			}
		}
		return true; 
	}
	
	
	private static boolean getdata(){
		 
		int lastpiecelength= torrentinfo.file_length - (torrentinfo.piece_length * (torrentinfo.piece_hashes.length-1));
		System.out.println(lastpiecelength);
		for (int count = 0; count <downloaded.length; count++){
			System.out.println(count);
			int temp = 0; 
			if (startedDL[count] == true){
				continue; 
			}
			else{ 
				startedDL[count]=true;
			}
			
		
			for (;;){
				byte [] msgrequest = new byte [17];
				System.arraycopy(toEndianArray(13), 0, msgrequest, 0, 4);
				msgrequest[4] = (byte)6;
				// normal pieces 
				if (count < torrentinfo.piece_hashes.length-1){
					if (temp==0){
						downloaded[count] = new byte [torrentinfo.piece_length];
						//downloaded2[count] = new byte [16384];
					}
					System.arraycopy(toEndianArray(count), 0, msgrequest, 5, 4);
					System.arraycopy(toEndianArray(temp), 0, msgrequest, 9, 4);
					System.arraycopy(toEndianArray(16384), 0, msgrequest, 13, 4);
					
					try {
						currpeer.send().write(msgrequest);
						currpeer.send().flush();
						currpeer.modifysocket().setSoTimeout(130000);
					} catch (IOException e) {
						return false; 
					}
					
					
					for (int i = 0; i < 13; i++) {
						try {
							currpeer.receive().readByte();
						} catch (IOException e) {
							return false; 
						}
					} 
					
					for (int c = temp; c< 16384+temp; c++){
						try {
							downloaded [count][c] = currpeer.receive().readByte();
						} catch (IOException e) {
							return false; 
						}
					}
					
			
					
	
				
					if (temp + 16384 == torrentinfo.piece_length)
						break; 
					else
						temp = temp + 16384; 
				}
				// last piece
				else {
					int size = 16384;  
					if (lastpiecelength < 16384){
						System.out.println("asdfsdfsd");
						size = lastpiecelength; 
						if(temp==0){
							downloaded[count] = new byte[size];
						}	
					} else if (temp ==0){
						downloaded[count] = new byte [size + (lastpiecelength-size)];
					}
					
					lastpiecelength = lastpiecelength - 16384;
				//	if (temp==0 ){
					//	downloaded[count] = new byte[size +()];
					//}
					//else {
					//	downloaded2[count] = new byte [size];
					//}
					
					System.arraycopy(toEndianArray(count), 0, msgrequest, 5, 4);
					System.arraycopy(toEndianArray(temp), 0, msgrequest, 9, 4);
					System.arraycopy(toEndianArray(size), 0, msgrequest, 13, 4);
					
					try {
						currpeer.send().write(msgrequest);
						currpeer.send().flush(); 
						currpeer.modifysocket().setSoTimeout(1000);
					} catch (IOException e) {
						
						return false; 
					}
				
					
					for (int i = 0; i < 13; i++) {
						try {
							currpeer.receive().readByte();
						} catch (IOException e) {
							return false; 
						}
					} 
	
					
					for (int c = temp; c< size+temp; c++){
						try {
							downloaded[count][c] = currpeer.receive().readByte();
						} catch (IOException e) {
							return false; 
						}
					}
					
					if (lastpiecelength < 0 )
						break; 
					
					temp  = temp + size; 
					
				}
				
			}
			completedDL[count] = true; 
		}
		return true; 
	}
	
	
	public void run(){
		//System.out.println(currpeer.getIP() + " " + currpeer.getPort());
		
		if (!(currpeer.getIP().equals("128.6.171.131") || currpeer.getIP().equals("128.6.171.130"))){
			return; 
		}
	
		

		
		if (currpeer.openSocket() == false) {
			System.out.println("Opening the socket failed.");
// connection failed on socket creation;
			return;
		}

	System.out.println(currpeer.getIP() + " " + currpeer.getPort());
	System.out.println("***********************");
		
		Message message = new Message(Constants.BITTORRENTPROTOCOL,
				Constants.PEERID, torrentinfo);

		if (currpeer.shakeHands(message, torrentinfo) == false) {
			currpeer.closeSocket(); 
			// error message;
			return;
		}
	
		System.out.println("made it here2");

	
		if (unchokepeer() == false){
			System.out.println("Error during unchoke");
			currpeer.closeSocket();
			return; 
		}
		
		
		System.out.println("made it here3");
		
		if(getdata() == false){
			System.out.println("Error getting data");
			currpeer.closeSocket();
			return; 
		}
		
		System.out.println("made it here4");
		if(getdata() == false){
			System.out.println("Error getting data");
			currpeer.closeSocket();
			return; 
		}
		
		currpeer.closeSocket(); 
	
	}
	

	
	

}
