package main.java.de.WegFetZ.CustomMusic;

import java.io.IOException;
import java.net.ServerSocket;

public class ServerUploadListener extends Thread {

	public static ServerSocket serverupSocket = null;

	public ServerUploadListener() {
		super("ServerUploadListener");
	}

	public void run() {

		try {
			serverupSocket = new ServerSocket(LoadSettings.ServerPort + 2);
		} catch (IOException e) {
			log.debug("binding to port: " + ((int)LoadSettings.ServerPort + 2), e);
		}

		while (CustomMusic.listening && !serverupSocket.isClosed())
			try {
				new uploadListener(serverupSocket.accept()).start();
			} catch (IOException e) {
				if (CustomMusic.listening)
					log.debug("accepting on ServerUploadListener-thread socket", e);
				else
					log.debug(null, e);
			}

		try {
			if (!serverupSocket.isClosed())
				serverupSocket.close();
		} catch (IOException e) {
			log.debug("closing ServerUploadListener-thread socket", e);
		}
	}

}
