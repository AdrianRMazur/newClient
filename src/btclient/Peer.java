package btclient;

import java.nio.ByteBuffer;
import java.util.Map;

public class Peer {
	
	private int port;
	private String ip;

	public Peer(Map<ByteBuffer,Object> peerinfo){
		
	}
	
	public int getPort(){
		return port; 
	}
	
	public String getIP(){
		return ip; 
	}
	
	
}
