package btclient;

public class Message {

	public String urlstring; 
	public byte [] toShake;  
	public byte [] upload; 
	
	
	public Message (TorrentInfo torrentinfo, byte [] peerid){
		String infohash, peeridstring; 
		int x = torrentinfo.info_hash.array().length; 
		infohash = buildstring(torrentinfo, x);
		x = peerid.length;	
		peeridstring = buildstring(torrentinfo, x);
		urlstring = torrentinfo.announce_url.toString() + "?info_hash=" + infohash + "&peer_id="+Constants.PEER_ID + "&port="+Constants.OUR_PORT+"&uploaded="+BTClient.u+"&downloaded="+BTClient.d+"&left=" +(torrentinfo.file_length-BTClient.d)+ "&event=started"  ;
		//System.out.println("URLSTRING: "+urlstring);
	}
	
	

	public Message(byte[] bittorrentprotocol, byte[] peerid,
			TorrentInfo torrentinfo) {
		urlstring=null; 
		toShake = new byte[68];
		toShake[0]= (byte) 19;
		System.arraycopy(bittorrentprotocol, 0, toShake, 1, 19);
		for(int i=20;i<28;i++){
			toShake[i]=(byte)0;
		}
		System.arraycopy(torrentinfo.info_hash.array(), 0, toShake, 28, 20);
		System.arraycopy(Constants.PEERID, 0, toShake, 48, 20);
		
	}
	public Message(int length,byte id, int begin, int index, byte [] block){
		/*Length prefix + message ID*/
		upload= new byte[length+4];
		System.arraycopy(BTClient.toEndianArray(length), 0, upload, 0, 4);
		upload[4]=(byte)7;
		
		/*Tough part..... payload*/
		System.arraycopy(BTClient.toEndianArray(index), 0, upload, 5, 4); 
		System.arraycopy(BTClient.toEndianArray(begin), 0, upload, 9, 4);
		System.arraycopy(block, 0, upload, 13, length-9); 
		
		
	}
	


	public String getURL(){
		return urlstring; 
	}
	
	public byte[] getShake(){
		return toShake; 
	}
	
	private static String buildstring (TorrentInfo torrentinfo, int x){

		StringBuilder temp = new StringBuilder(x * 2);
		
		for(byte temp2: torrentinfo.info_hash.array())
			 temp.append('%').append(String.format("%02x", temp2 & 0xff));
		return temp.toString(); 
	}
	
}
