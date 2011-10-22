
package RockManager.fileList;

import javax.microedition.io.file.FileSystemListener;
import javax.microedition.io.file.FileSystemRegistry;
import net.rim.device.api.ui.Screen;
import RockManager.archive.ArchiveListField;
import RockManager.fileList.filePicker.FilePicker;
import RockManager.ui.screen.fileScreen.FileScreen;


public class FileRootChangeListener implements FileSystemListener {

	private FileListField listField;


	public FileRootChangeListener(FileListField fileListField) {

		listField = fileListField;
		FileSystemRegistry.addFileSystemListener(this);

	}


	public void rootChanged(int state, String rootName) {

		String rootPath = "file:///" + rootName;
		String folderPath;

		if (listField instanceof ArchiveListField) {
			folderPath = ((ArchiveListField) listField).getParentPath();
		} else {
			folderPath = listField.getFolderPath();
		}

		if (state == FileSystemListener.ROOT_REMOVED) {

			if (listField.isDiskList()) {
				// 列出各盘的diskList.
				listField.refresh();
			} else if (folderPath.startsWith(rootPath)) {
				Screen screen = listField.getScreen();
				if (screen instanceof FileScreen) {
					// 关闭FileScreen or ArchiveFileScreen
					screen.close();
				} else if (screen instanceof FilePicker) {
					// 转到根目录
					listField.setDirPath(null);
				}
			}

		} else if (state == FileSystemListener.ROOT_ADDED) {

			if (folderPath == null) {
				listField.refresh();
			}

		}

	}


	public void unRegister() {

		FileSystemRegistry.removeFileSystemListener(this);

	}

}
