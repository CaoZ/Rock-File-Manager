
package RockManager.codInstaller;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Vector;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import net.rim.device.api.io.IOUtilities;
import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.system.ApplicationManager;
import net.rim.device.api.system.ApplicationManagerException;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.CodeModuleManager;
import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.ui.UiApplication;
import RockManager.archive.ArchiveFile;
import RockManager.fileHandler.FileHandler;
import RockManager.languages.LangRes;
import RockManager.ui.progressPopup.ProgressPopup;
import RockManager.util.CapabilityUtil;
import RockManager.util.UtilCommon;
import RockManager.util.ui.BaseDialog;


public class CodInstaller {

	/**
	 * 指示解压、安装进度的popup, 这样若是需先解压然后安装时两个过程之间不会有不必要的popup关闭再打开的情况了。
	 */
	private ProgressPopup installerPopup;


	public CodInstaller(final String codPath) {

		new Thread() {

			public void run() {

				try {
					installCod(codPath);
				} catch (Exception e) {
					e.printStackTrace();
					UtilCommon.trace("Install failed: " + UtilCommon.getErrorMessage(e));
				}
			}

		}.start();

	}


	private void installCod(String codPath) throws Exception {

		FileConnection cod = (FileConnection) Connector.open(codPath);
		String tempDirPath = null;

		if (isArchive(cod)) {

			// archive, need unzip
			tempDirPath = extractCod(cod);

			FileConnection codDir = (FileConnection) Connector.open(tempDirPath);
			installModules(codDir);
			codDir.close();

			FileHandler.deleteFile(tempDirPath, null);

		} else {

			installModules(cod);

		}

		cod.close();

	}


	/**
	 * 解压cod文件。
	 * 
	 * @param cod
	 * @return 解压路径。
	 * @throws IOException
	 */
	private String extractCod(final FileConnection cod) throws Exception {

		installerPopup = new ProgressPopup();
		installerPopup.setTitle(LangRes.get(LangRes.TITLE_EXTRACTING));

		UiApplication.getUiApplication().invokeAndWait(new Runnable() {

			public void run() {

				UiApplication.getUiApplication().pushScreen(installerPopup);

			}
		});

		try {
			Thread.sleep(300); // 等待窗口出现，使UI流畅
		} catch (Exception e) {
		}

		ArchiveFile codArchive = new ArchiveFile(cod.getURL());

		String tempDirPath = "file://" + cod.getPath() + getCodName(cod.getName()) + "_RockInstaller/";
		codArchive.extractAll(tempDirPath, installerPopup);
		codArchive.close();

		return tempDirPath;

	}


	/**
	 * 若是文件，安装这个cod, 若是目录，安装此目录下所有cod
	 * 
	 * @param cod
	 * @throws IOException
	 */
	private void installModules(FileConnection cod) throws IOException {

		Vector allCodPaths = new Vector();

		if (cod.exists() && cod.isDirectory() == false) {
			// 给定的File是文件，搜寻同名cod
			String dirPath = "file://" + cod.getPath();
			boolean isEncrypted = (UtilCommon.getSuffix(cod.getName()) == "rem");
			String codName = getCodName(cod.getName());

			for (int volume = 0;; volume++) {

				String volumeString = (volume > 0) ? ("-" + volume) : "";
				String thisCodName = codName + volumeString + ".cod";
				if (isEncrypted) {
					thisCodName += ".rem";
				}
				FileConnection thisCodFile = (FileConnection) Connector.open(dirPath + thisCodName);
				if (thisCodFile.exists() && thisCodFile.isDirectory() == false) {
					allCodPaths.addElement(dirPath + thisCodName);
					thisCodFile.close();
				} else {
					thisCodFile.close();
					break;
				}
			}

		} else if (cod.isDirectory()) {

			String basePath = cod.getURL();
			Enumeration codPaths = cod.list("*.cod", true);
			if (codPaths.hasMoreElements() == false) {
				// can't find any cod, may be it's a encrypted SDCard.
				codPaths = cod.list("*.cod.rem", true);
			}

			while (codPaths.hasMoreElements()) {
				String thisCodPath = basePath + (String) codPaths.nextElement();
				allCodPaths.addElement(thisCodPath);
			}

		}

		if (allCodPaths.isEmpty()) {
			// weird, but no cods will be loaded.
			UtilCommon.alert("No CODs need to be installed.", true);

			UiApplication.getUiApplication().invokeLater(new Runnable() {

				public void run() {

					installerPopup.close();
				}
			});

			return;
		}

		String[] codPaths = new String[allCodPaths.size()];
		allCodPaths.copyInto(codPaths);

		installAllModules(codPaths);

	}


	/**
	 * 安装列表中所有的cod.
	 * 
	 * @param codPaths
	 * @throws IOException
	 */
	private void installAllModules(String[] codPaths) throws IOException {

		final ProgressPopup popup = (installerPopup == null) ? new ProgressPopup() : installerPopup;
		popup.setTitle(LangRes.get(LangRes.INSTALLING));
		popup.setProgressName("initialing...");
		popup.setProgressRate(0);

		// 确保在实际载入module前显示进度窗口，因为载入第一个module可能需花费较多时间。

		UiApplication.getUiApplication().invokeLater(new Runnable() {

			public void run() {

				if (installerPopup == null) {
					UiApplication.getUiApplication().pushScreen(popup);
				}

				synchronized (popup) {
					popup.notifyAll();
				}

			}

		});

		// 载入第一个module可能需花费较多时间（屏幕上出现像钟一样的旋转标志）,
		// 可能导致popUp还未出现时却被阻塞了，需等一段时间才显示的情况。此处wait(), 直到popup出现才开始安装。
		// 但即使这样也不能保证popup在安装前出现，因为pushScreen只是将screen推入显示堆栈，screen虽然layout了，但可能还未开始绘制（虽然isDisplayed(),
		// isVisible()返回true, 但还是肉眼不可见的）。
		// 为改善这种情况，Thread.sleep()

		synchronized (popup) {
			try {
				popup.wait();
				Thread.sleep(250); // 等待直到popup完全显示。popup出现时有200ms的动画效果。
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		ApplicationDescriptor[] des = null;
		int totalCods = codPaths.length;
		int[] moduleHandlers = new int[totalCods];
		boolean isTheme = false;
		boolean installSuccessed = true;
		String installFailReason = null;

		int transitionHandle = CodeModuleManager.beginTransaction();

		for (int i = 0; i < totalCods; i++) {

			FileConnection thisCod = (FileConnection) Connector.open(codPaths[i]);
			popup.setProgressName(thisCod.getName());

			InputStream codInput = thisCod.openInputStream();
			int codSize = (int) thisCod.fileSize();
			byte[] codBytes = IOUtilities.streamToBytes(codInput);
			codInput.close();

			thisCod.close();

			int handler = CodeModuleManager.createNewModule(codSize, codBytes, codSize);

			if (handler == 0) {
				// 没有足够空间
				installSuccessed = false;
				installFailReason = "Invalid cod file or not enough free space.";
				break;
			}
			int result = CodeModuleManager.saveNewModule(handler);
			if (result != CodeModuleManager.CMM_OK && result != CodeModuleManager.CMM_OK_MODULE_OVERWRITTEN) {
				// 安装遇到错误
				installSuccessed = false;
				installFailReason = "Failed to save cod.";
				break;
			}

			moduleHandlers[i] = handler; // 此module安装成功，记录下来。

			String moduleName = CodeModuleManager.getModuleName(handler);
			if (moduleName.startsWith("com_plazmic_theme")) {
				// 是个主题，使用CodeModuleManager安装的话只有重启机器才能看到效果。
				isTheme = true;
			}

			popup.setProgressRate((i + 1) * 100 / totalCods);

			if (des == null) {
				des = CodeModuleManager.getApplicationDescriptors(handler);
			}

		}

		CodeModuleManager.endTransaction(transitionHandle);

		try {
			Thread.sleep(300); // 安装完毕，展示100%的进度。
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		UiApplication.getUiApplication().invokeAndWait(new Runnable() {

			public void run() {

				popup.close();

			}
		});

		if (installSuccessed && isTheme) {

			// 若是主题，使用CodeModuleManager安装的话只有重启机器才能看到效果。
			CodeModuleManager.promptForResetIfRequired();
			return;
		}

		String message = null;

		int lang_OK = CapabilityUtil.isPhysicalKeyboardAvailable() ? LangRes.BUTTON_LABEL_OK_S
				: LangRes.BUTTON_LABEL_OK;
		int lang_Run = CapabilityUtil.isPhysicalKeyboardAvailable() ? LangRes.BUTTON_LABEL_RUN_S
				: LangRes.BUTTON_LABEL_RUN;

		String choice_OK = LangRes.get(lang_OK);
		String choice_Run = LangRes.get(lang_Run);

		int choiceValue_OK = 1;
		final int choiceValue_Run = 2;
		String[] choices = { choice_OK };
		int[] choiceValues = { choiceValue_OK };
		int defaultValue = choiceValue_OK;
		Bitmap appIcon = null;
		ApplicationDescriptor appDes = null;
		String appName = null;

		if (installSuccessed == false) {
			// 安装失败。

			message = LangRes.get(LangRes.INSTALL_FAILED) + installFailReason;
			appIcon = Bitmap.getPredefinedBitmap(Bitmap.EXCLAMATION);

			// 回滚操作，删除安装了一半的module.
			for (int i = 0; i < totalCods; i++) {
				if (moduleHandlers[i] != 0) {
					try {
						CodeModuleManager.deleteModuleEx(moduleHandlers[i], true);
					} catch (Exception e) {
						// Delete failed.
					}

				}
			}

		} else {

			// 安装成功。

			if (des != null && des.length > 0) {

				// 安装成功，并且安装的很可能是uiApplication.
				appDes = des[0];

				try {
					appName = appDes.getLocalizedName(); // 有时会抛出异常。
				} catch (Exception e) {
					appName = appDes.getName();
				}

				message = UtilCommon.replaceString(LangRes.get(LangRes.INSTALL_SUCCESSED_INFO), "{1}", appName);
				EncodedImage appIconEncoded = appDes.getEncodedIcon();
				appIcon = appIconEncoded == null ? null : appIconEncoded.getBitmap();

				String[] newChoices = { choice_OK, choice_Run };
				choices = newChoices;
				int[] newChoiceValues = { choiceValue_OK, choiceValue_Run };
				choiceValues = newChoiceValues;

			} else {

				// 安装成功，安装的可能是lib.
				String codName = CodeModuleManager.getModuleName(moduleHandlers[0]);
				message = UtilCommon.replaceString(LangRes.get(LangRes.INSTALL_SUCCESSED_INFO), "{1}", codName);
				appIcon = Bitmap.getPredefinedBitmap(Bitmap.INFORMATION);

			}

		}

		final BaseDialog dialog = new BaseDialog(message, choices, choiceValues, defaultValue, appIcon);

		final ApplicationDescriptor appDes_F = appDes;
		final String appName_F = appName;

		UiApplication.getUiApplication().invokeLater(new Runnable() {

			public void run() {

				dialog.doModal();
				int answer = dialog.getSelectedValue();
				if (answer == choiceValue_Run) {

					ApplicationManager appManager = ApplicationManager.getApplicationManager();
					int appProgressId = appManager.getProcessId(appDes_F);
					boolean alreadyRunning = (appProgressId != -1);

					if (alreadyRunning) {
						// 已经在运行了(如，有些程序可以设置为安装后自动运行的)，让它到前台来。
						appManager.requestForeground(appProgressId);
					} else {
						try {
							ApplicationManager.getApplicationManager().runApplication(appDes_F);
						} catch (ApplicationManagerException e) {
							UtilCommon.trace("Failed to launch \"" + appName_F + "\", " + UtilCommon.getErrorMessage(e));
						}
					}

				}

			}
		});

	}


	/**
	 * 通过一个cod文件名获取code module的name. 例如：输入"happy.cod",
	 * "happy-1.cod"或"happy-1.cod.rem"将返回happy。
	 * 
	 * @param name
	 * @return
	 */
	private String getCodName(String name) {

		boolean isEncrypted = (UtilCommon.getSuffix(name) == "rem");
		if (isEncrypted) {
			name = UtilCommon.getName(name, false);
		}

		int end = name.lastIndexOf('-');
		if (end < 0) {
			end = name.lastIndexOf('.');
		}
		return name.substring(0, end);
	}


	/**
	 * 通过cod文件头判断是否是zip压缩文件。较大的cod文件是将较小的cod打包而成。
	 * 
	 * @param cod
	 * @return
	 * @throws IOException
	 */
	private boolean isArchive(FileConnection cod) throws IOException {

		InputStream in = cod.openInputStream();
		byte[] header = new byte[2];
		in.read(header);
		in.close();
		if (header[0] == 'P' && header[1] == 'K') {
			return true;
		} else {
			return false;
		}
	}

}
