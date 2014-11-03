package btclient;


/*Adrian Mazur && Kosti Papajani*/

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Uploader implements Runnable {

	int port = Constants.OUR_PORT;
	ServerSocket serverSide;

	public Uploader() throws IOException {
		serverSide = new ServerSocket(port);
		//serverSide.setSoTimeout(10000);
	}

	public void run() {
		System.out.println("Our listening port is: "+port);
		//for(int i=0;i<10;i++){
		try {
			Socket con = serverSide.accept();
			System.out.println("\n Connected to a peer!");
			con.setSoTimeout(100000);
			Peer peer = new Peer(con);
			Thread t = new Thread(peer);
			t.start();
			try {
				t.join();
				serverSide.close();
			} catch (InterruptedException e) {
				con.close();
				return;
			}
			con.close();
		} catch (IOException e) {
			return;
		}
		//}
	}
}
