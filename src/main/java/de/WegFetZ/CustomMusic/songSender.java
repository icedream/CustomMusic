package main.java.de.WegFetZ.CustomMusic;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import main.java.de.WegFetZ.CustomMusic.Utils.FileUtil;

public class songSender extends Thread {

	private Socket sendclientSocket = null;
	private OutputStream ausgangStream = null;
	private DataOutputStream songOutputStream = null;
	private InputStream eingang = null;


	public songSender(Socket sendclientSocket) {
		super("songSender");
		this.sendclientSocket = sendclientSocket;
	}

	public void run() {

		int input;
		
		try {
			eingang = sendclientSocket.getInputStream();
			String[] songList = FileUtil.recurseInDirFrom(CustomMusic.maindir + "Music").split("\\|");
			if (songList.length > 0) {
				ausgangStream = sendclientSocket.getOutputStream();
				songOutputStream = new DataOutputStream(ausgangStream);
				if (GlobalData.CMDeleteSongs.size() > 0) {
					for (int k=0;k<GlobalData.CMDeleteSongs.size();k++) {
						songOutputStream.writeUTF("bumper:delete:" + GlobalData.CMDeleteSongs.get(k) + ":bumper");
						eingang.read();
					}
				}
				for (int i = 0; i < songList.length; i++) {
					if (songList[i] != null && !songList[i].contains("uploading")) {
						File file = new File(songList[i]);
						if (file.exists() && !file.isDirectory()) {
							File song = new File(songList[i]);
							long songsize = 0;
							if (song.exists())
								songsize = song.length();
							if (sendclientSocket.isClosed())
								return;
							songOutputStream.writeUTF("bumper:" + songList[i] + ":" + songsize + ":bumper");
							input = eingang.read();
							if (input != -1) {
								if (input == 0) {
									if (songList[i] != null && songList[i].length() > 0) {
										try {
											byte[] buffer = new byte[16384];
											int bytesRead = -1;

											InputStream FileInputStream = new FileInputStream(songList[i]);
											// flush()
											while ((bytesRead = FileInputStream.read(buffer)) != -1) {
												songOutputStream.write(buffer, 0, bytesRead);
											}

											songOutputStream.flush();

											i = songList.length;

											FileInputStream.close();
											break;
										} catch (IOException e) {
											log.debug("sending songs to client", e);
										}
									}
								} else if (input == 1) {
									sleep(200);
								} else {
									break;
								}
							}
						}
					}
				}
				songOutputStream.writeUTF("bumper:ready:bumper");
			}
		} catch (IOException e) {
			log.debug(null, e);
		} catch (InterruptedException ex) {
			log.debug(null, ex);
		}

		if (!sendclientSocket.isClosed()) {
			try {
				sendclientSocket.close();
			} catch (IOException e) {
				log.debug("closing songSender-thread socket", e);
			}
		}
	}

}
