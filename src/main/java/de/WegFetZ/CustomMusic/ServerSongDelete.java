package main.java.de.WegFetZ.CustomMusic;

import java.io.IOException;
import java.net.ServerSocket;

public class ServerSongDelete extends Thread {

	public static ServerSocket serversongdeleteSocket = null;

	public void run() {

		try {
			serversongdeleteSocket = new ServerSocket(LoadSettings.ServerPort + 3);
		} catch (IOException e) {
			log.debug("binding to port: " + ((int)LoadSettings.ServerPort + 3), e);
		}

		while (CustomMusic.listening && !serversongdeleteSocket.isClosed())
			try {
				new songDelete(serversongdeleteSocket.accept()).start();
			} catch (IOException e) {
				if (CustomMusic.listening)
					log.debug("accepting on ServerSongDelete-thread socket", e);
				else
					log.debug(null, e);
			}

		try {
			if (!serversongdeleteSocket.isClosed())
				serversongdeleteSocket.close();
		} catch (IOException e) {
			log.debug("closing ServerSongDelete-thread socket", e);
		}
	}

}
