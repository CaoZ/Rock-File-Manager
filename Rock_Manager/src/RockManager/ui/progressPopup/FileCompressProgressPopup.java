
package RockManager.ui.progressPopup;

import net.rim.device.api.ui.UiApplication;
import RockManager.archive.ZipUtil;
import RockManager.fileList.FileItem;
import RockManager.fileList.FileListField;
import RockManager.languages.LangRes;
import RockManager.util.UtilCommon;


public class FileCompressProgressPopup extends ProgressPopup {

	private String originFileURL;

	private String saveURL;

	private int compressMethod;

	private FileListField parentFileList;


	public FileCompressProgressPopup(String originFileURL, String saveURL, int compressMethod,
			FileListField parentFileList) {

		this.originFileURL = originFileURL;
		this.saveURL = saveURL;
		this.compressMethod = compressMethod;
		this.parentFileList = parentFileList;

		setTitle(LangRes.getString(LangRes.TITLE_COMPRESSING));

		// invokeLater: 等窗口出现再开始压缩。
		UiApplication.getUiApplication().invokeLater(new Runnable() {

			public void run() {

				Thread compressThread = createCompressThread();
				compressThread.start();

			}

		});

	}


	private Thread createCompressThread() {

		final ProgressIndicator compressIndicator = new ProgressIndicator();
		compressIndicator.setDisplay(this);

		Thread compressThread = new Thread() {

			public void run() {

				try {

					ZipUtil.compress(originFileURL, saveURL, compressMethod, compressIndicator);

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
