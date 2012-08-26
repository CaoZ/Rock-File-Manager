
package RockManager.fileList;

import net.rim.device.api.io.file.FileSystemJournal;
import net.rim.device.api.io.file.FileSystemJournalEntry;
import net.rim.device.api.io.file.FileSystemJournalListener;
import net.rim.device.api.system.Application;
import RockManager.util.UtilCommon;


public class FileJournalListener implements FileSystemJournalListener {

	private String focusOnPath;

	private long lastUSN;

	private FileListField fileList;

	private static final String FILE_PROTOCOL = "file://";


	public FileJournalListener(FileListField fileList) {

		this.fileList = fileList;

		setFocusOnPath(fileList.getFolderPathTotalURL());

		Application.getApplication().addFileSystemJournalListener(this);

		lastUSN = FileSystemJournal.getNextUSN() - 1;

	}


	/**
	 * 从系统移除已注册的JournalListener.
	 */
	public void unRegister() {

		Application.getApplication().removeFileSystemJournalListener(this);

	}


	/**
	 * 设置要关注的文件夹地址。
	 */
	public synchronized void setFocusOnPath(String url) {

		if (url == null || !url.startsWith(FILE_PROTOCOL)) {
			focusOnPath = "";
			return;
		}

		focusOnPath = UtilCommon.replaceString(url, FILE_PROTOCOL, "");

	}


	public synchronized void fileJournalChanged() {

		long endUSN = FileSystemJournal.getNextUSN() - 1;

		for (long thisUSN = lastUSN + 1; thisUSN <= endUSN; thisUSN++) {

			FileSystemJournalEntry entry = FileSystemJournal.getEntry(thisUSN);
			if (entry == null) {
				break;
			}

			String oldPath = entry.getOldPath();
			String path = entry.getPath();

			String oldParentDir = UtilCommon.getParentDir(oldPath);
			String parentDir = UtilCommon.getParentDir(path);

			switch (entry.getEvent()) {

				case FileSystemJournalEntry.FILE_RENAMED:
					// 文件被更名或移动。
					// 添加到此文件夹？从此文件夹移出？只是子文件更名？本文件夹被更名了？祖先文件夹被更名了？

					if (oldPath.equals(focusOnPath)) {
						// 本文件夹更名了。
						folderNameChanged(entry);
					} else if (oldParentDir.equals(focusOnPath) && parentDir.equals(focusOnPath)) {
						// 子文件或文件夹更名了。
						fileList.refresh();
					} else if (oldParentDir.equals(focusOnPath)) {
						// 从此文件夹移出(相当于从此文件夹删除)。
						fileList.refresh();
					} else if (parentDir.equals(focusOnPath)) {
						// 移入此文件夹了。
						fileList.refresh();
					} else if (focusOnPath.startsWith(oldPath)) {
						// 祖先文件夹更名了/位置移动了。
						ancestorFolderNameChanged(entry);
					}

					break;

				case FileSystemJournalEntry.FILE_ADDED:
					// 添加了文件。
					// 添加到此文件夹？

					if (parentDir.equals(focusOnPath)) {
						// 添加到了此文件夹。
						fileList.refresh();
					}

					break;

				case FileSystemJournalEntry.FILE_DELETED:
					// 文件被删除。
					// 本文件夹被删除？子文件夹或文件被删除？祖先文件夹被删除？

					if (path.equals(focusOnPath)) {
						// 本文件夹被删除了。
						fileList.doReturnToParent();
					} else if (parentDir.equals(focusOnPath)) {
						// 子文件或文件夹被删除了。
						fileList.refresh();
					} else if (focusOnPath.startsWith(path)) {
						// 祖先文件夹被删除了。
						ancestorFolderDeleted(entry);
					}

					break;

				case FileSystemJournalEntry.FILE_CHANGED:
					// 文件大小改变，一般是文件大小调整了。
					// 是否是文件？是否在此文件夹中？

					if (parentDir.equals(focusOnPath)) {
						// 子文件大小可能改变了。
						fileList.refresh();
					}

					break;

			}

		}

		lastUSN = endUSN;

	}


	/**
	 * 本文件夹更名了。
	 */
	private void folderNameChanged(FileSystemJournalEntry entry) {

		fileList.logPosition(true);
		fileList.setDirPath(FILE_PROTOCOL + entry.getPath());
		fileList.restoreInnerPosition();

	}


	/**
	 * 祖先文件夹更名了。
	 */
	private void ancestorFolderNameChanged(FileSystemJournalEntry entry) {

		fileList.logPosition(true);
		fileList.setDirPath(FILE_PROTOCOL + entry.getPath());
		fileList.restoreInnerPosition();
		fileList.clearPositionData();

	}


	/**
	 * 祖先文件夹被删除了。
	 */
	private void ancestorFolderDeleted(FileSystemJournalEntry entry) {

		String upperPath = FILE_PROTOCOL + UtilCommon.getParentDir(entry.getPath());
		fileList.setDirPath(upperPath);
		fileList.clearPositionData();

	}

}
