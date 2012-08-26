
package RockManager.ui.screen.fileScreen;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.container.VerticalFieldManager;
import RockManager.fileList.FileListField;
import RockManager.ui.ScreenHeightChangeEvent;
import RockManager.util.ui.AnimatedMainScreen;
import RockManager.util.ui.VFMwithScrollbar;
import RockManager.util.ui.VFMwithScrollbarControl;


public class FileScreen extends AnimatedMainScreen implements VFMwithScrollbarControl {

	private MainManager mainVFM;

	protected VFMwithScrollbar vfm;

	private FileListField fileList;


	protected FileScreen() {

		super(NO_VERTICAL_SCROLL | NO_SYSTEM_MENU_ITEMS);

		// VFM, 主要目的是添加一个阴影效果及选择统计标签。
		// 也曾试图添加一个浅蓝色渐变背景，但BlackBerry只能显示65000色，表现不出渐变效果，遂移除。
		mainVFM = new MainManager();

		vfm = new VFMwithScrollbar(USE_ALL_WIDTH | USE_ALL_HEIGHT);
		mainVFM.addToContent(vfm);

		add(mainVFM);

	}


	public FileScreen(String initialURL) {

		this();

		fileList = new FileListField(initialURL);
		fileList.setManager(mainVFM);

		fileList.setClipboardAllowed(true);
		fileList.registerJournalListener();
		fileList.registerRootChangeListener();

		setScrollBarTrigger(fileList);
		setTitleField(fileList);

		fileList.setEditable(true);
		vfm.add(fileList);

		fileList.setFocus();

	}


	public MainManager getFileScreenMainManager() {

		return mainVFM;
	}


	protected boolean keyChar(char key, int status, int time) {

		if (getFileList().getKeywordField().isFocus()) {
			// 焦点正在关键字输入框中。
			boolean consumed = getFileList().getKeywordField().keyChar(key, status, time);
			if (consumed) {
				// 正常的文本编辑操作
				return true;
			}
		}
		// 将事件分发到FileList中处理。
		boolean consumed = getFileList().keyChar(key, status, time);
		if (consumed) {
			return true;
		} else {
			return super.keyChar(key, status, time);
		}

	}


	protected boolean onSavePrompt() {

		// 避免保存对话框的出现。
		return true;
	}


	public void close() {

		getFileList().unRegisterJournalListener();
		getFileList().unRegisterRootChangeListener();
		super.close();

	}


	/**
	 * 获取文件列表。
	 * 
	 * @return
	 */
	protected FileListField getFileList() {

		return fileList;
	}


	/**
	 * 设置地址栏的图标
	 * 
	 * @param icon
	 */
	public void setIcon(Bitmap icon) {

		getFileList().getAddressBar().setIcon(icon);
	}


	/**
	 * 设置当组件高度调整时是否让滚动条自动出现。有时不需要让它出现，如暂时的出个statusZBar.
	 */
	public void setScrollBarAutoShow(boolean value) {

		vfm.setScrollBarAutoShow(value);
	}


	/**
	 * 从fileListField中提取addressBar、searchBox并设置为screen的title.
	 * 
	 * @param fileListField
	 */
	protected void setTitleField(FileListField fileListField) {

		VerticalFieldManager titleField = new VerticalFieldManager();

		// int colorTop = 0x444444;
		// int colorBottom = 0x111111;
		// Background background =
		// BackgroundFactory.createLinearGradientBackground(colorTop,
		// colorTop, colorBottom, colorBottom);
		// titleField.setBackground(background);

		AddressAndClipboard addressAndClipboard = new AddressAndClipboard(fileListField);

		titleField.add(addressAndClipboard);
		titleField.add(fileListField.getSearchBox());

		setTitle(titleField);

	}


	/**
	 * 将一个FileListField与vfm绑定，在必要时使滚动条出现。
	 * 
	 * @param fileListField
	 */
	protected void setScrollBarTrigger(FileListField fileListField) {

		fileListField.setChangeListener(new FieldChangeListener() {

			public void fieldChanged(Field field, int context) {

				vfm.hideTip(); // 很可能是进入了新文件夹，隐藏原来的提示框。

				if (context == ScreenHeightChangeEvent.SCREEN_HEIGHT_NOT_CHANGED) {
					// 切换了目录但高度没变，手动激发使滚动条出现
					vfm.reComputeSliderbar(true);
				}

			}
		});
	}

}
