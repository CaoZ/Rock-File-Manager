
package RockManager.archive;

import java.io.IOException;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.util.SimpleSortingVector;
import RockManager.archive.indicator.ArchiveLoadingIndicator;
import RockManager.fileList.FileItem;
import RockManager.fileList.FileListField;
import RockManager.languages.LangRes;
import RockManager.util.UtilCommon;


public class ArchiveListField extends FileListField {

	private ArchiveFile archiveFile;


	public ArchiveListField(String fileURL) throws IOException {

		super();
		archiveFile = new ArchiveFile(fileURL);
		// 设置地地址栏显示地址为压缩文件名称。
		addressBar.setAddress(archiveFile.getName(true));
		setClipboardAllowed(false);

	}


	/**
	 * 初始化一个压缩文件。这可能瞬间完成，也可能花费数秒时间。
	 * 
	 * @throws IOException
	 */
	public void initialArchive(ArchiveLoadingIndicator indicator) throws IOException {

		try {
			archiveFile.initialArchive(indicator);
		} catch (IOException e) {
			// 初始化失败
			throw e;
		}

		// 设置初始显示路径为压缩文件下根目录。
		// 因为此方法可能在一个线程中被调用，所以需获得eventLock
		synchronized (UiApplication.getEventLock()) {
			setDirPath("");
		}

	}


	/**
	 * 返回压缩文件所在的文件夹地址。
	 */
	public String getParentPath() {

		return archiveFile.getParentPath();
	}


	/**
	 * 返回压缩文件名称。
	 * 
	 * @param withSuffix
	 * @return
	 */
	public String getName(boolean withSuffix) {

		return archiveFile.getName(withSuffix);
	}


	protected void setDirPath(String newPath) {

		String lastPath = getFolderPath();
		archiveFile.setSubDirPath(newPath);

		// 需在listFiles()之前设置folderPath, 因为listFiles()中的arrangeFiles()可能要用到此数据。
		folderPath = newPath;
		// ArchiveListField不需要folderPathURL就可以工作，但一些从FileListField继承的方法暂时需要此数据，如"刷新"。
		folderPathTotalURL = newPath;

		int oldSize = getSize();

		FileItem[] files = listFiles();
		setOriginData(files);
		set(files);

		// 设置该使哪行具有焦点状态。
		setFocusIndex(false, newPath, lastPath, files);

		// 设置地址栏显示的地址。
		String displayPath = archiveFile.getDisplayPath();
		addressBar.setAddress(displayPath);

		resetSearchData();

		checkIfEmpty();

		if (oldSize == getSize()) {
			// 手动重绘，使滚动条出现。
			screenHeightChangeNotify(SCREEN_HEIGHT_NOT_CHANGED);
		}

	}


	/**
	 * 返回文件打开是否较耗时。
	 * 
	 * @return
	 */
	public boolean isTooBig() {

		return archiveFile.isTooBig();
	}


	protected FileItem[] listFiles() {

		ArchiveEntry[] files = null;

		files = archiveFile.getFiles();

		SimpleSortingVector fileVector = new SimpleSortingVector();

		for (int i = 0; i < files.length; i++) {

			ArchiveEntry thisEntry = files[i];
			int type;
			boolean isDir = thisEntry.isDir();

			if (isDir) {
				type = FileItem.TYPE_DIR_IN_ARCHIVE;
			} else {
				type = FileItem.TYPE_FILE_IN_ARCHIVE;
			}

			FileItem thisItem = new FileItem(files[i].getName(), type);
			thisItem.setOriginArchiveEntry(thisEntry);

			if (isDir == false) {
				// 是文件，为fileItem设置大小。
				long fileSize = thisEntry.getFileSize();
				thisItem.setSize(fileSize);
			}

			fileVector.addElement(thisItem);

		}

		FileItem[] list = arrangeFiles(fileVector);

		return list;

	}


	protected boolean navigationClick(int status, int time) {

		if (isEmpty()) {
			return super.navigationClick(status, time);
		}

		FileItem thisItem = getThisItem();

		switch (thisItem.getType()) {

			case FileItem.TYPE_RETURN:
				doReturnToParent();
				return true;

			case FileItem.TYPE_DIR_IN_ARCHIVE:
				doEnterThisDir();
				return true;

			case FileItem.TYPE_FILE_IN_ARCHIVE:
				return false;

		}

		return super.navigationClick(status, time);

	}


	/**
	 * 进入子文件夹。
	 */
	protected void doEnterThisDir() {

		logPosition(false);
		FileItem thisItem = getThisItem();
		// 与父类不同的是它从不更新地址栏图标，因为我们总想让它显示一个压缩文件的图标。
		String newPath = getFolderPath() + thisItem.getRawPath();
		setDirPath(newPath);
	}


	/**
	 * 返回父目录。
	 */
	protected void doReturnToParent() {

		String newPath = UtilCommon.getParentDir(getFolderPath());
		setDirPath(newPath);
	}


	public ArchiveFile getArchiveFile() {

		return archiveFile;
	}


	/**
	 * 关闭压缩文件的流，释放文件连接。 对于zip文件，需关闭输入流及文件连接。
	 * 对于rar文件，只需调用它的close方法，它会自动关闭流和文件连接，并完成其他清理工作。
	 */
	public void closeArchiveFile() throws IOException {

		archiveFile.close();
	}


	protected String getNoFileFindString() {

		return LangRes.getString(LangRes.EMPTY_ARCHIVE_FILE);
	}

}
