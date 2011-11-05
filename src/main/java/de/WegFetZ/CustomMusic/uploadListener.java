package main.java.de.WegFetZ.CustomMusic;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

import org.bukkit.Bukkit;

import main.java.de.WegFetZ.CustomMusic.Utils.FileUtil;

public class uploadListener extends Thread {

	private Socket upclientSocket = null;

	public uploadListener(Socket upclientSocket) {
		super("uploadListener");
		this.upclientSocket = upclientSocket;
	}

	public void run() {

		try {
			OutputStream byteOutput = upclientSocket.getOutputStream();
			InputStream inputStream = upclientSocket.getInputStream();
			DataInputStream dataInputStream = new DataInputStream(inputStream);
			BufferedReader bufReader = new BufferedReader(new InputStreamReader(inputStream));
			
			
			while (!upclientSocket.isClosed()) {
				
				String input = null;

				String playername = null;
				if ((input = bufReader.readLine()) != null)
					playername = input; // get playername
				
				String songname = null; 
				if ((input = bufReader.readLine()) != null)
						songname = input; // get song name

				Long songsize = (long) 0;
				
				if ((input = bufReader.readLine()) != null)
					songsize = Long.parseLong(input); // get song size
				
				
				if(playername != null && songname != null && songsize != 0) {
					String extension = ".mp3";
					int dotPos = songname.lastIndexOf(".");
					if (dotPos != -1) {
						extension = songname.substring(dotPos);
					}
					
					String permInt = "0";
					if (extension.equalsIgnoreCase(".mp3"))
						permInt = "1"; 
					else if(extension.equalsIgnoreCase(".midi") || extension.equalsIgnoreCase(".mid"))
						permInt = "2";
					else if(extension.equalsIgnoreCase(".pls") || extension.equalsIgnoreCase(".asx") || extension.equalsIgnoreCase(".ram"))
						permInt = "3";
					
					
					if ((!permInt.equalsIgnoreCase("0") && GlobalData.CMUploadPerm.get(playername) != null) && ((GlobalData.CMUploadPerm.get(playername).contains(permInt)))) {	
						
						int maxSize = LoadSettings.defaultMaxMp3Size;
						if (extension.equalsIgnoreCase(".mp3"))
							maxSize  = Permission.getPermissionInteger(Bukkit.getServer().getPlayer(playername), "cm.maxMp3SizeMB", LoadSettings.defaultMaxMp3Size);
						else if (extension.equalsIgnoreCase(".midi") || extension.equalsIgnoreCase(".mid"))
							maxSize  = Permission.getPermissionInteger(Bukkit.getServer().getPlayer(playername), "cm.maxMidiSizeMB", LoadSettings.defaultMaxMidiSize);
						else if (extension.equalsIgnoreCase(".pls") || extension.equalsIgnoreCase(".asx") || extension.equalsIgnoreCase(".ram"))
							maxSize  = 0;

						
						if (songsize <= maxSize*1048576 || maxSize == 0) {					
		
							File song = null;
							File tempSong = null;
							if (extension.equalsIgnoreCase(".pls") || extension.equalsIgnoreCase(".asx") || extension.equalsIgnoreCase(".ram")) {
								song = new File(CustomMusic.maindir + "Music/" + "craftplayer{name=" + playername + "}/webradio/" + songname);
							    tempSong = new File(CustomMusic.maindir + "Music/uploading/" + "craftplayer{name=" + playername + "}/webradio/" + songname);
							} else {
								song = new File(CustomMusic.maindir + "Music/" + "craftplayer{name=" + playername + "}/" + songname);
								tempSong = new File(CustomMusic.maindir + "Music/uploading/" + "craftplayer{name=" + playername + "}/" + songname);
							}
							
							if ( (!song.exists() || (Math.abs(song.length() - songsize) > 500)) && (!tempSong.exists() || (Math.abs(tempSong.length() - songsize) > 500)) )  {
								if (song.exists())
									song.delete();
								if (tempSong.exists())
									tempSong.delete();
								
								File directory = new File(CustomMusic.maindir + "Music/craftplayer{name=" + playername + "}/webradio/");
								if (!directory.exists())
									directory.mkdirs();
								File tempDirectory = new File(CustomMusic.maindir + "Music/uploading/craftplayer{name=" + playername + "}/webradio/");
								if (!tempDirectory.exists())
									tempDirectory.mkdirs();
								
								int maxFiles = LoadSettings.defaultMaxMp3;
								int curFiles = 0;
								if (extension.equalsIgnoreCase(".mp3")) {
									maxFiles = Permission.getPermissionInteger(Bukkit.getServer().getPlayer(playername), "cm.maxMp3Files", LoadSettings.defaultMaxMp3);
									curFiles = FileUtil.getNumberOfFiles(CustomMusic.maindir + "Music/craftplayer{name=" + playername + "}/", extension) + FileUtil.getNumberOfFiles(CustomMusic.maindir + "Music/uploading/craftplayer{name=" + playername + "}/", extension);
								} else if (extension.equalsIgnoreCase(".midi") || extension.equalsIgnoreCase(".mid")) {
									maxFiles = Permission.getPermissionInteger(Bukkit.getServer().getPlayer(playername), "cm.maxMidiFiles", LoadSettings.defaultMaxMidi);
									curFiles = FileUtil.getNumberOfFiles(CustomMusic.maindir + "Music/craftplayer{name=" + playername + "}/", extension)+ FileUtil.getNumberOfFiles(CustomMusic.maindir + "Music/uploading/craftplayer{name=" + playername + "}/", extension);
								} else if (extension.equalsIgnoreCase(".pls") || extension.equalsIgnoreCase(".asx") || extension.equalsIgnoreCase(".ram")) {
									maxFiles = Permission.getPermissionInteger(Bukkit.getServer().getPlayer(playername), "cm.maxWebradioFiles", LoadSettings.defaultMaxWebradio);
									curFiles = FileUtil.getNumberOfFiles(CustomMusic.maindir + "Music/craftplayer{name=" + playername + "}/webradio/", extension) + FileUtil.getNumberOfFiles(CustomMusic.maindir + "Music/uploading/craftplayer{name=" + playername + "}/webradio/", extension);
								}
								
								
								if ((curFiles < maxFiles) || (maxFiles == 0)) {
									
									try {
										byteOutput.write(0);
				
										byte[] buffer = new byte[16384];
										int bytesRead = -1;
				
										OutputStream outputStream = new FileOutputStream(tempSong);
				
										while ((bytesRead = dataInputStream.read(buffer)) > 0) {
											outputStream.write(buffer, 0, bytesRead);
										}
										
										outputStream.close();
										
										if (Math.abs(tempSong.length() - songsize) > 500)
											tempSong.delete();
										else
											FileUtil.copyFile(tempSong,song,true);
									
									} catch (IOException e) {
										log.debug("receiving song from client: " + playername, e);
									}
								
								} else {//max number of files reached
									byteOutput.write(3);
									byteOutput.flush();
									byteOutput.write(maxFiles);
									byteOutput.flush();
								}
							} else {//song already uploaded
								byteOutput.write(4);
								byteOutput.flush();
							}
						} else {//song too big
							byteOutput.write(5);
							byteOutput.flush();
							byteOutput.write(maxSize);
							byteOutput.flush();
						}
					} else {//no upload perm
						byteOutput.write(6);
						byteOutput.flush();
					}
				}
			upclientSocket.close();
			}
		} catch (IOException e) {
			log.debug("waiting for songs from client", e);
		}
	}
	
}
