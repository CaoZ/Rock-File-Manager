
package RockManager.ui.progressPopup;

import net.rim.device.api.ui.UiApplication;
import RockManager.archive.ArchiveEntry;
import RockManager.archive.ArchiveFile;
import RockManager.archive.indicator.ExtractIndicator;
import RockManager.fileList.FileItem;
import RockManager.languages.LangRes;
import RockManager.util.UtilCommon;


/**
 * 解压文件时的进度显示窗口。
 */
public class FileExtractProgressPopup extends ProgressPopup {

	private FileItem[] itemsToExtract;

	private ArchiveFile archiveFile;

	private String targetURL;

	private ExtractIndicator indicator;


	public FileExtractProgressPopup(FileItem[] itemsToExtract, ArchiveFile archiveFile, String targetURL) {

		setTitle(LangRes.getString(LangRes.TITLE_EXTRACTING));
		this.itemsToExtract = itemsToExtract;
		this.archiveFile = archiveFile;
		this.targetURL = targetURL;

		UiApplication.getUiApplication().invokeLater(new Runnable() {

			public void run() {

				Thread extractThread = createExtractThread();
				extractThread.start();

			}

		});

	}


	/**
	 * 创建解压线程。
	 * 
	 * @return
	 */
	private Thread createExtractThread() {

		indicator = new ExtractIndicator();
		indicator.setDisplay(this);

		long totalPackedSize = computePackedSize(itemsToExtract);
		indicator.setTotalSize(totalPackedSize);

		Thread extractThread = new Thread() {

			public void run() {

				long totalRead = 0;

				for (int i = 0; i < itemsToExtract.length; i++) {

					FileItem thisItem = itemsToExtract[i];
					ArchiveEntry thisEntry = thisItem.getOriginArchiveEntry();

					try {
						archiveFile.extractEntry(thisEntry, targetURL, indicator);
					} catch (Exception e) {
						String message = "Failed to extract: " + UtilCommon.getErrorMessage(e);
						UtilCommon.alert(message, true);
						break;
					}

					long thisEntrySize = computeOnePackedSize(thisItem);
					totalRead += thisEntrySize;
					indicator.setTotalRead(totalRead);

				}

				UiApplication.getUiApplication().invokeLater(new Runnable() {

					public void run() {

						close();

					}
				});

			}
		};

		return extractThread;

	}


	/**
	 * 计算所选项的压缩后大小(因为在rar中使用压缩后大小计算比较方便).
	 * 
	 * @param items
	 * @return
	 */
	private long computePackedSize(FileItem[] items) {

		long packedSize = 0;

		for (int i = 0; i < items.length; i++) {
			packedSize += computeOnePackedSize(items[i]);
		}

		return packedSize;

	}


	/**
	 * 计算此项的压缩后后大小。
	 * 
	 * @param item
	 * @return
	 */
	private long computeOnePackedSize(FileItem item) {

		ArchiveEntry archiveEntry = item.getOriginArchiveEntry();
		return archiveEntry.getPackedSize();
	}

}
