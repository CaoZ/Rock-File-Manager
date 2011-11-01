
package RockManager.ui.screen.propertyScreen;

import java.io.IOException;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import net.rim.device.api.ui.Field;
import RockManager.fileHandler.FileSizeString;
import RockManager.fileList.FileItem;
import RockManager.languages.LangRes;
import RockManager.ui.titledPanel.TitledPanel;
import RockManager.util.ui.LeftRightManager;


public class DiskPropertyScreen extends BasePropertyScreen {

	private FileConnection fconn;


	public DiskPropertyScreen(FileItem file) {

		super(file);

		createConnect();

		addInfoPanel();

		closeConnect();

	}


	private void createConnect() {

		try {
			fconn = (FileConnection) Connector.open(getThisFile().getURL());
		} catch (IOException e) {
			e.printStackTrace();
		}

	}


	private void closeConnect() {

		if (fconn != null) {
			try {
				fconn.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}


	private void addInfoPanel() {

		boolean removeable = false;
		String diskName = getThisFile().getDisplayName();

		if (diskName.equalsIgnoreCase("SDCard")) {
			removeable = true;
		}

		String builtInStorage = LangRes.get(LangRes.BUILT_IN_STORAGE);
		String externalStorage = LangRes.get(LangRes.EXTERNAL_STORAGE);
		String title = (removeable ? externalStorage : builtInStorage) + diskName;

		TitledPanel infoPanel = new TitledPanel(title);
		infoPanel.setPadding(9, 10, 9, 10);

		addInfoToPanel(infoPanel, getLocationArea(), 7);

		addInfoToPanel(infoPanel, getTotalSizeArea(), 7);

		addInfoToPanel(infoPanel, getUsedSizeArea(), 7);

		addInfoToPanel(infoPanel, getFreeSizeArea());

		add(infoPanel);

	}


	private Field getLocationArea() {

		String location = getThisFile().getRawPath();

		LeftRightManager locationArea = new LeftRightManager();

		locationArea.addToLeft(new KeyLabel(LangRes.get(LangRes.LOCATION)));
		locationArea.addToRight(new ValueLabel(location));

		return locationArea;

	}


	private Field getTotalSizeArea() {

		long totalSize = fconn.totalSize();
		String totalSizeString = FileSizeString.getSizeString(totalSize, false, true);

		LeftRightManager totalSizeArea = new LeftRightManager();

		totalSizeArea.addToLeft(new KeyLabel(LangRes.get(LangRes.CAPACITY)));
		totalSizeArea.addToRight(new ValueLabel(totalSizeString));

		return totalSizeArea;

	}


	private Field getUsedSizeArea() {

		long usedSize = fconn.usedSize();
		String usedSizeString = FileSizeString.getSizeString(usedSize, false, true);

		LeftRightManager usedSizeArea = new LeftRightManager();

		usedSizeArea.addToLeft(new KeyLabel(LangRes.get(LangRes.USED_SPACE)));
		usedSizeArea.addToRight(new ValueLabel(usedSizeString));

		return usedSizeArea;

	}


	private Field getFreeSizeArea() {

		long freeSize = fconn.availableSize();
		String freeSizeString = FileSizeString.getSizeString(freeSize, false, true);

		LeftRightManager freeSizeArea = new LeftRightManager();

		freeSizeArea.addToLeft(new KeyLabel(LangRes.get(LangRes.FREE_SPACE)));
		freeSizeArea.addToRight(new ValueLabel(freeSizeString));

		return freeSizeArea;

	}

}
