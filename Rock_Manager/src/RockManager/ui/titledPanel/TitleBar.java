
package RockManager.ui.titledPanel;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.XYRect;
import net.rim.device.api.ui.decor.Background;
import net.rim.device.api.ui.decor.BackgroundFactory;
import RockManager.util.UtilCommon;
import RockManager.util.ui.BaseButtonLikeField;


public class TitleBar extends BaseButtonLikeField {

	private String title;

	private Bitmap icon;

	private static Bitmap expandedIcon;

	private static Bitmap expandedWhiteIcon;

	private static Bitmap shrinkedIcon;

	private static Bitmap shrinkedWhiteIcon;

	private static int textColor = 0x555555;

	private static int textColorHighlight = 0xffffff;

	/**
	 * focus时背景渐变色顶部颜色。
	 */
	private static int backHighlightColorTop = 0x088eef;

	/**
	 * focus时背景渐变色底部颜色。
	 */
	private static int backHighlightColorBottom = 0x1065de;

	private Font textFont = getFont().derive(Font.PLAIN, 25);

	private static int padding = 5;

	private static Background background;

	/**
	 * 是否展开的指示符，无内容是默认收缩态。
	 */
	private boolean expanded = false;

	static {
		/*
		 * 此处其实是一个渐变颜色的图片，作为背景图片。由于黑莓只能显示65000色（rgb565），不能用系统的
		 * 渐变函数生成，那样色阶跳跃较大，难看，图片不能太窄，它似乎是先抖动仿色后再拉升， 太窄的话效果也很差。
		 * 若黑莓能显示256^3种颜色，这些问题可能都能迎刃而解了。 方便起见，还是渐变函数吧，在手机上看起来还行。
		 */

		int colorTop = 0xfdfdfd;
		int colorBottom = 0xdddddd;
		background = BackgroundFactory.createLinearGradientBackground(colorTop, colorTop,
				colorBottom, colorBottom);
		expandedIcon = Bitmap.getBitmapResource("img/titledPanel/expanded.png");
		expandedWhiteIcon = Bitmap.getBitmapResource("img/titledPanel/expandedWhite.png");
		shrinkedIcon = Bitmap.getBitmapResource("img/titledPanel/shrinked.png");
		shrinkedWhiteIcon = Bitmap.getBitmapResource("img/titledPanel/shrinkedWhite.png");

	}


	public TitleBar(String title) {

		this.title = title;
		setBackground(background);
	}


	public void setIcon(Bitmap icon) {

		this.icon = icon;
	}


	public int getPreferredHeight() {

		return textFont.getHeight() + padding * 2;
	}


	public boolean toggleStatus() {

		expanded = !expanded;
		invalidate();
		return expanded;
	}


	/**
	 * 使状态变为收缩态。
	 */
	public void setStatusShrinked() {

		if (expanded == false) {
			return;
		}
		expanded = false;
		invalidate();
	}


	/**
	 * 使状态变为展开态。
	 */
	public void setStatusExpanded() {

		if (expanded == true) {
			return;
		}
		expanded = true;
		invalidate();
	}


	protected void layout(int width, int height) {

		setExtent(width, getPreferredHeight());
	}


	/**
	 * 原来drawFocus要如此重写啊，要是黑莓系统是开源的话早就掌握了。
	 */
	protected void drawFocus(Graphics g, boolean on) {

		XYRect focusRect = new XYRect();
		getFocusRect(focusRect);
		drawHighlightBack(g, focusRect);
		// 此时Graphics的drawStyle已设为DRAWSTYLE_FOCUS了。
		paint(g);
	}


	/**
	 * 与FileListField相配合，画出颜色一致的蓝颜色。
	 * 
	 * @param g
	 * @param rect
	 */
	private void drawHighlightBack(Graphics g, XYRect rect) {

		int[] xPts = { 0, rect.width, rect.width, 0 };
		int[] yPts = { 0, 0, rect.height, rect.height };
		int[] colors = { backHighlightColorTop, backHighlightColorTop, backHighlightColorBottom,
				backHighlightColorBottom };
		g.drawShadedFilledPath(xPts, yPts, null, colors, null);
	}


	protected void paint(Graphics g) {

		boolean focused = g.isDrawingStyleSet(Graphics.DRAWSTYLE_FOCUS);
		int x = drawIcon(g, focused);
		// 绘制文字
		int color = focused ? textColorHighlight : textColor;
		g.setColor(color);
		g.setFont(textFont);
		g.drawText(title, x, padding, DrawStyle.ELLIPSIS, getWidth() - x - expandedIcon.getWidth()
				- 10);
	}


	/**
	 * 绘制左侧小图标及右侧是否展开了的指示图标。
	 * 
	 * @return 返回title文字部分离左侧的距离，因为左侧小图标是可选的。
	 */
	private int drawIcon(Graphics g, boolean focused) {

		int x = padding;
		if (icon != null) {
			int height = getHeight();
			int y = UtilCommon.getOffset(height, icon.getHeight());
			g.drawBitmap(x, y, icon.getHeight(), icon.getWidth(), icon, 0, 0);
			x += icon.getWidth() + padding;
		} else {
			x += (padding * 0.6); // 没有图标时稍稍扩大与左侧的空白。
		}

		int expandX = getWidth() - expandedIcon.getWidth() - 6;
		int expandY = UtilCommon.getOffset(getHeight(), expandedIcon.getHeight());

		Bitmap expIcon;
		if (focused) {
			expIcon = expanded ? expandedWhiteIcon : shrinkedWhiteIcon;
		} else {
			expIcon = expanded ? expandedIcon : shrinkedIcon;
		}

		g.drawBitmap(expandX, expandY, expIcon.getWidth(), expIcon.getHeight(), expIcon, 0, 0);

		return x;
	}

}
