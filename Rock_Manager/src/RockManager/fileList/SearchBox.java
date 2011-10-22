package RockManager.fileList;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.capability.DeviceCapability;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.TouchEvent;
import net.rim.device.api.ui.Touchscreen;
import net.rim.device.api.ui.VirtualKeyboard;
import net.rim.device.api.ui.XYEdges;
import net.rim.device.api.ui.XYRect;
import net.rim.device.api.ui.component.TextField;
import net.rim.device.api.util.MathUtilities;
import RockManager.util.UtilCommon;

public class SearchBox extends TextField {

	/**
	 * 是否已经复制了系统的样式。
	 */
	private boolean themeApplied = false;
	private static Bitmap SEARCH_ICON = Bitmap.getBitmapResource("img/other/searchIcon.png");
	private static Bitmap CLEAR_ICON = Touchscreen.isSupported() ? Bitmap
			.getBitmapResource("img/other/cleanIcon.png") : null;
	private boolean focused = false;
	private static int TEXT_PADDING = 7;
	private static int CURSOR_WIDTH = 5;
	private String labelText;

	public SearchBox() {
		super(NO_EDIT_MODE_INPUT | NO_LEARNING | NO_NEWLINE);
	}

	/**
	 * 是否已经复制了系统的样式。
	 * 
	 * @return
	 */
	public boolean isThemeApplied() {
		return themeApplied;
	}

	/**
	 * 从一个TextField复制样式。
	 * 
	 * @param origin
	 */
	public void cloneFrom(TextField origin) {
		themeApplied = true;
		// copy border
		setBorder(origin.getBorder());
		// copy margin
		XYEdges margin = new XYEdges();
		origin.getMargin(margin);
		setMargin(margin);
		// copy label
		setLabelText(origin.getLabel());
		// copy padding
		XYEdges padding = new XYEdges();
		origin.getPadding(padding);
		setPadding(padding);
		// copy font setting
		setFont(origin.getFont());
		// copy editable
		setEditable(origin.isEditable());
	}

	protected void paint(Graphics g) {
		// 绘制搜索图标
		XYRect searchRect = new XYRect();
		searchRect.width = SEARCH_ICON.getWidth();
		searchRect.height = SEARCH_ICON.getHeight();
		searchRect.y = UtilCommon.getOffset(g.getClippingRect().height, searchRect.height);
		g.drawBitmap(searchRect, SEARCH_ICON, 0, 0);

		if (VirtualKeyboard.isSupported()) {
			// 若机器支持触摸屏，且搜索的关键字不为空，使它变为focused状态（防止再次单击时才使它变为focused其它的却什么也不干的情况）
			if (getTextLength() > 0) {
				focused = true;
			}
		}

		// 绘制清除图标
		if (focused) {
			XYRect clearRect = new XYRect();
			clearRect.width = CLEAR_ICON.getWidth();
			clearRect.height = CLEAR_ICON.getHeight();
			clearRect.y = UtilCommon.getOffset(g.getClippingRect().height, clearRect.height);
			clearRect.x = g.getClippingRect().width - clearRect.width;
			g.drawBitmap(clearRect, CLEAR_ICON, 0, 0);
		}

		// 绘制文字
		g.translate(searchRect.width + TEXT_PADDING, 0);
		if (focused == false && getTextLength() == 0) {
			// 内容为空，绘制提示文本"Search..."
			g.setColor(0x999999);
			g.drawText(getLabel(), 0, 0);
		} else {
			// 不绘制提示文本，直接绘制内容
			g.setColor(0x555555);
			g.drawText(getText(), 0, 0);
		}

		if (focused || getTextLength() > 0) {
			drawFocus(g, true);
		}

	}

	/**
	 * 绘制文字光标。
	 */
	protected void drawFocus(Graphics g, boolean on) {

		XYRect rect = new XYRect();
		getFocusRect(rect);

		int backColorTop = 0x088eef;
		int backColorBottom = 0x1065de;

		rect.x++; // 右移1像素
		rect.width = MathUtilities.round(rect.width * 0.8f); // 变细

		int[] xPts = { rect.x, rect.x + rect.width, rect.x + rect.width, rect.x };
		int[] yPts = { rect.y, rect.y, rect.y + rect.height, rect.y + rect.height };
		int[] colors = { backColorTop, backColorTop, backColorBottom, backColorBottom };
		g.drawShadedFilledPath(xPts, yPts, null, colors, null);

	}

	protected boolean touchEvent(TouchEvent message) {
		VirtualKeyboard virtualKeyboard = getScreen().getVirtualKeyboard();
		if (message.getEvent() == TouchEvent.CLICK) {
			if (focused) {
				// 已经focus了，决定是清除字段还是关闭虚拟键盘。
				if (clearIconClicked(message)) {
					boolean virtualKeyboardIsShown = virtualKeyboard.getVisibility() == VirtualKeyboard.SHOW;
					// 点击了右侧的清除/关闭按钮
					if (getTextLength() > 0 && virtualKeyboardIsShown) {
						// 有文字，清空文字。
						setText("");
					} else {
						// 没文字或虚拟键盘没有显示，直接关闭，设置focused为false,隐藏虚拟键盘。
						if (getTextLength() > 0) {
							setText("");
						}
						focused = false;
						virtualKeyboard.setVisibility(VirtualKeyboard.HIDE);
						invalidate();
					}
				}
			} else {
				// 现在让它显示虚拟键盘。
				focused = true;
				if (DeviceCapability.isPhysicalKeyboardAvailable() == false) {
					virtualKeyboard.setVisibility(virtualKeyboard.SHOW);
				}
				invalidate();
			}
			return true;

		}
		return super.touchEvent(message);
	}

	/**
	 * 获取绘制文本时可用的最大宽度。
	 * 
	 * @return
	 */
	private int getTextMaxWidth() {
		int width = getContentWidth();
		width -= (SEARCH_ICON.getWidth() + TEXT_PADDING);
		if (Touchscreen.isSupported()) {
			width -= (SEARCH_ICON.getWidth() + TEXT_PADDING);
		}
		width -= CURSOR_WIDTH;
		return width;
	}

	private boolean clearIconClicked(TouchEvent message) {
		int x = message.getX(1);
		XYRect content = getContentRect();
		int maxX = content.x + content.width - CLEAR_ICON.getWidth() - getBorder().getRight()
				- getPaddingRight() * 2;
		// 以上仅是图片最左开始的位置，现在扩大一下范围。
		maxX = maxX - 15;
		if (x >= maxX) {
			return true;
		} else {
			return false;
		}
	}

	public boolean isFocusable() {
		// 任何情况下都不应使焦点从文件列表移动，因此，它永远不能获得焦点。但可通过其它方式进行模拟获得焦点时的表现。
		return false;
	}

	protected void layout(int width, int height) {
		if (DeviceCapability.isPhysicalKeyboardAvailable() == false) {
			VirtualKeyboard virtualKeyboard = getScreen().getVirtualKeyboard();
			if (virtualKeyboard.getVisibility() == VirtualKeyboard.SHOW) {
				// 物理键盘不可用时，若出现了虚拟键盘，则使它变成focused状态。
				focused = true;
			} else if (virtualKeyboard.getVisibility() == virtualKeyboard.HIDE) {
				// 若关闭了虚拟键盘，使它变成unfocused状态。
				focused = false;
			}
		}

		super.layout(width, height);
	}

	public void setLabelText(String text) {
		labelText = text;
	}

	public String getLabel() {
		if (labelText != null) {
			return labelText;
		} else {
			return "";
		}
	}

	protected boolean keyChar(char key, int status, int time) {
		// 限制可输入的文字个数，由于重写了paint方法（看不到系统源码，所以不能100%兼容），文字过多时显示不对。
		// 也可用manager，但需找个图片当border了，现在是借用了系统主题的border。
		String targetString = getText() + key;
		if (getFont().getBounds(targetString) > getTextMaxWidth()) {
			setMaxSize(getTextLength());
		} else {
			setMaxSize(200);
		}
		return super.keyChar(key, status, time);
	}

}
