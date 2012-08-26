
package RockManager.util.ui;

import java.util.Timer;
import java.util.TimerTask;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.ScrollChangeListener;
import net.rim.device.api.ui.XYEdges;
import net.rim.device.api.ui.XYRect;
import net.rim.device.api.ui.container.VerticalFieldManager;
import net.rim.device.api.util.MathUtilities;
import RockManager.util.SplitedText;
import RockManager.util.UtilCommon;


/**
 * 有滚动条，且滚动条会自动隐藏的VerticalFieldManager。
 */
public class VFMwithScrollbar extends VerticalFieldManager implements VFMwithScrollbarControl, ScrollChangeListener {

	private Bitmap scrollTopImg;

	private Bitmap scrollBottomImg;

	private int scrollBarColor;

	private int scrollBarTopHeight;

	/**
	 * 滚动条的长度。
	 */
	private int sliderHeight;

	/**
	 * 滑轨的长度。
	 */
	private int trackHeight;

	/**
	 * 滑轨的宽度。
	 */
	private int trackWidth;

	/**
	 * 滑轨距vfm左侧的距离。
	 */
	private int trackOffsetX;

	/**
	 * 滑轨距vfm上部的距离。
	 */
	private final int trackOffsetY = 3;

	/**
	 * 滚动条距vfm上部的距离。
	 */
	private int sliderOffsetY;

	/**
	 * 实际高度。
	 */
	private int virtualHeight;

	/**
	 * 可见高度。
	 */
	private int visibleHeight;

	/**
	 * 上次的实际高度。
	 */
	private int lastVirtualHeight;

	/**
	 * 上次可见高度。
	 */
	private int lastVisibleHeight;

	private boolean needScrollbar;

	private Timer hideSliderTimer;

	private TimerTask hideSliderTask;

	/**
	 * 滚动条现在的透明度。
	 */
	private int sliderAlpha;

	/**
	 * 绘制时是否应绘制提示框。
	 */
	private boolean showTip = false;

	private Timer showTipTimer;

	private TimerTask showTipTask;

	/**
	 * 提示文字。
	 */
	private String tipText;

	/**
	 * 发出提示框请求的源field的位置。
	 */
	private XYRect tipOriginRect;

	/**
	 * 发出提示框请求的源field。
	 */
	private Field tipOriginField;

	/**
	 * 提示框文字字体。
	 */
	private Font tipFont;

	/**
	 * 提示框的绘制位置。
	 */
	private XYRect drawedTipRect;

	/**
	 * 是否在屏幕高度调整时自动出现滚动条。
	 */
	private boolean scrollBarAutoShow = true;

	/**
	 * 使用白色版本滚动条。默认是深色的。
	 */
	private boolean useWhiteVersionSlider = false;

	/**
	 * 滚动条显示时的透明度。
	 */
	private int sliderShowAlpha;


	public VFMwithScrollbar() {

		this(0);
	}


	public VFMwithScrollbar(long style) {

		super(style | VERTICAL_SCROLL | NO_VERTICAL_SCROLLBAR);
		setScrollListener(this);
	}


	/**
	 * 使用白色版本滚动条。默认是深色的。
	 */
	public void useWhiteVersionSlider() {

		useWhiteVersionSlider = true;
	}


	/**
	 * 加载所需资源，初始化。
	 */
	private synchronized void loadResource() {

		if (scrollTopImg != null) {
			// 所需资源已加载。
			return;
		}

		if (useWhiteVersionSlider) {
			// 白色版本滚动条。
			scrollTopImg = Bitmap.getBitmapResource("img/scrollBar/scrollTop_white.png");
			scrollBottomImg = Bitmap.getBitmapResource("img/scrollBar/scrollBottom_white.png");
			scrollBarColor = 0xffffff;
			sliderShowAlpha = (int) (255 * 0.6);
		} else {
			// 深色版本滚动条。
			scrollTopImg = Bitmap.getBitmapResource("img/scrollBar/scrollTop.png");
			scrollBottomImg = Bitmap.getBitmapResource("img/scrollBar/scrollBottom.png");
			scrollBarColor = UtilCommon.RGBColor(30, 30, 30);
			sliderShowAlpha = 120;
		}

		scrollBarTopHeight = scrollTopImg.getHeight();
		trackWidth = scrollTopImg.getWidth();

	}


	protected void sublayout(int maxWidth, int maxHeight) {

		super.sublayout(maxWidth, maxHeight);

		virtualHeight = getVirtualHeight();
		visibleHeight = getHeight();

		if (lastVirtualHeight != virtualHeight || lastVisibleHeight != visibleHeight) {
			reComputeSliderbar(scrollBarAutoShow);
		}

		lastVirtualHeight = virtualHeight;
		lastVisibleHeight = visibleHeight;

	}


	/**
	 * 重新计算滚动条的高度。
	 * 
	 * @param showItNow
	 *            若需要滚动条，是否立即显示一次。
	 */
	public synchronized void reComputeSliderbar(boolean showItNow) {

		needScrollbar = virtualHeight > visibleHeight;

		if (needScrollbar) {

			loadResource();

			trackHeight = visibleHeight - 2 * trackOffsetY;
			sliderHeight = MathUtilities.round(visibleHeight / (float) virtualHeight * trackHeight);
			sliderHeight = Math.max(20, sliderHeight);

			// 滚动条距右侧边缘的距离
			trackOffsetX = getWidth() - trackWidth - 2;

			if (showItNow) {
				sliderShowAndHide();
			}

		}

	}


	/**
	 * 显示滚动条， 并在一段时间后自动隐藏滚动条。
	 */
	private void sliderShowAndHide() {

		if (hideSliderTask != null) {
			hideSliderTask.cancel();
			hideSliderTask = null;
		}

		// 显示时的透明度，0~255.
		sliderAlpha = sliderShowAlpha;
		invalidate(trackOffsetX, getVerticalScroll(), trackWidth, visibleHeight);

		hideSliderTask = new TimerTask() {

			// TimerTask 只能使用一次，一旦cancel了，就不能再schedule了。

			public void run() {

				sliderAlpha = 0;
				invalidate(trackOffsetX, getVerticalScroll(), trackWidth, visibleHeight);

			}
		};

		if (hideSliderTimer == null) {
			hideSliderTimer = new Timer();
		}

		hideSliderTimer.schedule(hideSliderTask, 800);

	}


	protected void paint(Graphics g) {

		super.paint(g);

		if (showTip) {
			drawTip(g);
		}

		if (needScrollbar == false || sliderAlpha == 0) {
			return;
		}

		g.setGlobalAlpha(sliderAlpha);
		g.setColor(scrollBarColor);
		// Determine how far have we scrolled
		int scrollPosition = getVerticalScroll();
		// The slider vertical position on the screen is proportional to the
		// scroll position.
		// Please observe that we add the scroll position to the calculated
		// result since
		// everything on the screen starts there. All x and y coordinates
		// for this Graphics
		// object are within the Manager's FULL (virtual) rectangle.

		// 为什么要分别减去visibleHeigh与sliderHeight?多数情况下减不减都行，因为二者是成比例的。
		// 但当sliderHeight较小而取20时，二者不成比例了，所以需要分别减去。
		sliderOffsetY = MathUtilities.round(scrollPosition / (float) (virtualHeight - visibleHeight)
				* (trackHeight - sliderHeight))
				+ scrollPosition + trackOffsetY;
		// draw the scrollbar
		g.drawBitmap(trackOffsetX, sliderOffsetY, trackWidth, scrollBarTopHeight, scrollTopImg, 0, 0);
		g.fillRect(trackOffsetX, sliderOffsetY + scrollBarTopHeight, trackWidth, sliderHeight - 2 * scrollBarTopHeight);
		g.drawBitmap(trackOffsetX, sliderOffsetY + sliderHeight - scrollBarTopHeight, trackWidth, scrollBarTopHeight,
				scrollBottomImg, 0, 0);

	}


	/**
	 * 绘制提示框、提示文字。
	 * 
	 * @param g
	 */
	private void drawTip(Graphics g) {

		XYRect focusRect = new XYRect();
		getFocusRect(focusRect);

		int itemStartY = getVerticalScroll() + focusRect.y;
		int itemEndY = itemStartY + focusRect.height;

		Bitmap msgBack = Bitmap.getBitmapResource("img/other/msgBox.png");

		XYEdges backBorders = new XYEdges(47, 14, 14, 14); // 背景边框宽度。
		XYEdges textPadding = new XYEdges(22, 24, 22, 24); // 文字padding
		int linePadding = 3; // 绘制时行间距

		XYRect textRect = new XYRect(); // 文字区域。
		XYRect backRect = new XYRect(); // 背景区域。

		backRect.width = getWidth();
		textRect.x = textPadding.left;
		textRect.width = backRect.width - textPadding.left - textPadding.right;

		g.setFont(tipFont);

		SplitedText splitedText = new SplitedText(tipText, textRect.width, tipFont);
		int lineCount = splitedText.getLineCount();

		textRect.height = lineCount * tipFont.getHeight() + (lineCount - 1) * linePadding;
		backRect.height = textRect.height + textPadding.top + textPadding.bottom;

		int upperAvailable = itemStartY - getVerticalScroll();
		int lowerAvailable = getVisibleHeight() - (itemEndY - getVerticalScroll());

		boolean drawOnTop = true; // 是绘制在当前项的上部还是下部。

		if (upperAvailable < backRect.height && lowerAvailable > backRect.height) {
			drawOnTop = false;
		}

		if (drawOnTop) {

			backRect.y = itemStartY - backRect.height;
			textRect.y = backRect.y + textPadding.top;

		} else {

			backRect.y = itemEndY;
			textRect.y = backRect.y + textPadding.top;

		}

		drawedTipRect = backRect;

		if (backBorders.top + backBorders.bottom > backRect.height) {
			// 若字体较小时，可能需调整top border的大小。
			backBorders.top = backRect.height - backBorders.bottom;
		}

		UtilCommon.bitmapFill(g, backRect, msgBack, backBorders); // 绘制填充背景。

		String[] splitedNames = splitedText.getTextStrings();

		int textStartY = textRect.y;

		g.setColor(0x444444); // 文字颜色变浅。

		// int height = getHeight();
		// int managerHeight = getManager().getHeight();
		//
		// UtilCommon.trace(height + " " + managerHeight);

		for (int i = 0; i < splitedNames.length; i++) {

			g.drawText(splitedNames[i], textRect.x, textStartY);
			textStartY += linePadding + tipFont.getHeight();

		}

	}


	/**
	 * 滚动时显示滚动条。
	 */
	public void scrollChanged(Manager manager, int newHorizontalScroll, int newVerticalScroll) {

		hideTip(); // 滚动时不显示提示框。

		if (needScrollbar) {
			sliderShowAndHide();
		}

	}


	protected void onUnfocus() {

		hideTip(); // 移出时隐藏提示框。
		super.onUnfocus();

	}


	/**
	 * 若有tip框，使tip框隐藏。
	 */
	public synchronized void hideTip() {

		if (showTipTask != null) {
			showTipTask.cancel();
			showTipTask = null;
		}

		showTip = false;
		tipText = null;
		tipOriginRect = null;
		tipOriginField = null;

		if (drawedTipRect != null) {
			invalidate(drawedTipRect.x, drawedTipRect.y, drawedTipRect.width, drawedTipRect.height);
			drawedTipRect = null;
		}

	}


	/**
	 * 是否允许高度变化时滚动条自动出现。
	 * 
	 * @param value
	 */
	public void setScrollBarAutoShow(boolean value) {

		scrollBarAutoShow = value;
	}


	/**
	 * 在一段时间后显示一个提示信息。<br>
	 * 因为field提出提示请求的一个原因可能是field可用宽度不足，显示不完整，
	 * 重新paint时为了避免提示的重复绘制，需记录下field及其位置。
	 * 
	 * @param text
	 *            提示文字
	 * @param focusRect
	 *            发出提示的field的位置
	 * @param tipFont
	 *            提示用字体
	 * @param originField
	 *            发出提示的field
	 */
	public synchronized void showTip(String text, XYRect focusRect, Font tipFont, Field originField) {

		if (originField.equals(tipOriginField) && text.equals(tipText) && focusRect.equals(tipOriginRect)) {
			// 正是正在显示的那个，无需重复操作。
			return;
		}

		tipOriginRect = focusRect;
		tipOriginField = originField;
		tipText = text;
		this.tipFont = tipFont;

		if (showTipTimer == null) {
			showTipTimer = new Timer();
		}

		showTipTask = new TimerTask() {

			public void run() {

				showTip = true;
				invalidate(0, getVerticalScroll(), getVisibleWidth(), getVisibleHeight());

			}
		};

		showTipTimer.schedule(showTipTask, 800);

	}

}
