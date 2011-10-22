
package RockManager.fileClipboard;

import java.util.Vector;
import net.rim.device.api.ui.UiApplication;
import RockManager.fileList.FileItem;


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

}
