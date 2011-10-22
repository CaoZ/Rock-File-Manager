
package RockManager.ui.screen.fileScreen;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.container.VerticalFieldManager;
import net.rim.device.api.ui.decor.Background;
import net.rim.device.api.ui.decor.BackgroundFactory;
import RockManager.fileList.FileListField;
import RockManager.ui.ScreenHeightChangeEvent;
import RockManager.util.ui.AnimatedMainScreen;
import RockManager.util.ui.VFMwithScrollbar;
import RockManager.util.ui.VFMwithScrollbarControl;
import RockManager.util.ui.VFMwithTopShadow;


public class FileScreen extends AnimatedMainScreen implements VFMwithScrollbarControl {

	protected VFMwithScrollbar vfm;

	private FileListField fileList;


	public FileScreen() {

		super(NO_VERTICAL_SCROLL | NO_SYSTEM_MENU_ITEMS);

		// outerVFM, 主要目的是添加一个阴影效果。
		// 也曾试图添加一个浅蓝色渐变背景，但BlackBerry只能显示65000色，表现不出渐变效果，遂移除。
		VFMwithTopShadow outerVFM = new VFMwithTopShadow(USE_ALL_WIDTH | USE_ALL_HEIGHT);
		Bitmap rgb248 = Bitmap.getBitmapResource("img/other/248back.png");
		Background background = BackgroundFactory.createBitmapBackground(rgb248, Background.POSITION_X_LEFT,
				Background.POSITION_Y_TOP, Background.REPEAT_BOTH);
		outerVFM.setBackground(background);

		vfm = new VFMwithScrollbar(USE_ALL_WIDTH | USE_ALL_HEIGHT);
		outerVFM.add(vfm);

		add(outerVFM);

	}


	public FileScreen(String path) {

		this();

		fileList = new FileListField(path);

		fileList.setClipboardAllowed(true);
		fileList.registerJournalListener();
		fileList.registerRootChangeListener();

		setScrollBarTrigger(fileList);
		setTitleField(fileList);

		fileList.setEditable(true);
		vfm.add(fileList);

		fileList.setFocus();

	}


	protected boolean keyChar(char key, int status, int time) {

		if (getFileList().getKeywordField().isFocus()) {
			boolean consumed = getFileList().getKeywordField().keyChar(key, status, time);
			if (consumed) {
				// 正常的文本编辑操作
				return true;
			}
		}
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
