package main.java.de.WegFetZ.CustomMusic;

public class LoadSettings {
	static int defaultRange;
	static int defaultAreaFadeout;
	static int defaultMaxAreaSize;
	static int defaultMaxMp3;
	static int defaultMaxMp3Size;
	static int defaultMaxMidi;
	static int defaultMaxMidiSize;
	static int defaultMaxBoxesPerPlayer;
	static int defaultMaxAreasPerPlayer;
	static String defaultWorld;
	static int ServerPort;
	static String initFailedMsg;
	static int defaultMaxWebradio;
	static boolean ipVerification = false;
	static public boolean WorldMusic = false;
	static public boolean BiomeMusic = false;
	static public boolean debug = false;

	public static void loadMain() {
		String propertiesFile = CustomMusic.maindir + "cm.properties";
		PluginProperties properties = new PluginProperties(propertiesFile);
		properties.load();

		WorldMusic = properties.getBoolean("Enable-World-specific-Music", false);
		BiomeMusic = properties.getBoolean("Enable-Biome-specific-Music", false);
		ipVerification = properties.getBoolean("Verify-IP", true);
		debug = properties.getBooleanAndReplaceKey("Degbug-Mode", "Debug-Mode", false);
		defaultRange = properties.getIntegerAndReplaceKey("Music-range", "Default-Music-Range", 25);
		defaultAreaFadeout = properties.getIntegerAndReplaceKey("Area-Fadeout", "Default-Area-Fadeout", 5);
		defaultMaxAreaSize = properties.getInteger("Default-Maximum-Area-Edge-Length", 100);
		defaultMaxBoxesPerPlayer = properties.getIntegerAndReplaceKey("Maximum-Boxes-per-Player", "Default-Maximum-Boxes-per-Player", 3);
		defaultMaxAreasPerPlayer = properties.getIntegerAndReplaceKey("Maximum-Areas-per-Player", "Default-Maximum-Areas-per-Player", 3);
		defaultMaxMp3 = properties.getIntegerAndReplaceKey("Maximum-MP3-per-Player", "Default-Maximum-MP3-per-Player", 5);
		defaultMaxMp3Size = properties.getIntegerAndReplaceKey("Maximum-MP3-Size-MB", "Default-Maximum-MP3-Size-MB", 15);
		defaultMaxMidi = properties.getIntegerAndReplaceKey("Maximum-Midi-per-Player", "Default-Maximum-Midi-per-Player", 5);
		defaultMaxMidiSize = properties.getIntegerAndReplaceKey("Maximum-Midi-Size-MB", "Default-Maximum-Midi-Size-MB", 3);
		defaultMaxWebradio = properties.getInteger("Default-Maximum-Webradio-Files-per-Player", 5);
		ServerPort = properties.getIntegerAndReplaceKey("Server-Port", "Plugin-Port", 4224);
		initFailedMsg = properties.getString("Login-Initialization-Failed-Msg", "CustomMusic: #!gInitialization failed! Make sure you started the AudioClient!#!w");
		properties.save("===CustomMusic configuration===");

		loadServerProperties();
		
		if (BoxList.loadboxes()) {
			BoxList.addBoxDefaults();
			System.out.println("[CustomMusic] Jukeboxes transferred to default world, range or priority.");
			BoxList.loadboxes();
		}
		if (BoxList.loadareas()) {
			BoxList.addAreaDefaults();
			System.out.println("[CustomMusic] Default fadeout-range added to areas.");
			BoxList.loadareas();
		}
		
		BoxList.loadAreaIgnores();
		BoxList.loadBoxIgnores();
		
		System.out.println("[CustomMusic] " + GlobalData.box_count + " jukeboxe(s) loaded.");
		System.out.println("[CustomMusic] " + GlobalData.area_count + " area(s) loaded.");
		System.out.println("[CustomMusic] " + GlobalData.world_count + " world(s) loaded.");
		System.out.println("[CustomMusic] " + GlobalData.biome_count + " biome(s) loaded.");

	}

	public static void loadServerProperties() {
		String serverPropertiesFile = "server.properties";
		PluginProperties serverProperties = new PluginProperties(serverPropertiesFile);
		serverProperties.load();

		defaultWorld = serverProperties.getString("level-name", "world");
	}

}
