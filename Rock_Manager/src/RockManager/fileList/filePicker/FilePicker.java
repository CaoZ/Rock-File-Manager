
package RockManager.fileList.filePicker;

import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.LabelField;
import RockManager.fileList.AddressBar;
import RockManager.fileList.FileListField;
import RockManager.languages.LangRes;
import RockManager.ui.ScreenHeightChangeEvent;
import RockManager.util.ui.BasePopupScreen;
import RockManager.util.ui.VFMwithScrollbar;


public class FilePicker extends BasePopupScreen implements FilePickerListener {

	private VFMwithScrollbar vfm;

	private FileListField fileList;

	private String selectedPath;


	/**
	 * 生成一个新FilePicker.
	 * 
	 * @param initialURL
	 *            初始路径。若为null则为listRoot的diskLisk.
	 */
	public FilePicker(String initialURL) {

		super(NO_VERTICAL_SCROLL, DEFAULT_CLOSE | DEFAULT_MENU | NO_SYSTEM_MENU_ITEMS);
		useSystemTheme();

		setTitle(LangRes.getString(LangRes.TITLE_SELECT_DESTINATION));
		LabelField titleField = getTitleField();
		titleField.setMargin(0, 0, 0, 6); // AddressBar 在不绘制图标时与左侧的距离是6.

		vfm = new VFMwithScrollbar(USE_ALL_HEIGHT);
		vfm.useWhiteVersionSlider();

		fileList = new FileListField(initialURL);
		fileList.activePickerMode();
		fileList.registerRootChangeListener();
		fileList.registerJournalListener();
		fileList.setClipboardAllowed(false);
		fileList.setChangeListener(createFileListListener());
		fileList.setFilePickerListener(this);

		vfm.add(fileList);

		AddressBar addressBar = fileList.getAddressBar();
		addressBar.setDrawIcon(false);

		add(addressBar);
		add(vfm);

	}


	private FieldChangeListener createFileListListener() {

		FieldChangeListener listener = new FieldChangeListener() {

			public void fieldChanged(Field field, int context) {

				vfm.hideTip(); // 很可能是进入了新文件夹，隐藏原来的提示框。

				if (context == ScreenHeightChangeEvent.SCREEN_HEIGHT_NOT_CHANGED) {
					// 切换了目录但高度没变，手动激发使滚动条出现
					vfm.reComputeSliderbar(true);
				}
			}
		};

		return listener;

	}


	/**
	 * 使FilePicker出现(Modal Screen)。
	 * 
	 * @return 选中的文件/目录的路径, 若没有选择，返回null。
	 */
	public String show() {

		final FilePicker picker = this;

		UiApplication.getUiApplication().invokeAndWait(new Runnable() {

			public void run() {

				UiApplication.getUiApplication().pushModalScreen(picker);
			}
		});

		return selectedPath;

	}


	public void close() {

		fileList.unRegisterRootChangeListener();
		fileList.unRegisterJournalListener();
		super.close();
	}


	public void selectionDone(String filePath) {

		selectedPath = filePath;
		close();
	}

}
