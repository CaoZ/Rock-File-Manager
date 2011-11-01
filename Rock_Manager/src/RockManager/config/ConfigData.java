
package RockManager.config;

import java.util.Vector;


public class ConfigData {

	public static ConfigElement SHOW_HIDDEN_FILE;

	public static ConfigElement ADD_RETURN_ITEM;

	public static ConfigElement ANIMATION_EFFECT;

	private static ConfigElement[] CONFIGS;

	public static String configKey = "Rock File Manager Main Config V1";

	private static ConfigHashtable DATA;

	static {
		buildElements();
		readConfig();
	}


	private static void buildElements() {

		Vector configVector = new Vector();

		SHOW_HIDDEN_FILE = new ConfigElement("SHOW_HIDDEN_FILE", Boolean.TRUE);
		configVector.addElement(SHOW_HIDDEN_FILE);

		ADD_RETURN_ITEM = new ConfigElement("ADD_RETURN_ITEM", Boolean.TRUE);
		configVector.addElement(ADD_RETURN_ITEM);

		ANIMATION_EFFECT = new ConfigElement("ANIMATION_EFFECT", Boolean.TRUE);
		configVector.addElement(ANIMATION_EFFECT);

		CONFIGS = new ConfigElement[configVector.size()];
		configVector.copyInto(CONFIGS);

	}


	private static void readConfig() {

		DATA = ConfigHashtable.getTable(configKey);
		for (int i = 0; i < CONFIGS.length; i++) {
			DATA.read(CONFIGS[i]);
		}

	}


	public static void savaConfig() {

		for (int i = 0; i < CONFIGS.length; i++) {
			DATA.write(CONFIGS[i]);
		}

	}

}
