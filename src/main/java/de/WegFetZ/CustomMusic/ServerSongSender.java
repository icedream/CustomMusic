package main.java.de.WegFetZ.CustomMusic;

import java.io.IOException;
import java.net.ServerSocket;

public class ServerSongSender extends Thread {

	public static ServerSocket serversongSocket = null;

	public void run() {

		try {
			serversongSocket = new ServerSocket(LoadSettings.ServerPort + 1);
		} catch (IOException e) {
			log.debug("binding to port: " + ((int)LoadSettings.ServerPort + 1), e);
		}

		while (CustomMusic.listening && !serversongSocket.isClosed())
			try {
				new songSender(serversongSocket.accept()).start();
			} catch (IOException e) {
				if (CustomMusic.listening)
					log.debug("accepting on ServerSongSender-thread socket", e);
				else
					log.debug(null, e);
			}

		try {
			if (!serversongSocket.isClosed())
				serversongSocket.close();
		} catch (IOException e) {
			log.debug("closing ServerSongSender-thread socket", e);
		}
	}

}
