
package RockManager.fileHandler;

import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.UiApplication;
import RockManager.fileList.FileItem;
import RockManager.fileList.FileListField;
import RockManager.ui.screen.propertyScreen.DiskPropertyScreen;
import RockManager.ui.screen.propertyScreen.FileInArchivePropertyScreen;
import RockManager.ui.screen.propertyScreen.RealFilePropertyScreen;


/**
 * 文件属性相关内容。
 */
public class FileProperty {

	/**
	 * 对于不同类型的内容弹出不同的PropertyScreen.
	 * 
	 * @param parentFileList
	 * @param file
	 */
	public static void showPropertyScreen(FileListField parentFileList, FileItem file) {

		Screen propertyScreen = null;

		if (file.isRealFile()) {
			propertyScreen = new RealFilePropertyScreen(file);
		} else if (file.isDisk()) {
			propertyScreen = new DiskPropertyScreen(file);
		} else if (file.isFileInArchive()) {
			propertyScreen = new FileInArchivePropertyScreen(parentFileList, file);
		}

		if (propertyScreen != null) {
			UiApplication.getUiApplication().pushScreen(propertyScreen);
		}

	}

}
