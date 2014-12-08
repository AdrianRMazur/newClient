package btclient;

import java.awt.Dimension;
import java.io.BufferedInputStream;
import java.io.Console;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Scanner;

import javax.swing.JFrame;

import GUI.DisplayPanel;
import GUI.FirstPanel;

public class BTClient implements Cloneable, Serializable, Runnable {

	public static TorrentInfo torrentinfo = null; 
	public static byte [] []downloaded=null;
	 
	public static boolean [] startedDL = null; 
	public static boolean [] completedDL = null; 
	
	public boolean command = false;
	static String localIP=null; 
	public static String fileName= "downloaded.ser";
	public static boolean stopthread = false; 
	public static boolean threadstopped = false; 
	 
	
	public static int d=0;
	public static int u=0; 
	
	public static FileOutputStream savefile = null;
	private static DataInputStream input;

	private String torrent; 
	private String saved; 
	private String ip; 
	
	
	public BTClient (String torrent, String saved, String ip){
		
		this.torrent = torrent; 
		this.saved = saved; 
		this.ip = ip; 
			
		
	}
	
	
	public static byte[] EstablishConnection(){
		Message message = new Message(torrentinfo, Constants.PEERID); 
		StringBuilder temp; 
		
		String urlstring= message.getURL(); 
		
		URL url;
		try {
			url = new URL (urlstring);
		} catch (MalformedURLException e1) {
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
			closer(); 
			return null; 
		}
		con.disconnect(); 
		return serverreply;
		
	}
	
	

	private static boolean validatePeers(byte[] serverreply) throws IOException, InterruptedException{
		Map<ByteBuffer, Object> obj = null;  
		try {
			obj=(Map<ByteBuffer, Object>)Bencoder2.decode(serverreply);
		} catch (BencodingException e) {
			return false; 		
		} 
		//ToolKit.print(obj);
		ArrayList peerList = (ArrayList)obj.get(Constants.PEERS);
		Downloader[] peers = new Downloader[peerList.size()];
		for(int i=0;i<peerList.size();i++){
			peers[i]=new Downloader( (Map<ByteBuffer, Object>)peerList.get(i), i);
			
		}
		
		
		if (downloaded == null){
			downloaded = new byte [torrentinfo.piece_hashes.length][];
		}
		
		startedDL = new boolean [torrentinfo.piece_hashes.length];
		completedDL = new boolean [torrentinfo.piece_hashes.length];
		
		for (int i = 0; i < startedDL.length; i++) {
			startedDL[i] = false; 
			completedDL[i]=false; 
		}
		
		
		loadarrays();
		
		Thread [] threads = new Thread[peers.length];
		
		
		for (int c = 0; c<peerList.size(); c++){
			threads[c] = new Thread(peers[c]);	
			threads[c].start();
		}
		
		String str = null;
		Scanner reader = new Scanner(System.in);
		for (;;) {
			str = reader.next();
			if (str.equalsIgnoreCase("exit")) {
				break;
			} else {
				System.out.print("Wrong. Please write 'Exit' to quit the program\n");
			}
		}
		stopthread = true; 
		
	
		
		for (int c = 0; c<peerList.size(); c++){
			threads[c].join();
		}
		if (threadstopped == true){
			System.exit(1);
		}
		return true; 
	}
	
	private static void loadarrays() {

		for (int c = 0; c < downloaded.length; c++) {
			if (downloaded[c] != null) {
				completedDL[c] = true;
				startedDL[c] = true;
			}
			else {
				break; 
			}
		}
	}
	
	private static void savetofile() {
		for (int c = 0; c < downloaded.length; c++) {
			try {
				if (downloaded[c] == null) {
					DisplayPanel.error("ERROR writing file");
					return;
				}
				savefile.write(downloaded[c]);
			} catch (IOException e) {

			}
		}
	}


	private static void closer() {
		try {
			savefile.close();
		} catch (IOException e) {

		}
		// System.out.println("Error: A critical error occured");
		System.exit(1);
	}

	public static String byteArrayToHex(byte[] a) {
		StringBuilder sb = new StringBuilder(a.length * 2);
		for (byte b : a)
			sb.append('%').append(String.format("%02x", b & 0xff));
		return sb.toString();
	}
	  
	
	public static int fromEndianArray(byte[] x) {
		ByteBuffer temp = ByteBuffer.wrap(x);
		temp.order(ByteOrder.BIG_ENDIAN);
		return temp.getInt();
	}

	public static byte[] toEndianArray(int x) {
		ByteBuffer temp = ByteBuffer.allocate(4);
		temp.order(ByteOrder.BIG_ENDIAN);
		temp.putInt(x);
		temp.flip();
		return temp.array();
	}

	public static boolean checkIfAvailable(int index) {
		return completedDL[index];
	}


	public void run() {

		if(!ip.equals("")){
			localIP = ip; 
		}

		try {
			savefile = new FileOutputStream(new File(saved));
		} catch (FileNotFoundException e) {
			
		}
		
		
		try {
			Serialization.validateFile(fileName);
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
	           downloaded = (byte[][]) Serialization.deserialize(fileName);
		     //  System.out.println("Found previously downloaded file of size: "+downloaded.length);
	       } catch (ClassNotFoundException | IOException e) {
	           
	       }
	         		
		input = null; 
		File inputtorrent = new File (torrent);
		int torrentsize = (int) inputtorrent.length();
		
		if (torrentsize > 1000000){
			DisplayPanel.error("Error: File size too large"); 
			System.exit(1);
		}
		
		byte[] torrentbyte = new byte[torrentsize];

		
		try {
			input = new DataInputStream (new BufferedInputStream(new FileInputStream(inputtorrent)));
			input.read(torrentbyte);
			torrentinfo = new TorrentInfo(torrentbyte);
			input.close(); 
		} catch (FileNotFoundException e2) {
			closer(); 
		} catch (IOException e) {
			closer(); 
		} catch (BencodingException e) {
			closer(); 
		}
		
		Uploader upload = null;
		try {
			upload = new Uploader();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 
		Thread t= new Thread (upload);
		t.start();
		
		
		
		byte [] serverreply = EstablishConnection();
		if (serverreply == null){
			closer(); 
			return; 
		}
		
		try {
			if ( validatePeers(serverreply) == false){
				closer();
				return; 
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		try {
            Serialization.serialize(downloaded, fileName);
            
        } catch (IOException e) {
        	
        }
		
		
		savetofile(); 
		
		for (int c = 0; c<downloaded.length; c++){
			downloaded[c] = null; 
		}
		try {
			Serialization.serialize(downloaded, fileName);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		closer();
		
	}

}
