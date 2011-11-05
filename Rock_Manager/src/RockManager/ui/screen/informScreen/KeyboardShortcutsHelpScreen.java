
package RockManager.ui.screen.informScreen;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import net.rim.device.api.io.LineReader;
import RockManager.languages.LangRes;


public class KeyboardShortcutsHelpScreen extends InformScreen {

	protected void addMainArea() {

		String txtPath = LangRes.get(LangRes.HELP_SHORTCUTS_TXT_PATH);
		InputStream txtIn = getClass().getResourceAsStream(txtPath);

		if (txtIn != null) {

			LineReader reader = new LineReader(txtIn);

			while (true) {
				try {
					String thisLine = new String(reader.readLine(), "UTF-8");
					addLabelField(thisLine);
				} catch (EOFException e) {
					break;
				} catch (IOException e) {
					break;
				}
			}

			try {
				txtIn.close();
			} catch (IOException e) {
			}

		}

	}


	protected String getTitle() {

		String title = LangRes.get(LangRes.TITLE_KEYBOARD_SHORTCUTS);
		return title;

	}

}
