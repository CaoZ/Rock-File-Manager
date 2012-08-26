
package RockManager.fileHandler;

import java.io.IOException;
import java.util.Enumeration;
import javax.microedition.content.Invocation;
import javax.microedition.content.Registry;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import net.rim.blackberry.api.browser.Browser;
import net.rim.blackberry.api.browser.BrowserSession;
import net.rim.device.api.servicebook.ServiceBook;
import net.rim.device.api.servicebook.ServiceRecord;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.container.MainScreen;
import RockManager.archive.ArchiveFile;
import RockManager.codInstaller.CodInstaller;
import RockManager.fileHandler.filePopup.operationPopup.FileRenamePopup;
import RockManager.fileHandler.filePopup.operationPopup.FolderCreatePopup;
import RockManager.fileList.FileItem;
import RockManager.fileList.FileListField;
import RockManager.languages.LangRes;
import RockManager.ui.progressPopup.FileDeleteProgressPopup;
import RockManager.ui.progressPopup.ProgressPopup;
import RockManager.ui.screen.fileScreen.ArchiveScreen;
import RockManager.ui.statusBar.AutoHideStatusBar;
import RockManager.util.UtilCommon;
import RockManager.util.ui.BaseDialog;


/**
 * 处理文件打开等操作。
 */
public class FileHandler {

	private static Registry REGISTRY = Registry.getRegistry(FileListField.class.getName());


	/**
	 * 打开文件处理程序。
	 * 
	 * @param thisItem
	 */
	public static void openFile(FileItem fileItem) {

		String filePath = fileItem.getURL();
		String suffix = fileItem.getSuffix();

		if (suffix.equals("zip") || suffix.equals("rar")) {
			// 是压缩文件类型，打开它。
			openArchiveFile(fileItem);
			return;
		}

		if (suffix.equals("cod")) {
			// 是cod文件，尝试安装。
			installCod(fileItem);
			return;
		}

		if (suffix.equals("htm") || suffix.equals("html")) {
			// 是html文件，调用浏览器打开它。
			boolean succeed = openHTMLFile(filePath);
			if (succeed) {
				// 尝试使用调用浏览器的方式打开html, 若打开失败再尝试使用Invocation打开。
				return;
			}
		}

		Invocation invocation = new Invocation(filePath);

		try {
			REGISTRY.invoke(invocation);
		} catch (Exception e) {
			// 提示打开失败了。
			MainScreen parentScreen = getParentMainScreen();

			if (parentScreen != null) {
				// 提示无法打开此文件。
				AutoHideStatusBar noSuchAppStatusbar = new AutoHideStatusBar(LangRes.get(LangRes.CAN_NOT_OPEN), 1500);
				noSuchAppStatusbar.addTo(parentScreen);
			}

			e.printStackTrace();
		}

	}


	/**
	 * 安装cod文件，安装前要求确认。
	 * 
	 * @param fileItem
	 */
	private static void installCod(FileItem fileItem) {

		String codName = CodInstaller.getCodName(fileItem.getName(true));
		String installConfirmAsk = LangRes.get(LangRes.INSTALL_CONFIRM_ASK);
		String message = UtilCommon.replaceString(installConfirmAsk, "{1}", codName);
		Bitmap questionBMP = Bitmap.getPredefinedBitmap(Bitmap.QUESTION);

		BaseDialog installConfirm = new BaseDialog(Dialog.D_YES_NO, message, Dialog.YES, questionBMP, 0);

		int answer = installConfirm.doModal();

		if (answer == Dialog.YES) {
			String fileURL = fileItem.getURL();
			new CodInstaller(fileURL);
		}

	}


	/**
	 * 如果当前组件有父屏幕且是MainScreen则返回，否则返回null.
	 * 
	 * @return
	 */
	private static MainScreen getParentMainScreen() {

		Screen parentScreen = UiApplication.getUiApplication().getActiveScreen();
		if (parentScreen instanceof MainScreen) {
			return (MainScreen) parentScreen;
		} else {
			return null;
		}
	}


	/**
	 * 在一个新的Screen打开这个压缩文件。
	 * 
	 * @param fileItem
	 */
	private static void openArchiveFile(FileItem fileItem) {

		try {
			ArchiveScreen archiveScreen = new ArchiveScreen(fileItem.getURL());
			// 设置地址栏图标为压缩文件图标。
			archiveScreen.setIcon(fileItem.getIcon());
			UiApplication.getUiApplication().pushScreen(archiveScreen);
		} catch (IOException e) {
			// 不能打开此压缩文件。
			UtilCommon.trace("Failed to open this archive: " + UtilCommon.getErrorMessage(e));
		}

	}


	/**
	 * 调用浏览器打开html文件。
	 * 
	 * @param path
	 * @return 成功调用浏览器则返回true, 否则返回false, 表明需后续处理。
	 */
	private static boolean openHTMLFile(String path) {

		ServiceBook sb = ServiceBook.getSB();
		ServiceRecord[] records = sb.findRecordsByCid("BrowserConfig");

		// 存在BrowserConfig时才会调用成功，否则只会显示无ServiceBook，无法打开浏览器。
		if (records.length > 0) {
			BrowserSession browserSession = Browser.getDefaultSession();
			browserSession.displayPage(path);
			return true;
		} else {
			return false;
		}

	}


	/**
	 * 是否是空文件夹。
	 * 
	 * @param folderURL
	 * @return
	 */
	public static boolean isEmptyFolder(String folderURL) {

		boolean empty = false;

		FileConnection fconn = null;
		try {
			fconn = (FileConnection) Connector.open(folderURL, Connector.READ);
			Enumeration allFiles = fconn.list("*", true);
			if (allFiles.hasMoreElements() == false) {
				empty = true;
			}
		} catch (Exception e) {
		} finally {
			if (fconn != null) {
				try {
					fconn.close();
				} catch (Exception e) {
				}
			}
		}

		return empty;

	}


	/**
	 * 文件是否存在且是文件夹。
	 * 
	 * @param folderURL
	 * @return
	 */
	public static boolean isFolderExists(String folderURL) {

		boolean exists = false;

		FileConnection fconn = null;
		try {
			fconn = (FileConnection) Connector.open(folderURL, Connector.READ);
			exists = fconn.isDirectory();
		} catch (Exception e) {
		} finally {
			if (fconn != null) {
				try {
					fconn.close();
				} catch (Exception e) {
				}
			}
		}

		return exists;

	}


	/**
	 * 删除文件或文件夹.
	 * 
	 * @param indicator
	 * @param filePath
	 * @throws IOException
	 */
	public static void deleteFile(String fileURL, FileDeleteProgressPopup indicator) throws Exception {

		FileConnection fconn = null;

		try {
			fconn = (FileConnection) Connector.open(fileURL);

			if (fconn.isDirectory() == false) {
				try {
					fconn.delete(); // 删除一个文件

					if (indicator != null) {
						indicator.setDeletedName(fconn.getName());
						indicator.plusDeletedNumber();
					}

				} catch (Exception e) {
					// IOException or ControlledException(试图删除一些系统的文件时)
					// 展示给用户的地址。
					String exceptionPath = "file://" + fconn.getPath() + fconn.getName();
					throw new Exception(UtilCommon.getErrorMessage(e) + " @ " + exceptionPath);
				}

			} else {

				Enumeration allFiles = fconn.list("*", true);
				Exception firstError = null;

				while (allFiles.hasMoreElements()) {
					String thisFile = (String) allFiles.nextElement();
					try {
						deleteFile(fileURL + thisFile, indicator);
					} catch (Exception e) {
						// 仅捕获第一个异常，这是根本原因，也可能是后续异常产生的原因。
						if (firstError == null) {
							firstError = e;
						}
					}
				}

				if (firstError != null) {

					throw firstError;

				} else {

					try {
						fconn.delete(); // 删除所有子文件后删除这个目录

						if (indicator != null) {
							indicator.setDeletedName(fconn.getName());
							indicator.plusDeletedNumber();
						}

					} catch (Exception e) {
						String exceptionPath = "file://" + fconn.getPath() + fconn.getName();
						throw new Exception(UtilCommon.getErrorMessage(e) + " @ " + exceptionPath);
					}

				}

			}

		} catch (Exception e) {
			throw e;
		} finally {
			if (fconn != null) {
				try {
					fconn.close();
				} catch (Exception e) {
				}
			}
		}

	}


	/**
	 * 弹出重命名窗口，准备重命名。
	 * 
	 * @param fileItem
	 * @param fileListField
	 */
	public static void renameWithUI(FileItem fileItem, FileListField fileListField) {

		FileRenamePopup fileRenamePopup = new FileRenamePopup(fileItem, fileListField);
		UiApplication.getUiApplication().pushScreen(fileRenamePopup);

	}


	/**
	 * 弹出新建文件夹窗口。
	 * 
	 * @param fileList
	 */
	public static void createWithUI(FileListField fileList) {

		FolderCreatePopup folderCreatePopup = new FolderCreatePopup(fileList);
		UiApplication.getUiApplication().pushScreen(folderCreatePopup);

	}


	/**
	 * 弹出删除文件窗口，准备删除。
	 * 
	 * @param thisItem
	 */
	public static void deleteWithUI(FileItem thisItem) {

		String deleteConfirmAsk = LangRes.get(LangRes.DELETE_CONFIRM_ASK);
		String message = UtilCommon.replaceString(deleteConfirmAsk, "{1}", thisItem.getDisplayName());
		Bitmap bitmap = Bitmap.getPredefinedBitmap(Bitmap.QUESTION);

		BaseDialog deleteConfirm = new BaseDialog(Dialog.D_YES_NO, message, Dialog.YES, bitmap, 0);

		int answer = deleteConfirm.doModal();
		
		if (answer == Dialog.YES) {

			final String fileURL = thisItem.getURL();

			if (thisItem.isFile() || isEmptyFolder(fileURL)) {

				try {
					deleteFile(fileURL, null);
				} catch (Exception e) {
					UtilCommon.trace("Failed to delete this file: " + UtilCommon.getErrorMessage(e));
				}

			} else {

				// invokeLater: 等待前窗口隐藏后再弹出指示窗口。
				UiApplication.getUiApplication().invokeLater(new Runnable() {

					public void run() {

						// 删除多个文件，弹出进度指示框。
						FileDeleteProgressPopup deletePopup = new FileDeleteProgressPopup(fileURL);
						UiApplication.getUiApplication().pushScreen(deletePopup);
					}
				});

			}

		}

	}


	/**
	 * 检查目标目录，若不存在则创建，可以多级创建。 <br>
	 * 注意:除非明确知道目标路径是目录才可调用此方法，否则请使用createTargetFile(String), 那会自动判断是文件还是目录。<br>
	 * same as createTargetFolder(targetURL, false)。
	 */
	public static void createTargetFolder(String targetURL) throws Exception {

		createTargetFolder(targetURL, false);
	}


	/**
	 * 检查目标目录，若不存在则创建，可以多级创建。 <br>
	 * 注意:除非明确知道目标路径是目录才可调用此方法，否则请使用createTargetFile(String), 那会自动判断是文件还是目录。
	 * 
	 * @see #createTargetFile(String)
	 */
	public static void createTargetFolder(String targetURL, boolean isHidden) throws IOException {

		FileConnection fconn = null;
		String failedURL = null;

		try {

			fconn = (FileConnection) Connector.open(targetURL);

			if (fconn.isDirectory() == false) {
				// 不存在此目录，需创建。

				String parentDirURL = UtilCommon.getParentDir(targetURL);
				try {
					// 要创建的父级目录对属性无要求，默认不隐藏。
					createTargetFolder(parentDirURL, false);
				} catch (Exception e) {
					// 创建父目录失败。
					failedURL = parentDirURL;
					throw new IOException(e.getMessage());
				}

				// 创建此目录
				fconn.mkdir();
				fconn.setHidden(isHidden);
			}

		} catch (Exception e) {

			if (failedURL == null) {
				// 创建本目录失败。
				failedURL = targetURL;
			}
			throw new IOException("Failed to create folder \"" + UtilCommon.URLtoPath(failedURL) + "\".");

		} finally {

			if (fconn != null) {
				try {
					fconn.close();
				} catch (Exception e) {
				}

			}

		}

	}


	/**
	 * 检查目标文件是否存在（文件或目录），若不存在则创建，可以多级创建。<br>
	 * same as createTargetFile(targetURL, false)。
	 */
	public static void createTargetFile(String targetURL) throws Exception {

		createTargetFile(targetURL, false);
	}


	/**
	 * 检查目标文件是否存在（文件或目录），若不存在则创建，可以多级创建。
	 */
	public static void createTargetFile(String targetURL, boolean isHidden) throws IOException {

		if (UtilCommon.isFolder(targetURL)) {
			createTargetFolder(targetURL, isHidden);
		} else {

			FileConnection fconn = null;

			try {

				fconn = (FileConnection) Connector.open(targetURL);

				if (fconn.exists() == false) {

					String parentDir = UtilCommon.getParentDir(targetURL);
					createTargetFolder(parentDir, false);
					fconn.create();
					fconn.setHidden(isHidden);

				} else {

					// 若目标文件已存在，则设置长度为0（相当于先删除然后创建）。
					fconn.setHidden(isHidden);
					fconn.truncate(0);

				}

			} catch (Exception e) {

				throw new IOException("Failed to create file \"" + UtilCommon.URLtoPath(targetURL) + "\".");

			} finally {

				if (fconn != null) {
					try {
						fconn.close();
					} catch (Exception e) {
					}
				}

			}

		}

	}


	/**
	 * 解压压缩文件到指定文件夹。
	 * 
	 * @param archiveURL
	 * @param targetURL
	 */
	public static void extractArchive(final String archiveURL, final String targetURL,
			final FileListField parentFileList) {

		final ProgressPopup progressPopup = new ProgressPopup();
		progressPopup.setTitle(LangRes.get(LangRes.TITLE_EXTRACTING));

		final Thread extractThread = new Thread() {

			public void run() {

				ArchiveFile archive = null;

				try {
					archive = new ArchiveFile(archiveURL);
					archive.extractAll(targetURL, progressPopup);
				} catch (Exception e) {
					String message = "Failed to extract: " + UtilCommon.getErrorMessage(e);
					UtilCommon.alert(message, true);
				} finally {
					if (archive != null) {
						archive.close();
					}
				}

				UiApplication.getUiApplication().invokeLater(new Runnable() {

					public void run() {

						progressPopup.close();

						if (UtilCommon.getParentDir(targetURL).equals(parentFileList.getFolderPathURL())) {
							String targetFolderName = UtilCommon.URLtoPath(UtilCommon.getName(targetURL, false));
							parentFileList.setItemToFocus(targetFolderName, FileItem.TYPE_DIR);
							parentFileList.refresh();
						}

					}
				});

			}
		};

		UiApplication.getUiApplication().pushScreen(progressPopup);

		// invokeLater: 等窗口出现了再开始解压。
		UiApplication.getUiApplication().invokeLater(new Runnable() {

			public void run() {

				extractThread.start();
			}
		});

	}

}
