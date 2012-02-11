
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

		String targetFolderURL = targetListField.getFolderPathURL(); // 要粘贴的目标位置，父文件夹。
		String originURL = FileClipboard.ORIGIN_FILE.getURL(); // 要粘贴的文件的地址。

		if (targetFolderURL.startsWith(originURL)) {
			// 不能复制文件夹到子文件夹。
			throw new IOException("The destination folder is a subfolder of the source folder.");
		}

		String originFolderURL = UtilCommon.getParentDir(originURL); // 要粘贴的文件的父文件夹。

		String targetFileName = null;

		if (originFolderURL.equals(targetFolderURL)) {
			// 在同一文件夹内复制、剪贴。

			if (FileClipboard.METHOD_NOW == FileClipboard.METHOD_CUT) {

				// 使这项获得焦点。
				targetFileName = FileClipboard.ORIGIN_FILE.getDisplayName();
				targetListField.setItemToFocus(targetFileName, FileClipboard.ORIGIN_FILE.getType());

				synchronized (UiApplication.getEventLock()) {
					targetListField.refresh();
				}

				return;

			} else if (FileClipboard.METHOD_NOW == FileClipboard.METHOD_COPY) {

				// 在同一文件夹内复制，获取适当的文件名。
				targetFileName = getCopyName(FileClipboard.ORIGIN_FILE, originFolderURL);
				String nameToFocus = targetFileName;
				if (FileClipboard.ORIGIN_FILE.isDir()) {
					// 需去除最后的文件夹分割符'/'.
					nameToFocus = UtilCommon.getName(nameToFocus, false);
				}
				targetListField.setItemToFocus(nameToFocus, FileClipboard.ORIGIN_FILE.getType());

			}

		}

		FileConnection originFileConn = null;

		try {

			// 复制开始，准备工作，计算总文件大小。
			progressIndicator.setProgressName("Calculating file size...");

			originFileConn = (FileConnection) Connector.open(FileClipboard.ORIGIN_FILE.getURL());

			if (originFileConn.exists() == false) {
				FileClipboard.clear();
				throw new IOException("Item on the clipboard doesn't exist anymore.");
			}

			if (targetFileName == null) {
				// 不是在同一文件夹内复制，还未设置文件名。
				targetFileName = originFileConn.getName();
			}

			// 复制出的新文件/文件夹路径。
			String targetURL = targetFolderURL + UtilCommon.toURLForm(targetFileName);

			long totalSize = IOUtil.getFileSize(originFileConn);
			progressIndicator.setTotalSize(totalSize);

			int bufferSize = IOUtil.getBufferSize(totalSize);
			byte[] buffer = new byte[bufferSize];

			if (originFileConn.isDirectory()) {
				// 是文件夹。
				copyFolder(originFileConn, targetURL, buffer, progressIndicator);

			} else {
				// 是文件。
				copyFile(originFileConn, targetURL, buffer, progressIndicator);

			}

			if (FileClipboard.METHOD_NOW == FileClipboard.METHOD_CUT) {
				// 若是剪切，删除原文件。
				try {
					originFileConn.delete();
				} catch (Exception e) {
					throw new IOException("Failed to delete origin file \"" + originFileConn.getName() + "\".");
				}
			}

		} catch (StopRequest e) {
			// user canceled.
			throw e;
		} catch (IOException e) {
			throw e;
		} finally {
			IOUtil.closeConnection(originFileConn);
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

		String originFolderURL = "file://"
				+ UtilCommon.toURLForm(originFolderConn.getPath() + originFolderConn.getName());

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
	 * 获得正确副本名称。
	 * 
	 * @param originFile
	 * @param originFolderURL
	 * @return
	 */
	private String getCopyName(FileItem originFile, String originFolderURL) {

		// hello.mp3
		String copySuffix = LangRes.get(LangRes.FILE_COPY_SUFFIX); // " - 副本"
		String originFileName = originFile.getName(false); // hello
		String originFileSuffix = originFile.getOriginSuffix(); // mp3
		boolean haveSuffix = originFile.isFile() && originFileSuffix.length() > 0; // true
		String newFileSuffix = "";

		if (originFile.isDir()) {
			newFileSuffix = "/";
		} else {
			newFileSuffix = haveSuffix ? ('.' + originFileSuffix) : "";
		}

		String parentURL = UtilCommon.getParentDir(originFile.getRawURL());
		String newFileName;
		String targetURL;

		for (int i = 1;; i++) {

			if (i == 1) {
				newFileName = originFileName + copySuffix + newFileSuffix;
			} else {
				newFileName = originFileName + copySuffix + " (" + i + ")" + newFileSuffix;
			}
			targetURL = parentURL + UtilCommon.toURLForm(newFileName);
			if (IOUtil.isExists(targetURL) == false) {
				return newFileName;
			}

		}

	}

}
