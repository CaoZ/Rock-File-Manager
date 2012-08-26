
package RockManager.fileHandler.filePopup.operationPopup;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import net.rim.device.api.ui.text.FilenameTextFilter;
import RockManager.fileList.FileItem;
import RockManager.fileList.FileListField;
import RockManager.languages.LangRes;
import RockManager.ui.oneLineInputField.FileNameInputField;
import RockManager.util.UtilCommon;


public class FileRenamePopup extends FileOperationPopup {

	private FileItem itemToRename;


	public FileRenamePopup(FileItem fileItem, FileListField fileListField) {

		setParentFileList(fileListField);
		setTitle(LangRes.get(LangRes.RENAME));

		itemToRename = fileItem;

		String fileName = itemToRename.getDisplayName();
		if (itemToRename.isEncrypted()) {
			fileName = UtilCommon.getName(fileName, false); // 不显示最后的".rem"。
		}
		setInputText(fileName);

		setInputFilter(new FilenameTextFilter());

		if (fileItem.isFile()) {
			setAutoSelectLevel(FileNameInputField.LEVEL_NAME_ONLY);
		} else {
			setAutoSelectLevel(FileNameInputField.LEVEL_ALL);
		}

	}


	protected void doOperation() {

		String newFileName = getInputedText();
		String originURL = itemToRename.getURL();

		boolean renameSucceed = rename(originURL, newFileName);
		if (renameSucceed) {
			// 重命名成功，关闭对话框。
			close();
		} else {
			// 重命名失败，使输入框重新获得焦点。
			focusInputField();
		}
	}


	/**
	 * 重命名。
	 * 
	 * @return 是否成功（若跟原文件名一样也算成功）。
	 */
	private boolean rename(String originURL, String newName) {

		String oldName = itemToRename.getDisplayName();
		if (newName.equals(oldName)) {
			return true;
		}

		FileConnection fconn = null;
		try {
			fconn = (FileConnection) Connector.open(originURL);
			fconn.rename(newName);

			String newFileName = fconn.getName(); // 重命名后的实际名称，可能与newName不同。例如，若用户设置了媒体卡加密，重命名后可能会多了".rem"后缀。
			getParentFileList().setItemToFocus(newFileName, itemToRename.getType());
			return true;
		} catch (Exception e) {
			UtilCommon.trace(LangRes.get(LangRes.FAILED_TO_RENAME) + UtilCommon.getErrorMessage(e));
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
