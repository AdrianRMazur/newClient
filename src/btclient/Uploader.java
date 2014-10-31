package btclient;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Uploader implements Runnable {

	int port= 6881 ;
	ServerSocket serverSide; 
	
	public Uploader() throws IOException{
		serverSide=new ServerSocket(port);
	}
	
	

	public void run() {
		System.out.println("Our listening port is: "+port); 
		for(;;){
			try {
				Socket con = serverSide.accept();
				Peer peer=new Peer(con);
				new Thread (peer).start();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			
		}
	}
	
	
	
}
