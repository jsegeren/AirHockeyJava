package airhockeyjava.util;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileWriter {
	private Writer writer = null;
	private String fileName = "";

	public FileWriter(String name){
		this.fileName = name;
	}
	
	public void write(String text){
		try {
		    writer = new BufferedWriter(new OutputStreamWriter(
		          new FileOutputStream(this.fileName), "utf-8"));
		    writer.write(text);
		} catch (IOException ex) {
		  // report
		} finally {
		   try {writer.close();} catch (Exception ex) {}
		}
			}		
	
	public String read() throws IOException{

		byte[] encoded = Files.readAllBytes(Paths.get(this.fileName));
		  return new String(encoded, "utf-8");

	}
}
