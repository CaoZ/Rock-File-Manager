
package RockManager.ui.screen.fileScreen;

import net.rim.device.api.ui.container.HorizontalFieldManager;
import RockManager.fileClipboard.FileClipboardIndicator;
import RockManager.fileList.AddressBar;
import RockManager.fileList.FileListField;
import RockManager.util.UtilCommon;


public class AddressAndClipboard extends HorizontalFieldManager {

	private AddressBar addressBar;

	private FileClipboardIndicator clipboardIndicator;


	public AddressAndClipboard(FileListField fileListField) {

		addressBar = fileListField.getAddressBar();
		add(addressBar);

		if (fileListField.isClipboardAllowed()) {
			clipboardIndicator = new FileClipboardIndicator();
			add(clipboardIndicator);
		}

	}


	protected void sublayout(int width, int height) {

		if (clipboardIndicator == null) {
			super.sublayout(width, height);
			return;
		}

		int addressHeight = addressBar.getPreferredHeight();
		int clipboardHeight = clipboardIndicator.getPreferredHeight();
		height = Math.max(addressHeight, clipboardHeight);

		int addressY = UtilCommon.getOffset(height, addressHeight);
		int clipboardY = UtilCommon.getOffset(height, clipboardHeight);

		int clipboardWidth = clipboardIndicator.getPreferredWidth();

		setPositionChild(addressBar, 0, addressY);
		layoutChild(addressBar, width - clipboardWidth, height);

		setPositionChild(clipboardIndicator, width - clipboardWidth, clipboardY);
		layoutChild(clipboardIndicator, clipboardWidth, height);

		setExtent(width, height);

	}
}
