
package RockManager.ui.progressPopup;

import net.rim.device.api.ui.UiApplication;
import RockManager.archive.ArchiveEntry;
import RockManager.archive.ArchiveFile;
import RockManager.fileList.FileItem;
import RockManager.languages.LangRes;
import RockManager.util.IOUtil;
import RockManager.util.UtilCommon;


/**
 * 解压文件时的进度显示窗口。
 */
public class FileExtractProgressPopup extends ProgressPopup {

	private FileItem[] itemsToExtract;

	private ArchiveFile archiveFile;

	private String targetURL;


	public FileExtractProgressPopup(FileItem[] itemsToExtract, ArchiveFile archiveFile, String targetURL) {

		this.itemsToExtract = itemsToExtract;
		this.archiveFile = archiveFile;
		this.targetURL = targetURL;

		setTitle(LangRes.get(LangRes.TITLE_EXTRACTING));

		// invokeLater: 等窗口出现再开始解压。
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

		// 即将开始解压，准备工作，计算文件大小。
		setProgressName("Calculating file size...");

		final ProgressIndicator indicator = new ProgressIndicator();
		indicator.setDisplay(this);

		final long totalPackedSize = computePackedSize(itemsToExtract);
		indicator.setTotalSize(totalPackedSize);

		Thread extractThread = new Thread() {

			public void run() {

				long totalRead = 0;
				byte[] buffer = null;

				if (archiveFile.isZipArchive()) {
					// 只有Zip解压需要手动设置buffer.
					int bufferSize = IOUtil.getBufferSize(totalPackedSize);
					buffer = new byte[bufferSize];
				}

				for (int i = 0; i < itemsToExtract.length; i++) {

					FileItem thisItem = itemsToExtract[i];
					ArchiveEntry thisEntry = thisItem.getOriginArchiveEntry();

					try {
						archiveFile.extractEntry(thisEntry, targetURL, buffer, indicator);
					} catch (Exception e) {
						String message = "Failed to extract: " + UtilCommon.getErrorMessage(e);
						UtilCommon.alert(message, true);
						break;
					}

					long thisEntrySize = computeOnePackedSize(thisItem);
					totalRead += thisEntrySize;
					indicator.setTotalRead(totalRead);

				}

				// 解压完成，展示进度。
				// 特别的由于进度是由解压了的文件大小除以总大小计算出了，所以若是总共解压的大小是0（文件夹和空文件），
				// 此处的进度仍是0。所以应设置进度为100.
				indicator.setProgressRate(100);
				try {
					Thread.sleep(200);
				} catch (Exception e) {
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
