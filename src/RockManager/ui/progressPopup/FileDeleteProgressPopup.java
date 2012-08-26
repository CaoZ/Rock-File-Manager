
package RockManager.ui.progressPopup;

import net.rim.device.api.ui.UiApplication;
import RockManager.fileHandler.FileHandler;
import RockManager.fileHandler.fileCounter.FileCounter;
import RockManager.languages.LangRes;
import RockManager.util.UtilCommon;


public class FileDeleteProgressPopup extends ProgressPopup {

	private String folderURL;

	private int totalItemCount;

	private int deletedNumber;


	public FileDeleteProgressPopup(final String folderURL) {

		setTitle(LangRes.get(LangRes.TITLE_DELETING));

		this.folderURL = folderURL;

		UiApplication.getUiApplication().invokeLater(new Runnable() {

			public void run() {

				Thread deleteThread = createDeleteThread();
				deleteThread.start();

			}
		});

	}


	private Thread createDeleteThread() {

		final FileDeleteProgressPopup indicator = this;

		Thread deleteThread = new Thread() {

			public void run() {

				// 删除开始，准备工作，计算总文件数量。
				indicator.setProgressName("Calculating file number...");

				totalItemCount = FileCounter.countFolder(folderURL).getTotalNumber();

				try {
					FileHandler.deleteFile(folderURL, indicator);
				} catch (Exception e) {
					String message = "One or more files can't be deleted: " + UtilCommon.getErrorMessage(e);
					UtilCommon.alert(message, true);
				}

				UiApplication.getUiApplication().invokeLater(new Runnable() {

					public void run() {

						close();
					}
				});

			}
		};

		return deleteThread;

	}


	/**
	 * 设置刚删除的文件的名称。
	 * 
	 * @param name
	 */
	public void setDeletedName(String fileName) {

		setProgressName(fileName);
	}


	/**
	 * 成功删除了一个文件，更新进度。
	 */
	public void plusDeletedNumber() {

		deletedNumber = Math.min(deletedNumber + 1, totalItemCount);
		setProgressRate(deletedNumber * 100 / totalItemCount);

	}

}
