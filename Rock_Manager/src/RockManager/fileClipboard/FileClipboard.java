
package RockManager.fileClipboard;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Vector;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import net.rim.device.api.ui.UiApplication;
import RockManager.fileHandler.FileHandler;
import RockManager.fileList.FileItem;
import RockManager.fileList.FileListField;
import RockManager.languages.LangRes;
import RockManager.ui.progressPopup.ProgressIndicator;
import RockManager.ui.progressPopup.ProgressPopup;
import RockManager.util.IOUtil;
import RockManager.util.UtilCommon;


public class FileClipboard {

	public static final int METHOD_COPY = 1;

	public static final int METHOD_CUT = 2;

	private static int METHOD_NOW;

	private static FileItem ORIGIN_FILE;

	/**
	 * 所有indicator的一个列表。
	 */
	private static Vector indicatorList = new Vector();


	/**
	 * 将某一文件放到剪贴板上。
	 * 
	 * @param method
	 * @param fileItem
	 */
	public static void put(int method, FileItem fileItem) {

		METHOD_NOW = method;
		ORIGIN_FILE = fileItem;

		invalidateIndicators();

	}


	public static FileItem get() {

		return ORIGIN_FILE;

	}


	/**
	 * 获取当前剪贴板的方式（复制或剪贴）。
	 * 
	 * @return
	 */
	public static int getMethod() {

		return METHOD_NOW;

	}


	/**
	 * 清空剪贴板。
	 */
	public static void clear() {

		ORIGIN_FILE = null;

		invalidateIndicators();

	}


	/**
	 * invalidate所有indicator。
	 */
	private static synchronized void invalidateIndicators() {

		for (int i = 0; i < indicatorList.size(); i++) {
			((FileClipboardIndicator) indicatorList.elementAt(i)).invalidate();
		}

	}


	/**
	 * 剪贴板是否为空。
	 * 
	 * @return
	 */
	public static boolean isEmpty() {

		return ORIGIN_FILE == null;

	}


	/**
	 * 添加到indicator列表。
	 * 
	 * @param indicator
	 */
	public static synchronized void addToIndicatorList(FileClipboardIndicator indicator) {

		if (indicatorList.contains(indicator) == false) {
			indicatorList.addElement(indicator);
		}

	}


	/**
	 * 从indicator列表移出。
	 * 
	 * @param indicator
	 */
	public static synchronized void removeFromIndicatorList(FileClipboardIndicator indicator) {

		indicatorList.removeElement(indicator);

	}


	/**
	 * 已经粘贴过了。如果原来是复制，什么都不做，如果原来是剪切的，则应清空剪贴板。
	 */
	public static void pasted() {

		if (METHOD_NOW == METHOD_CUT) {
			clear();
		}

	}


	/**
	 * 弹出窗口，显示剪贴板详细信息并可清空剪贴板。
	 */
	public static void showProperty() {

		if (isEmpty()) {
			return;
		}

		FileClipboardPopup popup = new FileClipboardPopup();
		UiApplication.getUiApplication().pushScreen(popup);

	}


	/**
	 * 弹出进度指示窗口，并复制/移动文件。
	 * 
	 * @param fileListField
	 */
	public static void pasteWithUI(final FileListField fileListField) {

		final ProgressPopup popup = new ProgressPopup();

		if (METHOD_NOW == METHOD_CUT) {
			// 移动文件...
			popup.setTitle(LangRes.getString(LangRes.TITLE_MOVING_FILES));
		} else {
			// 复制文件...
			popup.setTitle(LangRes.getString(LangRes.TITLE_COPYING_FILES));
		}

		final ProgressIndicator progressIndicator = new ProgressIndicator();
		progressIndicator.setDisplay(popup);

		UiApplication.getUiApplication().pushScreen(popup);

		final Thread pasteThread = new Thread() {

			public void run() {

				try {
					FileClipboard.paste(fileListField, progressIndicator);
					progressIndicator.setProgressRate(100); // 100%
					FileClipboard.pasted();
				} catch (Exception e) {
					String message = "Failed to copy/move: " + UtilCommon.getErrorMessage(e);
					UtilCommon.alert(message, true);
				}

				UiApplication.getUiApplication().invokeLater(new Runnable() {

					public void run() {

						popup.close();
					}
				});

			}
		};

		UiApplication.getUiApplication().invokeLater(new Runnable() {

			public void run() {

				pasteThread.start();
			};
		});

	}


	/**
	 * 将剪贴板上的文件复制/移动到目标文件夹。
	 * 
	 * @param targetListField
	 * @param progressIndicator
	 * @throws Exception
	 */
	public static void paste(FileListField targetListField, ProgressIndicator progressIndicator) throws Exception {

		if (ORIGIN_FILE == null) { // 剪贴板上没有文件。
			clear();
			return;
		}

		String targetFolderURL = targetListField.getFolderPathURL(); // 要粘贴的目标位置，父文件夹。
		String originURL = ORIGIN_FILE.getURL(); // 要粘贴的文件的地址。

		if (targetFolderURL.startsWith(originURL)) {
			// 不能复制文件夹到子文件夹。
			throw new Exception("The destination folder is a subfolder of the source folder.");
		}

		String originFolderURL = UtilCommon.getParentDir(originURL); // 要粘贴的文件的父文件夹。

		String targetFileName = null;

		if (originFolderURL.equals(targetFolderURL)) {
			// 在同一文件夹内复制、剪贴。

			if (METHOD_NOW == METHOD_CUT) {

				// 使这项获得焦点。
				targetFileName = ORIGIN_FILE.getDisplayName();
				targetListField.setItemToFocus(targetFileName, ORIGIN_FILE.getType());

				synchronized (UiApplication.getEventLock()) {
					targetListField.refresh();
				}

				return;

			} else if (METHOD_NOW == METHOD_COPY) {

				// 在同一文件夹内复制，获取适当的文件名。
				targetFileName = getCopyName(ORIGIN_FILE, originFolderURL);
				String nameToFocus = targetFileName;
				if (ORIGIN_FILE.isDir()) {
					// 需去除最后的文件夹分割符'/'.
					nameToFocus = UtilCommon.getName(nameToFocus, false);
				}
				targetListField.setItemToFocus(nameToFocus, ORIGIN_FILE.getType());

			}

		}

		FileConnection originFileConn = null;

		try {

			originFileConn = (FileConnection) Connector.open(ORIGIN_FILE.getURL());

			if (originFileConn.exists() == false) {
				clear();
				throw new Exception("Item on the clipboard doesn't exist anymore.");
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

			if (METHOD_NOW == METHOD_CUT) {
				// 若是剪切，删除原文件。
				try {
					originFileConn.delete();
				} catch (Exception e) {
					throw new Exception("Failed to delete origin file \"" + originFileConn.getName() + "\".");
				}
			}

		} catch (Exception e) {
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
	private static void copyFile(FileConnection originFileConn, String targetURL, byte[] buffer,
			ProgressIndicator progressIndicator) throws Exception {

		progressIndicator.setProgressName(originFileConn.getName());
		boolean isHidden = originFileConn.isHidden();

		try {
			FileHandler.createTargetFile(targetURL, isHidden);
		} catch (Exception e) {
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

			while ((readCount = is.read(buffer)) > 0) {
				os.write(buffer, 0, readCount);
				progressIndicator.increaseRead(readCount);
			}

		} catch (Exception e) {
			throw new Exception("Can't copy/move file " + originFileConn.getName());
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
	 * @throws Exception
	 */
	private static void copyFolder(FileConnection originFolderConn, String targetURL, byte[] buffer,
			ProgressIndicator progressIndicator) throws Exception {

		progressIndicator.setProgressName(originFolderConn.getName());
		boolean isHidden = originFolderConn.isHidden();

		try {
			FileHandler.createTargetFolder(targetURL, isHidden);
		} catch (Exception e) {
			throw e;
		}

		String originFolderURL = "file://"
				+ UtilCommon.toURLForm(originFolderConn.getPath() + originFolderConn.getName());

		try {

			Enumeration allFiles = originFolderConn.list("*", true);

			while (allFiles.hasMoreElements()) {

				String thisFileName = (String) allFiles.nextElement();
				String thisFileNameURL = UtilCommon.toURLForm(thisFileName);
				String thisOriginFileURL = originFolderURL + thisFileNameURL;
				String thisTargetFileURL = targetURL + thisFileNameURL;

				FileConnection thisOriginFileConn = (FileConnection) Connector.open(thisOriginFileURL);

				if (thisOriginFileConn.isDirectory()) {
					copyFolder(thisOriginFileConn, thisTargetFileURL, buffer, progressIndicator);
				} else {
					copyFile(thisOriginFileConn, thisTargetFileURL, buffer, progressIndicator);
				}

				if (METHOD_NOW == METHOD_CUT) {
					try {
						thisOriginFileConn.delete();
					} catch (Exception e) {
						throw new Exception("Failed to delete origin file \"" + thisOriginFileConn.getName() + "\".");
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
	private static String getCopyName(FileItem originFile, String originFolderURL) {

		// hello.mp3
		String copySuffix = LangRes.getString(LangRes.FILE_COPY_SUFFIX); // " - 副本"
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
