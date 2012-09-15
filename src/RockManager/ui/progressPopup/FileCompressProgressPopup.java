
package RockManager.ui.progressPopup;

import net.rim.device.api.ui.UiApplication;
import RockManager.archive.ZipUtil;
import RockManager.fileList.FileItem;
import RockManager.fileList.FileListField;
import RockManager.languages.LangRes;
import RockManager.util.UtilCommon;


public class FileCompressProgressPopup extends ProgressPopup {

	private FileItem[] itemsToCompress;

	private String saveURL;

	private int compressMethod;

	private FileListField parentFileList;


	public FileCompressProgressPopup(FileItem[] itemsToCompress, String saveURL, int compressMethod,
			FileListField parentFileList) {

		this.itemsToCompress = itemsToCompress;
		this.saveURL = saveURL;
		this.compressMethod = compressMethod;
		this.parentFileList = parentFileList;

		setTitle(LangRes.get(LangRes.TITLE_COMPRESSING));

		// invokeLater: 等窗口出现再开始压缩。
		UiApplication.getUiApplication().invokeLater(new Runnable() {

			public void run() {

				Thread compressThread = createCompressThread();
				compressThread.start();

			}

		});

	}


	private Thread createCompressThread() {

		// 即将开始压缩，准备工作，计算文件大小。
		setProgressName("Calculating file size...");

		final ProgressIndicator compressIndicator = new ProgressIndicator();
		compressIndicator.setDisplay(this);

		Thread compressThread = new Thread() {

			public void run() {

				try {

					ZipUtil.compress(itemsToCompress, saveURL, compressMethod, compressIndicator);

				} catch (Exception e) {
					String failMessage = "Failed to compress: " + UtilCommon.getErrorMessage(e);
					UtilCommon.alert(failMessage, true);
				}

				UiApplication.getUiApplication().invokeLater(new Runnable() {

					public void run() {

						close();

						if (UtilCommon.getParentDir(saveURL).equals(parentFileList.getFolderPathURL())) {
							String targetArchiveName = UtilCommon.URLtoPath(UtilCommon.getName(saveURL, true));
							parentFileList.setItemToFocus(targetArchiveName, FileItem.TYPE_FILE);
							parentFileList.refresh();
						}

					}
				});

			}
		};

		return compressThread;

	}

}
