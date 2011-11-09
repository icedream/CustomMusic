package main.java.de.WegFetZ.CustomMusic;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class Calculations {

	public static void boxInRange(Player player, Location pos) {
				//check if player is in range of a box and start/stop playing or change the volume
		
		if (GlobalData.box_count > 0) {
			for (int i = 0; i < GlobalData.box_count; i++) { // check if player
																// in range of
																// any box
				float dist = Calculations.distancecalc(GlobalData.box_aposz[i], GlobalData.box_aposx[i], GlobalData.box_aposy[i], (float) pos.getZ(), (float) pos.getX(), (float) pos.getY());
				String box = GlobalData.box_aowner[i];
				int number = GlobalData.box_anumber[i];
				int priority = GlobalData.box_aprior[i];
				
				if ((int) dist <= GlobalData.box_arange[i] && GlobalData.box_aworld[i].equalsIgnoreCase(pos.getWorld().toString())) { //is in range
					float volume = (float) (100 - (((float) 100 / GlobalData.box_arange[i])) * dist); //calculate volume
					if (GlobalData.CMVolume.containsKey(player.getName().toLowerCase()))
						volume = (volume/100) * (float) GlobalData.CMVolume.get(player.getName().toLowerCase());
					if (volume > 0 && !ignoreBox(player, box, number)) { //not on ignorelist
						Mp3PlayerHandler.startPlaying(player, box, String.valueOf((volume)), String.valueOf(number), priority, "box"); //start playing or only change volume
					}
				} else { //not in range
					if (Mp3PlayerHandler.isPlaying(player, box, String.valueOf(number))) {
						Mp3PlayerHandler.stopPlaying(player, box, String.valueOf(number)); //stop playing
					}
				}
			}
		}
	}

	public static void inArea(Player player, Location pos) {
					//check if player is inside an area and start/stop playing or change the volume
		
		if (GlobalData.area_count > 0) {
			for (int i = 0; i < GlobalData.area_count; i++) { // check if player
															  // in an area
				String area = GlobalData.area_aowner[i];
				String number = GlobalData.area_anumber[i];												
				
				if (GlobalData.area_aworld[i].equalsIgnoreCase(pos.getWorld().toString())) {
					
					float dist = Calculations.distToAreaWall(GlobalData.area_afadeoutRange[i], GlobalData.area_aposx0[i], GlobalData.area_aposy0[i], GlobalData.area_aposz0[i], GlobalData.area_aposx1[i], GlobalData.area_aposy1[i], GlobalData.area_aposz1[i], (float) pos.getX(), (float) pos.getY(), (float) pos.getZ());		
					//==> check if player is in the area and calculate distance to the border if he is
					
					int priority = GlobalData.area_aprior[i];
					
					if ( dist >= 0 ) { //player in area
						float volume;
						if ((int) dist == 0) //no need to fade out he volume
							volume = 100;
						else
							volume = (float) (100 - ((float) 100 / GlobalData.area_afadeoutRange[i]) * dist); //calculate the volume
						
						if (GlobalData.CMVolume.containsKey(player.getName().toLowerCase()))
							volume = (volume/100) * (float) GlobalData.CMVolume.get(player.getName().toLowerCase());
						if (volume > 0 && !ignoreArea(player, area, number)) //not on ignorelist
							Mp3PlayerHandler.startPlaying(player, area, String.valueOf((volume)), number, priority, "area"); //start playing music or only change the volume
					} else {
						if (Mp3PlayerHandler.isPlaying(player, area, number)) 
							Mp3PlayerHandler.stopPlaying(player, area, number); //stop playing music
					}
				} else {
					if (Mp3PlayerHandler.isPlaying(player, area, number)) 
						Mp3PlayerHandler.stopPlaying(player, area, number);  //stop playing music
				}
			}
		}
	}
	
	public static void world(Player player, Location pos) {
		if (GlobalData.world_count > 0) {
			String world = pos.getWorld().getName();
			
			for (int i =0; i< GlobalData.world_count; i++) {
				
				String number = "w" + String.valueOf(i);
				
				if (GlobalData.world_aworld[i].equalsIgnoreCase(world)) {		
					int priority =  GlobalData.world_aprior[i];
					float volume =  (float) GlobalData.world_avolume[i];
					
					if (GlobalData.CMVolume.containsKey(player.getName().toLowerCase()))
						volume = (volume/100) * (float) GlobalData.CMVolume.get(player.getName().toLowerCase());
					if (volume > 0 && !ignoreWorld(player, world)) //not on ignorelist
						Mp3PlayerHandler.startPlaying(player, GlobalData.world_aowner[i], String.valueOf((volume)), number, priority, "world"); //start playing music
				} else if (Mp3PlayerHandler.isPlaying(player, GlobalData.world_aowner[i], number))
						Mp3PlayerHandler.stopPlaying(player, GlobalData.world_aowner[i], number); //stop playing music
				}
			}
	}
	
	
	public static void biome(Player player, Location pos) {
		if (GlobalData.biome_count > 0) {
			
			String biome = pos.getBlock().getBiome().toString();
			
			for (int i =0; i< GlobalData.biome_count; i++) {
				
				String number = "bio" + String.valueOf(i);
				
				if (GlobalData.biome_abiome[i].equalsIgnoreCase(biome)) {
			
					int priority =  GlobalData.biome_aprior[i];
					float volume =  (float) GlobalData.biome_avolume[i];
					
					if (GlobalData.CMVolume.containsKey(player.getName().toLowerCase()))
						volume = (volume/100) * (float) GlobalData.CMVolume.get(player.getName().toLowerCase());
					if (volume > 0 && !ignoreBiome(player, biome)) //not on ignorelis
						Mp3PlayerHandler.startPlaying(player, GlobalData.biome_aowner[i], String.valueOf((volume)), number, priority, "biome"); //start playing music
				} else if (Mp3PlayerHandler.isPlaying(player, GlobalData.biome_aowner[i], number)) 
						Mp3PlayerHandler.stopPlaying(player, GlobalData.biome_aowner[i], number); //stop playing music
				}
			}
	}
	
	private static float distToAreaWall(Integer fadeoutRange, Float x0, Float y0, Float z0, Float x1, Float y1, Float z1, float x, float y, float z) {
									//get distance to border of an area
			
		float minx = Math.min(x0, x1)-1;
		float miny = Math.min(y0, y1)-1;
		float minz = Math.min(z0, z1)-1;
		float maxx = Math.max(x0, x1)+1;
		float maxy = Math.max(y0, y1)+1;
		float maxz = Math.max(z0, z1)+1;
		//get minimum and maximum coordinates
		
		if (x>minx && y>miny && z>minz && x<maxx && y<maxy && z<maxz ){ //player inside an area
			
			float distx = Math.min(Math.abs(minx-x),Math.abs(maxx-x));
			float disty = Math.min(Math.abs(miny-y),Math.abs(maxy-y));
			float distz = Math.min(Math.abs(minz-z),Math.abs(maxz-z));
			//calculate distance to borders
			
			float min_dist = Math.min(Math.min(distx,disty),Math.min(distz,Math.min(distx,disty))); //get minimum distance
			
			if (min_dist < fadeoutRange) //need to fade the volume
				return (float) fadeoutRange-min_dist; //the further the border the lower the return value
			else // no need to fade the volume
				return 0;
		}
		return -1;	// player not inside an area
	}

	public static float distancecalc(float z1, float x1, float y1, float z2, float x2, float y2) { // calculate
																									// distance
																									// between
																									// 2
																									// locations
		float dz = z2 - z1;
		float dx = x2 - x1;
		float dy = y2 - y1;
		float dist = (float) (Math.sqrt(dz * dz + dx * dx + dy * dy));
		
		return dist;
	}

	public static String areaFree(float x0, float y0, float z0, float x1, float y1, float z1, String world) { 
															//check if a box or an area is in range of a new area
		
		float stat_min_x = Math.min(x0, x1)-1;
		float stat_min_y = Math.min(y0, y1)-1;
		float stat_min_z = Math.min(z0, z1)-1;
		
		float stat_max_x = Math.max(x0, x1)+1;
		float stat_max_y = Math.max(y0, y1)+1;
		float stat_max_z = Math.max(z0, z1)+1;
		//==> get the minimum and maximum coordinates of the new area
		
		for (int i = 0; i<GlobalData.box_count;i++) { //for each box
			
			if (GlobalData.box_aworld[i].equalsIgnoreCase(world)) {
				float x = GlobalData.box_aposx[i];
				float y = GlobalData.box_aposy[i];
				float z = GlobalData.box_aposz[i];
				int range = GlobalData.box_arange[i];
				//get coordinates of box
				
				if ((x+range)>stat_min_x && (y+range)>stat_min_y && (z+range)>stat_min_z && (x-range)<stat_max_x && (y-range)<stat_max_y && (z-range)<stat_max_z )
																								//box is inside the area
					return "box: " + GlobalData.box_aowner[i] + " " + GlobalData.box_anumber[i];	//the boxname and the boxnumber
			}
		}
		
		
		for (int i = 0; i<GlobalData.area_count;i++) { ////for each area check if new area overlaps with an existing area
			
			if (GlobalData.area_aworld[i].equalsIgnoreCase(world)) {
				float minx = Math.min(GlobalData.area_aposx0[i], GlobalData.area_aposx1[i]);
				float miny = Math.min(GlobalData.area_aposy0[i], GlobalData.area_aposy1[i]);
				float minz = Math.min(GlobalData.area_aposz0[i], GlobalData.area_aposz1[i]);
				float maxx = Math.max(GlobalData.area_aposx0[i], GlobalData.area_aposx1[i]);
				float maxy = Math.max(GlobalData.area_aposy0[i], GlobalData.area_aposy1[i]);
				float maxz = Math.max(GlobalData.area_aposz0[i], GlobalData.area_aposz1[i]);
				//==> get minimum and maximum coordinates of an area
				
				if ((stat_min_x>minx && stat_min_y>miny && stat_min_z>minz && stat_min_x<maxx && stat_min_y<maxy && stat_min_z<maxz) 
						|| (stat_max_x>minx && stat_min_y>miny && stat_min_z>minz && stat_max_x<maxx && stat_min_y<maxy && stat_min_z<maxz) 
						|| (stat_max_x>minx && stat_max_y>miny && stat_min_z>minz && stat_max_x<maxx && stat_max_y<maxy && stat_min_z<maxz) 
						|| (stat_min_x>minx && stat_max_y>miny && stat_min_z>minz && stat_min_x<maxx && stat_max_y<maxy && stat_min_z<maxz)
						|| (stat_min_x>minx && stat_min_y>miny && stat_max_z>minz && stat_min_x<maxx && stat_min_y<maxy && stat_max_z<maxz)
						|| (stat_max_x>minx && stat_min_y>miny && stat_max_z>minz && stat_max_x<maxx && stat_min_y<maxy && stat_max_z<maxz)
						|| (stat_max_x>minx && stat_max_y>miny && stat_max_z>minz && stat_max_x<maxx && stat_max_y<maxy && stat_max_z<maxz)
						|| (stat_min_x>minx && stat_max_y>miny && stat_max_z>minz && stat_min_x<maxx && stat_max_y<maxy && stat_max_z<maxz))
					//check if any corner of new area is inside the current area
					return "area: " + GlobalData.area_aowner[i] + " " + GlobalData.area_anumber[i]; //area's name and number
			}
			
		}
		
		return "area is free"; //nothing in range
	}
	
	public static String boxAreaFree(float x, float y, float z, String world, int range) {
										//check if a box or an area is in range of a new box
		
		for (int i = 0; i < GlobalData.box_count; i++) { //for each box
			
			float dist = Calculations.distancecalc(GlobalData.box_aposz[i], GlobalData.box_aposx[i], GlobalData.box_aposy[i], z, x, y); //distance of box to new box
			
			if (dist < (GlobalData.box_arange[i] + range) + 2 && GlobalData.box_aworld[i].equalsIgnoreCase(world)) //box in range of other box
				return "box: " + GlobalData.box_aowner[i] + " " + GlobalData.box_anumber[i]; // box name and number
		}
		
		for (int i = 0; i<GlobalData.area_count;i++) { //for each area
			
			if (GlobalData.area_aworld[i].equalsIgnoreCase(world)) {
				float minx = Math.min(GlobalData.area_aposx0[i], GlobalData.area_aposx1[i])-1;
				float miny = Math.min(GlobalData.area_aposy0[i], GlobalData.area_aposy1[i])-1;
				float minz = Math.min(GlobalData.area_aposz0[i], GlobalData.area_aposz1[i])-1;
				float maxx = Math.max(GlobalData.area_aposx0[i], GlobalData.area_aposx1[i])+1;
				float maxy = Math.max(GlobalData.area_aposy0[i], GlobalData.area_aposy1[i])+1;
				float maxz = Math.max(GlobalData.area_aposz0[i], GlobalData.area_aposz1[i])+1;
				//==> get minimum and maximum coordinates of the area
				
				if ((x+range)>minx && (y+range)>miny && (z+range)>minz && (x-range)<maxx && (y-range)<maxy && (z-range)<maxz ) //new box in range of an area
					return "area: " + GlobalData.area_aowner[i] + " " + GlobalData.area_anumber[i];	 // area name and number
			}
		}
		
		return "area is free"; //nothing in range
	}

	public static String status(float x, float y, float z, String world) {
			//check if point in range of a box or area
		
		for (int i = 0; i < GlobalData.box_count; i++) {  //for each box
			
			float dist = Calculations.distancecalc(GlobalData.box_aposz[i], GlobalData.box_aposx[i], GlobalData.box_aposy[i], z, x, y); //get distance of box to point
			
			if (dist < GlobalData.box_arange[i] && GlobalData.box_aworld[i].equalsIgnoreCase(world)) //point in range of box
				return "box: " + GlobalData.box_aowner[i] + " " + GlobalData.box_anumber[i]; //box name and number
		}
		
		for (int i = 0; i<GlobalData.area_count;i++) { //for each area
			
			if (GlobalData.area_aworld[i].equalsIgnoreCase(world)) {
				float minx = Math.min(GlobalData.area_aposx0[i], GlobalData.area_aposx1[i])-1;
				float miny = Math.min(GlobalData.area_aposy0[i], GlobalData.area_aposy1[i])-1;
				float minz = Math.min(GlobalData.area_aposz0[i], GlobalData.area_aposz1[i])-1;
				float maxx = Math.max(GlobalData.area_aposx0[i], GlobalData.area_aposx1[i])+1;
				float maxy = Math.max(GlobalData.area_aposy0[i], GlobalData.area_aposy1[i])+1;
				float maxz = Math.max(GlobalData.area_aposz0[i], GlobalData.area_aposz1[i])+1;
				//==> get min and max coordinates for the area
				
				if ( x>minx && y>miny && z>minz && x<maxx && y<maxy && z<maxz ) // check if point inside an area
					return "area: " + GlobalData.area_aowner[i] + " " + GlobalData.area_anumber[i]; // area name and number
			}
		}
		
		return "area is free"; //nothing in range
	}
	
	public static boolean ignoreArea(Player player, String area, String number){
		for (int i = 0;i<GlobalData.area_lignoByPlayer.size();i++) {
			if (GlobalData.area_lignoByPlayer.get(i).equalsIgnoreCase(String.valueOf(player)) && ("craftplayer{name=" + GlobalData.area_lignoOwner.get(i) + "}").equalsIgnoreCase(area) && GlobalData.area_lignoAreaNumber.get(i).equalsIgnoreCase(String.valueOf(number)))
				//area is in ignorelist
				return true;
		}
		
		return false;
	}
	
	
	public static boolean ignoreBox(Player player, String box, int number) {
		
		for (int i = 0;i<GlobalData.box_lignoByPlayer.size();i++) {
			if (GlobalData.box_lignoByPlayer.get(i).equalsIgnoreCase(String.valueOf(player)) && ("craftplayer{name=" + GlobalData.box_lignoOwner.get(i) + "}").equalsIgnoreCase(box) && GlobalData.box_lignoBoxNumber.get(i) == number)
				//box is in ignorelist
				return true;
		}
		
		return false;
		
	}
	
	public static boolean ignoreWorld(Player player, String world) {
		for (int i = 0;i<GlobalData.world_lignoByPlayer.size();i++) {
			if (GlobalData.world_lignoByPlayer.get(i).equalsIgnoreCase(String.valueOf(player)) && GlobalData.world_lignoWorld.get(i).equalsIgnoreCase(world))
				//world is in ignorelist
				return true;
		}
		
		return false;
	}

	public static boolean ignoreBiome(Player player, String biome) {
		for (int i = 0;i<GlobalData.biome_lignoByPlayer.size();i++) {
			if (GlobalData.biome_lignoByPlayer.get(i).equalsIgnoreCase(String.valueOf(player)) && GlobalData.biome_lignoBiome.get(i).equalsIgnoreCase(biome))
				//world is in ignorelist
				return true;
		}
		
		return false;
	}
}
