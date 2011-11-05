
package RockManager.ui.screen.informScreen;

import java.io.InputStream;
import net.rim.device.api.io.IOUtilities;
import RockManager.languages.LangRes;
import RockManager.util.IOUtil;


public class KeyboardShortcutsHelpScreen extends InformScreen {

	protected void addMainArea() {

		String helpInfo = null;

		String txtPath = LangRes.get(LangRes.HELP_SHORTCUTS_TXT_PATH);
		InputStream txtIn = getClass().getResourceAsStream(txtPath);
		try {
			helpInfo = new String(IOUtilities.streamToBytes(txtIn), "UTF-8");
		} catch (Exception e) {
		} finally {
			IOUtil.closeStream(txtIn);
		}

		addTextField(helpInfo);

	}


	protected String getTitle() {

		String title = LangRes.get(LangRes.TITLE_KEYBOARD_SHORTCUTS);
		return title;

	}

}
