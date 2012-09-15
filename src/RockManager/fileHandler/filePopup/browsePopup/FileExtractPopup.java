
package RockManager.fileHandler.filePopup.browsePopup;

import net.rim.device.api.ui.UiApplication;
import RockManager.archive.ArchiveEntry;
import RockManager.archive.ArchiveFile;
import RockManager.archive.ArchiveListField;
import RockManager.fileList.FileItem;
import RockManager.languages.LangRes;
import RockManager.ui.progressPopup.FileExtractProgressPopup;
import RockManager.util.UtilCommon;


/**
 * 解压文件弹出窗口。
 */
public class FileExtractPopup extends FileBrowsePopup {

	private FileItem[] itemsToExtract;

	private ArchiveListField archiveList;


	public FileExtractPopup(ArchiveListField fileList, FileItem[] itemsToExtract) {

		super();
		setTitle(LangRes.get(LangRes.MENU_TITLE_EXTRACT_FILES));
		this.itemsToExtract = itemsToExtract;
		archiveList = fileList;
		setDestinationPath(getPreferedDestinationPath());
		setDefaultDestinationPath(fileList.getParentPath());

		focusOKButton();

	}


	/**
	 * 根据要解压的项的所在位置获取默认解压路径。
	 * 
	 * @return
	 */
	private String getPreferedDestinationPath() {

		ArchiveEntry archiveEntry = itemsToExtract[0].getOriginArchiveEntry();

		String basePath = archiveList.getParentPath() + archiveList.getName(false) + '/';

		String subPath = archiveEntry.getParentEntry().getPath();
		if (archiveEntry.isRarEntry()) {
			subPath = UtilCommon.replaceAllString(subPath, "\\", "/");
		}

		String folderToExtract = basePath + subPath;
		return folderToExtract;

	}


	protected void doOperation() {

		close();

		final ArchiveFile archiveFile = archiveList.getArchiveFile();
		final String targetURL = UtilCommon.toURLForm(getInputedText_as_folder());

		// invokeLater: 等待此窗口关闭再弹出新窗口。
		UiApplication.getUiApplication().invokeLater(new Runnable() {

			public void run() {

				// popup中有一个将在invokeLater中执行的线程，故popup的构造要放在此invokeLater中。
				FileExtractProgressPopup progressPopup = new FileExtractProgressPopup(itemsToExtract, archiveFile,
						targetURL);
				UiApplication.getUiApplication().pushScreen(progressPopup);

			}
		});

	}

}
