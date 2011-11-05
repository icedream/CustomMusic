package main.java.de.WegFetZ.CustomMusic;

import java.net.*;
import java.io.*;

public class Server extends Thread {

	public static ServerSocket serverSocket = null;

	public void run() {

		try {
			serverSocket = new ServerSocket(LoadSettings.ServerPort);
		} catch (IOException e) {
			log.debug("binding to port: " + LoadSettings.ServerPort, e);
		}

		while (CustomMusic.listening && !serverSocket.isClosed())
			try {
				new MultiServerThread(serverSocket.accept()).start();
			} catch (IOException e) {
				if (CustomMusic.listening)
					log.debug("accepting on Server-thread socket", e);
				else
					log.debug(null, e);
			}

		try {
			if (!serverSocket.isClosed())
				serverSocket.close();
		} catch (IOException e) {
			log.debug("closing Server-thread socket", e);
		}
	}
}
