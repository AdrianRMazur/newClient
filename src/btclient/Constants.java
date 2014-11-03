package btclient;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Random;

public class Constants {

	static Random random= new Random(); 
	
	public final static byte[] PEERID = { 'k','o','s','t','i','a','n','d','a','d','r','i','a','n','@','@','@','@','@','@'};
	public final static String PEER_ID = "KostiAndAdrian@@**@@";
	public final static byte [] BITTORRENTPROTOCOL = new byte [] {'B','i','t','T','o','r','r','e','n','t',' ','p','r','o','t','o','c','o','l'};
	
	
	public final static ByteBuffer PEERS= ByteBuffer.wrap(new byte [] {'p','e','e','r','s'});
	public final static ByteBuffer PORT= (ByteBuffer.wrap(new byte [] {'p','o','r','t'}));
	public final static ByteBuffer IP= ByteBuffer.wrap(new byte [] {'i','p'});
	
	
	
	public final static String OK_PEER1 = "128.6.171.130";
	public final static String OK_PEER2 = "128.6.171.131";
	
	public final static byte INTERESTED_ID = 2; 
	public final static byte HAVE_ID = 4; 
	public final static byte REQUEST_ID = 6; 
	
	public final static int OUR_PORT = random.nextInt(7200 - 6881 + 1) + 6881; 
}
