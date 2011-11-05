package main.java.de.WegFetZ.CustomMusic;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;


public class log {
	
	private static File debugLog = new File(CustomMusic.maindir + "debug.txt");

	public static void debug(String message, Exception e) {
		
		if (message != null) 
			System.out.println(now("'['dd.MM.yyyy. HH:mm:ss']'") +"  [CustomMusic] An error occured while " + message + ".");
		try {
			debugLog.createNewFile();
		} catch (IOException e1) {
			System.out.println("Couldn't create debug.txt. create it manually in the CustomMusic directory.");
		}
		
		if (debugLog.exists()) {
			try {
				PrintWriter pw = new PrintWriter(new FileWriter(debugLog,true));
				if (message != null) 
					pw.println("[ERROR] An error occured while " + message + ".");
				e.printStackTrace(pw);
				pw.println();
				pw.close();
			} catch (Exception ex) {}
		}
		
	}
	
	public static void info(String message) {
		
		if (debugLog.exists()) {
			try {
				PrintWriter pw = new PrintWriter(new FileWriter(debugLog,true));
				if (message != null) 
					pw.println(now("'['dd.MM.yyyy. HH:mm:ss']'") + " [INFO] " + message + ".");
				
				pw.close();
			} catch (Exception ex) {}
				
		}
	}
	
	  public static String now(String dateFormat) {
		    Calendar cal = Calendar.getInstance();
		    SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
		    return sdf.format(cal.getTime());

		  }
	
}
