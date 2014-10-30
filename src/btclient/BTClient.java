package btclient;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

public class BTClient {

	public static TorrentInfo torrentinfo = null; 
	public static byte [] []downloaded=null;
	public static boolean [] startedDL = null; 
	public static boolean [] completedDL = null; 
	
	
	
	private static FileOutputStream savefile = null;
	private static DataInputStream input;
	private static boolean unChoke; 
	private static int lastPieceLength; 
	private static ArrayList <Peer> currentpeer; 
	private static ArrayList <Peer> goodpeers; 
	
	public static void main (String [] args) throws IOException, InterruptedException {
		
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
		
		byte [] serverreply = EstablishConnection();
		if (serverreply == null){
			
			closer(); 
		}
		
		if ( validatePeers(serverreply) == false){
			closer(); 
		}
	}
	
	
	public static byte[] EstablishConnection(){
		Message message = new Message(torrentinfo, Constants.PEERID); 
		StringBuilder temp; 
		
		String urlstring= message.getURL(); 
		
		URL url;
		try {
			url = new URL (urlstring);
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
			closer();
			return null; 
		}
		
		HttpURLConnection con =null;
		DataInputStream in = null;
		
		byte [] serverreply = null; 
		
		try {
			con = (HttpURLConnection) url.openConnection();
			in = new DataInputStream( con.getInputStream());
			serverreply = new byte[con.getContentLength()];
			in.readFully(serverreply);
			
			in.close();
		
		} catch (IOException e) {
			e.printStackTrace();
			closer(); 
			return null; 
		}
		con.disconnect(); 
		return serverreply;
		
	}
	
	public static boolean validatePeers(byte[] serverreply) throws IOException, InterruptedException{
		Map<ByteBuffer, Object> obj = null;  
		try {
			obj=(Map<ByteBuffer, Object>)Bencoder2.decode(serverreply);
		} catch (BencodingException e) {
			return false; 		
		} 
		ArrayList peerList = (ArrayList)obj.get(Constants.PEERS);
		//System.out.println(peerList.size());
		//ToolKit.print(obj);
		Peer[] peers = new Peer[peerList.size()];

		for(int i=0;i<peerList.size();i++){

			peers[i]=new Peer( (Map<ByteBuffer, Object>)peerList.get(i)); 	
		}
		

		
		
		downloaded = new byte [torrentinfo.piece_hashes.length][];
		startedDL = new boolean [torrentinfo.piece_hashes.length];
		completedDL = new boolean [torrentinfo.piece_hashes.length];
		
		for (int i = 0; i < startedDL.length; i++) {
			startedDL[i] = false; 
			completedDL[i]=false; 
		}
		
		for (int c = 0; c<peers.length; c++){
			Downloader temp = new Downloader(peers[c]);
			new Thread(temp).start();
		}
		
		/*downloading from peers starts here, multi threading..... */
		
		//for(int i = 0;i<peerList.size();i++){
			peerDownload(/*peers[0]*/);
		//}
		
		
		return false; 
	}
	
	public static void peerDownload(/*Peer peer*/) throws IOException, InterruptedException{
		
			
		
		Message message= new Message (Constants.BITTORRENTPROTOCOL,Constants.PEERID, torrentinfo); 
		for (int c =0; c< currentpeer.size(); c++){
			if (currentpeer.get(c).shakeHands(message, torrentinfo) == true){
				goodpeers.add(currentpeer.get(c));
			};
		}	
		unChoke=false; 
		lastPieceLength= torrentinfo.file_length - (torrentinfo.piece_length * (torrentinfo.piece_hashes.length-1));
		byte str; 
		
	
		for (int c = 0; c < currentpeer.size(); c++) {
			while (unChoke == false) {
				byte[] interested = new byte[5];
				System.arraycopy(toEndianArray(1), 0, interested, 0, 4);
				interested[4] = (byte) 2;
				currentpeer.get(c).send().write(interested);
				currentpeer.get(c).send().flush();
				currentpeer.get(c).modifysocket().setSoTimeout(1300000);

				for (int c2 = 0; c < 5; c++) {
					if (c2 == 4) {
						str = currentpeer.get(c).receive().readByte();
						System.out.println(str);
						if (str == 1) {
							unChoke = true;
							break;
						}
					}
					currentpeer.get(c).receive().readByte();
				}
			}
		}
		
		for (int c = 0; c < currentpeer.size(); c++) {
			currentpeer.get(c).closeSocket();
		}
	}
	
	public void peerUpload(Peer peer){
		
	}
	
	/**********************************************************************
	 * TOOOLS GO HERE 
	 * 
	 ***********************************************************************/
	
	
	private static void closer(){
		try {
			savefile.close();
		} catch (IOException e) {
			
			e.printStackTrace();
		} 
		System.out.println("Error: A critical error occured");
		System.exit(1);
	}
	
	  public static String byteArrayToHex(byte[] a) {
		 StringBuilder sb = new StringBuilder(a.length * 2);
		 for(byte b: a)
			 sb.append('%').append(String.format("%02x", b & 0xff));
		 return sb.toString();
	  }
	  
	
	public static  int fromEndianArray(byte[] x){
	    ByteBuffer temp = ByteBuffer.wrap(x);
	    temp.order(ByteOrder.BIG_ENDIAN);
	    return temp.getInt();
	}
	  
	public static byte[] toEndianArray(int x){
	    ByteBuffer temp = ByteBuffer.allocate(4);
	    temp.order(ByteOrder.BIG_ENDIAN);
	    temp.putInt(x);
	    temp.flip();
	    return temp.array();
	}
	
	public static boolean checkIfAvailable(int index){
		return completedDL[index]; 
	}
	
}
