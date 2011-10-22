
package RockManager.fileHandler.filePopup.operationPopup;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import net.rim.device.api.ui.text.FilenameTextFilter;
import RockManager.fileList.FileItem;
import RockManager.fileList.FileListField;
import RockManager.languages.LangRes;
import RockManager.ui.oneLineInputField.FileNameInputField;
import RockManager.util.UtilCommon;


public class FolderCreatePopup extends FileOperationPopup {

	public FolderCreatePopup(FileListField fileListField) {

		setParentFileList(fileListField);
		setTitle(LangRes.getString(LangRes.NEW_FOLDER_TITLE));

		setInputFilter(new FilenameTextFilter());
		setAutoSelectLevel(FileNameInputField.LEVEL_ALL);

	}


	protected void doOperation() {

		String folderName = getInputedText();

		boolean createSucceed = create(folderName);
		if (createSucceed) {
			close();
		} else {
			focusInputField();
		}

	}


	private boolean create(String folderName) {

		FileConnection fconn = null;
		String fullPath = getParentFileList().getFolderPathURL() + UtilCommon.toURLForm(folderName) + '/';
		try {
			fconn = (FileConnection) Connector.open(fullPath);
			fconn.mkdir();

			getParentFileList().setItemToFocus(folderName, FileItem.TYPE_DIR);
			return true;
		} catch (Exception e) {
			UtilCommon.trace(LangRes.getString(LangRes.FAILED_TO_CREATE_NEW_FOLDER) + e.getMessage());
			return false;
		} finally {
			if (fconn != null) {
				try {
					fconn.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

	}

}
