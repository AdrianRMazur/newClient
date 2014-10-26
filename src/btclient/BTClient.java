package btclient;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class BTClient {

	private static FileOutputStream savefile = null;
	private static DataInputStream input;
	
	
	public static void main (String [] args) {
		
		if (args.length!=2){
			System.out.println("Error: Provide torrent file name and save file name. \n");
			System.exit(1);
		} 
		
		try {
			savefile = new FileOutputStream(new File(args[1]));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		input = null; 
		File inputtorrent = new File (args[0]);
		int torrentsize = (int) inputtorrent.length();
		
		if (torrentsize > 1000000){
			System.out.println("Error: File size too large");
			System.exit(1);
		}
		
		byte[] torrentbyte = new byte[torrentsize];
		TorrentInfo torrentinfo = null; 
		
		try {
			input = new DataInputStream (new BufferedInputStream(new FileInputStream(inputtorrent)));
			input.read(torrentbyte);
			torrentinfo = new TorrentInfo(torrentbyte);
			input.close(); 
		} catch (FileNotFoundException e2) {
			e2.printStackTrace();
			closer(); 
		} catch (IOException e) {
			e.printStackTrace();
			closer(); 
		} catch (BencodingException e) {
			e.printStackTrace();
			closer(); 
		}
		
		byte [] serverreply = EstablishConnection(torrentinfo);
		if (serverreply == null){
			closer(); 
		}
		
		if ( validatePeers(serverreply,torrentinfo) == false){
			closer(); 
		}
		
		
		
	}
	
	
	public static byte[] EstablishConnection(TorrentInfo torrentinfo){
		Message message = new Message(torrentinfo, Constants.peerid); 
		StringBuilder temp; 
		
		String urlstring= message.getUrlString(); 
		
		
		
		
		return null;
		
	}
	
	public static boolean validatePeers(byte[] serverreply,TorrentInfo torrentinfo){
		

		
		
		
		
		
		return false; 
		
	}
	
	
	
	private static void closer(){
		try {
			savefile.close();
		} catch (IOException e) {
			
			e.printStackTrace();
		} 
		System.out.println("Error: A critical error occured");
		System.exit(1);
	}
}
