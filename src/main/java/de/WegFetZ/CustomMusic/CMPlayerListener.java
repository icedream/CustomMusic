package main.java.de.WegFetZ.CustomMusic;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Handle events for all Player related events
 * 
 * @author WegFetZ
 */
public class CMPlayerListener extends PlayerListener {


	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		if (Permission.permission(player, "cm.init", true)) {
			Mp3PlayerHandler.confirm(player); // try to initialize the audioclient
			
		} else if (GlobalData.CMConnected.containsKey(player.getName().toLowerCase()))
			GlobalData.CMConnected.put(player.getName().toLowerCase(), 2); //set the value to 2 
												//because the player has no permission to initialize
	}

	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		GlobalData.CMUsers.remove(player.getName().toLowerCase());
		GlobalData.CMVolume.remove(player.getName().toLowerCase());
		GlobalData.CMConnected.remove(player.getName().toLowerCase());
		GlobalData.CMPlaying.remove(player.getName().toLowerCase());
		GlobalData.CMUploadPerm.remove(player.getName().toLowerCase());
		GlobalData.CMAreaDefinitions.remove(player);
		
	}

	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		
		if (GlobalData.CMUsers.containsKey(player.getName().toLowerCase())) {
			if (GlobalData.CMConnected.containsKey(player.getName().toLowerCase())) {
				Location pos = event.getTo(); // get position of MoveEvent
				Calculations.boxInRange(player, pos); // check if player is in range of box and start playing music
				Calculations.inArea(player,pos); // check if player is in an area and start playing music
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
}