
package RockManager.fileList;

import net.rim.device.api.ui.ContextMenu;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import RockManager.archive.ArchiveListContextMenuHandler;
import RockManager.archive.ArchiveListField;
import RockManager.config.Config;
import RockManager.favouritesList.FavouritesData;
import RockManager.fileClipboard.FileClipboard;
import RockManager.fileHandler.FileHandler;
import RockManager.fileHandler.filePopup.browsePopup.CreateArchivePopup;
import RockManager.fileHandler.filePopup.browsePopup.FileBrowsePopup;
import RockManager.languages.LangRes;
import RockManager.util.UtilCommon;


public class FileListContextMenuHandler {

	protected static final int PRIORITY_ONE = 90;

	protected static final int PRIORITY_TWO = 91;


	public static void makeContextMenu(ContextMenu contextMenu, FileListField fileList) {

		// 添加剪贴板相关项。
		addClipboardMenus(contextMenu, fileList); // 100-199

		// 添加"属性", "重命名", "删除"等项。
		addFileOperationMenus(contextMenu, fileList); // 200-299

		// 添加"压缩", "解压缩"项。
		addArchiveMenus(contextMenu, fileList); // 300-399

		// 添加其它项。
		addOtherMenus(contextMenu, fileList); // 400-499

		// 收藏夹相关项
		addFavouriteMenus(contextMenu, fileList); // 500-599

	}


	// ==================== 各个菜单组 ====================

	/**
	 * 添加剪贴板相关项。Ordinal:100-199
	 */
	private static void addClipboardMenus(ContextMenu contextMenu, final FileListField fileList) {

		boolean added = false;

		if (fileList.isClipboardAllowed()) {

			if (fileList.isRealFileItem()) {
				addCutFileMenuItem(contextMenu, fileList, 120, 120); // 剪切 120
				addCopyFileMenuItem(contextMenu, fileList, 130, 130); // 复制 130
				added = true;
			}

			if (FileClipboard.isEmpty() == false) {
				addClipboardDetailMenuItem(contextMenu, fileList, 110, 110);// 剪贴板详情
																			// 110
				contextMenu.addItem(MenuItem.separator(111)); // 分割线 111
				addPasteFileMenuItem(contextMenu, fileList, 140, 140); // 粘贴 140
				added = true;
			}

		}

		if (added) {
			contextMenu.addItem(MenuItem.separator(100)); // 分割线
			contextMenu.addItem(MenuItem.separator(199)); // 分割线
		}

	}


	/**
	 * 添加"属性", "重命名", "删除"等项。Ordinal:200-299
	 */
	private static void addFileOperationMenus(ContextMenu contextMenu, final FileListField fileList) {

		boolean added = false;

		if (fileList.isFavouriteList() && fileList.isRealFileItem()) {
			addOpenItMenuItem(contextMenu, fileList, 210, PRIORITY_ONE);
			added = true;
		}

		if (fileList.shouldShowProperty()) {
			// 属性 220
			addShowPropertyMenuItem(contextMenu, fileList, 220, PRIORITY_TWO);
			added = true;
		}

		if (fileList.isNormalFolder() && !fileList.isPickerMode() && fileList.isRealFileItem()) {
			addRenameMenuItem(contextMenu, fileList, 230, 230); // 重命名 230
			addDeleteMenuItem(contextMenu, fileList, 240, 240); // 删除 240
			added = true;
		}

		if (Config.DEBUG_MODE && fileList.isNormalFolder()) { // 只在测试时添加此项。
			addRefreashMenuItem(contextMenu, fileList, 250, 250); // 刷新 250
			added = true;
		}

		if (added) {
			contextMenu.addItem(MenuItem.separator(200)); // 分割线
			contextMenu.addItem(MenuItem.separator(299)); // 分割线
		}

	}


	/**
	 * 添加"安装", "压缩", "解压缩"项。Ordinal:300-399
	 */
	private static void addArchiveMenus(ContextMenu contextMenu, FileListField fileList) {

		if (fileList instanceof ArchiveListField) {
			ArchiveListContextMenuHandler.addArchiveMenus(contextMenu, (ArchiveListField) fileList);
			return;
		}

		if (fileList.isEmpty()) {
			return;
		}

		boolean added = false;

		FileItem thisItem = fileList.getThisItem();

		if (fileList.isNormalFolder() && !fileList.isPickerMode() && thisItem.isRealFile()) {

			String suffix = thisItem.getSuffix();
			boolean isArchiveFile = suffix.equals("zip") || suffix.equals("rar");

			if (isArchiveFile) {
				// 解压到 "文件夹\" 310
				addExtractToFolderMenuItem(contextMenu, fileList, 310, PRIORITY_ONE);
				// 解压文件... 320
				addExtractMenuItem(contextMenu, fileList, 320, 320);
			} else {
				// 添加到压缩文件... 330
				addCompressMenuItem(contextMenu, fileList, 330, 330);
			}

			added = true;

		}

		if (added) {
			contextMenu.addItem(MenuItem.separator(300)); // 分割线
			contextMenu.addItem(MenuItem.separator(399)); // 分割线
		}

	}


	/**
	 * 添加其它项。400-499
	 * 
	 * @param contextMenu
	 * @param fileList
	 */
	private static void addOtherMenus(ContextMenu contextMenu, FileListField fileList) {

		boolean added = false;

		FileItem thisItem = fileList.getThisItem();
		boolean addCodItem = fileList.isNormalFolder() && !fileList.isPickerMode() && thisItem != null
				&& thisItem.isRealFile() && thisItem.getSuffix().equals("cod");

		if (addCodItem) {
			// cod文件, "安装" 410
			addInstallCodMenuItem(contextMenu, fileList, 410, PRIORITY_ONE);
			contextMenu.addItem(MenuItem.separator(411));
			added = true;
		}

		if (fileList.isPickerMode()) {

			boolean subAdded = false;

			if (fileList.isNormalFolder()) {
				// "选择此处" 420
				addSelectHereMenuItem(contextMenu, fileList, 420, PRIORITY_ONE);
				subAdded = true;
			}

			if (thisItem != null && (thisItem.isDisk() || thisItem.isDir())) {
				// 选择子文件夹。
				addSelectThisSubFolderMenuItem(contextMenu, fileList, 430, PRIORITY_TWO);
				subAdded = true;
			}

			if (subAdded) {
				contextMenu.addItem(MenuItem.separator(431));
				added = true;
			}

		}

		if (fileList.isNormalFolder()) {

			// "新建文件夹" 440
			addCreateNewFolderMenuItem(contextMenu, fileList, 440, 440);
			added = true;

		}

		if (added) {
			contextMenu.addItem(MenuItem.separator(400)); // 分割线
			contextMenu.addItem(MenuItem.separator(499)); // 分割线
		}

	}


	/**
	 * 添加收藏夹操作相关菜单项。500-599
	 * 
	 * @param contextMenu
	 * @param fileList
	 */
	private static void addFavouriteMenus(ContextMenu contextMenu, FileListField fileList) {

		if (fileList.isRealFileItem() == false) {
			return;
		}

		boolean added = false;

		if (fileList.isNormalFolder() && !fileList.isPickerMode()) {
			// "添加到收藏夹" 510
			addAddToFavouriteMenuItem(contextMenu, fileList, 510, 510);
			added = true;
		} else if (fileList.isFavouriteList()) {
			// 从收藏夹移除 520
			addDeleteFavouriteMenuItem(contextMenu, fileList, 520, 520);
			added = true;
		}

		if (added) {
			contextMenu.addItem(MenuItem.separator(500));
			contextMenu.addItem(MenuItem.separator(599));
		}

	}


	// ==================== 各个菜单项的具体实现 ====================

	/**
	 * 添加MenuItem粘贴。
	 */
	private static void addPasteFileMenuItem(ContextMenu contextMenu, final FileListField fileList, int ordinal,
			int priority) {

		String fileName = FileClipboard.get().getDisplayName();
		String labelPaste = UtilCommon.replaceString(LangRes.get(LangRes.MENU_PASTE), "{1}", fileName);

		MenuItem pasteFile = new MenuItem(labelPaste, ordinal, priority) {

			public void run() {

				UiApplication.getUiApplication().invokeLater(new Runnable() {

					public void run() {

						fileList.pasteFromClipboard();
					}
				});

			}
		};

		contextMenu.addItem(pasteFile);
	}


	/**
	 * 添加MenuItem复制。
	 */
	private static void addCopyFileMenuItem(ContextMenu contextMenu, final FileListField fileList, int ordinal,
			int priority) {

		MenuItem copyFile = new MenuItem(LangRes.get(LangRes.MENU_COPY), ordinal, priority) {

			public void run() {

				fileList.copyToClipboard();
			}
		};

		contextMenu.addItem(copyFile);
	}


	/**
	 * 添加MenuItem剪切。
	 * 
	 * @param
	 */
	private static void addCutFileMenuItem(ContextMenu contextMenu, final FileListField fileList, int ordinal,
			int priority) {

		MenuItem cutFile = new MenuItem(LangRes.get(LangRes.MENU_CUT), ordinal, priority) {

			public void run() {

				fileList.cutToClipboard();
			}
		};

		contextMenu.addItem(cutFile);
	}


	/**
	 * 添加MenuItem"剪贴板..."
	 */
	private static void addClipboardDetailMenuItem(ContextMenu contextMenu, FileListField fileList, int ordinal,
			int priority) {

		MenuItem clipBoard = new MenuItem(LangRes.get(LangRes.MENU_TITLE_CLIPBOARD), ordinal, priority) {

			public void run() {

				UiApplication.getUiApplication().invokeLater(new Runnable() {

					public void run() {

						// 显示剪贴板属性。
						FileClipboard.showProperty();
					}
				});

			}

		};

		contextMenu.addItem(clipBoard);
	}


	/**
	 * 添加"属性"项。
	 */
	private static void addShowPropertyMenuItem(ContextMenu contextMenu, final FileListField fileList, int ordinal,
			int priority) {

		String properties = LangRes.get(LangRes.PROPERTIES);

		MenuItem property = new MenuItem(properties, ordinal, priority) {

			public void run() {

				fileList.showProperty();
			}

		};

		contextMenu.addItem(property);
	}


	/**
	 * 添加"重命名"项。
	 */
	private static void addRenameMenuItem(ContextMenu contextMenu, final FileListField fileList, int ordinal,
			int priority) {

		MenuItem renameFile = new MenuItem(LangRes.get(LangRes.MENU_RENAME), ordinal, priority) {

			public void run() {

				UiApplication.getUiApplication().invokeLater(new Runnable() {

					public void run() {

						fileList.renameFile();
					}
				});

			}

		};

		contextMenu.addItem(renameFile);
	}


	/**
	 * 添加"删除"项。
	 */
	private static void addDeleteMenuItem(ContextMenu contextMenu, final FileListField fileList, int ordinal,
			int priority) {

		MenuItem deleteFile = new MenuItem(LangRes.get(LangRes.DELETE), ordinal, priority) {

			public void run() {

				UiApplication.getUiApplication().invokeLater(new Runnable() {

					public void run() {

						fileList.deleteFile();
					}
				});

			}

		};

		contextMenu.addItem(deleteFile);
	}


	private static void addRefreashMenuItem(ContextMenu contextMenu, final FileListField fileList, int ordinal,
			int priority) {

		MenuItem refresh = new MenuItem(LangRes.get(LangRes.MENU_REFRESH), ordinal, priority) {

			public void run() {

				UiApplication.getUiApplication().invokeLater(new Runnable() {

					public void run() {

						fileList.refresh();
					}
				});

			}
		};

		contextMenu.addItem(refresh);
	}


	private static void addInstallCodMenuItem(ContextMenu contextMenu, final FileListField fileList, int ordinal,
			int priority) {

		MenuItem installCod = new MenuItem(LangRes.get(LangRes.MENU_INSTALL), ordinal, priority) {

			public void run() {

				fileList.navigationClick(0, 0);
			}
		};
		contextMenu.addItem(installCod);
	}


	private static void addExtractToFolderMenuItem(ContextMenu contextMenu, final FileListField fileList, int ordinal,
			int priority) {

		final FileItem thisItem = fileList.getThisItem();
		final String targetFolder = UtilCommon.getName(thisItem.getPath(), false) + "/";

		String extractToFolderText = UtilCommon
				.replaceString(LangRes.get(LangRes.MENU_EXTRACT_TO), "{1}", targetFolder);

		MenuItem extractToFolder = new MenuItem(extractToFolderText, ordinal, priority) {

			public void run() {

				final String targetURL = fileList.getFolderPathURL() + UtilCommon.toURLForm(targetFolder);

				UiApplication.getUiApplication().invokeLater(new Runnable() {

					public void run() {

						FileHandler.extractArchive(thisItem.getURL(), targetURL, fileList);
					}
				});

			}
		};

		contextMenu.addItem(extractToFolder);
	}


	private static void addExtractMenuItem(ContextMenu contextMenu, final FileListField fileList, int ordinal,
			int priority) {

		MenuItem extract = new MenuItem(LangRes.get(LangRes.MENU_TITLE_EXTRACT_FILES), ordinal, priority) {

			public void run() {

				final FileBrowsePopup browsePopup = new FileBrowsePopup(LangRes.get(LangRes.MENU_TITLE_EXTRACT_FILES));

				final FileItem thisItem = fileList.getThisItem();

				String defaultDestinationPath = fileList.getFolderPath();
				String destinationPath = defaultDestinationPath + UtilCommon.getName(thisItem.getPath(), false) + "/";
				browsePopup.setDestinationPath(destinationPath);
				browsePopup.setDefaultDestinationPath(defaultDestinationPath);

				browsePopup.focusOKButton();

				UiApplication.getUiApplication().invokeLater(new Runnable() {

					public void run() {

						String targetPath = browsePopup.show();

						if (targetPath == null) {
							// 选择了"取消"
							return;
						}

						final String targetURL = UtilCommon.toURLForm(targetPath);

						UiApplication.getUiApplication().invokeLater(new Runnable() {

							public void run() {

								FileHandler.extractArchive(thisItem.getURL(), targetURL, fileList);
							}
						});

					}
				});

			}
		};
		contextMenu.addItem(extract);

	}


	private static void addCompressMenuItem(ContextMenu contextMenu, final FileListField fileList, int ordinal,
			int priority) {

		MenuItem compress = new MenuItem(LangRes.get(LangRes.MENU_ADD_TO_ARCHIVE), ordinal, priority) {

			public void run() {

				FileItem itemToCompress = fileList.getThisItem();
				final CreateArchivePopup createArchivePopup = new CreateArchivePopup(itemToCompress, fileList);

				UiApplication.getUiApplication().invokeLater(new Runnable() {

					public void run() {

						createArchivePopup.show();
					}
				});

			}
		};
		contextMenu.addItem(compress);
	}


	private static void addCreateNewFolderMenuItem(ContextMenu contextMenu, final FileListField fileList, int ordinal,
			int priority) {

		MenuItem createNewFolder = new MenuItem(LangRes.get(LangRes.NEW_FOLDER_MENU), ordinal, priority) {

			public void run() {

				UiApplication.getUiApplication().invokeLater(new Runnable() {

					public void run() {

						FileHandler.createWithUI(fileList);
					}
				});

			}

		};
		contextMenu.addItem(createNewFolder);

	}


	private static void addAddToFavouriteMenuItem(ContextMenu contextMenu, final FileListField fileList, int ordinal,
			int priority) {

		MenuItem addToFavourite = new MenuItem(LangRes.get(LangRes.MENU_ADD_TO_FAVOURITES), ordinal, priority) {

			public void run() {

				UiApplication.getUiApplication().invokeLater(new Runnable() {

					public void run() {

						FavouritesData.add(fileList.getThisItem());
					}
				});

			}
		};
		contextMenu.addItem(addToFavourite);

	}


	private static void addSelectHereMenuItem(ContextMenu contextMenu, final FileListField fileList, int ordinal,
			int priority) {

		MenuItem selectHere = new MenuItem(LangRes.get(LangRes.MENU_SELECT_HERE), ordinal, priority) {

			public void run() {

				fileList.pickFolder();
			}
		};
		contextMenu.addItem(selectHere);

	}


	private static void addSelectThisSubFolderMenuItem(ContextMenu contextMenu, final FileListField fileList,
			int ordinal, int priority) {

		String folderName = UtilCommon.getFullFileName(fileList.getThisItem().getRawPath());
		String label = UtilCommon.replaceString(LangRes.get(LangRes.MENU_SELECT_A_FOLDER), "{1}", folderName);
		MenuItem selectThisFolder = new MenuItem(label, ordinal, priority) {

			public void run() {

				fileList.pickFile();
			}
		};
		contextMenu.addItem(selectThisFolder);

	}


	/**
	 * 打开文件项
	 */
	private static void addOpenItMenuItem(ContextMenu contextMenu, final FileListField fileList, int ordinal,
			int priority) {

		MenuItem openItem = new MenuItem(LangRes.get(LangRes.MENU_OPEN), ordinal, priority) {

			public void run() {

				fileList.navigationClick(0, 0);
			}

		};
		contextMenu.addItem(openItem);
	}


	private static void addDeleteFavouriteMenuItem(ContextMenu contextMenu, final FileListField fileList, int ordinal,
			int priority) {

		MenuItem deleteFromFavouriteItem = new MenuItem(LangRes.get(LangRes.MENU_DELETE_FROM_FAVOURITES), ordinal,
				priority) {

			public void run() {

				UiApplication.getUiApplication().invokeLater(new Runnable() {

					public void run() {

						FileItem thisItem = fileList.getThisItem();
						FavouritesData.delete(thisItem);
					}
				});

			}
		};
		contextMenu.addItem(deleteFromFavouriteItem);

	}

}
