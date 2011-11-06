package main.java.de.WegFetZ.CustomMusic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class GlobalData {
	
	// ==> the hashmaps
	public final static HashMap<String, String> CMUsers = new HashMap<String, String>();
	public final static HashMap<String, Integer> CMConnected = new HashMap<String, Integer>();
	public final static HashMap<String, Integer> CMVolume = new HashMap<String, Integer>();
	public final static HashMap<String, ArrayList<String>> CMPlaying = new HashMap<String, ArrayList<String>>();
	public final static HashMap<String, String> CMUploadPerm = new HashMap<String, String>();
	public final static HashMap<Integer, String> CMDeleteSongs = new HashMap<Integer, String>();
	public final static HashMap<Player, Location[]> CMAreaDefinitions = new HashMap<Player,Location[]>();
	
	//available biomes
	public final static String[] av_biomes = new String[] {"DESERT","EXTREME_HILLS","FOREST","HELL","ICE_DESERT","OCEAN","PLAINS","RAINFOREST","RIVER","SAVANNA","SEASONAL_FOREST","SHRUBLAND","SKY","SWAMPLAND","TAIGA","TUNDRA"};
	
	// box stuff
	static String[] box_aworld = new String[0];
	static String[] box_aowner = new String[0];
	static String[] box_aSongList = new String[0];
	static Float[] box_aposx = new Float[0];
	static Float[] box_aposy = new Float[0];
	static Float[] box_aposz = new Float[0];
	static Integer[] box_anumber = new Integer[0];
	static Integer[] box_arange = new Integer[0];
	static Integer[] box_aprior = new Integer[0];

	static List<String> box_lworld = new ArrayList<String>();
	static List<String> box_lowner = new ArrayList<String>();
	static List<String> box_lSongList = new ArrayList<String>();
	static List<Float> box_lposx = new ArrayList<Float>();
	static List<Float> box_lposy = new ArrayList<Float>();
	static List<Float> box_lposz = new ArrayList<Float>();
	static List<Integer> box_lnumber = new ArrayList<Integer>();
	static List<Integer> box_lrange = new ArrayList<Integer>();
	static List<Integer> box_lprior = new ArrayList<Integer>();
	
	static List<String> box_lignoByPlayer = new ArrayList<String>();
	static List<String> box_lignoOwner = new ArrayList<String>();
	static List<Integer> box_lignoBoxNumber = new ArrayList<Integer>(); 
	
	static int box_count = 0;
	
	
	//area stuff
	static String[] area_aSongList = new String[0];
	static String[] area_aowner = new String[0];
	static String[] area_anumber = new String[0];
	static String[] area_aworld = new String[0];
	static Integer[] area_aprior = new Integer[0];
	static Integer[] area_afadeoutRange = new Integer[0];
	static Float[] area_aposx0 = new Float[0];
	static Float[] area_aposy0 = new Float[0];
	static Float[] area_aposz0 = new Float[0];
	static Float[] area_aposx1 = new Float[0];
	static Float[] area_aposy1 = new Float[0];
	static Float[] area_aposz1 = new Float[0];
	
	static List<String> area_lSongList = new ArrayList<String>();
	static List<String> area_lowner = new ArrayList<String>();
	static List<String> area_lnumber = new ArrayList<String>();
	static List<String> area_lworld = new ArrayList<String>();
	static List<Integer> area_lprior = new ArrayList<Integer>();
	static List<Integer> area_lfadeoutRange = new ArrayList<Integer>();
	static List<Float> area_lposx0 = new ArrayList<Float>();
	static List<Float> area_lposy0 = new ArrayList<Float>();
	static List<Float> area_lposz0 = new ArrayList<Float>();
	static List<Float> area_lposx1 = new ArrayList<Float>();
	static List<Float> area_lposy1 = new ArrayList<Float>();
	static List<Float> area_lposz1 = new ArrayList<Float>();
	
	
	static List<String> area_lignoByPlayer = new ArrayList<String>();
	static List<String> area_lignoOwner = new ArrayList<String>();
	static List<String> area_lignoAreaNumber = new ArrayList<String>(); 
	
	static int area_count = 0;
	
	
	//world stuff
	static String[] world_aowner = new String[0];
	static String[] world_aworld = new String[0];
	static String[] world_asongList = new String[0];
	static Integer[] world_aprior = new Integer[0];
	static Integer[] world_avolume = new Integer[0];
	
	static List<String> world_lowner = new ArrayList<String>();
	static List<String> world_lworld = new ArrayList<String>();
	static List<String> world_lsongList = new ArrayList<String>();
	static List<Integer> world_lprior = new ArrayList<Integer>();
	static List<Integer> world_lvolume = new ArrayList<Integer>();
	
	static List<String> world_lignoByPlayer = new ArrayList<String>();
	static List<String> world_lignoWorld = new ArrayList<String>();

	static int world_count = 0;
	
	
	//biome stuff
	static String[] biome_aowner = new String[0];
	static String[] biome_abiome = new String[0];
	static String[] biome_asongList = new String[0];
	static Integer[] biome_aprior = new Integer[0];
	static Integer[] biome_avolume = new Integer[0];

	static List<String> biome_lowner = new ArrayList<String>();
	static List<String> biome_lbiome = new ArrayList<String>();
	static List<String> biome_lsongList = new ArrayList<String>();
	static List<Integer> biome_lprior = new ArrayList<Integer>();
	static List<Integer> biome_lvolume = new ArrayList<Integer>();
	
	static List<String> biome_lignoByPlayer = new ArrayList<String>();
	static List<String> biome_lignoBiome = new ArrayList<String>();
	
	static int biome_count = 0;
	
	
	//functions

	public static void createBoxArrays() {
		resizeArray(box_aworld, box_lworld.size());
		resizeArray(box_aowner, box_lowner.size());
		resizeArray(box_aSongList, box_count);
		resizeArray(box_aposx, box_lposx.size());
		resizeArray(box_aposy, box_lposy.size());
		resizeArray(box_aposz, box_lposz.size());
		resizeArray(box_anumber, box_lnumber.size());
		resizeArray(box_arange, box_lrange.size());
		resizeArray(box_aprior, box_lprior.size());
		

		box_aworld = (String[]) box_lworld.toArray(box_aworld);
		box_aowner = (String[]) box_lowner.toArray(box_aowner);
		box_aSongList = (String[]) box_lSongList.toArray(box_aSongList);
		box_aposx = (Float[]) box_lposx.toArray(box_aposx);
		box_aposy = (Float[]) box_lposy.toArray(box_aposy);
		box_aposz = (Float[]) box_lposz.toArray(box_aposz);
		box_anumber = (Integer[]) box_lnumber.toArray(box_anumber);
		box_arange = (Integer[]) box_lrange.toArray(box_arange);
		box_aprior = (Integer[]) box_lprior.toArray(box_aprior);
		
	}

	public static void createAreaArrays() {
		
		resizeArray(area_aSongList, area_count);
		resizeArray(area_aposx0, area_lposx0.size());
		resizeArray(area_aposy0, area_lposx0.size());
		resizeArray(area_aposz0, area_lposx0.size());
		resizeArray(area_aposx1, area_lposx1.size());
		resizeArray(area_aposy1, area_lposx1.size());
		resizeArray(area_aposz1, area_lposx1.size());
		resizeArray(area_anumber, area_lnumber.size());
		resizeArray(area_aowner, area_lowner.size());
		resizeArray(area_aprior, area_lprior.size());
		resizeArray(area_aworld, area_lworld.size());
		resizeArray(area_afadeoutRange, area_lfadeoutRange.size());
		
		
		area_aSongList = (String[]) area_lSongList.toArray(area_aSongList);
		area_aposx0 = (Float[]) area_lposx0.toArray(area_aposx0);
		area_aposy0 = (Float[]) area_lposy0.toArray(area_aposy0);
		area_aposz0 = (Float[]) area_lposz0.toArray(area_aposz0);
		area_aposx1 = (Float[]) area_lposx1.toArray(area_aposx1);
		area_aposy1 = (Float[]) area_lposy1.toArray(area_aposy1);
		area_aposz1 = (Float[]) area_lposz1.toArray(area_aposz1);
		area_anumber = (String[]) area_lnumber.toArray(area_anumber);
		area_aowner = (String[]) area_lowner.toArray(area_aowner);
		area_aprior = (Integer[]) area_lprior.toArray(area_aprior);
		area_afadeoutRange = (Integer[]) area_lfadeoutRange.toArray(area_afadeoutRange);
		area_aworld = (String[]) area_lworld.toArray(area_aworld);
	}
	
	public static void createWorldArrays() {
		resizeArray(world_aowner, world_lowner.size());
		resizeArray(world_aworld, world_lworld.size());
		resizeArray(world_asongList, world_lsongList.size());
		resizeArray(world_avolume, world_lvolume.size());
		resizeArray(world_aprior, world_lprior.size());
		
		world_aowner = (String[]) world_lowner.toArray(world_aowner);
		world_aworld = (String[]) world_lworld.toArray(world_aworld);
		world_asongList = (String[]) world_lsongList.toArray(world_asongList);
		world_avolume = (Integer[]) world_lvolume.toArray(world_avolume);
		world_aprior = (Integer[]) world_lprior.toArray(world_aprior);
	}
	
	public static void createBiomeArrays() {
		resizeArray(biome_aowner, biome_lowner.size());
		resizeArray(biome_abiome, biome_lbiome.size());
		resizeArray(biome_asongList, biome_lsongList.size());
		resizeArray(biome_avolume, biome_lvolume.size());
		resizeArray(biome_aprior, biome_lprior.size());
		
		biome_aowner = (String[]) biome_lowner.toArray(biome_aowner);
		biome_abiome = (String[]) biome_lbiome.toArray(biome_abiome);
		biome_asongList = (String[]) biome_lsongList.toArray(biome_asongList);
		biome_avolume = (Integer[]) biome_lvolume.toArray(biome_avolume);
		biome_aprior = (Integer[]) biome_lprior.toArray(biome_aprior);
	}


	public static void clearBoxArrays() {
		box_lworld.clear();
		box_lowner.clear();
		box_lSongList.clear();
		box_lposx.clear();
		box_lposy.clear();
		box_lposz.clear();
		box_lnumber.clear();
		box_lrange.clear();
		box_lprior.clear();
		

		box_count = 0;

		resizeArray(box_aworld, 0);
		resizeArray(box_aowner, 0);
		resizeArray(box_aSongList, 0);
		resizeArray(box_aposx, 0);
		resizeArray(box_aposy, 0);
		resizeArray(box_aposz, 0);
		resizeArray(box_anumber, 0);
		resizeArray(box_arange, 0);
		resizeArray(box_aprior, 0);
		
	}

	public static void clearAreaArrays() {
		area_lSongList.clear();
		area_lposx0.clear();
		area_lposy0.clear();
		area_lposz0.clear();
		area_lposx1.clear();
		area_lposy1.clear();
		area_lposz1.clear();
		area_lnumber.clear();
		area_lowner.clear();
		area_lprior.clear();
		area_lworld.clear();
		
		
		area_count = 0;
		
		resizeArray(area_aSongList, 0);
		resizeArray(area_aposx0, 0);
		resizeArray(area_aposy0, 0);
		resizeArray(area_aposz0, 0);
		resizeArray(area_aposx1, 0);
		resizeArray(area_aposy1, 0);
		resizeArray(area_aposz1, 0);
		resizeArray(area_anumber, 0);
		resizeArray(area_aowner, 0);
		resizeArray(area_aprior, 0);
		resizeArray(area_aworld, 0);
		resizeArray(area_afadeoutRange, 0);
	}
	
	public static void clearWorldArrays() {
		world_lowner.clear();
		world_lworld.clear();
		world_lsongList.clear();
		world_lvolume.clear();
		world_lprior.clear();

		
		resizeArray(world_aowner, 0);
		resizeArray(world_aworld, 0);
		resizeArray(world_asongList, 0);
		resizeArray(world_avolume, 0);
		resizeArray(world_aprior, 0);
	}
	
	public static void clearBiomeArrays() {
		biome_lowner.clear();
		biome_lbiome.clear();
		biome_lsongList.clear();
		biome_lvolume.clear();
		biome_lprior.clear();

		
		resizeArray(biome_aowner, 0);
		resizeArray(biome_abiome, 0);
		resizeArray(biome_asongList, 0);
		resizeArray(biome_avolume, 0);
		resizeArray(biome_aprior, 0);
	}
	
	public static void clearBoxIgno() {
		box_lignoByPlayer.clear();
		box_lignoOwner.clear();
		box_lignoBoxNumber.clear();
	}
	
	public static void clearAreaIgno() {
		area_lignoByPlayer.clear();
		area_lignoOwner.clear();
		area_lignoAreaNumber.clear();
	}

	public static void clearWorldIgno() {
		world_lignoByPlayer.clear();
		world_lignoWorld.clear();
	}
	
	public static void clearBiomeIgno() {
		biome_lignoByPlayer.clear();
		biome_lignoBiome.clear();
	}
	
	private static Object resizeArray(Object oldArray, int newSize) {
		int oldSize = java.lang.reflect.Array.getLength(oldArray);
		Class<?> elementType = oldArray.getClass().getComponentType();
		Object newArray = java.lang.reflect.Array.newInstance(elementType, newSize);
		int preserveLength = Math.min(oldSize, newSize);
		if (preserveLength > 0)
			System.arraycopy(oldArray, 0, newArray, 0, preserveLength);
		return newArray;
	}
	
}
