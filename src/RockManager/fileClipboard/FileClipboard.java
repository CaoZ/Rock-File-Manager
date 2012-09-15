
package RockManager.fileClipboard;

import java.util.Enumeration;
import java.util.Vector;
import net.rim.device.api.ui.UiApplication;
import RockManager.fileList.FileItem;
import RockManager.fileList.FileListField;
import RockManager.languages.LangRes;
import RockManager.ui.progressPopup.ProgressIndicator;
import RockManager.ui.progressPopup.ProgressPopup;
import RockManager.util.UtilCommon;
import RockManager.util.stopableThread.StopableThread;


public class FileClipboard {

	public static final int METHOD_COPY = 1;

	public static final int METHOD_CUT = 2;

	static int METHOD_NOW;

	/**
	 * 剪贴板上的所有文件(FileItem).
	 */
	static FileItem[] BOARD_ITEMS;

	/**
	 * 所有indicator的一个列表。
	 */
	private static Vector INDICATOR_LIST = new Vector();

	/**
	 * 是否已经粘贴过一次了.
	 */
	private static boolean PASTED = false;


	/**
	 * 将某一文件放到剪贴板上.
	 * 
	 * @param method
	 * @param item
	 */
	public static void put(int method, FileItem item) {

		FileItem[] all_items = new FileItem[1];
		all_items[0] = item;
		put(method, all_items);
	}


	/**
	 * 将列表中的文件都放到剪贴板上.
	 * 
	 * @param method
	 * @param items
	 */
	public static void put(int method, FileItem[] items) {

		METHOD_NOW = method;
		BOARD_ITEMS = items;
		PASTED = false;
		invalidateIndicators();
	}


	public static FileItem[] get() {

		return BOARD_ITEMS;
	}


	/**
	 * 获得剪贴板上的文件的父目录位置.
	 * 
	 * @return
	 */
	public static String get_source_folder_url() {

		return UtilCommon.getParentDir(BOARD_ITEMS[0].getRawURL());
	}


	/**
	 * 获取在菜单中出现时的名称, 如: 粘贴 ("a.txt"), 粘贴 (共3项)
	 * 
	 * @return
	 */
	public static String get_menu_name() {

		String pattern = null;
		String item_name = null;

		if (BOARD_ITEMS.length == 1) {
			pattern = LangRes.get(LangRes.MENU_PASTE);
			item_name = BOARD_ITEMS[0].getDisplayName();
		} else {
			pattern = LangRes.get(LangRes.MENU_PASTE_SELECTED);
			item_name = Integer.toString(BOARD_ITEMS.length);
		}
		String menu_name = UtilCommon.replaceString(pattern, "{1}", item_name);
		return menu_name;
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

		BOARD_ITEMS = null;
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

		return BOARD_ITEMS == null;
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
	 * 已经粘贴过了。设 PASTED 为 true. 如果原来是复制，什么都不做，如果原来是剪切的，则应清空剪贴板。
	 */
	public static void pasted() {

		if (METHOD_NOW == METHOD_CUT) {
			clear();
		}
		PASTED = true;
	}


	public static boolean is_pasted() {

		return PASTED;
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
