package main.java.de.WegFetZ.CustomMusic.Utils;

import java.util.ArrayList;

public class StringUtil {
	
	public static boolean isInteger(String str) { //checks if a string contains only integer
		for (int i = 0; i < str.length(); i++) {
			if (!Character.isDigit(str.charAt(i)))
				return false;
		}

		return true;
	}
	
	public static int listContains(ArrayList<String> list, String string) {
		//checks if an arraylist contains a certain string
		//1 if string is found, -1 else
		
		for (int i = 0; i < list.size(); i++) {
			String valueString = list.get(i);
			if (valueString.contains(string))
				return i;
		}
		
		return -1;
	}

}
