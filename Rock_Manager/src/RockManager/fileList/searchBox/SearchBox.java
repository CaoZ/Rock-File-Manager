
package RockManager.fileList.searchBox;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.TouchEvent;
import net.rim.device.api.ui.Touchscreen;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.VirtualKeyboard;
import net.rim.device.api.ui.XYEdges;
import net.rim.device.api.ui.XYRect;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.decor.Border;
import net.rim.device.api.ui.decor.BorderFactory;
import RockManager.fileList.FileListField;
import RockManager.ui.MyUI;
import RockManager.ui.oneLineInputField.InputField;
import RockManager.ui.oneLineInputField.OneLineInputArea;
import RockManager.util.OSVersionUtil;
import RockManager.util.UtilCommon;


public class SearchBox extends HorizontalFieldManager implements FieldChangeListener {

	/**
	 * 存储输入区的区域。
	 */
	private OneLineInputArea inputArea;

	private ClearIcon clearIcon;

	private Border borderNormal;

	private Border borderFocus;

	/**
	 * 与内部的间隔，水平方向。
	 */
	private int paddingX = 4;

	/**
	 * 与文字的间距，如‘放大镜’搜索标志与文字的间隔（右侧）。
	 */
	private int paddingText = 7;

	private int paddingY = 2;

	private FileListField parentListField;


	public SearchBox(FileListField parentListField) {

		super(NO_HORIZONTAL_SCROLL | NON_FOCUSABLE | USE_ALL_WIDTH);

		setFont(MyUI.SMALLER_FONT); // 较小字体

		// setMargin(3, 4, 5, 4); // 外面的黑色区域
		setPadding(0, paddingX, 0, paddingX); // 与内部的间隔

		XYEdges borderEdges = new XYEdges(11, 9, 10, 9);
		borderNormal = BorderFactory.createBitmapBorder(borderEdges,
				Bitmap.getBitmapResource("img/other/inputBack.png"));
		borderFocus = BorderFactory.createBitmapBorder(borderEdges,
				Bitmap.getBitmapResource("img/other/inputBack_Focus.png"));
		setBorder(borderNormal);

		addSearchIcon();

		inputArea = new OneLineInputArea(FIELD_VCENTER | USE_ALL_WIDTH);
		inputArea.setPadding(paddingY, 0, paddingY, 0);
		add(inputArea);

		this.parentListField = parentListField;
		new SearchLabelHandler(this);

	}


	private void addSearchIcon() {

		Bitmap searchImg = Bitmap.getBitmapResource("img/other/searchIcon.png");
		searchImg = MyUI.deriveImg(searchImg);

		BitmapField searchIcon = new BitmapField(searchImg, FIELD_VCENTER);

		// 不直接安置在容器SearchBox上而在每个Field都设置padding，是希望扩大关闭按钮的范围，使触摸操作容易完成。
		searchIcon.setPadding(paddingY, paddingText, paddingY, 0);
		add(searchIcon);

	}


	protected void onFocus(int direction) {

		showFocusBorder();

		if (Touchscreen.isSupported()) {

			showClearIcon();

			// 使虚拟键盘显示。
			if (VirtualKeyboard.isSupported()) {
				VirtualKeyboard vk = getScreen().getVirtualKeyboard();
				vk.setVisibility(VirtualKeyboard.SHOW);
			}

		}

		super.onFocus(direction);
	}


	protected void onUnfocus() {

		hideFocusBorder();

		if (inputArea.isEmpty() && Touchscreen.isSupported()) {

			hideClearIcon();

			// 使虚拟键盘隐藏。
			if (VirtualKeyboard.isSupported()) {
				VirtualKeyboard vk = getScreen().getVirtualKeyboard();
				vk.setVisibility(VirtualKeyboard.HIDE);
			}

		}

		super.onUnfocus();
	}


	protected void sublayout(int width, int height) {

		super.sublayout(width, height); // 全部layout

		if (clearIcon == null || clearIcon.getManager() == null) {
			return;
		}

		// 如有需要，重新layout输入框和清除图标

		XYRect clearRect = new XYRect();

		clearRect.width = clearIcon.getPreferredWidth() + clearIcon.getPaddingLeft() + clearIcon.getPaddingRight();
		clearRect.height = clearIcon.getPreferredHeight() + clearIcon.getPaddingTop() + clearIcon.getPaddingBottom();

		clearRect.x = getContentWidth() - clearRect.width;
		clearRect.y = UtilCommon.getOffset(getContentHeight(), clearRect.height);

		setPositionChild(clearIcon, clearRect.x, clearRect.y);
		layoutChild(clearIcon, clearRect.width, clearRect.height);

		XYRect inputRect = inputArea.getExtent();
		inputRect.width = clearRect.x - inputRect.x;

		setPositionChild(inputArea, inputRect.x, inputRect.y);
		layoutChild(inputArea, inputRect.width, inputRect.height);

	}


	public InputField getInputField() {

		return inputArea.getInputField();
	}


	/**
	 * 设置inputBox的label, 提示性文字。如“Press 'S' to search.”。
	 * 
	 * @param label
	 */
	public void setLabelText(String label) {

		inputArea.setLabelText(label);
	}


	/**
	 * 设置inputBox的文字。
	 * 
	 * @param text
	 */
	public void setText(String text) {

		inputArea.setText(text);
	}


	/**
	 * 将边框变为蓝色，focused样式。 <br>
	 * setBorder(int visual, Border border, boolean updateLayout)方法似乎在os5上有bug。
	 */
	public void showFocusBorder() {

		if (getBorder() != borderFocus) {
			setBackground(null);
			setBorder(borderFocus);
		}
	}


	/**
	 * 将边框恢复为普通样式。<br>
	 * setBorder(int visual, Border border, boolean updateLayout)方法似乎在os5上有bug。
	 */
	public void hideFocusBorder() {

		if (getBorder() != borderNormal) {

			// 清除背景色。
			// 在两种border中间部分颜色不同时需调用。
			// setBackground(null);

			// 5.0 os 上一个bug导致在此时设置边框将阻止后面的onUnfocus的完成。
			// 经测试，此bug在5.0.0.900上没有了，在5.0.0.681上还有，具体哪个版本修复的不知道。
			// 一个解决办法是将这个操作放到invokeLater中完成。

			if (OSVersionUtil.isOS5() && OSVersionUtil.getRevisionVersion() < 900) {
				// 有此bug的解决方法。
				UiApplication.getUiApplication().invokeLater(new Runnable() {

					public void run() {

						setBorder(borderNormal);
					}
				});

			} else {
				// 可直接完成的方法。
				setBorder(borderNormal);
			}
		}
	}


	/**
	 * 显示关闭图标。
	 */
	public void showClearIcon() {

		if (clearIcon == null) {

			Bitmap clearIconImg = Bitmap.getBitmapResource("img/other/clearIcon.png");
			clearIconImg = MyUI.deriveImg(clearIconImg);

			clearIcon = new ClearIcon(clearIconImg, FIELD_VCENTER | FIELD_RIGHT);
			clearIcon.setPadding(paddingY, paddingX, paddingY, paddingText);
			clearIcon.setChangeListener(this);

		}

		if (clearIcon.getManager() != null) {
			return;
		}

		setPadding(0, 0, 0, paddingX); // 减小右边空闲距离添加到clearIcon上，使触摸操作容易完成。
		add(clearIcon);

	}


	/**
	 * 隐藏关闭图标。
	 */
	public void hideClearIcon() {

		if (clearIcon == null || clearIcon.getManager() == null) {
			return;
		}

		setPadding(0, paddingX, 0, paddingX); // 恢复原来的padding
		delete(clearIcon);
	}


	public void fieldChanged(Field field, int context) {

		if (field == clearIcon && context == TouchEvent.CLICK) {

			if (parentListField.getKeyword().length() > 0) {
				// 已经输入了关键字，使关键字重置。
				parentListField.setKeyword("");

				if (isFocus() == false) {
					// 不处于焦点状态，隐藏clearIcon.
					hideClearIcon();
				}

			} else {
				// 还未输入关键字，使输入框失去焦点。
				parentListField.setFocus();
			}

		}

	}

}
