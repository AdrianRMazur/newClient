package btclient;

import java.io.IOException;
import java.util.ArrayList;


public class Downloader extends BTClient implements Runnable{ 
	
	private static Peer currpeer; 
	
	
	public Downloader(Peer peer) {
		
		currpeer = peer; 
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
				currpeer.send().write(interested);
				currpeer.send().flush();
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
		
		for (int count = 0; count <downloaded.length; count++){
			System.out.println(count);
			int temp = 0; 
			if (startedDL[count] == true){
				continue; 
			}
			else{ 
				startedDL[count]=true;
			}
			
			downloaded[count] = new byte [torrentinfo.piece_length];
			for (;;){
				byte [] msgrequest = new byte [17];
				System.arraycopy(toEndianArray(13), 0, msgrequest, 0, 4);
				msgrequest[4] = (byte)6;
				// normal pieces 
				if (count < torrentinfo.piece_hashes.length-1){
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
					
								
					for (int c = temp; c< 16384; c++){
						try {
							downloaded [count][c] = currpeer.receive().readByte();
						} catch (IOException e) {
							return false; 
						}
					}
					
					//savefile.write(peerresponse);
				
					if (temp + 16384 == torrentinfo.piece_length)
						break; 
					else
						temp = temp + 16384; 
				}
				// last piece
				else {
					int size = 16384;  
					if (lastpiecelength < 16384)
						size = lastpiecelength; 
					
					lastpiecelength = lastpiecelength - 16384;
					
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
	
					
					for (int c = temp; c< size; c++){
						try {
							downloaded[count][c] = currpeer.receive().readByte();
						} catch (IOException e) {
							return false; 
						}
					}
				//	savefile.write(peerresponse);
					
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
			// peer not required
			return; 
		}
	
		

		
		if (currpeer.openSocket() == false) {
			System.out.println("333");
			// connection failed on socket creation;
			return;
		}

	
		
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
		
		System.out.println("made it here3");
		if(getdata() == false){
			System.out.println("Error getting data");
			currpeer.closeSocket();
			return; 
		}
		
		currpeer.closeSocket(); 
	
	}
	
	

}
