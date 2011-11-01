
package RockManager.archive;

import net.rim.device.api.ui.ContextMenu;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import RockManager.fileHandler.filePopup.browsePopup.FileExtractPopup;
import RockManager.fileList.FileItem;
import RockManager.fileList.FileListContextMenuHandler;
import RockManager.languages.LangRes;


public class ArchiveListContextMenuHandler extends FileListContextMenuHandler {

	public static void addArchiveMenus(ContextMenu contextMenu, ArchiveListField fileList) {

		boolean added = false;

		FileItem thisItem = fileList.getThisItem();

		if (thisItem != null && !thisItem.isReturn()) {
			// 解压此项
			addExtractThisItem(contextMenu, fileList, 310, PRIORITY_ONE, thisItem);
			added = true;
		}

		FileItem[] allFiles = fileList.getAllFiles();
		if (allFiles.length > 1) {
			// 全部解压
			addExtractAllItem(contextMenu, fileList, 320, PRIORITY_ONE, allFiles);
			added = true;
		}

		if (added) {
			contextMenu.addItem(MenuItem.separator(300)); // 分割线
			contextMenu.addItem(MenuItem.separator(399)); // 分割线
		}

	}


	private static void addExtractThisItem(ContextMenu contextMenu, final ArchiveListField fileList, int ordinal,
			int priority, final FileItem thisItem) {

		MenuItem extractThis = new MenuItem(LangRes.get(LangRes.MENU_EXTRACT_THIS), ordinal, priority) {

			public void run() {

				UiApplication.getUiApplication().invokeLater(new Runnable() {

					public void run() {

						FileItem[] allFiles = { thisItem };
						FileExtractPopup extractPopup = new FileExtractPopup(fileList, allFiles);
						UiApplication.getUiApplication().pushScreen(extractPopup);

					}
				});

			}

		};

		contextMenu.addItem(extractThis);

	}


	private static void addExtractAllItem(ContextMenu contextMenu, final ArchiveListField fileList, int ordinal,
			int priority, final FileItem[] allFiles) {

		MenuItem extractAll = new MenuItem(LangRes.get(LangRes.MENU_EXTRACT_ALL), ordinal, priority) {

			public void run() {

				UiApplication.getUiApplication().invokeLater(new Runnable() {

					public void run() {

						FileExtractPopup extractPopup = new FileExtractPopup(fileList, allFiles);
						UiApplication.getUiApplication().pushScreen(extractPopup);

					}
				});

			}

		};

		contextMenu.addItem(extractAll);

	}

}
