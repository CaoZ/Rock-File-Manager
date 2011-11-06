
package RockManager.fileClipboard;

import java.util.Enumeration;
import java.util.Vector;
import net.rim.device.api.ui.UiApplication;
import RockManager.fileList.FileItem;
import RockManager.fileList.FileListField;
import RockManager.languages.LangRes;
import RockManager.ui.progressPopup.ProgressIndicator;
import RockManager.ui.progressPopup.ProgressPopup;
import RockManager.util.stopableThread.StopableThread;


public class FileClipboard {

	public static final int METHOD_COPY = 1;

	public static final int METHOD_CUT = 2;

	static int METHOD_NOW;

	static FileItem ORIGIN_FILE;

	/**
	 * 所有indicator的一个列表。
	 */
	private static Vector INDICATOR_LIST = new Vector();


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

		Enumeration indicators = INDICATOR_LIST.elements();

		while (indicators.hasMoreElements()) {
			FileClipboardIndicator thisIndicator = (FileClipboardIndicator) indicators.nextElement();
			thisIndicator.invalidate();
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

		if (INDICATOR_LIST.contains(indicator) == false) {
			INDICATOR_LIST.addElement(indicator);
		}

	}


	/**
	 * 从indicator列表移出。
	 * 
	 * @param indicator
	 */
	public static synchronized void removeFromIndicatorList(FileClipboardIndicator indicator) {

		INDICATOR_LIST.removeElement(indicator);

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

		if (isEmpty()) { // 剪贴板上没有文件。
			return;
		}

		final ProgressPopup popup = new ProgressPopup();
		popup.setCancelable(true);

		if (METHOD_NOW == METHOD_CUT) {
			// 移动文件...
			popup.setTitle(LangRes.get(LangRes.TITLE_MOVING_FILES));
		} else {
			// 复制文件...
			popup.setTitle(LangRes.get(LangRes.TITLE_COPYING_FILES));
		}

		final ProgressIndicator progressIndicator = new ProgressIndicator();
		progressIndicator.setDisplay(popup);

		UiApplication.getUiApplication().pushScreen(popup);

		final StopableThread pasteThread = new FilePasteThread(fileListField, progressIndicator);

		Runnable cancelRunnable = new Runnable() {

			public void run() {

				pasteThread.doStop();
			}
		};
		popup.setCancelRunnable(cancelRunnable);

		UiApplication.getUiApplication().invokeLater(new Runnable() {

			public void run() {

				pasteThread.start();
			};
		});

	}

}
