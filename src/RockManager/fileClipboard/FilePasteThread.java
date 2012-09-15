
package RockManager.fileClipboard;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import net.rim.device.api.ui.UiApplication;
import RockManager.fileHandler.FileHandler;
import RockManager.fileList.FileItem;
import RockManager.fileList.FileListField;
import RockManager.languages.LangRes;
import RockManager.ui.progressPopup.ProgressIndicator;
import RockManager.util.IOUtil;
import RockManager.util.UtilCommon;
import RockManager.util.stopableThread.StopRequest;
import RockManager.util.stopableThread.StopableThread;


public class FilePasteThread extends StopableThread {

	private FileListField targetListField;

	private ProgressIndicator progressIndicator;


	public FilePasteThread(FileListField targetListField, ProgressIndicator indicator) {

		this.targetListField = targetListField;
		this.progressIndicator = indicator;
	}


	public void run() {

		try {
			paste(targetListField, progressIndicator);
			progressIndicator.setProgressRate(100);
			FileClipboard.pasted();
		} catch (StopRequest e) {
			// user canceled.
		} catch (IOException e) {
			String message = "Failed to copy/move: " + UtilCommon.getErrorMessage(e);
			UtilCommon.alert(message, true);
		}

		UiApplication.getUiApplication().invokeLater(new Runnable() {

			public void run() {

				progressIndicator.closeDisplay();
			}
		});

	}


	/**
	 * 将剪贴板上的文件复制/移动到目标文件夹。
	 * 
	 * @param targetListField
	 * @param progressIndicator
	 * @throws StopRequest
	 * @throws Exception
	 */
	private void paste(FileListField targetListField, ProgressIndicator progressIndicator) throws IOException,
			StopRequest {

		if (FileClipboard.isEmpty()) { // 剪贴板上没有文件。
			return;
		}

		// 要粘贴的目标位置，父文件夹。
		String targetFolderURL = targetListField.getFolderPathURL();
		// 要粘贴的文件的来源位置, 父文件夹.
		String originFolderURL = FileClipboard.get_source_folder_url();

		if (!targetFolderURL.equals(originFolderURL) && targetFolderURL.startsWith(originFolderURL)) {
			// 不能复制文件夹到子文件夹。
			throw new IOException("The destination folder is a subfolder of the source folder.");
		}

		boolean same_folder_paste = false;

		if (originFolderURL.equals(targetFolderURL)) {

			// 在同一文件夹内复制、剪切。
			same_folder_paste = true;

			if (FileClipboard.METHOD_NOW == FileClipboard.METHOD_CUT) {

				// 不能剪切至自己原本的目录.
				throw new IOException("Can't cut and paste file to it's own folder.");

			}

		}

		// 要复制的文件们.
		FileItem[] items_to_paste = FileClipboard.get();

		try {

			// 复制开始，准备工作，计算总文件大小。
			progressIndicator.setProgressName("Calculating file size...");

			long totalSize = IOUtil.getFileSize(items_to_paste);
			progressIndicator.setTotalSize(totalSize);

			int bufferSize = IOUtil.getBufferSize(totalSize);
			byte[] buffer = new byte[bufferSize];

			for (int i = 0; i < items_to_paste.length; i++) {
				FileItem this_item = items_to_paste[i];
				copyFileItem(this_item, targetFolderURL, buffer, same_folder_paste, progressIndicator, targetListField);
				if (FileClipboard.METHOD_NOW == FileClipboard.METHOD_CUT) {
					// 若是剪切, 删除原文件
					try {
						FileHandler.deleteFile(this_item.getURL(), null);
					} catch (Exception e) {
						throw new IOException("Failed to delete origin file \"" + this_item.getDisplayName() + "\".");
					}
				}
			}

		} catch (StopRequest e) {
			// user canceled.
			throw e;
		} catch (IOException e) {
			throw e;
		}

	}


	/**
	 * 将一个 FileItem 复制到目标文件夹.
	 * 
	 * @param item
	 * @param targetFolderURL
	 * @param buffer
	 * @param same_folder_paste
	 * @param progressIndicator
	 * @param targetListField
	 * @throws IOException
	 * @throws StopRequest
	 */
	private void copyFileItem(FileItem item, String targetFolderURL, byte[] buffer, boolean same_folder_paste,
			ProgressIndicator progressIndicator, FileListField targetListField) throws IOException, StopRequest {

		FileConnection originFileConn = (FileConnection) Connector.open(item.getURL());

		if (!originFileConn.exists()) {
			FileClipboard.clear();
			throw new IOException("Item on the clipboard doesn't exist anymore.");
		}

		String target_file_name = null;

		if (same_folder_paste) {
			target_file_name = getCopyName(item, targetFolderURL);
		} else {
			target_file_name = originFileConn.getName();
		}

		boolean item_is_dir = item.isDir();

		String name_to_focus = item_is_dir ? UtilCommon.getName(target_file_name, true) : target_file_name;
		targetListField.setItemToFocus(name_to_focus, item.getType());

		String target_file_url = targetFolderURL + UtilCommon.toURLForm(target_file_name);

		if (item_is_dir) {
			copyFolder(originFileConn, target_file_url, buffer, progressIndicator);
		} else {
			copyFile(originFileConn, target_file_url, buffer, progressIndicator);
		}

	}


	/**
	 * 复制文件。
	 * 
	 * @param originFileConn
	 *            源文件。
	 * @param targetURL
	 *            目标地址。
	 * @param bufferSize
	 *            BufferSize.
	 * @param progressIndicator
	 *            进度指示。
	 * @throws Exception
	 */
	private void copyFile(FileConnection originFileConn, String targetURL, byte[] buffer,
			ProgressIndicator progressIndicator) throws IOException, StopRequest {

		progressIndicator.setProgressName(originFileConn.getName());
		boolean isHidden = originFileConn.isHidden();

		try {
			FileHandler.createTargetFile(targetURL, isHidden);
		} catch (IOException e) {
			throw e;
		}

		InputStream is = null;
		FileConnection targetConn = null;
		OutputStream os = null;

		try {

			is = originFileConn.openInputStream();

			targetConn = (FileConnection) Connector.open(targetURL);
			os = targetConn.openOutputStream();

			int readCount = -1;

			while (true) {

				if (isStopped()) {
					throw new StopRequest();
				}

				if ((readCount = is.read(buffer)) <= 0) {
					break;
				}

				if (isStopped()) {
					throw new StopRequest();
				}

				os.write(buffer, 0, readCount);
				progressIndicator.increaseRead(readCount);

			}

		} catch (Exception e) {

			if (e instanceof StopRequest) {
				// 用户取消操作。
				IOUtil.closeStream(os);
				targetConn.delete(); // 删除未完成的文件。
				throw (StopRequest) e;
			} else {
				throw new IOException("Can't copy/move file " + originFileConn.getName());
			}

		} finally {
			IOUtil.closeStream(is);
			IOUtil.closeStream(os);
			IOUtil.closeConnection(targetConn);
		}

	}


	/**
	 * 复制目录。
	 * 
	 * @param originFolderConn
	 *            源文件。
	 * @param targetURL
	 *            目标地址。
	 * @param bufferSize
	 *            BufferSize.
	 * @param progressIndicator
	 *            进度指示。
	 * @throws StopRequest
	 * @throws Exception
	 */
	private void copyFolder(FileConnection originFolderConn, String targetURL, byte[] buffer,
			ProgressIndicator progressIndicator) throws IOException, StopRequest {

		progressIndicator.setProgressName(originFolderConn.getName());
		boolean isHidden = originFolderConn.isHidden();

		try {
			FileHandler.createTargetFolder(targetURL, isHidden);
		} catch (IOException e) {
			throw e;
		}

		String originFolderURL = originFolderConn.getURL();

		try {

			Enumeration allFiles = originFolderConn.list("*", true);

			while (allFiles.hasMoreElements()) {

				if (isStopped()) {
					break;
				}

				String thisFileName = (String) allFiles.nextElement();
				String thisFileNameURL = UtilCommon.toURLForm(thisFileName);
				String thisOriginFileURL = originFolderURL + thisFileNameURL;
				String thisTargetFileURL = targetURL + thisFileNameURL;

				FileConnection thisOriginFileConn = (FileConnection) Connector.open(thisOriginFileURL, Connector.READ);

				if (thisOriginFileConn.isDirectory()) {
					copyFolder(thisOriginFileConn, thisTargetFileURL, buffer, progressIndicator);
				} else {
					copyFile(thisOriginFileConn, thisTargetFileURL, buffer, progressIndicator);
				}

				if (FileClipboard.METHOD_NOW == FileClipboard.METHOD_CUT) {
					try {
						thisOriginFileConn.delete();
					} catch (Exception e) {
						throw new IOException("Failed to delete origin file \"" + thisOriginFileConn.getName() + "\".");
					}
				}

				IOUtil.closeConnection(thisOriginFileConn);

			}

		} catch (IOException e) {
			throw e;
		}

	}


	/**
	 * 如果在同一文件夹内粘贴, 获得正确副本名称, 如: A - 副本 (2).txt
	 * 
	 * @param item
	 * @param target_folder_url
	 * @return
	 */
	private String getCopyName(FileItem item, String target_folder_url) {

		// hello.mp3
		String copySuffix = LangRes.get(LangRes.FILE_COPY_SUFFIX); // " - 副本"
		String originFileName = item.getName(false); // hello
		String originFileSuffix = item.getOriginSuffix(); // mp3
		boolean haveSuffix = item.isFile() && originFileSuffix.length() > 0; // true
		String newFileSuffix = "";

		if (item.isDir()) {
			newFileSuffix = "/";
		} else {
			newFileSuffix = haveSuffix ? ('.' + originFileSuffix) : "";
		}

		String parentURL = UtilCommon.getParentDir(item.getRawURL());
		String newFileName;
		String targetURL;

		for (int i = 1;; i++) {

			if (i == 1) {
				newFileName = originFileName + copySuffix + newFileSuffix;
			} else {
				newFileName = originFileName + copySuffix + " (" + i + ")" + newFileSuffix;
			}
			targetURL = parentURL + UtilCommon.toURLForm(newFileName);
			if (!IOUtil.isExists(targetURL)) {
				return newFileName;
			}

		}

	}

}
