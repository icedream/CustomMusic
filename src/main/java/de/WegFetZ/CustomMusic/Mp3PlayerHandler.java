package main.java.de.WegFetZ.CustomMusic;

import java.io.File;
import java.util.ArrayList;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import main.java.de.WegFetZ.CustomMusic.Utils.FileUtil;
import main.java.de.WegFetZ.CustomMusic.Utils.StringUtil;

public class Mp3PlayerHandler {

	// CMPlaying
	public static boolean isPlaying(Player player, String box, String number) {

		if (GlobalData.CMPlaying.containsKey(player.getName().toLowerCase())) {

			ArrayList<String> value = GlobalData.CMPlaying.get(player.getName().toLowerCase()); // get
																								// a
																								// list
																								// of
																								// playing
																								// boxes
																								// and
																								// areas

			for (int i = 0; i < value.size(); i++) {
				String[] curBoxValue = value.get(i).split(":");
				String box_map = curBoxValue[1]; // box/area name
				String number_map = curBoxValue[2]; // number of box/area

				if (box_map.equals(box) && number.equalsIgnoreCase(number_map)) { // map
																					// contains
																					// the
																					// box/area
																					// with
																					// the
																					// right
																					// number
					return true;
				}
			}
			return false;
		} else {
			return false;
		}
	}

	public static void startPlaying(Player player, String box, String volume, String number, int priority, Boolean isArea) {
		// add the box to the CMPlaying map or only change the volume if it
		// already exists

		String name = player.getName().toLowerCase();

		if (!isPlaying(player, box, number)) {

			ArrayList<String> playerList;

			if (GlobalData.CMPlaying.containsKey(name))
				playerList = GlobalData.CMPlaying.get(name); // get the list of
																// boxes and
																// areas that
																// need to be
																// played
			else
				playerList = new ArrayList<String>(); // create a new list for
														// the boxes and areas
														// that need to be
														// played

			String songString = null;
			if (isArea)
				songString = getAreaSongList(box, number); // get the string of
															// songs to play on
															// this area
			else
				songString = getBoxSongList(box, number); // get the string of
															// songs to play on
															// this box

			if (songString != null) {
				playerList.add(volume + ":" + box + ":" + String.valueOf(number) + ":" + String.valueOf(priority) + ":" + songString);
				// add the box to the player list in format:
				// 'volume:boxname:boxnumber:priority:songstring'
				GlobalData.CMPlaying.put(name, playerList); // put the
															// playerlist into
															// the CMPlaying map
			}

		} else {

			ArrayList<String> playerList = GlobalData.CMPlaying.get(name); // get
																			// the
																			// list
																			// of
																			// boxes
																			// and
																			// areas
																			// that
																			// need
																			// to
																			// be
																			// played
			int index = StringUtil.listContains(playerList, ":" + box + ":" + number + ":"); // search
																								// for
																								// the
																								// position
																								// of
																								// our
																								// box

			if (index > -1) {
				String[] value = playerList.get(index).split(":");

				if (value.length > 4) {
					if (!value[0].equals(volume) || !value[1].equals(box) || !value[2].equalsIgnoreCase(number)) {

						playerList.remove(index); // remove the line
						playerList.add(volume + ":" + box + ":" + number + ":" + String.valueOf(priority) + ":" + value[4]);
						// add the line again with changed volume

						GlobalData.CMPlaying.put(name, playerList);
						// put the playerlist into the CMPlaying map
					}
				}
			}
		}
	}

	private static String getBoxSongList(String box, String number) {
		// get the songs a certain box needs to play
		String songString;

		for (int l = 0; l < GlobalData.box_count; l++) {

			if (GlobalData.box_aowner[l] != null && GlobalData.box_aowner[l].equalsIgnoreCase(box) && String.valueOf(GlobalData.box_anumber[l]).equalsIgnoreCase(number)) {
				// get the right box index
				songString = GlobalData.box_aSongList[l];

				if (songString == null) { // play every song

					songString = "";
					File file = new File(CustomMusic.maindir + "Music/" + (String.valueOf(box).toLowerCase()) + "/");

					if (file.exists()) {
						String[] songList = file.list();

						for (int n = 0; n < songList.length; n++) {
							if (!(new File(CustomMusic.maindir + "Music/" + (String.valueOf(box).toLowerCase()) + "/" + songList[n]).isDirectory()))
								songString = songString + songList[n] + ">>";
						} // ==> adds the content of the player's music
							// directory to the song string
					}
				}
				return songString;
			}
		}
		return null;
	}

	private static String getAreaSongList(String box, String number) {
		// same as getBoxSongList but with the variables and arrays for areas

		String songString;

		for (int l = 0; l < GlobalData.area_count; l++) {

			if (GlobalData.area_aowner[l] != null && GlobalData.area_aowner[l].equalsIgnoreCase(box) && String.valueOf(GlobalData.area_anumber[l]).equalsIgnoreCase(number)) {
				songString = GlobalData.area_aSongList[l];

				if (songString == null) {

					songString = "";
					File file = new File(CustomMusic.maindir + "Music/" + (String.valueOf(box).toLowerCase()) + "/");

					if (file.exists()) {
						String[] songList = file.list();

						for (int n = 0; n < songList.length; n++) {
							if (!(new File(CustomMusic.maindir + "Music/" + (String.valueOf(box).toLowerCase()) + "/" + songList[n]).isDirectory()))
								songString = songString + songList[n] + ">>";
						}
					}
				}
				return songString;
			}
		}
		return null;
	}

	public static void stopPlaying(Player player, String box, String number) {
		// remove the box from CMPlaying

		String name = player.getName().toLowerCase();
		ArrayList<String> playerList = GlobalData.CMPlaying.get(name);

		int index = StringUtil.listContains(playerList, ":" + box + ":" + number + ":");

		if (index > -1)
			playerList.remove(index);
	}

	public static void confirm(Player player) {
		// confirm that the player is runing the audio client

		String name = player.getName().toLowerCase();

		if (GlobalData.CMConnected.containsKey(name)) {
			GlobalData.CMConnected.put(name, 1); // value 1 indicates that the
													// player is logged in
			GlobalData.CMUsers.put(player.getName().toLowerCase(), player.getName()); // switches
																						// on
																						// the
																						// music
																						// for
																						// the
																						// player

			String permInts = "";

			if (Permission.permission(player, "cm.upload.mp3", true))
				permInts = permInts + "1:"; // can upload mp3

			if (Permission.permission(player, "cm.upload.midi", true))
				permInts = permInts + "2:"; // can upload midi

			if (Permission.permission(player, "cm.upload.webradio", true))
				permInts = permInts + "3:"; // can upload .pls, .asx and .ram

			if (permInts.equalsIgnoreCase(""))
				permInts = "0"; // is not allowed to upload anything

			GlobalData.CMUploadPerm.put(name, permInts);

			player.sendMessage("CustomMusic: " + ChatColor.DARK_GREEN + "AudioClient initialized");

			checkForBox(player);

		} else {

			GlobalData.CMUsers.remove(player.getName().toLowerCase());
			player.sendMessage(CustomMusic.cColour(LoadSettings.initFailedMsg));
			// the audioclient isn't running
		}
	}

	public static boolean playSong(Player user, String songNumbers, Boolean global, String OtherPlayer) {

		String name = user.getName().toLowerCase();
		String playerString = ("CraftPlayer{name=" + OtherPlayer + "}").toLowerCase();

		File file = new File(CustomMusic.maindir + "Music/" + playerString + "/");
		if (file.exists() && !FileUtil.isempty(CustomMusic.maindir + "Music/" + playerString + "/")) {

			if (songNumbers.equals("all")) {
				String[] songList = FileUtil.listOnlyFiles(CustomMusic.maindir + "Music/" + playerString + "/");
				String SongString = "";
				for (int k = 0; k < songList.length; k++) {
					SongString = SongString + songList[k] + ">>";
				}

				if (global) {
					if (!playGlobal(user, SongString, playerString))
						return false;
				} else {
					ArrayList<String> playerList;
					if (GlobalData.CMPlaying.containsKey(name))
						playerList = GlobalData.CMPlaying.get(name);
					else
						playerList = new ArrayList<String>();

					int index = StringUtil.listContains(playerList, ":-1:");
					if (index > -1) {
						user.sendMessage("You are already playing a song or radio station!");
						return false;
					}

					float volume = 100;

					if (GlobalData.CMVolume.containsKey(name))
						volume = (volume / 100) * (float) GlobalData.CMVolume.get(name);

					playerList.add(volume + ":" + playerString + ":-1:10:" + SongString);
					GlobalData.CMPlaying.put(name, playerList);
				}
				return true;

			} else {
				String[] split = songNumbers.split(",");
				for (int i = 0; i < split.length; i++) {
					if (!StringUtil.isInteger(split[i])) {
						user.sendMessage("Use " + ChatColor.RED + "'/cm play [player] <songnumbers>'" + ChatColor.WHITE + " (seperated by comma)");
						user.sendMessage("Use " + ChatColor.RED + "'/cm songlist [player] <page>'" + ChatColor.WHITE + " to get a list of songs and their numbers");
						return false;
					}
				}
				String[] songList = FileUtil.listOnlyFiles(CustomMusic.maindir + "Music/" + (String.valueOf(user)).toLowerCase() + "/");
				String SongString = "";
				for (int k = 0; k < split.length; k++) {
					try {
						SongString = SongString + songList[Integer.parseInt(split[k]) - 1] + ">>";

					} catch (Exception e) {
						user.sendMessage("No song number " + Integer.parseInt(split[k]) + " found for user " + OtherPlayer);
						return false;
					}
				}

				if (global) {
					if (!playGlobal(user, SongString, playerString))
						return false;
				} else {
					ArrayList<String> playerList;
					if (GlobalData.CMPlaying.containsKey(name))
						playerList = GlobalData.CMPlaying.get(name);
					else
						playerList = new ArrayList<String>();

					int index = StringUtil.listContains(playerList, ":-1:");
					if (index > -1) {
						user.sendMessage("You are already playing a song or radio station!");
						return false;
					}

					float volume = 100;

					if (GlobalData.CMVolume.containsKey(name))
						volume = (volume / 100) * (float) GlobalData.CMVolume.get(name);

					playerList.add(volume + ":" + playerString + ":-1:10:" + SongString);
					GlobalData.CMPlaying.put(name, playerList);
				}
				return true;

			}

		} else {
			user.sendMessage(OtherPlayer + " has no songs!");
			return false;
		}
	}

	public static boolean playStation(Player user, int stationNumber, Boolean global, String OtherPlayer) {

		String name = user.getName().toLowerCase();
		String playerString = ("CraftPlayer{name=" + OtherPlayer + "}").toLowerCase();

		File file = new File(CustomMusic.maindir + "Music/" + playerString + "/webradio/");
		if (file.exists() && !FileUtil.isempty(CustomMusic.maindir + "Music/" + playerString + "/webradio/")) {

			String[] songList = FileUtil.listOnlyFiles(CustomMusic.maindir + "Music/" + playerString + "/webradio/");
			String SongString = "";
			try {
				SongString = "webradio/" + songList[stationNumber - 1] + ">>";

			} catch (Exception e) {
				user.sendMessage("No station number " + stationNumber + " found for user " + OtherPlayer);
				return false;
			}

			if (global) {
				if (!playGlobal(user, SongString, playerString))
					return false;
			} else {
				ArrayList<String> playerList;
				if (GlobalData.CMPlaying.containsKey(name))
					playerList = GlobalData.CMPlaying.get(name);
				else
					playerList = new ArrayList<String>();

				int index = StringUtil.listContains(playerList, ":-1:");
				if (index > -1) {
					user.sendMessage("You are already playing a song or radio station!");
					return false;
				}

				float volume = 100;

				if (GlobalData.CMVolume.containsKey(name))
					volume = (volume / 100) * (float) GlobalData.CMVolume.get(name);

				playerList.add(volume + ":" + playerString + ":-1:10:" + SongString);
				GlobalData.CMPlaying.put(name, playerList);
			}
			return true;

		} else {
			user.sendMessage(OtherPlayer + " has no songs!");
			return false;
		}
	}

	private static boolean playGlobal(Player player, String songString, String box) {
		Object[] players = GlobalData.CMUsers.values().toArray();
		for (int i = 0; i < players.length; i++) {
			String name = String.valueOf(players[i]).toLowerCase();

			ArrayList<String> playerList;
			if (GlobalData.CMPlaying.containsKey(name))
				playerList = GlobalData.CMPlaying.get(name);
			else
				playerList = new ArrayList<String>();

			int index = StringUtil.listContains(playerList, ":-2:11:");
			if (index > -1) {
				player.sendMessage("There is already playing a song or radio station globally!");
				return false;
			}

			float volume = 100;

			if (GlobalData.CMVolume.containsKey(name))
				volume = (volume / 100) * (float) GlobalData.CMVolume.get(name);

			playerList.add(volume + ":" + box + ":-2:11:" + songString);
			GlobalData.CMPlaying.put(name, playerList);
		}
		return true;
	}

	public static void stopSong(Player player) {
		Boolean stopped = false;
		String name = player.getName().toLowerCase();
		ArrayList<String> playerList = GlobalData.CMPlaying.get(name);
		if (playerList != null) {
			for (int k = 0; k < playerList.size(); k++) {
				int index = StringUtil.listContains(playerList, ":-1:10:");
				if (index > -1) {
					playerList.remove(index);
					stopped = true;
				}
			}
			if (stopped)
				player.sendMessage(ChatColor.RED + "Song stopped!");
			else
				player.sendMessage(ChatColor.RED + "Cannot stop song!");
		} else
			player.sendMessage("There is no song playing!");
	}

	public static void gstopSong(Player player) {
		Boolean stopped = false;
		Object[] players = GlobalData.CMUsers.values().toArray();
		for (int i = 0; i < players.length; i++) {
			String name = String.valueOf(players[i]).toLowerCase();

			ArrayList<String> playerList = GlobalData.CMPlaying.get(name);

			if (playerList != null) {

				for (int k = 0; k < playerList.size(); k++) {
					int index = StringUtil.listContains(playerList, ":-2:11:");
					if (index > -1) {
						playerList.remove(index);
						stopped = true;
					}
				}
			}
		}
		if (stopped)
			player.sendMessage(ChatColor.RED + "Song stopped globally!");
		else
			player.sendMessage(ChatColor.RED + "Cannot stop song!");
	}

	public static void checkForBox(Player player) {
		if (GlobalData.CMUsers.containsKey(player.getName().toLowerCase())) {
			if (GlobalData.CMConnected.containsKey(player.getName().toLowerCase())) {
				Location pos = player.getLocation(); // get position of
														// MoveEvent
				Calculations.boxInRange(player, pos); // check if player is in
														// range of box and
														// start playing music
				Calculations.inArea(player, pos); // check if player is i an
													// area and start playing
													// music
				Calculations.world(player,pos); //play world-specific music
				Calculations.biome(player,pos); //play biome-specific music

			} else {
				player.sendMessage("CustomMusic: " + ChatColor.RED + "Connection to AudioClient lost!");
				GlobalData.CMUsers.remove(player.getName().toLowerCase());
				GlobalData.CMVolume.remove(player.getName().toLowerCase());
				GlobalData.CMPlaying.remove(player.getName().toLowerCase());
				GlobalData.CMUploadPerm.remove(player.getName().toLowerCase());
				GlobalData.CMAreaDefinitions.remove(player);

			}
		}
	}

	public static void changeVolume(Player player, int newVolume) {

		String name = player.getName().toLowerCase();

		int oldVolume = 100;
		if (GlobalData.CMVolume.containsKey(name))
			oldVolume = GlobalData.CMVolume.get(name);

		GlobalData.CMVolume.put(player.getName().toLowerCase(), newVolume); // put
																			// volume
																			// into
																			// hashmap

		ArrayList<String> playerList; // adjust volume for every song tht is
										// playing
		if (GlobalData.CMPlaying.containsKey(name))
			playerList = GlobalData.CMPlaying.get(name);
		else
			playerList = new ArrayList<String>();

		for (int i = 0; i < playerList.size(); i++) {
			String oldValueString = playerList.get(i);
			String[] split = oldValueString.split(":");
			float volume = (float) Float.parseFloat(split[0]) * ((float) newVolume / (float) oldVolume);
			String newValueString = String.valueOf(volume) + oldValueString.substring(split[0].length());
			playerList.remove(i);
			playerList.add(newValueString);
		}

	}

}
