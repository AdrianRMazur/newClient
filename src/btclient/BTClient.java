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

	private static FileOutputStream savefile = null;
	private static DataInputStream input;
	private static TorrentInfo torrentinfo = null; 
	private static boolean unChoke; 
	private static int lastPieceLength; 
	
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
			peers[i]=new Peer( (Map<ByteBuffer, Object>) peerList.get(i)); 	
		}
		
		/*downloading from peers starts here, multi threading..... */
		
		//for(int i = 0;i<peerList.size();i++){
			peerDownload(/*peers[0]*/);
		//}
		
		
		return false; 
	}
	
	public static void peerDownload(/*Peer peer*/) throws IOException, InterruptedException{
		/*handshake part might have to be done outside...... Since it is only done once?*/
		Socket s = null;
		Message message= null; 
		InputStream input=null;
		OutputStream output =null; 
		DataOutputStream dataout= null;
		DataInputStream datain=null;
		
		/*hard coded peer*/
		try {
			s = new Socket(/*peer.getIP()*/"128.6.171.131", /*peer.getPort()*/24399);
			input= s.getInputStream();
			output = s.getOutputStream(); 
			dataout= new DataOutputStream(output);
			datain=new DataInputStream(input);
		} catch (IOException e) {
			
			e.printStackTrace();
			System.out.println("3");
			closer();
		}
		
		message= new Message (Constants.BITTORRENTPROTOCOL,Constants.PEERID, torrentinfo); 
		try {
			dataout.write(message.toShake);
			dataout.flush();
			s.setSoTimeout(1000);
		} catch (IOException e) {
			System.out.println("1");
			e.printStackTrace();
			closer();
		}
		
		/*The problem is that the shakeFrom response tends to come too quick, and the fromShake gets a few additional bits*/
		
		int j=0;
		byte[] fromShake = new byte[67];
		//byte[] fromShake2 = new byte[68];
		
		
		try {
			
			while(datain.readByte()!=19);
			datain.read(fromShake);
			//datain.read(fromShake2); 
		} catch (IOException e) {
			e.printStackTrace();
			closer(); 

		}
		
		System.out.println("toShake");
		ToolKit.print(message.getShake());
		System.out.println("fromShake:");
		ToolKit.print(fromShake);
		/*System.out.println("fromShake2:");
		ToolKit.print(fromShake2);
*/
		
		byte[] infohashpart = Arrays.copyOfRange(fromShake, 27, 47);
		
		if (Arrays.equals(infohashpart, torrentinfo.info_hash.array()) == false){
			System.out.println("BLAH");
			try {
				s.close();
				dataout.close();
				datain.close();
			} catch (IOException e) {
				closer(); 
				e.printStackTrace();
			} 

		}
		
		unChoke=false; 
		lastPieceLength= torrentinfo.file_length - (torrentinfo.piece_length * (torrentinfo.piece_hashes.length-1));
		byte str; 
		
		/*Don't think we need this!*/
		/*for (;;){
			str=datain.readByte();
			System.out.println(str);
			if (str ==-1)
				break;  		
		}*/
		
		while(unChoke==false){
			byte [] interested = new byte [5];	
			System.arraycopy(toEndianArray(1), 0, interested, 0, 4);
			interested[4] = (byte) 2;
			dataout.write(interested);
			dataout.flush(); 
			s.setSoTimeout(1300000);
			
			for (int c = 0; c<5; c++){
				if (c==4){
					str=datain.readByte();
					System.out.println(str);
					if (str ==1){
						unChoke = true; 
						break; 
					}
				}
				datain.readByte();
			}
		}
		
		datain.close();
		dataout.close();
		s.close();
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
	
}
