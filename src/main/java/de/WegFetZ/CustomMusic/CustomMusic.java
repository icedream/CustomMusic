package main.java.de.WegFetZ.CustomMusic;

import java.io.File;
import java.io.IOException;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import main.java.de.WegFetZ.CustomMusic.Utils.StringUtil;

/**
 * CustomMusic for Bukkit
 * 
 * @author WegFetZ
 */

public class CustomMusic extends JavaPlugin {
	
	public static final String requiredClientVersion = "0.9";

	public static String maindir = "plugins/CustomMusic/";
	public static File Boxes = new File(maindir + "BoxList.db");
	public static File Areas = new File(maindir + "AreaList.db");
	public static File Worlds = new File(maindir + "WorldList.db");
	public static File Biomes = new File(maindir + "BiomeList.db");
	public static File ignoredBoxes = new File(maindir + "ignoredBoxes.db");
	public static File ignoredAreas = new File(maindir + "ignoredAreas.db");
	public static File ignoredWorlds = new File(maindir + "ignoredWorlds.db");
	public static File ignoredBiomes = new File(maindir + "ignoredBiomes.db");

	private final CMPlayerListener playerListener = new CMPlayerListener(); //register the player listener
	
	public static boolean listening = true;

	
	public void onEnable() {
		
		if (LoadSettings.debug) System.out.println("[CustomMusic] Running in debug-mode.");
		
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(this.playerListener, this);
		//==> register the player events

		new File(maindir).mkdir();
		new File(maindir + "Music/").mkdir();
		new File(maindir + "Music/uploading/").mkdir();
				
		try {
			if (!Boxes.exists())
				Boxes.createNewFile();
			if (!Areas.exists())
				Areas.createNewFile();
			if (!Worlds.exists())
				Worlds.createNewFile();
			if (!Biomes.exists())
				Biomes.createNewFile();
			if (!ignoredBoxes.exists())
				ignoredBoxes.createNewFile();
			if (!ignoredAreas.exists())
				ignoredAreas.createNewFile();
			if (!ignoredWorlds.exists())
				ignoredWorlds.createNewFile();
			if (!ignoredBiomes.exists())
				ignoredBiomes.createNewFile();
		} catch (IOException e) {
			log.debug("creating databases", e);
		}
			
		//create the config directory and files
		
		LoadSettings.loadMain(); //load the config

		Permission.initialize(getServer());

		System.out.println("[CustomMusic] " + "Starting audio-server on port " + LoadSettings.ServerPort + "-" + (LoadSettings.ServerPort + 3) + "...");

		new Server().start();
		new ServerSongDelete().start();
		new ServerSongSender().start();
		new ServerUploadListener().start();
		// start the server threads

		Bukkit.getServer().broadcastMessage("[CustomMusic] " + ChatColor.RED + "Audio-server started.");
		if (LoadSettings.debug) System.out.println("[CustomMusic] Running in debug-mode.");
		
		PluginDescriptionFile pdfFile = this.getDescription();
		System.out.println(pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!");
		log.info(pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!");
		
	}

	public void onDisable() {

		Bukkit.getServer().broadcastMessage("[CustomMusic] " + ChatColor.RED + "Stopping audio-server...");
		
		GlobalData.CMUsers.clear();
		GlobalData.CMConnected.clear();
		GlobalData.CMPlaying.clear();
		GlobalData.CMUploadPerm.clear();
		GlobalData.CMDeleteSongs.clear();
		GlobalData.CMAreaDefinitions.clear();
		GlobalData.CMVolume.clear();
		//==> clear the hashmaps	
		GlobalData.clearBoxArrays();
		GlobalData.clearAreaArrays();
		GlobalData.clearWorldArrays();
		GlobalData.clearBiomeArrays();
		GlobalData.clearBoxIgno();
		GlobalData.clearAreaIgno();
		GlobalData.clearWorldIgno();
		GlobalData.clearBiomeIgno();
		
		listening = false;
		try {
			Server.serverSocket.close();
			ServerSongDelete.serversongdeleteSocket.close();
			ServerUploadListener.serverupSocket.close();
			ServerSongSender.serversongSocket.close();
		} catch (IOException e) {
			log.debug(null,e);
		}
		//==> close the server sockets
		
		
		PluginDescriptionFile pdfFile = this.getDescription();
		System.out.println(pdfFile.getName() + " version " + pdfFile.getVersion() + " is disabled!");
		log.info(pdfFile.getName() + " version " + pdfFile.getVersion() + " is disabled!");
	}


	public void toggleMusic(Player player) { //toggles the music for a player
		
		if (GlobalData.CMUsers.containsKey(player.getName().toLowerCase())) {
			GlobalData.CMPlaying.remove(player.getName().toLowerCase());
			GlobalData.CMUsers.remove(player.getName().toLowerCase());
			player.sendMessage(ChatColor.RED + "Music disabled!");
			// remove the player from these maps
		
		} else {
			if (GlobalData.CMConnected.containsKey(player.getName().toLowerCase()) && (GlobalData.CMConnected.get(player.getName().toLowerCase()) == 1)) {
												//if player is connected and is initialized
				GlobalData.CMUsers.put(String.valueOf(player).toLowerCase(), player.getName());
				player.sendMessage(ChatColor.RED + "Music enabled!");
				//add the players to the CMUsers map
											
				Mp3PlayerHandler.checkForBox(player); // check if player is in
														// range of box or area and
														// start/stop playing
			} else {
				player.sendMessage("AudioClient not initialized! type " + ChatColor.RED + "'/cm init'");
			}
		}
	}

	public static String cColour(String message) { //process color codes in config file
		
		CharSequence cChk = null;
		String temp = null;
		String[] Colours = { "#!k", "#!b", "#!g", "#!c", "#!r", "#!m", "#!y", "#!w", "#!K", "#!B", "#!G", "#!C", "#!R", "#!M", "#!Y", "#!W" };

		ChatColor[] cCode = { ChatColor.BLACK, ChatColor.DARK_BLUE, ChatColor.DARK_GREEN, ChatColor.DARK_AQUA, ChatColor.DARK_RED, ChatColor.DARK_PURPLE, ChatColor.GOLD, ChatColor.GRAY,
				ChatColor.DARK_GRAY, ChatColor.BLUE, ChatColor.GREEN, ChatColor.AQUA, ChatColor.RED, ChatColor.LIGHT_PURPLE, ChatColor.YELLOW, ChatColor.WHITE };

		for (int x = 0; x < Colours.length; x++) {
			cChk = Colours[x];

			if (message.contains(cChk)) {
				temp = message.replace(cChk, cCode[x].toString());
				message = temp;
			}
		}

		return message;

	}// author: weasel5i2

	
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		
		String[] split = args;
		String commandName = command.getName().toLowerCase();
		
		if (sender instanceof Player) {
			Player player = (Player) sender;
			
			if (commandName.equals("cm") || commandName.equals("custommusic")) {

				// "/cm"
				if (split.length == 0) {
					player.sendMessage("Available commands ( [ ] means optional): ");
					player.sendMessage(ChatColor.RED + "'/cm init'");
					player.sendMessage(ChatColor.RED + "'/cm toggle'");
					player.sendMessage(ChatColor.RED + "'/cm setbox <number> [range] [priority]'");
					player.sendMessage(ChatColor.RED + "'/cm deletebox [player] <number>'");
					player.sendMessage(ChatColor.RED + "'/cm definearea <1|2>'");
					player.sendMessage(ChatColor.RED + "'/cm setarea <number> [fadeout-range] [priority]'");
					player.sendMessage(ChatColor.RED + "'/cm deletearea [player] <number>'");	
					player.sendMessage(ChatColor.RED + "'/cm setworld [world] [<volume> <priority>]'");
					player.sendMessage(ChatColor.RED + "'/cm setbiome [biome] [<volume> <priority>]'");
					player.sendMessage(ChatColor.RED + "'/cm achoose <areanumber> <songnumbers>'");
					player.sendMessage(ChatColor.RED + "'/cm bchoose <boxnumber> <songnumbers>'");
					player.sendMessage(ChatColor.RED + "'/cm achooseradio <areanumber> <radionumber>'");
					player.sendMessage(ChatColor.RED + "'/cm bchooseradio <boxnumber> <radionumber>'");
					player.sendMessage(ChatColor.RED + "'/cm wchoose [world] <songnumbers|all|none>'");
					player.sendMessage(ChatColor.RED + "'/cm biochoose [biome] <songnumbers|all|none>'");
					player.sendMessage(ChatColor.RED + "'/cm wchooseradio [world] <radionumber>'");
					player.sendMessage(ChatColor.RED + "'/cm biochooseradio [biome] <radionumber>'");
					player.sendMessage(ChatColor.RED + "'/cm volume <value>'");
					player.sendMessage("Use " + ChatColor.RED + "'/cm help 2'" + ChatColor.WHITE + " to see more commands.");
					return true;

					
					//cm help [page]
				}else if (split.length >= 1 && split[0].equalsIgnoreCase("help")) {
					if (split.length >= 1) {
						try {
							if (split.length > 1 && Integer.parseInt(split[1]) == 2) {
								player.sendMessage("Available commands ( [ ] means optional): ");
								player.sendMessage(ChatColor.RED + "'/cm deletesong [player] <number>'");
								player.sendMessage(ChatColor.RED + "'/cm deleteradio [player] <number>'");
								player.sendMessage(ChatColor.RED + "'/cm play [player] <songnumbers>'");
								player.sendMessage(ChatColor.RED + "'/cm gplay [player] <songnumbers>'");
								player.sendMessage(ChatColor.RED + "'/cm playradio [player] <radionumber>'");
								player.sendMessage(ChatColor.RED + "'/cm gplayradio [player] <radionumber>'");
								player.sendMessage(ChatColor.RED + "'/cm stop'");
								player.sendMessage(ChatColor.RED + "'/cm gstop'");
								player.sendMessage(ChatColor.RED + "'/cm ignorebox <player> <number>'");
								player.sendMessage(ChatColor.RED + "'/cm ignorearea <player> <number>'");
								player.sendMessage(ChatColor.RED + "'/cm unignorebox <player> <number>'");
								player.sendMessage(ChatColor.RED + "'/cm unignorearea <player> <number>'");
								player.sendMessage(ChatColor.RED + "'/cm songlist [player] <page>'");
								player.sendMessage(ChatColor.RED + "'/cm radiolist [player] <page>'");
								player.sendMessage(ChatColor.RED + "'/cm boxlist [player] <page>'");
								player.sendMessage(ChatColor.RED + "'/cm arealist [player] <page>'");
								player.sendMessage(ChatColor.RED + "'/cm worldlist <page>'");
								player.sendMessage(ChatColor.RED + "'/cm biomelist <page>'");
								player.sendMessage("Use " + ChatColor.RED + "'/cm help 3'" + ChatColor.WHITE + " to see more commands.");	
								
							} else if(split.length > 1 && Integer.parseInt(split[1]) == 3) {
								player.sendMessage(ChatColor.RED + "'/cm ignorelist <page>'");
								player.sendMessage(ChatColor.RED + "'/cm status'");	
								player.sendMessage(ChatColor.RED + "'/cm users'");
								player.sendMessage(ChatColor.RED + "'/cm biomes'");
								player.sendMessage(ChatColor.RED + "'/cm reload'");
								
							} else {
								player.sendMessage("Available commands ( [ ] means optional): ");
								player.sendMessage(ChatColor.RED + "'/cm init'");
								player.sendMessage(ChatColor.RED + "'/cm toggle'");
								player.sendMessage(ChatColor.RED + "'/cm setbox <number> [range] [priority]'");
								player.sendMessage(ChatColor.RED + "'/cm deletebox [player] <number>'");
								player.sendMessage(ChatColor.RED + "'/cm definearea <1|2>'");
								player.sendMessage(ChatColor.RED + "'/cm setarea <number> [fadeout-range] [priority]'");
								player.sendMessage(ChatColor.RED + "'/cm deletearea [player] <number>'");	
								player.sendMessage(ChatColor.RED + "'/cm setworld [world] [<volume> <priority>]'");
								player.sendMessage(ChatColor.RED + "'/cm setbiome [biome] [<volume> <priority>]'");
								player.sendMessage(ChatColor.RED + "'/cm achoose <areanumber> <songnumbers>'");
								player.sendMessage(ChatColor.RED + "'/cm bchoose <boxnumber> <songnumbers>'");
								player.sendMessage(ChatColor.RED + "'/cm achooseradio <areanumber> <radionumber>'");
								player.sendMessage(ChatColor.RED + "'/cm bchooseradio <boxnumber> <radionumber>'");
								player.sendMessage(ChatColor.RED + "'/cm wchoose [world] <songnumbers|all|none>'");
								player.sendMessage(ChatColor.RED + "'/cm biochoose [biome] <songnumbers|all|none>'");
								player.sendMessage(ChatColor.RED + "'/cm wchooseradio [world] <radionumber>'");
								player.sendMessage(ChatColor.RED + "'/cm biochooseradio [biome] <radionumber>'");
								player.sendMessage(ChatColor.RED + "'/cm volume <value>'");
								player.sendMessage("Use " + ChatColor.RED + "'/cm help 2'" + ChatColor.WHITE + " to see more commands.");								
							}
						} catch (NumberFormatException e) {
							player.sendMessage("Use " + ChatColor.RED + "/cm help [page]");
						}
					}
					return true;
					
					
					
					// "/cm reload"
				} else if (split.length >= 1 && split[0].equalsIgnoreCase("reload")) {
					
					if (!Permission.permission(player,"cm.reload", false)) {
						player.sendMessage("You don't have permission to reload CustomMusic!");
					
					} else {
						Bukkit.getServer().broadcastMessage("[CustomMusic] " + ChatColor.RED + "Reloading audio-server...");
						
						GlobalData.CMUsers.clear();
						GlobalData.CMConnected.clear();
						GlobalData.CMPlaying.clear();
						GlobalData.CMUploadPerm.clear();
						GlobalData.CMDeleteSongs.clear();
						GlobalData.CMAreaDefinitions.clear();
						GlobalData.CMVolume.clear();
						//==> clear the hashmaps	
						GlobalData.clearBoxArrays();
						GlobalData.clearAreaArrays();
						GlobalData.clearWorldArrays();
						GlobalData.clearBiomeArrays();
						GlobalData.clearBoxIgno();
						GlobalData.clearAreaIgno();
						GlobalData.clearWorldIgno();
						GlobalData.clearBiomeIgno();
						
						listening = false;
						try {
							Server.serverSocket.close();
							ServerSongDelete.serversongdeleteSocket.close();
							ServerUploadListener.serverupSocket.close();
							ServerSongSender.serversongSocket.close();
						} catch (IOException e) {
							log.debug(null,e);
						}
						
						
						//load again
						LoadSettings.loadMain(); //load the config

						Permission.initialize(getServer());

						System.out.println("[CustomMusic] " + "Starting audio-server on port " + LoadSettings.ServerPort + "-" + (LoadSettings.ServerPort + 3) + "...");

						listening = true;
						
						new Server().start();
						new ServerSongDelete().start();
						new ServerSongSender().start();
						new ServerUploadListener().start();
						// start the server threads

						Bukkit.getServer().broadcastMessage("[CustomMusic] " + ChatColor.RED + "Reload done.");
					
					}
					return true;
					
					
					
					// "/cm definearea"
				} else if (split.length >= 1 && split[0].equalsIgnoreCase("definearea")) {
					
					if (!Permission.permission(player,"cm.area.define", true)) {
						player.sendMessage("You don't have permission to define areas!");
					
					} else if (split.length > 1) {
						try {
							int corner = Integer.parseInt(split[1]); //number of the corner of the cuboid
							if (corner > 0 && corner < 3)
								BoxList.areaDefine(player,corner); //put the location of a corner in a map
							else
								player.sendMessage("Number of corner must be 1 or 2");
						
						} catch (NumberFormatException e){
							player.sendMessage("Use " + ChatColor.RED + "/cm definearea <1|2>");
						}
					
					} else 
						player.sendMessage("Use " + ChatColor.RED + "/cm definearea <1|2>");
					return true;
					
					
					// "/cm setarea <number> [fadeout-range] [priority]"
				} else if (split.length >= 1 && split[0].equalsIgnoreCase("setarea")) {
					
					if (!Permission.permission(player, "cm.area.set", true)) {
						player.sendMessage("You don't have permission to set areas!");
					
					} else {
						if (split.length > 1) {
							try {
								int number = Integer.parseInt(split[1]); //number of area
								
								if (number <= Permission.getPermissionInteger(player, "cm.maxAreas", LoadSettings.defaultMaxAreasPerPlayer)
										|| Permission.getPermissionInteger(player, "cm.maxAreas", LoadSettings.defaultMaxAreasPerPlayer) == 0) {
									
									if (number > 0) {
										int fadeoutRange = LoadSettings.defaultAreaFadeout;
										
										if (split.length > 2)
											fadeoutRange = Integer.parseInt(split[2]); //fadeout-range of area
										
										int priority = Permission.getPermissionInteger(player, "cm.maxBoxPriority",10);
										
										if (split.length > 3) 
											priority = Integer.parseInt(split[3]); //priority of area
											
										if (priority <= 0 || priority > 10 || fadeoutRange < 0)
											player.sendMessage("Fadeout-Range and priority must be higher than 0 and priority can't be higher than 10"); 
										
										else {
											if (priority <= Permission.getPermissionInteger(player, "cm.maxBoxPriority",10))
												BoxList.setarea(player,number,fadeoutRange,priority); //set and save the area
											else
												player.sendMessage("You can set areas with a maximum priority of " + Permission.getPermissionInteger(player, "cm.maxBoxPriority",10) + "."); 
										}
									} else
										player.sendMessage("Areanumber must be higher than 0");
								} else
									player.sendMessage("You can only have " + Permission.getPermissionInteger(player, "cm.maxAreas", LoadSettings.defaultMaxAreasPerPlayer) + " Areas!");
							} catch (NumberFormatException e) {
								player.sendMessage("Use " + ChatColor.RED + "/cm setarea <number> [fadeout-range] [priority]");
							}
						} else 
							player.sendMessage("Use " + ChatColor.RED + "/cm setarea <number> [fadeout-range] [priority]");
					}
					return true;
					
					
					// "/cm deletearea [player] <number>"
				} else if (split.length >= 1 && split[0].equalsIgnoreCase("deletearea")) {
					
					if (split.length > 1) {
						try {
							if (split.length > 2) { //player parameter is used
								if (Permission.permission(player, "cm.area.delete.player", false)) {
									int number = Integer.parseInt(split[2]); //number of the area
									BoxList.deleteArea(player, "a" + String.valueOf(number), true, split[1]); //delete the area of a player
								
								} else
									player.sendMessage("You don't have permission to delete other player's areas!");
							} else {
								int number = Integer.parseInt(split[1]); //number of the area
								BoxList.deleteArea(player, "a" + String.valueOf(number), true, player.getName()); //delete an own area
							}
						} catch (NumberFormatException e) {
							player.sendMessage("Use " + ChatColor.RED + "/cm deletearea [player] <number>");
						}
					} else
						player.sendMessage("Use " + ChatColor.RED + "/cm deletearea [player] <number>");

					return true;
					
					
					// "/cm setbox <number> [range] [priority]"
				} else if (split.length >= 1 && split[0].equalsIgnoreCase("setbox")) {
					if (!Permission.permission(player, "cm.box.set", true)) {
						player.sendMessage("You don't have permission to set boxes!");
					
					} else {
						if (split.length > 1) {
							try {
								int number = Integer.parseInt(split[1]);  //number of the box
								if (number <= Permission.getPermissionInteger(player, "cm.maxBoxes", LoadSettings.defaultMaxBoxesPerPlayer)
										|| Permission.getPermissionInteger(player, "cm.maxBoxes", LoadSettings.defaultMaxBoxesPerPlayer) == 0) {
									
									if (number > 0) {
										int range = LoadSettings.defaultRange;
										int priority = Permission.getPermissionInteger(player, "cm.maxBoxPriority",10);
										
										if (split.length > 2)  //range parameter is used
											range = Integer.parseInt(split[2]); 
										
										if (split.length > 3) //priority parameter is used
											priority = Integer.parseInt(split[3]);
										
										if (range > 0 && priority > 0 && priority <= 10 ) {
											if ((range <= Permission.getPermissionInteger(player, "cm.maxBoxRange", LoadSettings.defaultRange)
													|| Permission.getPermissionInteger(player, "cm.maxBoxRange", LoadSettings.defaultRange) == 0)
													&& priority <= Permission.getPermissionInteger(player, "cm.maxBoxPriority",10))
												
												BoxList.setbox(player, number, range, priority); //set and save the box
											
											else
												player.sendMessage("You can set boxes with a maximum range of " + Permission.getPermissionInteger(player, "cm.maxBoxRange", LoadSettings.defaultRange) + " and priority of " + Permission.getPermissionInteger(player, "cm.maxBoxPriority",10) + ".");
										} else 
											player.sendMessage("Range and priority must be higher than 0 and priority can't be higher than 10"); 
									} else
										player.sendMessage("Boxnumber must be higher than 0");
								} else
									player.sendMessage("You can only have " + Permission.getPermissionInteger(player, "cm.maxBoxes", LoadSettings.defaultMaxBoxesPerPlayer) + " boxes!");
							} catch (NumberFormatException e) {
								player.sendMessage("Use " + ChatColor.RED + "/cm setbox <number> [range] [priority]");
							}
						} else
							player.sendMessage("Use " + ChatColor.RED + "/cm setbox <number> [range] [priority]");
					}
					return true;

					// "/cm deletebox [player] <number>"
				} else if (split.length >= 1 && split[0].equalsIgnoreCase("deletebox")) {
					
					if (split.length > 1) {
						try {
							if (split.length > 2) { //player parameter is used
								if (Permission.permission(player, "cm.box.delete.player", false)) {
									int number = Integer.parseInt(split[2]); //get number of the box
									BoxList.deleteBox(player, number, true, split[1]); //delete the box of a player
								
								} else
									player.sendMessage("You don't have permission to delete other player's boxes!");
							} else {
								int number = Integer.parseInt(split[1]); //get number of the box
								BoxList.deleteBox(player, number, true, player.getName()); //delete an own box
							}
						} catch (NumberFormatException e) {
							player.sendMessage("Use " + ChatColor.RED + "/cm deletebox [player] <number>");
						}
					} else
						player.sendMessage("Use " + ChatColor.RED + "/cm deletebox [player] <number>");

					return true;
					
				
					//cm setworld [world] [<volume> <priority>]
				}else if(split.length >= 1 && split[0].equalsIgnoreCase("setworld")) {
					if (!Permission.permission(player, "cm.world.set", false))
						player.sendMessage("You don't have permission to set music for worlds!");
					else {
						String world = null;
						Integer volume = null;
						Integer priority = null;
						try {
							if (split.length > 3){
								world = split[1];
								volume = Integer.parseInt(split[2]);
								priority = Integer.parseInt(split[3]);
								int maxPrior = Permission.getPermissionInteger(player, "cm.maxBoxPriority", 10);
								if ((priority <= maxPrior || maxPrior==0) && priority <= 10 && volume <= 500) {
									BoxList.setWorld(player, world, volume, priority);
								} else
									player.sendMessage("Volume must be an integral value from 1-500. Priority must be between 1-" + Permission.getPermissionInteger(player, "cm.maxBoxPriority", 10));
							} else if (split.length == 2) {
								world = split[1];
								BoxList.setWorld(player, world, 70, Permission.getPermissionInteger(player, "cm.maxBoxPriority", 10));
							} else if (split.length == 1) {
								world = player.getWorld().getName();
								BoxList.setWorld(player, world, 70, Permission.getPermissionInteger(player, "cm.maxBoxPriority", 10));
							} else {
								player.sendMessage("Use " + ChatColor.RED + "'/cm setworld [world] [<volume> <priority>]'");
							}
						} catch (NumberFormatException e) {
							player.sendMessage("Use " + ChatColor.RED + "'/cm setworld [world] [<volume> <priority>]'");
						}
						
					}
					return true;
					
					
					//cm wchoose [world] <songnumber(s)|all|none>
				} else if(split.length >= 1 && split[0].equalsIgnoreCase("wchoose")) {
					if (!Permission.permission(player,"cm.world.set", false)) {
						player.sendMessage("You don't have permission to manage world-music!");
					} else {
						if (split.length > 2) {
							if (BoxList.chooseWorldSongs(player, split[1], split[2]))  //choose the songs for the box
								player.sendMessage(ChatColor.RED + "CustomMusic: Songs for world " + split[1] + "  chosen!");						
							 else {
								player.sendMessage("Use " + ChatColor.RED + "'/cm wchoose [world] <songnumbers|all|none>'");
								player.sendMessage("Use " + ChatColor.RED + "'/cm songlist'" + ChatColor.WHITE + " to get a list of songs and their numbers");
							 }
						} else if (split.length > 1) {
							String world = player.getWorld().getName();
							if (BoxList.chooseWorldSongs(player, world, split[1]))  //choose the songs for the box
								player.sendMessage(ChatColor.RED + "CustomMusic: Songs for world " + world + "  chosen!");						
							 else {
								player.sendMessage("Use " + ChatColor.RED + "'/cm wchoose [world] <songnumbers|all|none>'");
								player.sendMessage("Use " + ChatColor.RED + "'/cm songlist'" + ChatColor.WHITE + " to get a list of songs and their numbers");
							 }
							
						} else {
							player.sendMessage("Use " + ChatColor.RED + "'/cm wchoose [world] <songnumbers|all|none>'");	
							player.sendMessage("Use " + ChatColor.RED + "'/cm songlist'" + ChatColor.WHITE + " to get a list of songs and their numbers");
						}
					}
					return true;
					
					
					// "/cm wchooseradio [world] <radionumber>"
				} else if (split.length >= 1 && split[0].equalsIgnoreCase("wchooseradio")) {
					
					if (!Permission.permission(player,"cm.world.set", false)) {
						player.sendMessage("You don't have permission to manage world-music!");
						return true;
					}
					
					String radionumberstring = null;
					String world = null;
					
					if (split.length > 2) {
						radionumberstring = split[2];
						world = split[1];
					} else {
						world = player.getWorld().getName();
					}
					
					if (split.length > 1) {
						try {
							radionumberstring = split[1];
							if (BoxList.chooseWorldStation(player, world, Integer.parseInt(radionumberstring))) { 
								player.sendMessage(ChatColor.RED + "CustomMusic: Stations for world " + world + "  chosen!");
							}
													
						} catch (NumberFormatException e) {
							player.sendMessage("Use " + ChatColor.RED + "'/cm wchooseradio [world] <radionumber>'");
							player.sendMessage("Use " + ChatColor.RED + "'/cm radiolist'" + ChatColor.WHITE + " to get a list of radio stations and their numbers");
						}
					
					} else {
						player.sendMessage("Use " + ChatColor.RED + "'/cm wchooseradio [world] <radionumber>'");
						player.sendMessage("Use " + ChatColor.RED + "'/cm radiolist'" + ChatColor.WHITE + " to get a list of radio stations and their numbers");
					}
					return true;
					
					
					//cm setbiome [biome] [<volume> <priority>]
				}else if(split.length >= 1 && split[0].equalsIgnoreCase("setbiome")) {
					if (!Permission.permission(player, "cm.biome.set", false))
						player.sendMessage("You don't have permission to set music for biomes!");
					else {
						String biome = null;
						Integer volume = null;
						Integer priority = null;
						try {
							if (split.length > 3){
								biome = split[1];
								volume = Integer.parseInt(split[2]);
								priority = Integer.parseInt(split[3]);
								int maxPrior = Permission.getPermissionInteger(player, "cm.maxBoxPriority", 10);
								if ((priority <= maxPrior || maxPrior==0) && priority <= 10 && volume <= 500) {
									BoxList.setBiome(player, biome, volume, priority);
								} else
									player.sendMessage("Volume must be an integral value from 1-500. Priority must be between 1-" + Permission.getPermissionInteger(player, "cm.maxBoxPriority", 10));
							} else if (split.length == 2) {
								biome = split[1];
								BoxList.setBiome(player, biome, 70, Permission.getPermissionInteger(player, "cm.maxBoxPriority", 10));
							} else if (split.length == 1) {
								biome = player.getLocation().getBlock().getBiome().toString();
								BoxList.setBiome(player, biome, 70, Permission.getPermissionInteger(player, "cm.maxBoxPriority", 10));
							} else {
								player.sendMessage("Use " + ChatColor.RED + "'/cm setbiome [biome] [<volume> <priority>]'");
								player.sendMessage("Use " + ChatColor.RED + "'/cm biomes'" + ChatColor.WHITE + " to get a list of available biomes");
							}
						} catch (NumberFormatException e) {
							player.sendMessage("Use " + ChatColor.RED + "'/cm setbiome [biome] [<volume> <priority>]'");
							player.sendMessage("Use " + ChatColor.RED + "'/cm biomes'" + ChatColor.WHITE + " to get a list of available biomes");
						}
						
					}
					return true;
					
					
					//cm biochoose [biome] <songnumber(s)|all|none>
				} else if(split.length >= 1 && split[0].equalsIgnoreCase("biochoose")) {
					if (!Permission.permission(player,"cm.biome.set", false)) {
						player.sendMessage("You don't have permission to manage biome-music!");
					} else {
						if (split.length > 2) {
							if (BoxList.chooseBiomeSongs(player, split[1], split[2]))  //choose the songs for the box
								player.sendMessage(ChatColor.RED + "CustomMusic: Songs for biome " + split[1] + "  chosen!");							
							else {
								player.sendMessage("Use " + ChatColor.RED + "'/cm biochoose <biome> <songnumbers|all|none>'");
								player.sendMessage("Use " + ChatColor.RED + "'/cm songlist'" + ChatColor.WHITE + " to get a list of songs and their numbers");
							}
							
						} else if (split.length > 1) {
							String biome = player.getLocation().getBlock().getBiome().toString();
							if (BoxList.chooseBiomeSongs(player, biome, split[1]))  //choose the songs for the box
								player.sendMessage(ChatColor.RED + "CustomMusic: Songs for biome " + biome + "  chosen!");							
							else {
								player.sendMessage("Use " + ChatColor.RED + "'/cm biochoose <biome> <songnumbers|all|none>'");
								player.sendMessage("Use " + ChatColor.RED + "'/cm songlist'" + ChatColor.WHITE + " to get a list of songs and their numbers");
							}
							
						} else {
							player.sendMessage("Use " + ChatColor.RED + "'/cm biochoose <biome> <songnumbers|all|none>'");
							player.sendMessage("Use " + ChatColor.RED + "'/cm songlist'" + ChatColor.WHITE + " to get a list of songs and their numbers");
						}
						
					}
					return true;
					
					
					// "/cm biochooseradio [biome] <radionumber>"
				} else if (split.length >= 1 && split[0].equalsIgnoreCase("biochooseradio")) {
					
					if (!Permission.permission(player,"cm.biome.set", false)) {
						player.sendMessage("You don't have permission to manage biome-music!");
						return true;
					}
					
					String radionumberstring = null;
					String biome = null;
					
					if (split.length > 2) {
						radionumberstring = split[2];
						biome = split[1];
					} else {
						biome = player.getLocation().getBlock().getBiome().toString();
					}
					
					if (split.length > 1) {
						try {
							radionumberstring = split[1];
							if (BoxList.chooseBiomeStation(player, biome, Integer.parseInt(radionumberstring))) { 
								player.sendMessage(ChatColor.RED + "CustomMusic: Stations for biome " + biome + "  chosen!");
							}
													
						} catch (NumberFormatException e) {
							player.sendMessage("Use " + ChatColor.RED + "'/cm biochooseradio [biome] <radionumber>'");
							player.sendMessage("Use " + ChatColor.RED + "'/cm radiolist'" + ChatColor.WHITE + " to get a list of radio stations and their numbers");
						}
					
					} else {
						player.sendMessage("Use " + ChatColor.RED + "'/cm biochooseradio [biome] <radionumber>'");
						player.sendMessage("Use " + ChatColor.RED + "'/cm radiolist'" + ChatColor.WHITE + " to get a list of radio stations and their numbers");
					}
					return true;					
					
					
					// "/cm biomes"
				} else if (split.length == 1 && split[0].equalsIgnoreCase("biomes")) {
					player.sendMessage("Available biomes:");
					for(int i = 0;i<GlobalData.av_biomes.length;i++) {
						player.sendMessage(GlobalData.av_biomes[i]);
					}
					return true;
					
				
					
					// "/cm worldlist <page>"
				} else if (split.length >= 1 && split[0].equalsIgnoreCase("worldlist")) {
					
					if (split.length > 1) {
						try {
							int Page = Integer.parseInt(split[1]);
							if (Page > 0)
								BoxList.listWorlds(player, Page);
							
						} catch (NumberFormatException e) {
							player.sendMessage("Use " + ChatColor.RED + "/cm worldlist <page>");
						}
					} else
						player.sendMessage("Use " + ChatColor.RED + "/cm worldlist <page>");
					return true;
					
					// "/cm biomelist <page>"
				} else if (split.length >= 1 && split[0].equalsIgnoreCase("biomelist")) {
					
					if (split.length > 1) {
						try {
							int Page = Integer.parseInt(split[1]);
							if (Page > 0)
								BoxList.listBiomes(player, Page);
							
						} catch (NumberFormatException e) {
							player.sendMessage("Use " + ChatColor.RED + "/cm biomelist <page>");
						}
					} else
						player.sendMessage("Use " + ChatColor.RED + "/cm biomelist <page>");
					return true;

					// "/cm init"
				} else if (split.length == 1 && split[0].equalsIgnoreCase("init")) {
					if (!Permission.permission(player, "cm.init", true)) {
						player.sendMessage("You don't have permission to use the AudioClient!");
					
					} else
						Mp3PlayerHandler.confirm(player); //initialize the audioclient
					return true;

					// "/cm status"
				} else if (split.length == 1 && split[0].equalsIgnoreCase("status")) {
					
					float x = (float) player.getLocation().getX();
					float y = (float) player.getLocation().getY();
					float z = (float) player.getLocation().getZ();
					String world = player.getWorld().toString();
					//==> get the players coordinates
					
					String result = Calculations.status(x, y, z, world); //check if box or area in range
					
					if (result.equals("area is free")) {
						player.sendMessage("There's no box or area in range!");
					} else
						player.sendMessage("You are in range of " + ChatColor.RED + result);
					return true;

				
						// "/cm radiolist [player] <page>"
				} else if (split.length >= 1 && split[0].equalsIgnoreCase("radiolist")) {
					
					if (split.length == 2) {
						try {
							int Page = Integer.parseInt(split[1]); //page of list
							if (Page > 0)
								BoxList.listStations(player, Page, player.getName()); //list own stations
							
						} catch (NumberFormatException e) {
							player.sendMessage("Use " + ChatColor.RED + "/cm radiolist [player] <page>");
						}
					
					} else if (split.length > 2) { //player parameter is used
						if (Permission.permission(player, "cm.radio.list.player", true)) {
							try {
								int Page = Integer.parseInt(split[2]); //page of list
								if (Page > 0)
									BoxList.listStations(player, Page, split[1]); //list the player's songs
								
							} catch (NumberFormatException e) {
								player.sendMessage("Use " + ChatColor.RED + "/cm radiolist [player] <page>");
							}
						} else
							player.sendMessage("You don't have permission to list other player's radio stations!");
					} else
						player.sendMessage("Use " + ChatColor.RED + "/cm radiolist [player] <page>");
					return true;
					
					// "/cm songlist [player] <page>"
				} else if (split.length >= 1 && split[0].equalsIgnoreCase("songlist")) {
					
					if (split.length == 2) {
						try {
							int Page = Integer.parseInt(split[1]); //page of list
							if (Page > 0)
								BoxList.listSongs(player, Page, player.getName()); //list own songs
							
						} catch (NumberFormatException e) {
							player.sendMessage("Use " + ChatColor.RED + "/cm songlist [player] <page>");
						}
					
					} else if (split.length > 2) { //player parameter is used
						if (Permission.permission(player, "cm.song.list.player", true)) {
							try {
								int Page = Integer.parseInt(split[2]); //page of list
								if (Page > 0)
									BoxList.listSongs(player, Page, split[1]); //list the player's songs
								
							} catch (NumberFormatException e) {
								player.sendMessage("Use " + ChatColor.RED + "/cm songlist [player] <page>");
							}
						} else
							player.sendMessage("You don't have permission to list other player's songs!");
					} else
						player.sendMessage("Use " + ChatColor.RED + "/cm songlist [player] <page>");
					return true;

					// "/cm deleteradio [player] <number>"
				} else if (split.length >= 1 && split[0].equalsIgnoreCase("deleteradio")) {
					
					if (split.length == 2) {
						try {
							int number = Integer.parseInt(split[1]); //song's number
							if (number > 0)
								BoxList.deleteStation(player, number, player.getName()); //delete own station
							
						} catch (NumberFormatException e) {
							player.sendMessage("Use " + ChatColor.RED + "/cm deleteradio [player] <number>");
						}
					
					} else if (split.length > 2) { //player parameter is used
						if (Permission.permission(player, "cm.radio.delete.player", false)) {
							try {
								int number = Integer.parseInt(split[2]); //song's number
								if (number > 0)
									BoxList.deleteStation(player, number, split[1]); //delete the player's station
								
							} catch (NumberFormatException e) {
								player.sendMessage("Use " + ChatColor.RED + "/cm deleteradio [player] <number>");
							}
						} else
							player.sendMessage("You don't have permission to delete other player's radio stations!");
					} else
						player.sendMessage("Use " + ChatColor.RED + "/cm deleteradio [player] <number>");
					return true;
					
					// "/cm deletesong [player] <number>"
				} else if (split.length >= 1 && split[0].equalsIgnoreCase("deletesong")) {
					
					if (split.length == 2) {
						try {
							int number = Integer.parseInt(split[1]); //song's number
							if (number > 0)
								BoxList.deleteSong(player, number, player.getName()); //delete the own song
							
						} catch (NumberFormatException e) {
							player.sendMessage("Use " + ChatColor.RED + "/cm deletesong [player] <number>");
						}
					
					} else if (split.length > 2) { //player parameter is used
						if (Permission.permission(player, "cm.song.delete.player", true)) {
							try {
								int number = Integer.parseInt(split[2]); //song's number
								if (number > 0)
									BoxList.deleteSong(player, number, split[1]); //delete the player's song
								
							} catch (NumberFormatException e) {
								player.sendMessage("Use " + ChatColor.RED + "/cm deletesong [player] <number>");
							}
						} else
							player.sendMessage("You don't have permission to delete other player's songs!");
					} else
						player.sendMessage("Use " + ChatColor.RED + "/cm deletesong [player] <number>");
					return true;

					// "/cm boxlist [player] <page>"
				} else if (split.length >= 1 && split[0].equalsIgnoreCase("boxlist")) {
					
					if (split.length == 2) {
						try {
							int Page = Integer.parseInt(split[1]);
							if (Page > 0)
								BoxList.listBoxes(player, Page, player.getName()); //list the own boxes
							
						} catch (NumberFormatException e) {
							player.sendMessage("Use " + ChatColor.RED + "/cm boxlist [player] <page>");
						}
					
					} else if (split.length > 2) { //player parameter is used
						if (Permission.permission(player, "cm.box.list.player", true)) {
							try {
								int Page = Integer.parseInt(split[2]);
								if (Page > 0)
									BoxList.listBoxes(player, Page, split[1]); //list player's boxes
							
							} catch (NumberFormatException e) {
								player.sendMessage("Use " + ChatColor.RED + "/cm boxlist [player] <page>");
							}
						} else
							player.sendMessage("You don't have permission to list other player's boxes!");
					} else
						player.sendMessage("Use " + ChatColor.RED + "/cm boxlist [player] <page>");
					return true;
					
					
					// "/cm arealist [player] <page>"
				} else if (split.length >= 1 && split[0].equalsIgnoreCase("arealist")) {
					
					if (split.length == 2) {
						try {
							int Page = Integer.parseInt(split[1]);
							if (Page > 0)
								BoxList.listAreas(player, Page, player.getName()); //list own areas
							
						} catch (NumberFormatException e) {
							player.sendMessage("Use " + ChatColor.RED + "/cm arealist [player] <page>");
						}
					
					} else if (split.length > 2) { //player parameter is used
						if (Permission.permission(player, "cm.area.list.player", true)) {
							try {
								int Page = Integer.parseInt(split[2]);
								if (Page > 0)
									BoxList.listAreas(player, Page, split[1]); //list player's areas
								
							} catch (NumberFormatException e) {
								player.sendMessage("Use " + ChatColor.RED + "/cm arealist [player] <page>");
							}
						} else
							player.sendMessage("You don't have permission to list other player's areas!");
					} else
						player.sendMessage("Use " + ChatColor.RED + "/cm arealist [player] <page>");
					return true;

					// "/cm bchoose <number> <songnumbers>"
				} else if (split.length >= 1 && split[0].equalsIgnoreCase("bchoose")) {
					
					if (split.length > 2) {
						try {
							if (StringUtil.isInteger(split[1])) {
								if (BoxList.chooseBoxSongs(player, Integer.parseInt(split[1]), split[2])) { //choose the songs for the box
									player.sendMessage(ChatColor.RED + "CustomMusic: Songs for box " + split[1] + "  chosen!");
								}
							
							} else {
								player.sendMessage("Use " + ChatColor.RED + "'/cm bchoose <boxnumber> <songnumbers>/all'" + ChatColor.WHITE + "(seperated by comma)");
								player.sendMessage("Use " + ChatColor.RED + "'/cm songlist'" + ChatColor.WHITE + " to get a list of songs and their numbers");
							}
						
						} catch (NumberFormatException e) {
							player.sendMessage("Use " + ChatColor.RED + "'/cm bchoose <boxnumber> <songnumbers>'" + ChatColor.WHITE + "(seperated by comma)");
							player.sendMessage("Use " + ChatColor.RED + "'/cm songlist'" + ChatColor.WHITE + " to get a list of songs and their numbers");
						}
					
					} else {
						player.sendMessage("Use " + ChatColor.RED + "'/cm bchoose <boxnumber> <songnumbers>'" + ChatColor.WHITE + " (seperated by comma)");
						player.sendMessage("Use " + ChatColor.RED + "'/cm songlist'" + ChatColor.WHITE + " to get a list of songs and their numbers");
					}
					return true;
					
					
					// "/cm achoose <number> <sungnumbers>"
				} else if (split.length >= 1 && split[0].equalsIgnoreCase("achoose")) {
					
					if (split.length > 2) {
						try {
							if (StringUtil.isInteger(split[1])) {
								if (BoxList.chooseAreaSongs(player, Integer.parseInt(split[1]), split[2])) { //choose the songs for the area
									player.sendMessage(ChatColor.RED + "CustomMusic: Songs for area " + split[1] + " chosen!");
								}
							
							} else {
								player.sendMessage("Use " + ChatColor.RED + "'/cm achoose <areanumber> <songnumbers>/all'" + ChatColor.WHITE + "(seperated by comma)");
								player.sendMessage("Use " + ChatColor.RED + "'/cm songlist'" + ChatColor.WHITE + " to get a list of songs and their numbers");
							}
						
						} catch (NumberFormatException e) {
							player.sendMessage("Use " + ChatColor.RED + "'/cm achoose <areanumber> <songnumbers>'" + ChatColor.WHITE + "(seperated by comma)");
							player.sendMessage("Use " + ChatColor.RED + "'/cm songlist'" + ChatColor.WHITE + " to get a list of songs and their numbers");
						}
			
					} else {
						player.sendMessage("Use " + ChatColor.RED + "'/cm achoose <areanumber> <songnumbers>'" + ChatColor.WHITE + " (seperated by comma)");
						player.sendMessage("Use " + ChatColor.RED + "'/cm songlist'" + ChatColor.WHITE + " to get a list of songs and their numbers");
					}
					return true;
					
					// "/cm bchooseradio <number> <radionumber>"
				} else if (split.length >= 1 && split[0].equalsIgnoreCase("bchooseradio")) {
					
					if (split.length > 2) {
						try {
							if (StringUtil.isInteger(split[1])) {
								if (BoxList.chooseBoxStation(player, Integer.parseInt(split[1]), Integer.parseInt(split[2]))) { //choose the songs for the box
									player.sendMessage(ChatColor.RED + "CustomMusic: Stations for box " + split[1] + "  chosen!");
								}
							
							} else {
								player.sendMessage("Use " + ChatColor.RED + "'/cm bchooseradio <boxnumber> <radionumber>'");
								player.sendMessage("Use " + ChatColor.RED + "'/cm radiolist'" + ChatColor.WHITE + " to get a list of radio stations and their numbers");
							}
						
						} catch (NumberFormatException e) {
							player.sendMessage("Use " + ChatColor.RED + "'/cm bchooseradio <boxnumber> <radionumber>'");
							player.sendMessage("Use " + ChatColor.RED + "'/cm radiolist'" + ChatColor.WHITE + " to get a list of radio stations and their numbers");
						}
					
					} else {
						player.sendMessage("Use " + ChatColor.RED + "'/cm bchooseradio <boxnumber> <radionumber>'");
						player.sendMessage("Use " + ChatColor.RED + "'/cm radiolist'" + ChatColor.WHITE + " to get a list of radio stations and their numbers");
					}
					return true;
					
					// "/cm achooseradio <number> <radionumber>"
				} else if (split.length >= 1 && split[0].equalsIgnoreCase("achooseradio")) {
					
					if (split.length > 2) {
						try {
							if (StringUtil.isInteger(split[1])) {
								if (BoxList.chooseAreaStation(player, Integer.parseInt(split[1]), Integer.parseInt(split[2]))) { //choose the songs for the box
									player.sendMessage(ChatColor.RED + "CustomMusic: Stations for area " + split[1] + "  chosen!");
								}
							
							} else {
								player.sendMessage("Use " + ChatColor.RED + "'/cm achooseradio <areanumber> <radionumber>'");
								player.sendMessage("Use " + ChatColor.RED + "'/cm radiolist'" + ChatColor.WHITE + " to get a list of radio stations and their numbers");
							}
						
						} catch (NumberFormatException e) {
							player.sendMessage("Use " + ChatColor.RED + "'/cm achooseradio <areanumber> <radionumber>'");
							player.sendMessage("Use " + ChatColor.RED + "'/cm radiolist'" + ChatColor.WHITE + " to get a list of radio stations and their numbers");
						}
					
					} else {
						player.sendMessage("Use " + ChatColor.RED + "'/cm achooseradio <areanumber> <radionumber>'");
						player.sendMessage("Use " + ChatColor.RED + "'/cm radiolist'" + ChatColor.WHITE + " to get a list of radio stations and their numbers");
					}
					return true;

					// "/cm toggle"
				} else if (split.length == 1 && split[0].equalsIgnoreCase("toggle")) {
					
					toggleMusic(player); //toggle the music
					return true;
					
					// "/cm ignorebox <player> <number>"
				} else if (split.length >= 1 && split[0].equalsIgnoreCase("ignorebox")) {
					if (split.length > 2) {
						try {
							int number = Integer.parseInt(split[2]); //get number of the box
							BoxList.ignoreBox(player, number, split[1]); //player = split[1]
						} catch (NumberFormatException e) {
							player.sendMessage("Use " + ChatColor.RED + "/cm ignorebox <player> <number>");
						}
					} else
						player.sendMessage("Use " + ChatColor.RED + "/cm ignorebox <player> <number>");
					return true;
					
					// "/cm unignorebox <player> <number>"
				} else if (split.length >= 1 && split[0].equalsIgnoreCase("unignorebox")) {
					if (split.length > 2) {
						try {
							int number = Integer.parseInt(split[2]); //get number of the box
							BoxList.unignoreBox(player, number, split[1]); //player = split[1]
						} catch (NumberFormatException e) {
							player.sendMessage("Use " + ChatColor.RED + "/cm unignorebox <player> <number>");
						}
					} else
						player.sendMessage("Use " + ChatColor.RED + "/cm unignorebox <player> <number>");
					return true;
					
					
					// "/cm ignorearea <player> <number>"
				} else if (split.length >= 1 && split[0].equalsIgnoreCase("ignorearea")) {
					if (split.length > 2) {
						try {
							int number = Integer.parseInt(split[2]); //get number of the area
							BoxList.ignoreArea(player, "a" + number, split[1]); //player = split[1]
						} catch (NumberFormatException e) {
							player.sendMessage("Use " + ChatColor.RED + "/cm ignorearea <player> <number>");
						}
					} else
						player.sendMessage("Use " + ChatColor.RED + "/cm ignorearea <player> <number>");
					return true;
					
					
					// "/cm unignorearea <player> <number>"
				} else if (split.length >= 1 && split[0].equalsIgnoreCase("unignorearea")) {
					if (split.length > 2) {
						try {
							int number = Integer.parseInt(split[2]); //get number of the area
							BoxList.unignoreArea(player, "a" + number, split[1]); //player = split[1]
						} catch (NumberFormatException e) {
							player.sendMessage("Use " + ChatColor.RED + "/cm unignorearea <player> <number>");
						}
					} else
						player.sendMessage("Use " + ChatColor.RED + "/cm unignorearea <player> <number>");
					return true;
					
					
					// "/cm ignoreworld [world]"
				} else if (split.length >= 1 && split[0].equalsIgnoreCase("ignoreworld")) {
					if (split.length > 1)
						BoxList.ignoreWorld(player, split[1]); //player = split[1]
					else {
						String world = player.getWorld().getName();
						BoxList.ignoreWorld(player, world); //player = split[1]
					}
					return true;
					
					// "/cm unignoreworld [world]"
				} else if (split.length >= 1 && split[0].equalsIgnoreCase("unignoreworld")) {
					if (split.length > 1)
						BoxList.unignoreWorld(player, split[1]); //player = split[1]
					else {
						String world = player.getWorld().getName();
						BoxList.unignoreWorld(player, world); //player = split[1]
					}
					return true;
				
					// "/cm ignorebiome [biome]"
				} else if (split.length >= 1 && split[0].equalsIgnoreCase("ignorebiome")) {
					if (split.length > 1)
						BoxList.ignoreBiome(player, split[1]); //player = split[1]
					else {
						String biome = player.getLocation().getBlock().getBiome().toString();
						BoxList.ignoreBiome(player, biome); //player = split[1]
					}
					return true;
					
					// "/cm unignorebiome [biome]"
				} else if (split.length >= 1 && split[0].equalsIgnoreCase("unignorebiome")) {
					if (split.length > 1)
						BoxList.unignoreBiome(player, split[1]); //player = split[1]
					else {
						String biome = player.getLocation().getBlock().getBiome().toString();
						BoxList.unignoreBiome(player, biome); //player = split[1]
					}
					return true;
					
					// "/cm ignorelist <page>"
				} else if (split.length >= 1 && split[0].equalsIgnoreCase("ignorelist")) {
					
					if (split.length > 1) {
						try {
							int Page = Integer.parseInt(split[1]);
							if (Page > 0)
								BoxList.listIgno(player, Page);
							
						} catch (NumberFormatException e) {
							player.sendMessage("Use " + ChatColor.RED + "/cm ignorelist <page>");
						}
					} else
						player.sendMessage("Use " + ChatColor.RED + "/cm ignorelist <page>");
					return true;
					
					
					// "/cm volume <value>"
				}else if (split.length >= 1 && split[0].equalsIgnoreCase("volume")) {
					if (split.length > 1) {
						try {
							int volume = Integer.parseInt(split[1]);
							
							if (volume > 0 && volume <= 500) {
								Mp3PlayerHandler.changeVolume(player, volume);
								player.sendMessage(ChatColor.RED + "CustomMusic: Volume set to " + volume + ".");
							} else
								player.sendMessage("Volume must be higher than 0 and cannot be higher than 500.");
						} catch (NumberFormatException e) {
							player.sendMessage("Use " + ChatColor.RED + "/cm volume <value>");
						}
					} else
						player.sendMessage("Use " + ChatColor.RED + "/cm volume <value>");
					return true;

					// "/cm users"
				} else if (split.length == 1 && split[0].equalsIgnoreCase("users")) {
					
					player.sendMessage("Currently using AudioClient (" + GlobalData.CMUsers.size() + "): " + ChatColor.RED + GlobalData.CMUsers.values());
					//==> show a list of players that are using the audioclient
					return true;

					// "/cm play [player] <songnumbers>"
				} else if (split.length >= 1 && split[0].equalsIgnoreCase("play")) {
					
					if (split.length == 2) {

						if (Mp3PlayerHandler.playSong(player, split[1], false, player.getName()))  //play an own song
							player.sendMessage(ChatColor.RED + "Playing...!");
						
					} else if (split.length > 2) { //player parameter is used

						if (Mp3PlayerHandler.playSong(player, split[2], false, split[1])) //play a player's song
							player.sendMessage(ChatColor.RED + "Playing...!");

					} else
						player.sendMessage("Use " + ChatColor.RED + "/cm play [player] <songnumbers>");
					return true;
					
					
					// "/cm playradio [player] <radionumber>"
				} else if (split.length >= 1 && split[0].equalsIgnoreCase("playradio")) {
					
					if (split.length == 2) {

						try {
							if (Mp3PlayerHandler.playStation(player, Integer.parseInt(split[1]), false, player.getName()))  //play an own station
								player.sendMessage(ChatColor.RED + "Playing...!");
						} catch (NumberFormatException e) {
							player.sendMessage("Use " + ChatColor.RED + "/cm playradio [player] <radionumber>");
						}
						
					} else if (split.length > 2) { //player parameter is used

						try {
							if (Mp3PlayerHandler.playStation(player, Integer.parseInt(split[2]), false, split[1])) //play a player's station
								player.sendMessage(ChatColor.RED + "Playing...!");
						} catch (NumberFormatException e) {
							player.sendMessage("Use " + ChatColor.RED + "/cm playradio [player] <radionumber>");
						}
					

					} else
						player.sendMessage("Use " + ChatColor.RED + "/cm playradio [player] <radionumber>");
					return true;
					
					
					// "/cm gplayradio"
				} else if (split.length >= 1 && split[0].equalsIgnoreCase("gplayradio")) {
					
					if (Permission.permission(player, "cm.song.play.global", true)) {
						if (split.length == 2) {
	
							try {
								if (Mp3PlayerHandler.playStation(player, Integer.parseInt(split[1]), true, player.getName())) //globally play an own station
									player.sendMessage(ChatColor.RED + "Playing globally...!");
							} catch (NumberFormatException e) {
								player.sendMessage("Use " + ChatColor.RED + "/cm gplayradio [player] <radionumber>");
							}
							
						} else if (split.length > 2) { //player parameter is used
							
							try {
								if (Mp3PlayerHandler.playStation(player, Integer.parseInt(split[2]), true, split[1])) // globally play a player's station
									player.sendMessage(ChatColor.RED + "Playing globally...!");
							} catch (NumberFormatException e) {
								player.sendMessage("Use " + ChatColor.RED + "/cm gplayradio [player] <radionumber>");
							}
							
						} else
							player.sendMessage("Use " + ChatColor.RED + "/cm gplayradio [player] <songnumbers>");
					} else 
						player.sendMessage("You don't have permission to play webradio streams globally!");
					return true;
					
					// "/cm gplay"
				} else if (split.length >= 1 && split[0].equalsIgnoreCase("gplay")) {
					
					if (Permission.permission(player, "cm.song.play.global", true)) {
						if (split.length == 2) {
	
							if (Mp3PlayerHandler.playSong(player, split[1], true, player.getName())) //globally play an own song
								player.sendMessage(ChatColor.RED + "Playing globally...!");
							
						} else if (split.length > 2) { //player parameter is used
	
							if (Mp3PlayerHandler.playSong(player, split[2], true, split[1])) // globally play a player's song
								player.sendMessage(ChatColor.RED + "Playing globally...!");
	
						} else
							player.sendMessage("Use " + ChatColor.RED + "/cm gplay [player] <songnumbers>");
					} else 
						player.sendMessage("You don't have permission to play songs globally!");
					return true;
					

					// "/cm stop"
				} else if (split.length == 1 && split[0].equalsIgnoreCase("stop")) {
					Mp3PlayerHandler.stopSong(player); //stop playing the song that is played with /cm play
					return true;
					
					// "/cm gstop"
				} else if (split.length == 1 && split[0].equalsIgnoreCase("gstop")) {
					if (Permission.permission(player, "cm.song.stop.global", true)) { 
						Mp3PlayerHandler.gstopSong(player);//globally stop playing the song that is played with /cm gplay
					
					} else
						player.sendMessage("You don't have permission to stop songs globally!");
					return true;

				} else { //show available commands
					player.sendMessage("Available commands ( [ ] means optional): ");
					player.sendMessage(ChatColor.RED + "'/cm init'");
					player.sendMessage(ChatColor.RED + "'/cm toggle'");
					player.sendMessage(ChatColor.RED + "'/cm setbox <number> [range] [priority]'");
					player.sendMessage(ChatColor.RED + "'/cm deletebox [player] <number>'");
					player.sendMessage(ChatColor.RED + "'/cm definearea <1|2>'");
					player.sendMessage(ChatColor.RED + "'/cm setarea <number> [fadeout-range] [priority]'");
					player.sendMessage(ChatColor.RED + "'/cm deletearea [player] <number>'");	
					player.sendMessage(ChatColor.RED + "'/cm setworld [world] [<volume> <priority>]'");
					player.sendMessage(ChatColor.RED + "'/cm setbiome [biome] [<volume> <priority>]'");
					player.sendMessage(ChatColor.RED + "'/cm achoose <areanumber> <songnumbers>'");
					player.sendMessage(ChatColor.RED + "'/cm bchoose <boxnumber> <songnumbers>'");
					player.sendMessage(ChatColor.RED + "'/cm achooseradio <areanumber> <radionumber>'");
					player.sendMessage(ChatColor.RED + "'/cm bchooseradio <boxnumber> <radionumber>'");
					player.sendMessage(ChatColor.RED + "'/cm wchoose [world] <songnumbers|all|none>'");
					player.sendMessage(ChatColor.RED + "'/cm biochoose [biome] <songnumbers|all|none>'");
					player.sendMessage(ChatColor.RED + "'/cm wchooseradio [world] <radionumber>'");
					player.sendMessage(ChatColor.RED + "'/cm biochooseradio [biome] <radionumber>'");
					player.sendMessage(ChatColor.RED + "'/cm volume <value>'");
					player.sendMessage("Use " + ChatColor.RED + "'/cm help 2'" + ChatColor.WHITE + " to see more commands.");	
					return true;
				}
			} else
				return false;
		} else {
			return false;
		}

	}

}
