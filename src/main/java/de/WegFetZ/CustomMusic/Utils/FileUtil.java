package main.java.de.WegFetZ.CustomMusic.Utils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.File;
import java.io.FileWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import main.java.de.WegFetZ.CustomMusic.log;

public class FileUtil {

	
	
	public static String getTextContent(URL url) throws IOException {
	    Scanner s = new Scanner(url.openStream()).useDelimiter("\\Z");;
	    String content = s.next();
	    return content;
	}

	
	public static String getPlsTitle(File file) {
		String result = file.getName();
		
		if (!file.exists())
			return file.getName();
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			
			String inLine;
			while ((inLine = br.readLine()) != null) {
				if (inLine.toLowerCase().contains("title") && inLine.length() > 7) {
					if (inLine.contains("(#") && inLine.contains(" - ") && inLine.contains("/") && inLine.contains(") ")) {
						String[] split = inLine.split("\\) "); //remove number of listeners information
						if (split != null && split.length > 1) {
							result = split[1];
							break;
						}
					} else {
							result = inLine.substring(7);
							break;
					}
				}
			}
			br.close();
			
		} catch (FileNotFoundException e) {
			log.debug("reading .pls file", e);
		} catch (IOException e) {
			log.debug("reading .pls file", e);
		}
		
		return result;
		
	}

	
	
	public static Boolean isempty(String path) { //checks if a directory is empty
		File file = new File(path);
		if (file.exists()) {
			String[] files = file.list();
			if (files.length > 0) {
				return false;
			} else {
				return true;
			}
		} else {
			return true;
		}
	}
	
	public static String recurseInDirFrom(String dirItem) { //returns a String of all files and directories in a directory
		File file;
		String list[], result;

		result = dirItem;

		file = new File(dirItem);
		if (file.isDirectory()) {
			list = file.list();
			for (int i = 0; i < list.length; i++)
				result = result + "|" + recurseInDirFrom(dirItem + "/" + list[i]);
		}
		return result;
	}

	public static String[] listOnlyFiles(String path) { //returns an array of all files in a directory but no subdirectories

		File dirFile = new File(path);

		if (!dirFile.exists())
			return null;

		String[] list = dirFile.list();
		List<String> resultList = new ArrayList<String>();

		for (int i = 0; i < list.length; i++) {
			if (!(new File(path + list[i]).isDirectory()))
				resultList.add(list[i]);
		}

		String[] result = new String[resultList.size()];
		for (int i = 0; i < resultList.size(); i++) {
			result[i] = resultList.get(i);
		}

		return result;
	}
	
	
	public static void removeLineFromFile(String file, String lineToRemove) {
		try {

			File inFile = new File(file);

			if (!inFile.isFile()) {
				System.out.println("Parameter is not an existing file");
				return;
			}

			// Construct the new file that will later be renamed to the original
			// filename.
			File tempFile = new File(inFile.getAbsolutePath() + ".tmp");

			BufferedReader br = new BufferedReader(new FileReader(file));
			PrintWriter pw = new PrintWriter(new FileWriter(tempFile));

			String line = null;

			// Read from the original file and write to the new
			// unless content matches data to be removed.
			while ((line = br.readLine()) != null) {

				if (!line.trim().equals(lineToRemove)) {

					pw.println(line);
					pw.flush();
				}
			}
			pw.close();
			br.close();

			// Delete the original file
			if (!inFile.delete()) {
				System.out.println("Could not delete file");
				return;
			}

			// Rename the new file to the filename the original file had.
			if (!tempFile.renameTo(inFile))
				System.out.println("Could not rename file");
		} catch (FileNotFoundException ex) {
			log.debug("removing line from file", ex);
		} catch (IOException ex) {
			log.debug("removing line from file", ex);
		}
	}
	
	
	public static int getNumberOfFiles(String path, String extension) {
		int filecount = 0;
		File[] Files = new File(path).listFiles();
		for (int i = 0;i<Files.length;i++) {
			int dotPos = Files[i].getName().lastIndexOf(".");
			if (dotPos != -1) {
				String ext = Files[i].getName().substring(dotPos);
				if (extension.equalsIgnoreCase(".midi") || extension.equalsIgnoreCase(".mid")) {
					if (ext.equalsIgnoreCase(".midi") || ext.equalsIgnoreCase(".mid")) 
						filecount++;
				} else if (extension.equalsIgnoreCase(".pls") || extension.equalsIgnoreCase(".asx") || extension.equalsIgnoreCase(".ram")) {
					if (ext.equalsIgnoreCase(".pls") || ext.equalsIgnoreCase(".asx") || ext.equalsIgnoreCase(".ram")) 
						filecount++;
				} else if (ext.equalsIgnoreCase(extension)) {
					filecount++;
				}
			}
		}
		return filecount;
	}

	public static String getExtension(File file) {
		String result = null;

		if (file.exists()) {
			int dotPos = file.getName().lastIndexOf(".");
			if (dotPos != -1) {
				result = file.getName().substring(dotPos);
			}
		}

		return result;
	}
	
	public static boolean copyFile(File inputFile, File outputFile, Boolean moveFile) {

		if (!inputFile.exists())
			return false;

		if (outputFile.exists())
			outputFile.delete();

		InputStream in;

		try {

			in = new FileInputStream(inputFile);
			OutputStream out = new FileOutputStream(outputFile);

			byte[] buffer = new byte[65536];
			int bytesRead = -1;

			while ((bytesRead = in.read(buffer)) != -1)
				out.write(buffer, 0, bytesRead);

			in.close();
			out.close();
		} catch (FileNotFoundException e) {
			log.debug("copying " + inputFile.getPath() + "to " + outputFile.getPath(), e);
			return false;
		} catch (IOException e) {
			log.debug("copying " + inputFile.getPath() + "to " + outputFile.getPath(), e);
			return false;
		}
		
		if (moveFile)
			inputFile.delete();

		return true;

	}
	
}
