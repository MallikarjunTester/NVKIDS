package utils;

import java.io.FileInputStream;
import java.util.Properties;

public class ReadPropertyFile {

	/*
	 * 
	 */
	public static String get(String propertyname) {
		
		String returnproperty=null;
		Properties property = new Properties();
		try {
			FileInputStream file = new FileInputStream("C:\\Users\\IID_User01\\git\\NVKIDS\\src\\main\\java\\utils\\ReadPropertyFile.java");
			property.load(file);
			returnproperty = property.getProperty(propertyname);
			if(returnproperty==null) {

			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
		return returnproperty;
		
	}
	
}
