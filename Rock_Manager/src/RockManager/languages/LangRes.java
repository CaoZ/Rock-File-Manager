package RockManager.languages;

import net.rim.device.api.i18n.ResourceBundle;

public class LangRes implements RockLangResource {

	public static ResourceBundle rockLangRes = ResourceBundle.getBundle(RockLangResource.BUNDLE_ID,
			RockLangResource.BUNDLE_NAME);

	public static String getString(int key) {
		return rockLangRes.getString(key);
	}

}
