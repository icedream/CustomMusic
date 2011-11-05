package main.java.de.WegFetZ.CustomMusic;

import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.io.*;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import main.java.de.WegFetZ.CustomMusic.Utils.StringUtil;

public class MultiServerThread extends Thread {

	private Socket socket = null;
	private PrintWriter output = null;
	private OutputStream out = null;
	private InputStream in = null;

	private String name = null;

	public MultiServerThread(Socket socket) {
		super("MultiServerThread");
		this.socket = socket;
	}

	public void run() {
		Boolean exit = false;
		boolean initialized = false;
		try {
			out = socket.getOutputStream();
			output = new PrintWriter(out, true);
			in = socket.getInputStream();
			BufferedReader input = new BufferedReader(new InputStreamReader(in));

			String inputLine;
			int inputInt = -1;
			int outputInt;

			out.write(0);
			out.flush();

			in.read();

			output.println(CustomMusic.requiredClientVersion);
			output.flush();

			ConnectionProtocol cp = new ConnectionProtocol();
			outputInt = cp.processInput(100);
			output.println(outputInt);
			output.flush();

			while (!exit && !socket.isClosed()) {
				try {
					if ((inputInt = in.read()) != -1) {
						outputInt = cp.processInput(inputInt);
						output.println(outputInt);
						output.flush();
						if (outputInt == 2) {
							while (!exit && !socket.isClosed()) {
								try {
									inputLine = input.readLine();
									if (inputLine != null && inputLine.length() == 0) {
										output.println(-3);
										output.flush();
										break;
									} else if (inputLine != null) {
										name = inputLine.toLowerCase();
										GlobalData.CMConnected.put(name, 0);
										Thread t = Thread.currentThread();
										t.setName(name);
										output.println(3);
										output.flush();
										break;
									}
								} catch (IOException e) {
									log.debug(null, e);
									exit = true;
									break;
								}
							}
							Player player = Bukkit.getServer().getPlayer(name);
							if (player != null && player.isOnline()) {
								if (Permission.permission(player, "cm.init", true)) {
									Mp3PlayerHandler.confirm(player); // try to
																		// initialize
																		// the
																		// audioclient

								} else if (GlobalData.CMConnected.containsKey(player.getName().toLowerCase()))
									GlobalData.CMConnected.put(player.getName().toLowerCase(), 2); // set
																									// the
																									// value
																									// to
																									// 2
								// because the player has no permission to
								// initialize
							}

							int i = 0;
							while (!exit && !socket.isClosed()) { // wait
																	// for
																	// initialization
								if (GlobalData.CMConnected.containsKey(name)) {
									if (GlobalData.CMConnected.get(name) == 1) {
										player = Bukkit.getServer().getPlayer(name);
										if (player != null && player.isOnline()) {
											InetAddress playerIP = (player.getAddress()).getAddress(); // player's																									// ip
											InetAddress clientIP = socket.getInetAddress(); // client's
																							// ip
											if (playerIP.equals(clientIP) || !LoadSettings.ipVerification) {
												output.println(4);
												output.flush();
												in.read();
												initialized = true;
												break;
											} else {
												output.println(6);
												output.flush();
												exit = true;
												break;
											}
										} else {
											output.println(7);
											output.flush();
											exit = true;
											break;
										}
									} else if (GlobalData.CMConnected.get(name) == 2) {
										output.println(5);
										output.flush();
										exit = true;
										break;
									} else {
										sleep(1000);
										i++;
										if (i >= 60) {
											output.println(-4);
											output.flush();
											exit = true;
											break;
										}
									}
								} else {
									exit = true;
									break;
								}
							}
						} else if (inputInt == 0) {
							exit = true;
							break;
						}
					} else {
						exit = true;
						break;
					}
					sleep(100);
					if (initialized)
						break;
				} catch (IOException ex) {
					exit = true;
					break;
				} catch (InterruptedException e) {
					log.debug(null, e);
					break;
				}
			}

			if (!exit && !socket.isClosed()) {
				List<String> oldPlayerList = new ArrayList<String>();

				Boolean isPlaying = false;

				int checkDelay = 0;

				try {
					while (!exit && !socket.isClosed()) {
						if (GlobalData.CMPlaying.containsKey(name)) {
							ArrayList<String> playerList = GlobalData.CMPlaying.get(name);
							if (!playerList.equals(oldPlayerList)) {
								for (int i = 0; i < oldPlayerList.size(); i++) {
									String oldValueString = oldPlayerList.get(i);
									String[] oldvalue = oldValueString.split(":");
									if (oldvalue.length > 4) {
										if (StringUtil.listContains(playerList, oldvalue[1] + ":" + oldvalue[2] + ":" + oldvalue[3]) == -1) {
											output.println("0:" + oldvalue[1] + ":" + oldvalue[2] + ":" + oldvalue[3] + ":" + oldvalue[4] + ":" + System.currentTimeMillis());
											output.flush();
										}
									}
								}
								oldPlayerList.clear();
								for (int k = 0; k < playerList.size(); k++) {
									Boolean sendToClient = true;
									String valueString = playerList.get(k);
									String[] valueSplit = valueString.split(":");
									if (valueSplit.length > 2) {
										try {
											if (!valueSplit[2].contains("a") && Integer.parseInt(valueSplit[2]) < 0) {
												if (valueSplit.length > 5)
													sendToClient = false;
												else {
													playerList.remove(k);
													playerList.add(valueString + ":dontsend");
													GlobalData.CMPlaying.put(name, playerList);
												}
											}
										} catch (NumberFormatException e) {
											log.debug("handling /cm (g, e)play players", e);
										}
									}
									if (sendToClient) {
										output.println(valueString + ":" + System.currentTimeMillis());
										output.flush();
									}
									oldPlayerList.add(valueString);
								}
								isPlaying = true;
							}
						} else if (!GlobalData.CMConnected.containsKey(name)) {
							output.println("bye");
							output.flush();
							exit = true;
							break;
						} else {
							if (isPlaying) {
								output.println("stopall");
								output.flush();
								isPlaying = false;
							}
						}
						sleep(150);

						if (checkDelay == 0) {
							output.println("check");
							output.flush();
							socket.setSoTimeout(10000);
							in.read();
							socket.setSoTimeout(0);
						}
						checkDelay++;
						if (checkDelay > 65)
							checkDelay = 0;
					}

					if (!socket.isClosed()) {
						output.println("bye");
						output.close();
					}

				} catch (NullPointerException ex) {
					log.debug(null, ex);
				}

			}

			if (!socket.isClosed()) {
				output.println(-5);
				output.flush();
				socket.close();
			}
			removePlayer();

		} catch (IOException e) {
			removePlayer();
			log.debug(null, e);
		} catch (Exception ex) {
			removePlayer();
			log.debug(null, ex);
		}
		if (name != null)
			System.out.println("[CustomMusic] Disconnected audioclient: " + name + ".");
	}

	public void removePlayer() {

		GlobalData.CMConnected.remove(name);
		GlobalData.CMPlaying.remove(name);
		GlobalData.CMUploadPerm.remove(name);
		GlobalData.CMVolume.remove(name);
	}
}