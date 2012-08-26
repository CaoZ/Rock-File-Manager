
package RockManager.ui.screen.fileScreen;

import java.io.IOException;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.UiApplication;
import RockManager.archive.ArchiveListField;
import RockManager.archive.indicator.ArchiveLoadingIndicator;
import RockManager.fileList.FileListField;
import RockManager.languages.LangRes;
import RockManager.ui.statusBar.StatusBar;
import RockManager.util.UtilCommon;
import RockManager.util.quickExit.QuickExitScreen;


public class ArchiveScreen extends FileScreen implements QuickExitScreen {

	private ArchiveListField archiveList;


	public ArchiveScreen(String fileURL) throws IOException {

		super();

		archiveList = new ArchiveListField(fileURL);
		archiveList.setManager(getFileScreenMainManager());

		setScrollBarTrigger(archiveList);
		setTitleField(archiveList);

		vfm.add(archiveList);
		archiveList.setFocus();

		// 载入时不显示文字。
		archiveList.setEmptyString("", DrawStyle.HCENTER);
		boolean archiveTooBig = archiveList.isTooBig();

		if (archiveTooBig) {
			// 初始化文件结构可能需些时间，显示进度条。

			StatusBar statusBar = new StatusBar(LangRes.get(LangRes.LOADING), StatusBar.STYLE_BLUE);
			final ArchiveLoadingIndicator indicator = new ArchiveLoadingIndicator(statusBar);
			indicator.setProgress(0);
			setStatus(statusBar);

			final Screen thisScreen = this;

			// 从Event Dispatch Thread中分离出来。
			new Thread() {

				public void run() {

					boolean openSucceed = true;

					try {
						archiveList.initialArchive(indicator);
					} catch (IOException e) {
						openSucceed = false;
						UtilCommon.alert(UtilCommon.getErrorMessage(e), true);
					}

					final boolean openFailed = !openSucceed;

					// 载入后将进度条隐藏，须获得eventLocker
					UiApplication.getUiApplication().invokeLater(new Runnable() {

						public void run() {

							if (!openFailed) {
								// 打开成功。
								setStatus(null);
							} else {
								// 打开失败。
								thisScreen.close();
							}

						}
					});

				}

			}.start();

		} else {
			// 初始化文件结构可能较快，不显示进度条，直接初始化。
			archiveList.initialArchive(null);
		}

		archiveList.registerRootChangeListener();

	}


	protected FileListField getFileList() {

		return archiveList;
	}


	public void close() {

		super.close();
		// super.close()中会unRegisterRootChangeListener()。

		doCleanJob();

	}


	public void doCleanJob() {

		try {
			// 关闭文件，释放文件连接。
			archiveList.closeArchiveFile();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
