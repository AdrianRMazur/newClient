package btclient;

import java.util.ArrayList;


public class Downloader extends BTClient implements Runnable{ 
	
	private static Peer currpeer;  
	
	public Downloader(Peer peer) {
		currpeer = peer; 
	}

	public void run(){
	
		if (currpeer.openSocket() == false){
			// connection failed on socket creation; 
			
		}
		if ()
	}
}
