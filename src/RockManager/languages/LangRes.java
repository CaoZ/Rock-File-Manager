
package RockManager.languages;

import net.rim.device.api.i18n.ResourceBundle;


public class LangRes implements RockLangResource {

	public static ResourceBundle rockLangRes = ResourceBundle.getBundle(RockLangResource.BUNDLE_ID,
			RockLangResource.BUNDLE_NAME);


	/**
	 * 根据key返回对应于语言的String.
	 * 
	 * @param key
	 * @return
	 */
	public static String get(int key) {

		return rockLangRes.getString(key);
	}

}
