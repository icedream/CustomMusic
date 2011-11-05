package main.java.de.WegFetZ.CustomMusic;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class PluginProperties extends Properties {
	static final long serialVersionUID = 0L;
	private String fileName;

	public PluginProperties(String file) {
		this.fileName = file;
	}

	public void load() {
		File file = new File(this.fileName);
		if (file.exists()) {
			try {
				load(new FileInputStream(this.fileName));
			} catch (IOException ex) {
				log.debug("loading information from cm.properties", ex);
			}
		}
	}

	public void save(String start) {
		try {
			store(new FileOutputStream(this.fileName), start);
		} catch (IOException ex) {
			log.debug("writing information to cm.properties", ex);
		}
	}
	
	public Boolean getBoolean(String key, boolean value) {
		if (containsKey(key)) {
			return Boolean.valueOf(getProperty(key)); // get a string value
														// from the property file
		}
		put(key, String.valueOf(value));
		return value;
	}
	
	public Boolean getBooleanAndReplaceKey(String oldKey, String key, boolean value) {
		if (containsKey(key)) {
			return Boolean.valueOf(getProperty(key)); // get a string value
														// from the property file
		} else if(containsKey(oldKey)) {
			value = Boolean.valueOf(getProperty(oldKey));
			remove(oldKey); //remove the old key and value
		}
		put(key, String.valueOf(value));
		return value;
	}

	public int getInteger(String key, int value) {
		if (containsKey(key)) {
			return Integer.parseInt(getProperty(key));
		}
		put(key, String.valueOf(value));
		return value;
	}

	public int getIntegerAndReplaceKey(String oldKey, String newKey, int value) {
		if (containsKey(newKey)) {
			return Integer.parseInt(getProperty(newKey)); // get string value
														// from the property
														// file
		} else if(containsKey(oldKey)) {
			value = Integer.parseInt(getProperty(oldKey));
			remove(oldKey); //remove the old key and value
		}
		put(newKey, String.valueOf(value)); //add the new key and the value
		return value;
	}
	
	public String getString(String key, String value) {
		if (containsKey(key)) {
			return String.valueOf(getProperty(key));
		}
		put(key, String.valueOf(value));
		return value;
	}
	

}
