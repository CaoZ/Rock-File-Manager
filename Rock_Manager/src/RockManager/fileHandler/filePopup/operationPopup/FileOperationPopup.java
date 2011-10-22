
package RockManager.fileHandler.filePopup.operationPopup;

import net.rim.device.api.ui.text.TextFilter;
import RockManager.fileHandler.filePopup.BaseFilePopup;
import RockManager.fileList.FileListField;
import RockManager.ui.oneLineInputField.FileNameInputField;


public abstract class FileOperationPopup extends BaseFilePopup {

	private FileListField parentFileList;

	private FileNameInputField nameInputField;


	public FileOperationPopup() {

		nameInputField = new FileNameInputField();
		setInputField(nameInputField);

	}


	/**
	 * 设置inputField的TextFilter.
	 * 
	 * @param filter
	 */
	public void setInputFilter(TextFilter filter) {

		nameInputField.setFilter(filter);
	}


	/**
	 * 设置AutoSelect的级别（one of FileNamePathInputField.LEVEL_NONE,
	 * LEVEL_NAME_ONLY, LEVEL_ALL）。
	 * 
	 * @param level
	 */
	public void setAutoSelectLevel(int level) {

		nameInputField.setAutoSelectLevel(level);
	}


	/**
	 * 设置父FileListField。
	 * 
	 * @param parentFileList
	 */
	public void setParentFileList(FileListField parentFileList) {

		this.parentFileList = parentFileList;
	}


	/**
	 * 获取父FileListField(如果设置了的话).
	 * 
	 * @return
	 */
	public FileListField getParentFileList() {

		return parentFileList;
	}

}
