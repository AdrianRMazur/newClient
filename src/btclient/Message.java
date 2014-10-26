package btclient;

public class Message {

	public String urlstring; 
	
	public Message (TorrentInfo torrentinfo, byte [] peerid){
		String infohash, peeridstring, urlstring; 
		int x = torrentinfo.info_hash.array().length; 
		infohash = buildstring(torrentinfo, x);
		x = peerid.length;	
		peeridstring = buildstring(torrentinfo, x);
		urlstring = torrentinfo.announce_url.toString() + "?info_hash=" + infohash + "&peer_id="+peeridstring + "&port=6885&uploaded=0&downloaded=0&left=" +torrentinfo.file_length ;
	}
	
	

	public String getURL(){
		return urlstring; 
	}
	
	
	private static String buildstring (TorrentInfo torrentinfo, int x){

		StringBuilder temp = new StringBuilder(x * 2);
		
		for(byte temp2: torrentinfo.info_hash.array())
			 temp.append('%').append(String.format("%02x", temp2 & 0xff));
		return temp.toString(); 
	}
	
}
