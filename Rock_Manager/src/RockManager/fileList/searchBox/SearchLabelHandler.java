
package RockManager.fileList.searchBox;

import RockManager.config.ShortCutKeyConfig;
import RockManager.languages.LangRes;
import RockManager.util.CapabilityUtil;
import RockManager.util.KeyUtil;
import RockManager.util.UtilCommon;
import net.rim.device.api.system.capability.DeviceCapability;
import net.rim.device.api.system.capability.DeviceCapabilityListener;


/**
 * 处理在搜索框中应显示什么文字。
 */

public class SearchLabelHandler implements DeviceCapabilityListener {

	private SearchBox searchBox;


	public SearchLabelHandler(SearchBox searchBox) {

		this.searchBox = searchBox;
		setLabel();

		DeviceCapability.addPhysicalKeyboardListener(this);

	}


	private void setLabel() {

		searchBox.setLabelText(getLabel());
	}


	private String getLabel() {

		boolean hasRealKeypad = CapabilityUtil.isPhysicalKeyboardAvailable();

		String label;

		if (hasRealKeypad) {
			char convenientKey = KeyUtil.getDistinctKey(ShortCutKeyConfig.SEARCH);
			String[] pattern = { LangRes.get(LangRes.SEARCH_LABEL_KEY_VERSION), "{1}" };
			label = UtilCommon.replaceString(pattern[0], pattern[1], String.valueOf(convenientKey));
		} else {
			label = LangRes.get(LangRes.SEARCH_LABEL_TOUCH_VERSION);
		}

		return label;

	}


	public void allowedChanged(boolean changedTo) {

	}


	public void availableChanged(boolean changedTo) {

		setLabel();
	}


	public void supportedChanged(boolean changedTo) {

	}

}
