package main.java.de.WegFetZ.CustomMusic;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import main.java.de.WegFetZ.CustomMusic.Utils.FileUtil;
import main.java.de.WegFetZ.CustomMusic.Utils.StringUtil;

public class BoxList {

	// ############################ SET FUNCTIONS
	// ##################################

	public static void areaDefine(Player player, int corner) {
		int othercorner = 2;
		if (corner == 2)
			othercorner = 1;
		Location[] definition = new Location[2];

		if (GlobalData.CMAreaDefinitions.containsKey(player))
			definition = GlobalData.CMAreaDefinitions.get(player);

		Location pos = player.getLocation();

		if (definition[othercorner - 1] != null && ((int) (definition[othercorner - 1].getX()) == (int) (pos.getX()) || (int) (definition[othercorner - 1].getY()) == (int) (pos.getY()) || (int) (definition[othercorner - 1].getZ()) == (int) (pos.getZ()))) {
			player.sendMessage("Cannot define two corners at the same X or Y or Z coordinates!");
		} else {
			definition[corner - 1] = pos;
			GlobalData.CMAreaDefinitions.put(player, definition);
			player.sendMessage(ChatColor.RED + "CustomMusic: Corner " + corner + " defined!");
		}
	}

	public static void setarea(Player player, int intnumber, int fadeoutRange, int priority) {

		String number = "a" + intnumber;
		loadareas();

		Location[] definition = new Location[2];

		if (GlobalData.CMAreaDefinitions.containsKey(player))
			definition = GlobalData.CMAreaDefinitions.get(player);

		if (definition[0] != null && definition[1] != null) {
			float x0 = Math.round(definition[0].getX());
			float y0 = Math.round(definition[0].getY());
			float z0 = Math.round(definition[0].getZ());

			float x1 = Math.round(definition[1].getX());
			float y1 = Math.round(definition[1].getY());
			float z1 = Math.round(definition[1].getZ());

			int maxSize = Permission.getPermissionInteger(player, "cm.maxAreaEdgeLength", LoadSettings.defaultMaxAreaSize);
			if (maxSize != 0 && (maxSize <= Math.abs(x0 - x1) && maxSize <= Math.abs(y0 - y1) || maxSize <= Math.abs(z0 - z1))) {
				player.sendMessage("You can set areas with a maximum edge length of " + maxSize + ".");
				return;
			}

			String world = player.getLocation().getWorld().toString();
			String result = Calculations.areaFree(x0, y0, z0, x1, y1, z1, world);
			if (result.equals("area is free")) {
				deleteArea(player, number, false, player.getName());

				savearea(player, number, priority, fadeoutRange, x0, y0, z0, x1, y1, z1, world);
				player.sendMessage(ChatColor.RED + "CustomMusic: Area number " + number + " set with f-range " + fadeoutRange + " and priority " + priority + ".");
				loadareas(); // reload area data
				Mp3PlayerHandler.checkForBox(player);
			} else {
				if (!Permission.permission(player, "cm.area.set.overlap", true)) {
					player.sendMessage("You don't have permission to set an area in range of: " + ChatColor.RED + result);
				} else {
					deleteArea(player, number, false, player.getName()); // delete
																			// player's
					// box if
					// already exists
					savearea(player, number, priority, fadeoutRange, x0, y0, z0, x1, y1, z1, world);
					player.sendMessage(ChatColor.RED + "CustomMusic: Area number " + number + " set with priority " + priority + ".");
					player.sendMessage(ChatColor.RED + "This area overlaps with " + result);
					loadareas(); // reload area data
					Mp3PlayerHandler.checkForBox(player);
				}
			}
		} else
			player.sendMessage("Use " + ChatColor.RED + "/cm definearea <1|2>" + ChatColor.WHITE + "  to define an area first.");

	}

	private static void savearea(Player player, String number, int priority, int fadeoutRange, float x0, float y0, float z0, float x1, float y1, float z1, String world) {
		try {
			String fileName = CustomMusic.maindir + "AreaList.db";
			PrintWriter pw = new PrintWriter(new FileWriter(fileName, true));
			pw.println(String.valueOf(player) + ":" + number + ":" + priority + ":" + fadeoutRange + ":" + x0 + ":" + y0 + ":" + z0 + ":" + x1 + ":" + y1 + ":" + z1 + ":" + world);
			pw.close();
		} catch (Exception e) {
			log.debug("writing information to AreaList.db", e);
		}

	}

	public static void setbox(Player player, int number, int range, int priority) {
		loadboxes();
		float x = (float) player.getLocation().getX();// get player's
														// current location
		float y = (float) player.getLocation().getY();
		float z = (float) player.getLocation().getZ();
		String world = player.getLocation().getWorld().toString();
		String result = Calculations.boxAreaFree(x, y, z, world, range);

		if (result.equals("area is free")) {
			deleteBox(player, number, false, player.getName()); // delete
																// player's box
																// if
			// already exists
			savebox(player, number, range, x, y, z, world, priority);
			player.sendMessage(ChatColor.RED + "CustomMusic: Box number " + number + " set with range " + range + " and priority " + priority + ".");
			loadboxes(); // reload box data
			Mp3PlayerHandler.checkForBox(player);
		} else {
			if (!Permission.permission(player, "cm.box.set.overlap", true)) {
				player.sendMessage("You don't have permission to set a box in range of: " + ChatColor.RED + result);
			} else {
				deleteBox(player, number, false, player.getName()); // delete
																	// player's
																	// box if
				// already exists
				savebox(player, number, range, x, y, z, world, priority);
				player.sendMessage(ChatColor.RED + "CustomMusic: Box number " + number + " set with range " + range + " and priority " + priority + ".");
				player.sendMessage(ChatColor.RED + "This box overlaps with " + result);
				loadboxes(); // reload box data
				Mp3PlayerHandler.checkForBox(player);
			}
		}
	}

	public static void savebox(Player player, int number, int range, float posx, float posy, float posz, String world, int priority) {
		try {
			String fileName = CustomMusic.maindir + "BoxList.db";
			PrintWriter pw = new PrintWriter(new FileWriter(fileName, true));
			pw.println(String.valueOf(player) + ":" + posx + ":" + posy + ":" + posz + ":" + number + ":" + range + ":" + world + ":" + priority);
			pw.close();
		} catch (Exception e) {
			log.debug("writing information to BoxList.db", e);
		}
	}

	public static void setWorld(Player player, String world, int volume, int priority) {
		loadworlds();
		deleteWorld(player, world, false);

		if ((Bukkit.getServer().getWorlds()).contains(Bukkit.getServer().getWorld(world))) {
			saveWorld(player, world, volume, priority);
			player.sendMessage(ChatColor.RED + "CustomMusic: World " + world + " set with volume " + volume + " and priority " + priority + ".");
			loadworlds();
			Mp3PlayerHandler.checkForBox(player);
		} else
			player.sendMessage("World " + world + " does not exist!");
	}

	public static void saveWorld(Player player, String world, int volume, int priority) {
		try {
			String fileName = CustomMusic.maindir + "WorldList.db";
			PrintWriter pw = new PrintWriter(new FileWriter(fileName, true));
			pw.println(String.valueOf(player) + ":" + world.toLowerCase() + ":" + String.valueOf(volume) + ":" + String.valueOf(priority));
			pw.close();
		} catch (Exception e) {
			log.debug("writing information to WorldList.db", e);
		}
	}

	public static void setBiome(Player player, String biome, int volume, int priority) {
		loadbiomes();
		deleteBiome(player, biome, false);

		if (Arrays.asList(GlobalData.av_biomes).toString().toLowerCase().contains(biome.toLowerCase())) {
			saveBiome(player, biome, volume, priority);
			player.sendMessage(ChatColor.RED + "CustomMusic: Biome " + biome + " set with volume " + volume + " and priority " + priority + ".");
			loadbiomes();
			Mp3PlayerHandler.checkForBox(player);
		} else
			player.sendMessage("Biome " + biome + " does not exist!");
	}

	public static void saveBiome(Player player, String biome, int volume, int priority) {
		try {
			String fileName = CustomMusic.maindir + "BiomeList.db";
			PrintWriter pw = new PrintWriter(new FileWriter(fileName, true));
			pw.println(String.valueOf(player) + ":" + biome.toLowerCase() + ":" + String.valueOf(volume) + ":" + String.valueOf(priority));
			pw.close();
		} catch (Exception e) {
			log.debug("writing information to BiomeList.db", e);
		}
	}

	// ############################ DELETE FUNCTIONS
	// ##################################

	static void deleteArea(Player player, String number, boolean showChat, String playername) {
		String playerstring = "CraftPlayer{name=" + playername + "}";

		for (int k = 0; k < GlobalData.area_count; k++) {

			if (GlobalData.area_aowner[k] != null && GlobalData.area_aowner[k].equalsIgnoreCase(playerstring) && GlobalData.area_anumber[k].equalsIgnoreCase(number)) {

				String SongListZusatz = "";

				if (GlobalData.area_aSongList[k] != null)
					SongListZusatz = ":" + GlobalData.area_aSongList[k];

				FileUtil.removeLineFromFile(CustomMusic.maindir + "AreaList.db", GlobalData.area_aowner[k] + ":" + GlobalData.area_anumber[k] + ":" + GlobalData.area_aprior[k] + ":" + GlobalData.area_afadeoutRange[k] + ":" + GlobalData.area_aposx0[k] + ":" + GlobalData.area_aposy0[k] + ":"
						+ GlobalData.area_aposz0[k] + ":" + GlobalData.area_aposx1[k] + ":" + GlobalData.area_aposy1[k] + ":" + GlobalData.area_aposz1[k] + ":" + GlobalData.area_aworld[k] + SongListZusatz);
				// ==> delete line

				player.sendMessage(ChatColor.RED + "CustomMusic: " + playername + "'s area " + number + " deleted!");

				if (Mp3PlayerHandler.isPlaying(player, GlobalData.area_aowner[k], number)) {
					Mp3PlayerHandler.stopPlaying(player, GlobalData.area_aowner[k], number);
				}

				BoxList.loadareas();
				unignoreAreaForAll(playername, number);
				return;
			}
		}
		if (showChat)
			player.sendMessage("No area found for user " + playername + " and number " + number + ".");
	}

	public static void deleteBox(Player user, int number, Boolean showChat, String playername) {

		String playerstring = "CraftPlayer{name=" + playername + "}";
		for (int k = 0; k < GlobalData.box_count; k++) {

			if (GlobalData.box_aowner[k] != null && GlobalData.box_aowner[k].equalsIgnoreCase(playerstring) && GlobalData.box_anumber[k] == number) {// if
				// player
				// already
				// registered
				// a
				// box
				// on
				// this
				// number
				String SongListZusatz = "";

				if (GlobalData.box_aSongList[k] != null)
					SongListZusatz = ":" + GlobalData.box_aSongList[k];

				FileUtil.removeLineFromFile(CustomMusic.maindir + "BoxList.db", GlobalData.box_aowner[k] + ":" + GlobalData.box_aposx[k] + ":" + GlobalData.box_aposy[k] + ":" + GlobalData.box_aposz[k] + ":" + GlobalData.box_anumber[k] + ":" + GlobalData.box_arange[k] + ":"
						+ GlobalData.box_aworld[k] + ":" + GlobalData.box_aprior[k] + SongListZusatz);
				// ==> delete line

				user.sendMessage(ChatColor.RED + "CustomMusic: " + playername + "'s box number " + number + " deleted!");

				if (Mp3PlayerHandler.isPlaying(user, GlobalData.box_aowner[k], String.valueOf(number))) {
					Mp3PlayerHandler.stopPlaying(user, GlobalData.box_aowner[k], String.valueOf(number));
				}

				BoxList.loadboxes();
				unignoreBoxForAll(playername, number);
				return;
			}
		}
		if (showChat)
			user.sendMessage("No box found for user " + playername + " and number " + number + ".");
	}

	public static void deleteWorld(Player player, String world, boolean showChat) {
		for (int k = 0; k < GlobalData.world_count; k++) {
			if (GlobalData.world_aworld[k].equalsIgnoreCase(world)) {
				String SongListZusatz = "";

				if (GlobalData.world_asongList[k] != null)
					SongListZusatz = ":" + GlobalData.world_asongList[k];

				FileUtil.removeLineFromFile(CustomMusic.maindir + "WorldList.db", GlobalData.world_aowner[k] + ":" + GlobalData.world_aworld[k] + ":" + GlobalData.world_avolume[k] + ":" + GlobalData.world_aprior[k] + SongListZusatz);
				player.sendMessage(ChatColor.RED + "CustomMusic: Music for world " + world + " removed!");

				if (Mp3PlayerHandler.isPlaying(player, GlobalData.world_aowner[k], "w" + String.valueOf(k))) {
					Mp3PlayerHandler.stopPlaying(player, GlobalData.world_aowner[k], "w" + String.valueOf(k));
				}

				loadworlds();
				unignoreWorldForAll(world);
				return;
			}
		}
		if (showChat)
			player.sendMessage("World " + world + " is not configured to play music!");
	}

	public static void deleteBiome(Player player, String biome, boolean showChat) {
		for (int k = 0; k < GlobalData.biome_count; k++) {
			if (GlobalData.biome_abiome[k].equalsIgnoreCase(biome)) {
				String SongListZusatz = "";

				if (GlobalData.biome_asongList[k] != null)
					SongListZusatz = ":" + GlobalData.biome_asongList[k];

				FileUtil.removeLineFromFile(CustomMusic.maindir + "BiomeList.db", GlobalData.biome_aowner[k] + ":" + GlobalData.biome_abiome[k] + ":" + GlobalData.biome_avolume[k] + ":" + GlobalData.biome_aprior[k] + SongListZusatz);
				player.sendMessage(ChatColor.RED + "CustomMusic: Music for biome " + biome + " removed!");

				if (Mp3PlayerHandler.isPlaying(player, GlobalData.biome_aowner[k], "bio" + String.valueOf(k))) {
					Mp3PlayerHandler.stopPlaying(player, GlobalData.biome_aowner[k], "bio" + String.valueOf(k));
				}

				loadbiomes();
				unignoreBiomeForAll(biome);
				return;
			}
		}
		if (showChat)
			player.sendMessage("Biome " + biome + " is not configured to play music!");
	}

	public static void deleteSong(Player player, int number, String playername) {
		String playerstring = "CraftPlayer{name=" + playername + "}";
		File file = new File(CustomMusic.maindir + "Music/" + playerstring.toLowerCase() + "/");
		if (file.exists() && file.list().length > 1) {
			String[] songList = FileUtil.listOnlyFiles(CustomMusic.maindir + "Music/" + playerstring.toLowerCase() + "/");
			if (number > 0 && number <= songList.length) {
				File deleteFile = new File(CustomMusic.maindir + "Music/" + playerstring.toLowerCase() + "/" + songList[number - 1]);
				if (deleteFile.exists()) {
					GlobalData.CMDeleteSongs.put(GlobalData.CMDeleteSongs.size(), CustomMusic.maindir + "Music/" + playerstring.toLowerCase() + "/" + songList[number - 1]);
					deleteFile.delete();
					loadboxes();
					loadareas();
					player.sendMessage(ChatColor.RED + "CustomMusic: " + playername + "'s song number " + number + " deleted!");
				} else
					player.sendMessage("Couldn't delete song: unknown error");
			} else
				player.sendMessage(playername + " has no song with the number " + number + "!");
		} else
			player.sendMessage(playername + " hasn't got any songs!");
	}

	public static void deleteStation(Player player, int number, String playername) {
		String playerstring = "CraftPlayer{name=" + playername + "}";
		File file = new File(CustomMusic.maindir + "Music/" + playerstring.toLowerCase() + "/webradio/");
		if (file.exists() && file.list().length > 1) {
			String[] radioList = FileUtil.listOnlyFiles(CustomMusic.maindir + "Music/" + playerstring.toLowerCase() + "/webradio/");
			if (number > 0 && number <= radioList.length) {
				File deleteFile = new File(CustomMusic.maindir + "Music/" + playerstring.toLowerCase() + "/webradio/" + radioList[number - 1]);
				if (deleteFile.exists()) {
					GlobalData.CMDeleteSongs.put(GlobalData.CMDeleteSongs.size(), CustomMusic.maindir + "Music/" + playerstring.toLowerCase() + "/webradio/" + radioList[number - 1]);
					deleteFile.delete();
					loadboxes();
					loadareas();
					player.sendMessage(ChatColor.RED + "CustomMusic: " + playername + "'s radio station number " + number + " deleted!");
				} else
					player.sendMessage("Couldn't delete radio station: unknown error");
			} else
				player.sendMessage(playername + " has no radio station with the number " + number + "!");
		} else
			player.sendMessage(playername + " hasn't got any radio stations!");
	}

	// ############################ IGNORE FUNCTIONS
	// ##################################

	public static void ignoreBox(Player player, int number, String owner) { // ignore
																			// a
																			// player's
																			// box

		boolean boxfound = false;

		String playerstring = "CraftPlayer{name=" + owner + "}";
		for (int k = 0; k < GlobalData.box_count; k++) { // search for box

			if (GlobalData.box_aowner[k] != null && GlobalData.box_aowner[k].equalsIgnoreCase(playerstring) && GlobalData.box_anumber[k] == number) {
				// box found

				try { // write to ignoredBoxes.db
					String fileName = CustomMusic.maindir + "ignoredBoxes.db";
					PrintWriter pw = new PrintWriter(new FileWriter(fileName, true));
					pw.println(String.valueOf(player) + ":" + owner + ":" + number);
					pw.close();
				} catch (Exception e) {
					log.debug("writing information to ignoredBoxes.db", e);
				}

				loadBoxIgnores();

				player.sendMessage(ChatColor.RED + "CustomMusic: Ignoring " + owner + "'s box number " + number + "!");

				if (Mp3PlayerHandler.isPlaying(player, GlobalData.box_aowner[k], String.valueOf(number)))
					Mp3PlayerHandler.stopPlaying(player, GlobalData.box_aowner[k], String.valueOf(number));

				boxfound = true;
			}
		}
		if (!boxfound)
			player.sendMessage("No box found for player " + owner + " and number " + number + ".");
	}

	public static void ignoreArea(Player player, String number, String owner) { // ignore
																				// a
																				// player's
																				// box

		boolean areafound = false;

		String playerstring = "CraftPlayer{name=" + owner + "}";
		for (int k = 0; k < GlobalData.box_count; k++) { // search for area

			if (GlobalData.area_aowner[k] != null && GlobalData.area_aowner[k].equalsIgnoreCase(playerstring) && GlobalData.area_anumber[k].equalsIgnoreCase(String.valueOf(number))) {
				// area found

				try { // write to ignoredAreas.db
					String fileName = CustomMusic.maindir + "ignoredAreas.db";
					PrintWriter pw = new PrintWriter(new FileWriter(fileName, true));
					pw.println(String.valueOf(player) + ":" + owner + ":" + number);
					pw.close();
				} catch (Exception e) {
					log.debug("writing information to ignoredAreas.db", e);
				}

				loadAreaIgnores();

				player.sendMessage(ChatColor.RED + "CustomMusic: Ignoring " + owner + "'s area number " + number + "!");

				if (Mp3PlayerHandler.isPlaying(player, GlobalData.area_aowner[k], String.valueOf(number)))
					Mp3PlayerHandler.stopPlaying(player, GlobalData.area_aowner[k], String.valueOf(number));

				areafound = true;
			}
		}
		if (!areafound)
			player.sendMessage("No area found for player " + owner + " and number " + number + ".");
	}

	public static void unignoreArea(Player player, String number, String owner) {
		String playerstring = String.valueOf(player);
		boolean ignoredAreaFound = false;

		for (int k = 0; k < GlobalData.area_lignoByPlayer.size(); k++) {

			if (GlobalData.area_lignoByPlayer.get(k).equalsIgnoreCase(playerstring) && GlobalData.area_lignoAreaNumber.get(k).equalsIgnoreCase(number) && GlobalData.area_lignoOwner.get(k).equalsIgnoreCase(owner)) {

				FileUtil.removeLineFromFile(CustomMusic.maindir + "ignoredAreas.db", GlobalData.area_lignoByPlayer.get(k) + ":" + GlobalData.area_lignoOwner.get(k) + ":" + number);
				// ==> delete line from ignoredBoxes.db

				ignoredAreaFound = true;
			}
		}
		if (!ignoredAreaFound)
			player.sendMessage("You are not ignoring " + owner + "'s area number " + number + ".");
		else {
			player.sendMessage(ChatColor.RED + "CustomMusic: You are no longer ignoring " + owner + "'s area number " + number + ".");
			loadAreaIgnores();
			Mp3PlayerHandler.checkForBox(player);
		}
	}

	public static void unignoreBox(Player player, int number, String owner) {
		String playerstring = String.valueOf(player);
		boolean ignoredBoxFound = false;

		for (int k = 0; k < GlobalData.box_lignoByPlayer.size(); k++) {

			if (GlobalData.box_lignoByPlayer.get(k).equalsIgnoreCase(playerstring) && GlobalData.box_lignoBoxNumber.get(k) == number && GlobalData.box_lignoOwner.get(k).equalsIgnoreCase(owner)) {

				FileUtil.removeLineFromFile(CustomMusic.maindir + "ignoredBoxes.db", GlobalData.box_lignoByPlayer.get(k) + ":" + GlobalData.box_lignoOwner.get(k) + ":" + String.valueOf(number));
				// ==> delete line from ignoredBoxes.db

				ignoredBoxFound = true;
			}
		}
		if (!ignoredBoxFound)
			player.sendMessage("You are not ignoring " + owner + "'s box number " + number + ".");
		else {
			player.sendMessage(ChatColor.RED + "CustomMusic: You are no longer ignoring " + owner + "'s box number " + number + ".");
			loadBoxIgnores();
			Mp3PlayerHandler.checkForBox(player);
		}
	}
	
	public static void ignoreWorld(Player player, String world) {

		boolean worldfound = false;

		for (int k = 0; k < GlobalData.world_count; k++) { // search for area

			if (GlobalData.world_aworld[k] != null && GlobalData.world_aworld[k].equalsIgnoreCase(world)) {
				// world found

				try { // write to ignoredWorlds.db
					String fileName = CustomMusic.maindir + "ignoredWorlds.db";
					PrintWriter pw = new PrintWriter(new FileWriter(fileName, true));
					pw.println(String.valueOf(player) + ":" + world);
					pw.close();
				} catch (Exception e) {
					log.debug("writing information to ignoredWorlds.db", e);
				}

				loadWorldIgnores();

				player.sendMessage(ChatColor.RED + "CustomMusic: Ignoring world " + world + "!");

				if (Mp3PlayerHandler.isPlaying(player, GlobalData.world_aowner[k], "w"+String.valueOf(k)))
					Mp3PlayerHandler.stopPlaying(player, GlobalData.world_aowner[k], "w"+String.valueOf(k));

				worldfound = true;
			}
		}
		if (!worldfound)
			player.sendMessage("World " + world + " is no music-playing world.");
	}
	
	
	public static void unignoreWorld(Player player, String world) {
		boolean ignoredWorldFound = false;

		for (int k = 0; k < GlobalData.world_lignoByPlayer.size(); k++) {

			if (GlobalData.world_lignoByPlayer.get(k).equalsIgnoreCase(String.valueOf(player)) && GlobalData.world_lignoWorld.get(k).equalsIgnoreCase(world)) {

				FileUtil.removeLineFromFile(CustomMusic.maindir + "ignoredWorlds.db", GlobalData.world_lignoByPlayer.get(k) + ":" + GlobalData.world_lignoWorld.get(k));
				// ==> delete line from ignoredBoxes.db

				ignoredWorldFound = true;
			}
		}
		if (!ignoredWorldFound)
			player.sendMessage("You are not ignoring world " + world + ".");
		else {
			player.sendMessage(ChatColor.RED + "CustomMusic: You are no longer ignoring world " + world + ".");
			loadWorldIgnores();
			Mp3PlayerHandler.checkForBox(player);
		}
	}
	
	
	public static void ignoreBiome(Player player, String biome) {

		boolean biomefound = false;

		for (int k = 0; k < GlobalData.biome_count; k++) { // search for area

			if (GlobalData.biome_abiome[k] != null && GlobalData.biome_abiome[k].equalsIgnoreCase(biome)) {
				// world found

				try { // write to ignoredWorlds.db
					String fileName = CustomMusic.maindir + "ignoredBiomes.db";
					PrintWriter pw = new PrintWriter(new FileWriter(fileName, true));
					pw.println(String.valueOf(player) + ":" + biome);
					pw.close();
				} catch (Exception e) {
					log.debug("writing information to ignoredBiomes.db", e);
				}

				loadBiomeIgnores();

				player.sendMessage(ChatColor.RED + "CustomMusic: Ignoring world " + biome + "!");

				if (Mp3PlayerHandler.isPlaying(player, GlobalData.world_aowner[k], "bio"+String.valueOf(k)))
					Mp3PlayerHandler.stopPlaying(player, GlobalData.world_aowner[k], "bio"+String.valueOf(k));

				biomefound = true;
			}
		}
		if (!biomefound)
			player.sendMessage("Biome " + biome + " is no music-playing biome.");
	}

	
	
	public static void unignoreBiome(Player player, String biome) {
		boolean ignoredBiomeFound = false;

		for (int k = 0; k < GlobalData.biome_lignoByPlayer.size(); k++) {

			if (GlobalData.biome_lignoByPlayer.get(k).equalsIgnoreCase(String.valueOf(player)) && GlobalData.biome_lignoBiome.get(k).equalsIgnoreCase(biome)) {

				FileUtil.removeLineFromFile(CustomMusic.maindir + "ignoredBiomes.db", GlobalData.biome_lignoByPlayer.get(k) + ":" + GlobalData.biome_lignoBiome.get(k));
				// ==> delete line from ignoredBoxes.db

				ignoredBiomeFound = true;
			}
		}
		if (!ignoredBiomeFound)
			player.sendMessage("You are not ignoring biome " + biome + ".");
		else {
			player.sendMessage(ChatColor.RED + "CustomMusic: You are no longer ignoring biome " + biome + ".");
			loadBiomeIgnores();
			Mp3PlayerHandler.checkForBox(player);
		}
	}
	
	
	
	public static void unignoreAreaForAll(String owner, String number) {
		boolean ignoredAreaFound = false;

		for (int k = 0; k < GlobalData.area_lignoByPlayer.size(); k++) {

			if (GlobalData.area_lignoAreaNumber.get(k).equalsIgnoreCase(number) && GlobalData.area_lignoOwner.get(k).equalsIgnoreCase(owner)) {

				FileUtil.removeLineFromFile(CustomMusic.maindir + "ignoredAreas.db", GlobalData.area_lignoByPlayer.get(k) + ":" + GlobalData.area_lignoOwner.get(k) + ":" + number);
				// ==> delete line from ignoredBoxes.db

				ignoredAreaFound = true;
			}
		}
		if (ignoredAreaFound)
			loadAreaIgnores();
	}

	public static void unignoreBoxForAll(String owner, int number) {
		boolean ignoredBoxFound = false;

		for (int k = 0; k < GlobalData.box_lignoByPlayer.size(); k++) {

			if (GlobalData.box_lignoBoxNumber.get(k) == number && GlobalData.box_lignoOwner.get(k).equalsIgnoreCase(owner)) {

				FileUtil.removeLineFromFile(CustomMusic.maindir + "ignoredBoxes.db", GlobalData.box_lignoByPlayer.get(k) + ":" + GlobalData.box_lignoOwner.get(k) + ":" + String.valueOf(number));
				// ==> delete line from ignoredBoxes.db

				ignoredBoxFound = true;
			}
		}
		if (ignoredBoxFound)
			loadBoxIgnores();
	}
	
	
	public static void unignoreWorldForAll(String world) {
		boolean ignoredWorldFound = false;

		for (int k = 0; k < GlobalData.world_lignoByPlayer.size(); k++) {

			if (GlobalData.world_lignoWorld.get(k).equalsIgnoreCase(world)) {

				FileUtil.removeLineFromFile(CustomMusic.maindir + "ignoredWorlds.db", GlobalData.world_lignoByPlayer.get(k) + ":" + GlobalData.world_lignoWorld.get(k));
				// ==> delete line from ignoredWorlds.db

				ignoredWorldFound = true;
			}
		}
		if (ignoredWorldFound)
			loadWorldIgnores();
	}
	
	public static void unignoreBiomeForAll(String biome) {
		boolean ignoredBiomeFound = false;

		for (int k = 0; k < GlobalData.biome_lignoByPlayer.size(); k++) {

			if (GlobalData.biome_lignoBiome.get(k).equalsIgnoreCase(biome)) {

				FileUtil.removeLineFromFile(CustomMusic.maindir + "ignoredBiomes.db", GlobalData.biome_lignoByPlayer.get(k) + ":" + GlobalData.biome_lignoBiome.get(k));
				// ==> delete line from ignoredWorlds.db

				ignoredBiomeFound = true;
			}
		}
		if (ignoredBiomeFound)
			loadBiomeIgnores();
	}

	// ############################ LIST FUNCTIONS
	// ##################################

	public static void listIgno(Player player, int page) {
		String playerstring = String.valueOf(player);
		int count = 0;

		for (int i = ((page * 17) - 17); i < GlobalData.box_lignoByPlayer.size(); i++) {
			if (GlobalData.box_lignoByPlayer.get(i).equalsIgnoreCase(playerstring)) {
				count++;
				if (count == 1)
					player.sendMessage("Ignored boxes:");
				player.sendMessage(ChatColor.RED + "Player: " + ChatColor.WHITE + GlobalData.box_lignoOwner.get(i) + ChatColor.RED + " | Nb.: " + ChatColor.WHITE + GlobalData.box_lignoBoxNumber.get(i));
				if (count >= 17) {
					player.sendMessage("Use " + ChatColor.RED + "/cm ignorelist " + (page + 1) + ChatColor.WHITE + " to see the next page.");
					break;
				}
			}
		}

		if (count < 17) {
			for (int i = ((page * 17) - 17); i < GlobalData.area_lignoByPlayer.size(); i++) {
				if (GlobalData.area_lignoByPlayer.get(i).equalsIgnoreCase(playerstring)) {
					count++;
					if (i == ((page * 17) - 17))
						player.sendMessage("Ignored areas:");
					player.sendMessage(ChatColor.RED + "Player: " + ChatColor.WHITE + GlobalData.area_lignoOwner.get(i) + ChatColor.RED + " | Nb.: " + ChatColor.WHITE + GlobalData.area_lignoAreaNumber.get(i));
					if (count >= 17) {
						player.sendMessage("Use " + ChatColor.RED + "/cm ignorelist " + (page + 1) + ChatColor.WHITE + " to see the next page.");
						break;
					}
				}
			}
		}
		
		if (count < 17) {
			for (int i = ((page * 17) - 17); i < GlobalData.world_lignoByPlayer.size(); i++) {
				if (GlobalData.world_lignoByPlayer.get(i).equalsIgnoreCase(playerstring)) {
					count++;
					if (i == ((page * 17) - 17))
						player.sendMessage("Ignored worlds:");
					player.sendMessage(ChatColor.RED + "World: " + ChatColor.WHITE + GlobalData.world_lignoWorld.get(i));
					if (count >= 17) {
						player.sendMessage("Use " + ChatColor.RED + "/cm ignorelist " + (page + 1) + ChatColor.WHITE + " to see the next page.");
						break;
					}
				}
			}
		}

		if (count < 17) {
			for (int i = ((page * 17) - 17); i < GlobalData.biome_lignoByPlayer.size(); i++) {
				if (GlobalData.biome_lignoByPlayer.get(i).equalsIgnoreCase(playerstring)) {
					count++;
					if (i == ((page * 17) - 17))
						player.sendMessage("Ignored biomes:");
					player.sendMessage(ChatColor.RED + "Biome: " + ChatColor.WHITE + GlobalData.biome_lignoBiome.get(i));
					if (count >= 17) {
						player.sendMessage("Use " + ChatColor.RED + "/cm ignorelist " + (page + 1) + ChatColor.WHITE + " to see the next page.");
						break;
					}
				}
			}
		}
		
		if (count == 0)
			player.sendMessage("You are not ignorig anything");
	}

	public static void listSongs(Player player, int Page, String playername) {
		String playerstring = "CraftPlayer{name=" + playername + "}";

		String[] songList = FileUtil.listOnlyFiles(CustomMusic.maindir + "Music/" + playerstring.toLowerCase() + "/");
		if (songList != null && songList.length > 0) {
			for (int i = ((Page * 17) - 17); i < songList.length; i++) {
				player.sendMessage(ChatColor.RED + String.valueOf(((int) (i + 1))) + ChatColor.WHITE + " - " + songList[i]);
				if (i >= Page * 17) {
					player.sendMessage("Use " + ChatColor.RED + "/cm list " + playername + " " + (Page + 1) + ChatColor.WHITE + " to see more songs.");
					break;
				}
			}
		} else
			player.sendMessage(playername + " hasn't got any songs!");
	}

	public static void listStations(Player player, int Page, String playername) {
		String playerstring = "CraftPlayer{name=" + playername + "}";

		String[] stationList = FileUtil.listOnlyFiles(CustomMusic.maindir + "Music/" + playerstring.toLowerCase() + "/webradio/");
		if (stationList != null && stationList.length > 0) {
			for (int i = ((Page * 17) - 17); i < stationList.length; i++) {
				File tempFile = new File("Music/" + playerstring.toLowerCase() + "/webradio/" + stationList[i]);
				String title = tempFile.getName();
				String ext = FileUtil.getExtension(tempFile);
				if (ext != null && ext.equalsIgnoreCase(".pls")) // read the
																	// title
																	// from pls
																	// file
					title = FileUtil.getPlsTitle(tempFile);
				player.sendMessage(ChatColor.RED + String.valueOf(((int) (i + 1))) + ChatColor.WHITE + " - " + title);
				if (i >= Page * 17) {
					player.sendMessage("Use " + ChatColor.RED + "/cm list " + playername + " " + (Page + 1) + ChatColor.WHITE + " to see more radio stations.");
					break;
				}
			}
		} else
			player.sendMessage(playername + " hasn't got any radio stations!");
	}

	public static void listBoxes(Player player, int page, String playername) {
		String playerstring = "CraftPlayer{name=" + playername + "}";
		int count = 0;

		for (int i = ((page * 18) - 18); i < GlobalData.box_count; i++) {
			if (GlobalData.box_aowner[i] != null && GlobalData.box_aowner[i].equalsIgnoreCase(playerstring)) {
				count++;
				int length = GlobalData.box_aworld[i].length();
				String world = GlobalData.box_aworld[i].substring(16, length - 1);
				player.sendMessage(ChatColor.RED + "Box-Nb: " + GlobalData.box_anumber[i] + ChatColor.WHITE + " | Range: " + GlobalData.box_arange[i] + " | Priority: " + GlobalData.box_aprior[i] + " | World: " + world + " | X:" + Math.round(GlobalData.box_aposx[i]) + " | Y:"
						+ Math.round(GlobalData.box_aposy[i]) + " | Z:" + Math.round(GlobalData.box_aposz[i]));
				if (count >= 18) {
					player.sendMessage("Use " + ChatColor.RED + "/cm boxlist " + (page + 1) + ChatColor.WHITE + " to see more boxes.");
					break;
				}
			}
		}
		if (count == 0)
			player.sendMessage(playername + " hasn't got any boxes!");
	}

	public static void listWorlds(Player player, int page) {
		int count = 0;

		for (int i = ((page * 18) - 18); i < GlobalData.world_count; i++) {
			count++;
			player.sendMessage(ChatColor.RED + "World-name: " + GlobalData.world_aworld[i] + ChatColor.WHITE + " | Volume: " + GlobalData.world_avolume[i] + " | Priority: " + GlobalData.world_aprior[i] + " | Created: " + GlobalData.world_aowner[i]);
			if (count >= 18) {
				player.sendMessage("Use " + ChatColor.RED + "/cm worldlist " + (page + 1) + ChatColor.WHITE + " to see more worlds.");
				break;
			}
		}
		if (count == 0)
			player.sendMessage("There are no music-playing worlds.");
	}

	public static void listBiomes(Player player, int page) {
		int count = 0;

		for (int i = ((page * 18) - 18); i < GlobalData.biome_count; i++) {
			count++;
			player.sendMessage(ChatColor.RED + "Biome-name: " + GlobalData.biome_abiome[i] + ChatColor.WHITE + " | Volume: " + GlobalData.biome_avolume[i] + " | Priority: " + GlobalData.biome_aprior[i] + " | Created: " + GlobalData.biome_aowner[i]);
			if (count >= 18) {
				player.sendMessage("Use " + ChatColor.RED + "/cm biomelist " + (page + 1) + ChatColor.WHITE + " to see more biomes.");
				break;
			}
		}
		if (count == 0)
			player.sendMessage("There are no music-playing biomes.");
	}

	public static void listAreas(Player player, int page, String playername) {
		String playerstring = "CraftPlayer{name=" + playername + "}";
		int count = 0;

		for (int i = ((page * 18) - 18); i < GlobalData.area_count; i++) {
			if (GlobalData.area_aowner[i] != null && GlobalData.area_aowner[i].equalsIgnoreCase(playerstring)) {
				count++;
				int length = GlobalData.area_aworld[i].length();
				String world = GlobalData.area_aworld[i].substring(16, length - 1);
				player.sendMessage(ChatColor.RED + "Area-Nb: " + GlobalData.area_anumber[i] + ChatColor.WHITE + " | F-Range: " + GlobalData.area_afadeoutRange[i] + " | Priority: " + GlobalData.area_aprior[i] + " | World: " + world + " | X1:" + Math.round(GlobalData.area_aposx0[i]) + " | Y1:"
						+ Math.round(GlobalData.area_aposy0[i]) + " | Z1:" + Math.round(GlobalData.area_aposz0[i]) + " | X2:" + Math.round(GlobalData.area_aposx1[i]) + " | Y2:" + Math.round(GlobalData.area_aposy1[i]) + " | Z2:" + Math.round(GlobalData.area_aposz1[i]));
				if (count >= 18) {
					player.sendMessage("Use " + ChatColor.RED + "/cm arealist " + (page + 1) + ChatColor.WHITE + " to see more areas.");
					break;
				}
			}
		}
		if (count == 0)
			player.sendMessage(playername + " hasn't got any areas!");
	}

	// ############################ CHOOSE FUNCTIONS
	// ##################################

	public static boolean chooseAreaStation(Player player, int AreaNumber, int stationNumber) {
		File file = new File(CustomMusic.maindir + "Music/" + String.valueOf(player).toLowerCase() + "/webradio/");
		if (file.exists() && file.list().length > 0) {
			String[] stationList = FileUtil.listOnlyFiles(CustomMusic.maindir + "Music/" + String.valueOf(player).toLowerCase() + "/webradio/");
			String StationString = "";
			if (stationList != null) {
				try {
					StationString = "webradio/" + stationList[stationNumber - 1] + ">>";

				} catch (Exception e) {
					player.sendMessage("You don't have a radio station with the number " + stationNumber);
					return false;
				}

			}
			if (AreaNumber > 0) {
				if (addSongsToAreaList(player, AreaNumber, StationString)) {
					loadareas();
					unignoreAreaForAll(player.getName(), "a" + String.valueOf(AreaNumber));
					try {
						Thread.sleep(150);
					} catch (InterruptedException e) {
						log.debug(null, e);
					}
					Mp3PlayerHandler.checkForBox(player);
					return true;
				} else
					return false;
			} else {
				player.sendMessage("Areanumber must be higher than 0.");
				return false;
			}
		} else {
			player.sendMessage("You have no radio stations!");
			return false;
		}
	}

	public static boolean chooseBoxStation(Player player, int BoxNumber, int stationNumber) {
		File file = new File(CustomMusic.maindir + "Music/" + String.valueOf(player).toLowerCase() + "/webradio/");
		if (file.exists() && file.list().length > 0) {
			String[] stationList = FileUtil.listOnlyFiles(CustomMusic.maindir + "Music/" + String.valueOf(player).toLowerCase() + "/webradio/");
			String StationString = "";
			if (stationList != null) {
				try {
					StationString = "webradio/" + stationList[stationNumber - 1] + ">>";

				} catch (Exception e) {
					player.sendMessage("You don't have a radio station with the number " + stationNumber);
					return false;
				}

			}
			if (BoxNumber > 0) {
				if (addSongsToBoxList(player, BoxNumber, StationString)) {
					loadboxes();
					unignoreBoxForAll(player.getName(), BoxNumber);
					try {
						Thread.sleep(150);
					} catch (InterruptedException e) {
						log.debug(null, e);
					}
					Mp3PlayerHandler.checkForBox(player);
					return true;
				} else
					return false;
			} else {
				player.sendMessage("Boxnumber must be higher than 0.");
				return false;
			}
		} else {
			player.sendMessage("You have no radio stations!");
			return false;
		}
	}

	public static boolean chooseBoxSongs(Player player, int BoxNumber, String SongNumbers) {
		if (SongNumbers.equals("all")) {
			if (BoxNumber > 0) {
				if (addSongsToBoxList(player, BoxNumber, "")) {
					loadboxes();
					unignoreBoxForAll(player.getName(), BoxNumber);
					try {
						Thread.sleep(150);
					} catch (InterruptedException e) {
						log.debug(null, e);
					}
					Mp3PlayerHandler.checkForBox(player);
					return true;
				} else
					return false;
			} else {
				player.sendMessage("Boxnumber must be higher than 0.");
				return false;
			}
		} else {
			String[] split = SongNumbers.split(",");
			for (int i = 0; i < split.length; i++) {
				if (!StringUtil.isInteger(split[i])) {
					player.sendMessage("Use " + ChatColor.RED + "'/cm choose <boxnumber> <songnumbers>'" + ChatColor.WHITE + " (seperated by comma)");
					player.sendMessage("Use " + ChatColor.RED + "'/cm songlist [player] <page>'" + ChatColor.WHITE + " to get a list of songs and their numbers");
					return false;
				}
			}
			File file = new File(CustomMusic.maindir + "Music/" + String.valueOf(player).toLowerCase());
			if (file.exists() && file.list().length > 1) {
				String[] songList = FileUtil.listOnlyFiles(CustomMusic.maindir + "Music/" + String.valueOf(player).toLowerCase() + "/");
				String SongString = "";
				if (songList != null) {
					for (int k = 0; k < split.length; k++) {
						try {
							SongString = SongString + songList[Integer.parseInt(split[k]) - 1] + ">>";

						} catch (Exception e) {
							player.sendMessage("You don't have a song with the number " + Integer.parseInt(split[k]));
							return false;
						}
					}
				}
				if (BoxNumber > 0) {
					if (addSongsToBoxList(player, BoxNumber, SongString)) {
						loadboxes();
						unignoreBoxForAll(player.getName(), BoxNumber);
						try {
							Thread.sleep(150);
						} catch (InterruptedException e) {
							log.debug(null, e);
						}
						Mp3PlayerHandler.checkForBox(player);
						return true;
					} else
						return false;
				} else {
					player.sendMessage("Boxnumber must be higher than 0.");
					return false;
				}
			} else {
				player.sendMessage("You have no songs!");
				return false;
			}
		}
	}

	private static boolean addSongsToBoxList(Player player, int boxNumber, String songString) {

		String playerstring = String.valueOf(player);
		String newLine = null;

		for (int k = 0; k < GlobalData.box_count; k++) {
			if (GlobalData.box_aowner[k] != null && GlobalData.box_aowner[k].equals(playerstring) && GlobalData.box_anumber[k] == boxNumber) {// if
																																				// player
																																				// already
																																				// registered
																																				// a
																																				// box
																																				// on
																																				// this
																																				// number

				if (!songString.equals("")) {
					newLine = GlobalData.box_aowner[k] + ":" + GlobalData.box_aposx[k] + ":" + GlobalData.box_aposy[k] + ":" + GlobalData.box_aposz[k] + ":" + GlobalData.box_anumber[k] + ":" + GlobalData.box_arange[k] + ":" + GlobalData.box_aworld[k] + ":" + GlobalData.box_aprior[k] + ":"
							+ songString;
				} else
					newLine = GlobalData.box_aowner[k] + ":" + GlobalData.box_aposx[k] + ":" + GlobalData.box_aposy[k] + ":" + GlobalData.box_aposz[k] + ":" + GlobalData.box_anumber[k] + ":" + GlobalData.box_arange[k] + ":" + GlobalData.box_aworld[k] + ":" + GlobalData.box_aprior[k];

				String SongListZusatz = "";
				if (GlobalData.box_aSongList[k] != null)
					SongListZusatz = ":" + GlobalData.box_aSongList[k];

				FileUtil.removeLineFromFile(CustomMusic.maindir + "BoxList.db", GlobalData.box_aowner[k] + ":" + GlobalData.box_aposx[k] + ":" + GlobalData.box_aposy[k] + ":" + GlobalData.box_aposz[k] + ":" + GlobalData.box_anumber[k] + ":" + GlobalData.box_arange[k] + ":"
						+ GlobalData.box_aworld[k] + ":" + GlobalData.box_aprior[k] + SongListZusatz);
				// ==> delete line

				try {
					PrintWriter pw = new PrintWriter(new FileWriter(CustomMusic.Boxes, true));
					pw.println(newLine);
					pw.close();
				} catch (IOException ex) {
					player.sendMessage("An error has occured while adding the songs. You may have to delete the box.");
					log.debug("writing information to BoxList.db", ex);
				}

				if (Mp3PlayerHandler.isPlaying(player, GlobalData.box_aowner[k], String.valueOf(boxNumber))) {
					Mp3PlayerHandler.stopPlaying(player, GlobalData.box_aowner[k], String.valueOf(boxNumber));
				}
				return true;
			}
		}
		player.sendMessage("You don't have a box with the number " + boxNumber);
		return false;
	}

	public static boolean chooseAreaSongs(Player player, int areaNumber, String SongNumbers) {
		if (SongNumbers.equals("all")) {
			if (areaNumber > 0) {
				if (addSongsToAreaList(player, areaNumber, "")) {
					loadareas();
					unignoreAreaForAll(player.getName(), "a" + String.valueOf(areaNumber));
					try {
						Thread.sleep(150);
					} catch (InterruptedException e) {
						log.debug(null, e);
					}
					Mp3PlayerHandler.checkForBox(player);
					return true;
				} else
					return false;
			} else {
				player.sendMessage("Areanumber must be higher than 0.");
				return false;
			}
		} else {
			String[] split = SongNumbers.split(",");
			for (int i = 0; i < split.length; i++) {
				if (!StringUtil.isInteger(split[i])) {
					player.sendMessage("Use " + ChatColor.RED + "'/cm choose <areanumber> <songnumbers>'" + ChatColor.WHITE + " (seperated by comma)");
					player.sendMessage("Use " + ChatColor.RED + "'/cm songlist [player] <page>'" + ChatColor.WHITE + " to get a list of songs and their numbers");
					return false;
				}
			}
			File file = new File(CustomMusic.maindir + "Music/" + String.valueOf(player).toLowerCase());
			if (file.exists() && file.list().length > 1) {
				String[] songList = FileUtil.listOnlyFiles(CustomMusic.maindir + "Music/" + String.valueOf(player).toLowerCase() + "/");
				String SongString = "";
				if (songList != null) {
					for (int k = 0; k < split.length; k++) {
						try {
							SongString = SongString + songList[Integer.parseInt(split[k]) - 1] + ">>";

						} catch (Exception e) {
							player.sendMessage("You don't have a song with the number " + Integer.parseInt(split[k]));
							return false;
						}
					}
				}
				if (areaNumber > 0) {
					if (addSongsToAreaList(player, areaNumber, SongString)) {
						loadareas();
						unignoreAreaForAll(player.getName(), "a" + String.valueOf(areaNumber));
						try {
							Thread.sleep(150);
						} catch (InterruptedException e) {
							log.debug(null, e);
						}
						Mp3PlayerHandler.checkForBox(player);
						return true;
					} else
						return false;
				} else {
					player.sendMessage("Areanumber must be higher than 0.");
					return false;
				}
			} else {
				player.sendMessage("You have no songs!");
				return false;
			}
		}
	}

	private static boolean addSongsToAreaList(Player player, int intnumber, String songString) {

		String number = "a" + String.valueOf(intnumber);
		String playerstring = String.valueOf(player);
		String newLine = null;

		for (int k = 0; k < GlobalData.area_count; k++) {
			if (GlobalData.area_aowner[k] != null && GlobalData.area_aowner[k].equals(playerstring) && GlobalData.area_anumber[k].equalsIgnoreCase(number)) {

				if (!songString.equals("")) {
					newLine = GlobalData.area_aowner[k] + ":" + GlobalData.area_anumber[k] + ":" + GlobalData.area_aprior[k] + ":" + GlobalData.area_afadeoutRange[k] + ":" + GlobalData.area_aposx0[k] + ":" + GlobalData.area_aposy0[k] + ":" + GlobalData.area_aposz0[k] + ":"
							+ GlobalData.area_aposx1[k] + ":" + GlobalData.area_aposy1[k] + ":" + GlobalData.area_aposz1[k] + ":" + GlobalData.area_aworld[k] + ":" + songString;
				} else
					newLine = GlobalData.area_aowner[k] + ":" + GlobalData.area_anumber[k] + ":" + GlobalData.area_aprior[k] + ":" + GlobalData.area_afadeoutRange[k] + ":" + GlobalData.area_aposx0[k] + ":" + GlobalData.area_aposy0[k] + ":" + GlobalData.area_aposz0[k] + ":"
							+ GlobalData.area_aposx1[k] + ":" + GlobalData.area_aposy1[k] + ":" + GlobalData.area_aposz1[k] + ":" + GlobalData.area_aworld[k];

				String SongListZusatz = "";
				if (GlobalData.area_aSongList[k] != null)
					SongListZusatz = ":" + GlobalData.area_aSongList[k];

				FileUtil.removeLineFromFile(CustomMusic.maindir + "AreaList.db", GlobalData.area_aowner[k] + ":" + GlobalData.area_anumber[k] + ":" + GlobalData.area_aprior[k] + ":" + GlobalData.area_afadeoutRange[k] + ":" + GlobalData.area_aposx0[k] + ":" + GlobalData.area_aposy0[k] + ":"
						+ GlobalData.area_aposz0[k] + ":" + GlobalData.area_aposx1[k] + ":" + GlobalData.area_aposy1[k] + ":" + GlobalData.area_aposz1[k] + ":" + GlobalData.area_aworld[k] + SongListZusatz);
				// ==> delete line

				try {
					PrintWriter pw = new PrintWriter(new FileWriter(CustomMusic.Areas, true));
					pw.println(newLine);
					pw.close();
				} catch (IOException ex) {
					player.sendMessage("An error has occured while adding the songs. You may have to delete the area.");
					log.debug("writing information to AreaList.db", ex);
				}

				if (Mp3PlayerHandler.isPlaying(player, GlobalData.area_aowner[k], number)) {
					Mp3PlayerHandler.stopPlaying(player, GlobalData.area_aowner[k], number);
				}
				return true;
			}
		}
		player.sendMessage("You don't have an area with the number " + intnumber);
		return false;
	}

	public static boolean chooseWorldSongs(Player player, String world, String numbers) {
		if (numbers.equals("all")) {
			if (addSongsToWorldList(player, world, "")) {
				loadworlds();
				unignoreWorldForAll(world);
				try {
					Thread.sleep(150);
				} catch (InterruptedException e) {
					log.debug(null, e);
				}
				Mp3PlayerHandler.checkForBox(player);
				return true;
			} else
				return false;
		} else if (numbers.equals("none")) {
			deleteWorld(player, world, false);
			return true;
		} else {
			String[] split = numbers.split(",");
			for (int i = 0; i < split.length; i++) {
				if (!StringUtil.isInteger(split[i])) {
					player.sendMessage("Use " + ChatColor.RED + "'/cm choose <areanumber> <songnumbers>'" + ChatColor.WHITE + " (seperated by comma)");
					player.sendMessage("Use " + ChatColor.RED + "'/cm songlist [player] <page>'" + ChatColor.WHITE + " to get a list of songs and their numbers");
					return false;
				}
			}
			File file = new File(CustomMusic.maindir + "Music/" + String.valueOf(player).toLowerCase());
			if (file.exists() && file.list().length > 1) {
				String[] songList = FileUtil.listOnlyFiles(CustomMusic.maindir + "Music/" + String.valueOf(player).toLowerCase() + "/");
				String SongString = "";
				if (songList != null) {
					for (int k = 0; k < split.length; k++) {
						try {
							SongString = SongString + songList[Integer.parseInt(split[k]) - 1] + ">>";

						} catch (Exception e) {
							player.sendMessage("You don't have a song with the number " + Integer.parseInt(split[k]));
							return false;
						}
					}
				}
				if (addSongsToWorldList(player, world, SongString)) {
					loadworlds();
					unignoreWorldForAll(world);
					try {
						Thread.sleep(150);
					} catch (InterruptedException e) {
						log.debug(null, e);
					}
					Mp3PlayerHandler.checkForBox(player);
					return true;
				} else
					return false;
			} else {
				player.sendMessage("You have no songs!");
				return false;
			}
		}
	}

	public static boolean chooseWorldStation(Player player, String world, int stationNumber) {
		File file = new File(CustomMusic.maindir + "Music/" + String.valueOf(player).toLowerCase() + "/webradio/");
		if (file.exists() && file.list().length > 0) {
			String[] stationList = FileUtil.listOnlyFiles(CustomMusic.maindir + "Music/" + String.valueOf(player).toLowerCase() + "/webradio/");
			String StationString = "";
			if (stationList != null) {
				try {
					StationString = "webradio/" + stationList[stationNumber - 1] + ">>";

				} catch (Exception e) {
					player.sendMessage("You don't have a radio station with the number " + stationNumber);
					return false;
				}

			}
			if (addSongsToWorldList(player, world, StationString)) {
				loadworlds();
				unignoreWorldForAll(world);
				try {
					Thread.sleep(150);
				} catch (InterruptedException e) {
					log.debug(null, e);
				}
				Mp3PlayerHandler.checkForBox(player);
				return true;
			} else
				return false;
		} else {
			player.sendMessage("You have no radio stations!");
			return false;
		}
	}

	private static boolean addSongsToWorldList(Player player, String world, String songString) {

		String newLine = null;

		for (int k = 0; k < GlobalData.world_count; k++) {
			if (GlobalData.world_aworld[k] != null && GlobalData.world_aworld[k].equalsIgnoreCase(world)) {

				if (!songString.equals("")) {
					newLine = GlobalData.world_aowner[k] + ":" + GlobalData.world_aworld[k] + ":" + GlobalData.world_avolume[k] + ":" + GlobalData.world_aprior[k] + ":" + songString;
				} else
					newLine = GlobalData.world_aowner[k] + ":" + GlobalData.world_aworld[k] + ":" + GlobalData.world_avolume[k] + ":" + GlobalData.world_aprior[k];

				String SongListZusatz = "";
				if (GlobalData.world_asongList[k] != null)
					SongListZusatz = ":" + GlobalData.world_asongList[k];

				FileUtil.removeLineFromFile(CustomMusic.maindir + "WorldList.db", GlobalData.world_aowner[k] + ":" + GlobalData.world_aworld[k] + ":" + GlobalData.world_avolume[k] + ":" + GlobalData.world_aprior[k] + SongListZusatz);
				// ==> delete line

				try {
					PrintWriter pw = new PrintWriter(new FileWriter(CustomMusic.Worlds, true));
					pw.println(newLine);
					pw.close();
				} catch (IOException ex) {
					player.sendMessage("An error has occured while adding the songs. You may have to delete the world in WorldList.db.");
					log.debug("writing information to WorldList.db", ex);
				}

				if (Mp3PlayerHandler.isPlaying(player, GlobalData.world_aowner[k], "w" + String.valueOf(k))) {
					Mp3PlayerHandler.stopPlaying(player, GlobalData.world_aowner[k], "w" + String.valueOf(k));
				}
				return true;
			}
		}
		player.sendMessage("World " + world + " is not set up as a music-playing world. Use " + ChatColor.RED + "'/cm setworld'" + ChatColor.WHITE + "first.");
		return false;
	}

	public static boolean chooseBiomeSongs(Player player, String biome, String numbers) {
		if (numbers.equals("all")) {
			if (addSongsToBiomeList(player, biome, "")) {
				loadbiomes();
				unignoreBiomeForAll(biome);
				try {
					Thread.sleep(150);
				} catch (InterruptedException e) {
					log.debug(null, e);
				}
				Mp3PlayerHandler.checkForBox(player);
				return true;
			} else
				return false;
		} else if (numbers.equals("none")) {
			deleteBiome(player, biome, false);
			return true;
		} else {
			String[] split = numbers.split(",");
			for (int i = 0; i < split.length; i++) {
				if (!StringUtil.isInteger(split[i])) {
					player.sendMessage("Use " + ChatColor.RED + "'/cm choose <areanumber> <songnumbers>'" + ChatColor.WHITE + " (seperated by comma)");
					player.sendMessage("Use " + ChatColor.RED + "'/cm songlist [player] <page>'" + ChatColor.WHITE + " to get a list of songs and their numbers");
					return false;
				}
			}
			File file = new File(CustomMusic.maindir + "Music/" + String.valueOf(player).toLowerCase());
			if (file.exists() && file.list().length > 1) {
				String[] songList = FileUtil.listOnlyFiles(CustomMusic.maindir + "Music/" + String.valueOf(player).toLowerCase() + "/");
				String SongString = "";
				if (songList != null) {
					for (int k = 0; k < split.length; k++) {
						try {
							SongString = SongString + songList[Integer.parseInt(split[k]) - 1] + ">>";

						} catch (Exception e) {
							player.sendMessage("You don't have a song with the number " + Integer.parseInt(split[k]));
							return false;
						}
					}
				}
				if (addSongsToBiomeList(player, biome, SongString)) {
					loadbiomes();
					unignoreBiomeForAll(biome);
					try {
						Thread.sleep(150);
					} catch (InterruptedException e) {
						log.debug(null, e);
					}
					Mp3PlayerHandler.checkForBox(player);
					return true;
				} else
					return false;
			} else {
				player.sendMessage("You have no songs!");
				return false;
			}
		}
	}

	public static boolean chooseBiomeStation(Player player, String biome, int stationNumber) {
		File file = new File(CustomMusic.maindir + "Music/" + String.valueOf(player).toLowerCase() + "/webradio/");
		if (file.exists() && file.list().length > 0) {
			String[] stationList = FileUtil.listOnlyFiles(CustomMusic.maindir + "Music/" + String.valueOf(player).toLowerCase() + "/webradio/");
			String StationString = "";
			if (stationList != null) {
				try {
					StationString = "webradio/" + stationList[stationNumber - 1] + ">>";

				} catch (Exception e) {
					player.sendMessage("You don't have a radio station with the number " + stationNumber);
					return false;
				}

			}
			if (addSongsToBiomeList(player, biome, StationString)) {
				loadbiomes();
				unignoreBiomeForAll(biome);
				try {
					Thread.sleep(150);
				} catch (InterruptedException e) {
					log.debug(null, e);
				}
				Mp3PlayerHandler.checkForBox(player);
				return true;
			} else
				return false;
		} else {
			player.sendMessage("You have no radio stations!");
			return false;
		}
	}

	private static boolean addSongsToBiomeList(Player player, String biome, String songString) {

		String newLine = null;

		for (int k = 0; k < GlobalData.biome_count; k++) {
			if (GlobalData.biome_abiome[k] != null && GlobalData.biome_abiome[k].equalsIgnoreCase(biome)) {

				if (!songString.equals("")) {
					newLine = GlobalData.biome_aowner[k] + ":" + GlobalData.biome_abiome[k] + ":" + GlobalData.biome_avolume[k] + ":" + GlobalData.biome_aprior[k] + ":" + songString;
				} else
					newLine = GlobalData.biome_aowner[k] + ":" + GlobalData.biome_abiome[k] + ":" + GlobalData.biome_avolume[k] + ":" + GlobalData.biome_aprior[k];

				String SongListZusatz = "";
				if (GlobalData.biome_asongList[k] != null)
					SongListZusatz = ":" + GlobalData.biome_asongList[k];

				FileUtil.removeLineFromFile(CustomMusic.maindir + "BiomeList.db", GlobalData.biome_aowner[k] + ":" + GlobalData.biome_abiome[k] + ":" + GlobalData.biome_avolume[k] + ":" + GlobalData.biome_aprior[k] + SongListZusatz);
				// ==> delete line

				try {
					PrintWriter pw = new PrintWriter(new FileWriter(CustomMusic.Biomes, true));
					pw.println(newLine);
					pw.close();
				} catch (IOException ex) {
					player.sendMessage("An error has occured while adding the songs. You may have to delete the biome in BiomeList.db.");
					log.debug("writing information to BiomeList.db", ex);
				}

				if (Mp3PlayerHandler.isPlaying(player, GlobalData.biome_aowner[k], "bio" + String.valueOf(k))) {
					Mp3PlayerHandler.stopPlaying(player, GlobalData.biome_aowner[k], "bio" + String.valueOf(k));
				}
				return true;
			}
		}
		player.sendMessage("Biome " + biome + " is not set up as a music-playing biome. Use " + ChatColor.RED + "'/cm setbiome'" + ChatColor.WHITE + "first.");
		return false;
	}

	// ############################ LOAD FUNCTIONS
	// ##################################

	public static boolean loadareas() {
		boolean missingDefaults = false;
		if (CustomMusic.Areas.exists()) {
			try {
				GlobalData.clearAreaArrays();
				FileReader fr = new FileReader(CustomMusic.Areas);
				LineNumberReader ln = new LineNumberReader(fr);
				int i = 0;
				while (true) {
					String string = ln.readLine();
					if (string != null) {
						String split[] = string.split(":"); // read area
															// coordinates and
															// save to
															// GlobalData.*
						if (split.length > 10 && !split[10].contains(">>")) {
							GlobalData.area_lowner.add(split[0]);
							GlobalData.area_lnumber.add(split[1]);
							GlobalData.area_lprior.add(Integer.parseInt(split[2]));
							GlobalData.area_lfadeoutRange.add(Integer.parseInt(split[3]));
							GlobalData.area_lposx0.add(Float.parseFloat(split[4]));
							GlobalData.area_lposy0.add(Float.parseFloat(split[5]));
							GlobalData.area_lposz0.add(Float.parseFloat(split[6]));
							GlobalData.area_lposx1.add(Float.parseFloat(split[7]));
							GlobalData.area_lposy1.add(Float.parseFloat(split[8]));
							GlobalData.area_lposz1.add(Float.parseFloat(split[9]));
							GlobalData.area_lworld.add(split[10]);

							if (split.length > 11)
								GlobalData.area_lSongList.add(split[11]);
							else
								GlobalData.area_lSongList.add(null);
							i++;
						} else {
							missingDefaults = true;
							break;
						}
					} else {// end of file
						GlobalData.area_count = i;
						GlobalData.createAreaArrays();
						break;
					}
				}
				ln.close();
			} catch (IOException e) {
				log.debug("loading information from ignoredAreas.db", e);
			}
		} else {
			System.out.println("[CustomMusic] No arealist found");
		}
		return missingDefaults;
	}

	public static boolean loadboxes() {
		boolean missingDefaults = false;
		if (CustomMusic.Boxes.exists()) {
			try {
				GlobalData.clearBoxArrays();
				FileReader fr = new FileReader(CustomMusic.Boxes);
				LineNumberReader ln = new LineNumberReader(fr);
				int i = 0;
				while (true) {
					String string = ln.readLine();
					if (string != null) {
						String split[] = string.split(":"); // read box
															// coordinates and
															// save to
															// GlobalData.*
						if (split.length > 4) {
							GlobalData.box_lowner.add(split[0]);
							GlobalData.box_lposx.add(Float.parseFloat(split[1]));
							GlobalData.box_lposy.add(Float.parseFloat(split[2]));
							GlobalData.box_lposz.add(Float.parseFloat(split[3]));
							GlobalData.box_lnumber.add(Integer.parseInt(split[4]));
							if (split.length > 5 && StringUtil.isInteger(split[5])) {
								GlobalData.box_lrange.add(Integer.parseInt(split[5]));
							} else {
								GlobalData.box_lrange.add(null);
								missingDefaults = true;
								break;
							}
							if (split.length > 6 && split[6].contains("CraftWorld{")) {
								GlobalData.box_lworld.add(split[6]);
							} else {
								GlobalData.box_lworld.add(null);
								missingDefaults = true;
								break;
							}
							if (split.length > 7 && !split[7].contains(">>")) {
								GlobalData.box_lprior.add(Integer.parseInt(split[7]));
							} else {
								GlobalData.box_lprior.add(null);
								missingDefaults = true;
								break;
							}
							if (split.length > 8)
								GlobalData.box_lSongList.add(split[8]);
							else
								GlobalData.box_lSongList.add(null);
							i++;
						}
					} else {// end of file
						GlobalData.box_count = i;
						GlobalData.createBoxArrays();
						break;
					}
				}
				ln.close();
			} catch (IOException e) {
				log.debug("loading information from BoxList.db", e);
			}
		} else {
			System.out.println("[CustomMusic] No boxlist found");
		}
		return missingDefaults;
	}

	public static void loadworlds() {
		if (CustomMusic.Worlds.exists()) {
			try {
				GlobalData.clearWorldArrays();
				FileReader fr = new FileReader(CustomMusic.Worlds);
				LineNumberReader ln = new LineNumberReader(fr);
				int i = 0;
				while (true) {
					String string = ln.readLine();
					if (string != null) {
						String split[] = string.split(":"); // read box
															// coordinates and
															// save to
															// GlobalData.*
						if (split.length > 3) {
							GlobalData.world_lowner.add(split[0]);
							GlobalData.world_lworld.add(split[1]);
							GlobalData.world_lvolume.add(Integer.parseInt(split[2]));
							GlobalData.world_lprior.add(Integer.parseInt(split[3]));
							if (split.length > 4)
								GlobalData.world_lsongList.add(split[4]);
							else
								GlobalData.world_lsongList.add(null);
							i++;
						}
					} else {// end of file
						GlobalData.world_count = i;
						GlobalData.createWorldArrays();
						break;
					}
				}
				ln.close();
			} catch (IOException e) {
				log.debug("loading information from WorldList.db", e);
			}
		} else {
			System.out.println("[CustomMusic] No worldlist found");
		}
	}

	public static void loadbiomes() {
		if (CustomMusic.Biomes.exists()) {
			try {
				GlobalData.clearBiomeArrays();
				FileReader fr = new FileReader(CustomMusic.Biomes);
				LineNumberReader ln = new LineNumberReader(fr);
				int i = 0;
				while (true) {
					String string = ln.readLine();
					if (string != null) {
						String split[] = string.split(":"); // read box
															// coordinates and
															// save to
															// GlobalData.*
						if (split.length > 3) {
							GlobalData.biome_lowner.add(split[0]);
							GlobalData.biome_lbiome.add(split[1]);
							GlobalData.biome_lvolume.add(Integer.parseInt(split[2]));
							GlobalData.biome_lprior.add(Integer.parseInt(split[3]));
							if (split.length > 4)
								GlobalData.biome_lsongList.add(split[4]);
							else
								GlobalData.biome_lsongList.add(null);
							i++;
						}
					} else {// end of file
						GlobalData.biome_count = i;
						GlobalData.createBiomeArrays();
						break;
					}
				}
				ln.close();
			} catch (IOException e) {
				log.debug("loading information from BiomeList.db", e);
			}
		} else {
			System.out.println("[CustomMusic] No biomelist found");
		}
	}

	public static void loadAreaIgnores() {
		if (CustomMusic.ignoredAreas.exists()) {
			try {
				GlobalData.clearAreaIgno(); // clear old ignolist
				FileReader fr = new FileReader(CustomMusic.ignoredAreas);
				LineNumberReader ln = new LineNumberReader(fr);

				while (true) {
					String string = ln.readLine();
					if (string != null) {
						String split[] = string.split(":"); // read
															// ignoredAreas.db

						if (split.length > 2) {
							GlobalData.area_lignoByPlayer.add(split[0]); // add
																			// to
																			// ignoredList
							GlobalData.area_lignoOwner.add(split[1]);
							GlobalData.area_lignoAreaNumber.add(split[2]);
						}
					} else
						// end of file
						break;
				}
				ln.close();
			} catch (IOException e) {
				log.debug("loading information from ignoredAreas.db", e);
			}
		} else {
			System.out.println("[CustomMusic] No area-ignorelist found");
		}
	}

	public static void loadBoxIgnores() {
		if (CustomMusic.ignoredBoxes.exists()) {
			try {
				GlobalData.clearBoxIgno(); // clear old ignolist
				FileReader fr = new FileReader(CustomMusic.ignoredBoxes);
				LineNumberReader ln = new LineNumberReader(fr);

				while (true) {
					String string = ln.readLine();
					if (string != null) {
						String split[] = string.split(":"); // read
															// ignoredAreas.db

						if (split.length > 2) {
							GlobalData.box_lignoByPlayer.add(split[0]); // add
																		// to
							// ignoredList
							GlobalData.box_lignoOwner.add(split[1]);
							GlobalData.box_lignoBoxNumber.add(Integer.parseInt(split[2]));
						}
					} else
						// end of file
						break;
				}
				ln.close();
			} catch (IOException e) {
				log.debug("loading information from ignoredBoxes.db", e);
			}
		} else {
			System.out.println("[CustomMusic] No box-ignorelist found");
		}
	}
	
	public static void loadWorldIgnores() {
		if (CustomMusic.ignoredWorlds.exists()) {
			try {
				GlobalData.clearWorldIgno(); // clear old ignolist
				FileReader fr = new FileReader(CustomMusic.ignoredWorlds);
				LineNumberReader ln = new LineNumberReader(fr);

				while (true) {
					String string = ln.readLine();
					if (string != null) {
						String split[] = string.split(":"); // read
															// ignoredWorlds.db

						if (split.length > 1) {
							GlobalData.world_lignoByPlayer.add(split[0]); // add
																			// to
																			// ignoredList
							GlobalData.world_lignoWorld.add(split[1]);
						}
					} else
						// end of file
						break;
				}
				ln.close();
			} catch (IOException e) {
				log.debug("loading information from ignoredWorlds.db", e);
			}
		} else {
			System.out.println("[CustomMusic] No world-ignorelist found");
		}
	}
	
	public static void loadBiomeIgnores() {
		if (CustomMusic.ignoredBiomes.exists()) {
			try {
				GlobalData.clearBiomeIgno(); // clear old ignolist
				FileReader fr = new FileReader(CustomMusic.ignoredBiomes);
				LineNumberReader ln = new LineNumberReader(fr);

				while (true) {
					String string = ln.readLine();
					if (string != null) {
						String split[] = string.split(":"); // read
															// ignoredBiomes.db

						if (split.length > 1) {
							GlobalData.biome_lignoByPlayer.add(split[0]); // add
																			// to
																			// ignoredList
							GlobalData.biome_lignoBiome.add(split[1]);
						}
					} else
						// end of file
						break;
				}
				ln.close();
			} catch (IOException e) {
				log.debug("loading information from ignoredBiomes.db", e);
			}
		} else {
			System.out.println("[CustomMusic] No biome-ignorelist found");
		}
	}

	public static void addAreaDefaults() {
		if (CustomMusic.Areas.exists()) {
			try {
				FileReader fr = new FileReader(CustomMusic.Areas);
				LineNumberReader ln = new LineNumberReader(fr);
				List<String> lines = new ArrayList<String>();
				while (true) {
					String string = ln.readLine();
					if (string != null) {
						lines.add(string);
					} else {// end of file
						break;
					}
				}
				ln.close();

				File tempFile = new File(CustomMusic.Areas.getAbsolutePath() + ".tmp");
				PrintWriter pw = new PrintWriter(new FileWriter(tempFile));
				for (int i = 0; i < lines.size(); i++) {
					String writeString = "";
					String addString = "";
					String restString = "";
					String valueString = lines.get(i);
					String[] values = valueString.split(":");
					if ((values.length < 12 && values[values.length - 1].contains(">>")) || values.length < 11) {
						if (values.length > 10)
							restString = ":" + values[10];
						addString = ":" + LoadSettings.defaultAreaFadeout + ":" + values[3] + ":" + values[4] + ":" + values[5] + ":" + values[6] + ":" + values[7] + ":" + values[8] + ":" + values[9] + restString;
						writeString = values[0] + ":" + values[1] + ":" + values[2] + addString;
					} else
						writeString = valueString;
					pw.println(writeString);
				}

				pw.close();
				CustomMusic.Areas.delete();
				tempFile.renameTo(CustomMusic.Areas);
				lines = null;

			} catch (IOException e) {
				log.debug("writing default-value information to AreaList.db", e);
			}
		}
	}

	public static void addBoxDefaults() {
		if (CustomMusic.Boxes.exists()) {
			try {
				FileReader fr = new FileReader(CustomMusic.Boxes);
				LineNumberReader ln = new LineNumberReader(fr);
				List<String> lines = new ArrayList<String>();
				while (true) {
					String string = ln.readLine();
					if (string != null) {
						lines.add(string);
					} else {// end of file
						break;
					}
				}
				ln.close();

				File tempFile = new File(CustomMusic.Boxes.getAbsolutePath() + ".tmp");
				PrintWriter pw = new PrintWriter(new FileWriter(tempFile));
				for (int i = 0; i < lines.size(); i++) {
					String writeString = "";
					String addString = "";
					String restString = "";
					String valueString = lines.get(i);
					String[] values = valueString.split(":");
					if ((values.length < 9 && values.length > 4 && values[values.length - 1].contains(">>")) || (values.length < 8 && values.length > 4)) {
						if (values.length >= 6 && values[5].contains("CraftWorld{name=")) {
							if (values.length > 6)
								restString = ":" + values[6];
							addString = ":" + LoadSettings.defaultRange + ":" + values[5] + ":10" + restString;
						} else {
							addString = ":" + LoadSettings.defaultRange + ":CraftWorld{name=" + LoadSettings.defaultWorld + "}:10";
						}
						if (values.length > 6 && values[6].contains("CraftWorld{name=")) {
							if (values.length > 7)
								restString = ":" + values[7];
							addString = ":" + values[5] + ":" + values[6] + ":10" + restString;
						}
						writeString = values[0] + ":" + values[1] + ":" + values[2] + ":" + values[3] + ":" + values[4] + addString;
					} else
						writeString = valueString;
					pw.println(writeString);
				}

				pw.close();
				CustomMusic.Boxes.delete();
				tempFile.renameTo(CustomMusic.Boxes);
				lines = null;

			} catch (IOException e) {
				log.debug("writing default-value information to BoxList.db", e);
			}
		}

	}

}
