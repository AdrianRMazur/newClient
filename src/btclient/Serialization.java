package btclient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

public class Serialization {

	public static Object deserialize(String fileName) throws IOException,
			ClassNotFoundException {
		FileInputStream fis = new FileInputStream(fileName);
		ObjectInputStream ois = new ObjectInputStream(fis);
		Object obj = ois.readObject();
		ois.close();
		return obj;
	}

	// serialize the given object and save it to file
	public static void serialize(Object obj, String fileName)
			throws IOException {
		FileOutputStream fos = new FileOutputStream(fileName);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		oos.writeObject(obj);

		fos.close();
	}
	public static void validateFile(String fileName) throws FileNotFoundException, UnsupportedEncodingException{
		File f=new File(fileName);
		if(!f.exists()){
			System.out.println("serialization file not found... Creating downloadws.ser");
			PrintWriter writer = new PrintWriter(fileName, "UTF-8");
			writer.println("The first line");
			writer.println("The second line");
			writer.close();
			return;
		}
		System.out.println("Serializable download file found!");
		
	}

}
