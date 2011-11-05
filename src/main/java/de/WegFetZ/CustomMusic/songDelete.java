package main.java.de.WegFetZ.CustomMusic;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;

import main.java.de.WegFetZ.CustomMusic.Utils.FileUtil;

public class songDelete extends Thread {

	private Socket songdeleteSocket = null;
	private String name = null;

	public songDelete(Socket songdeleteSocket) {
		super("songDelete");
		this.songdeleteSocket = songdeleteSocket;
	}

	public void run() {
		try {

			OutputStream output = songdeleteSocket.getOutputStream();
			InputStream inputStream = songdeleteSocket.getInputStream();
			DataInputStream dataInputStream = new DataInputStream(inputStream);

			InputStream input = songdeleteSocket.getInputStream();
			OutputStream outputStream = songdeleteSocket.getOutputStream();
			DataOutputStream dataOutputStream = new DataOutputStream(outputStream);

			name = dataInputStream.readUTF();

			
			if (GlobalData.CMUploadPerm.containsKey(name)) {
				String permInts = GlobalData.CMUploadPerm.get(name);
				dataOutputStream.writeUTF(permInts);
				if (!permInts.equalsIgnoreCase("0")) {
				
					// get list to delete own songs
					int number = Integer.parseInt(dataInputStream.readUTF());
					String[] mp3 = new String[number];
	
					for (int i = 0; i < number; i++) {
						if (!songdeleteSocket.isClosed()) {
							mp3[i] = CustomMusic.maindir + dataInputStream.readUTF();
							output.write(1);
						}
					}
	
					if (!songdeleteSocket.isClosed()) {
						String[] songList_get = FileUtil.recurseInDirFrom(CustomMusic.maindir + "Music/craftplayer{name=" + name + "}").split("\\|");
						for (int n = 0; n < songList_get.length; n++) {
							if (!(Arrays.asList(mp3).contains(songList_get[n]))) {
								File file = new File(songList_get[n]);
								if (file.exists() && !file.isDirectory()) 								
									file.delete();
							}
						}
					}
				}
			}

			if (!songdeleteSocket.isClosed()) {
				// send list to delete client's songs
				String[] songList = FileUtil.recurseInDirFrom(CustomMusic.maindir + "Music").split("\\|");

				dataOutputStream.writeUTF(String.valueOf(songList.length));// write songlist
															// length
				for (int n = 0; n < songList.length; n++) {
					if (!songdeleteSocket.isClosed()) {
						dataOutputStream.writeUTF(songList[n]);
						input.read();
					}
				}
			}


			output.close();
			input.close();
			dataOutputStream.close();
			dataInputStream.close();
			songdeleteSocket.close();

		} catch (IOException e) {
			log.debug("exchanging song information with client: " + name, e);
		}

	}

}
