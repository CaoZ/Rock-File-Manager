
package RockManager.ui.statusBar;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.XYRect;
import net.rim.device.api.util.MathUtilities;
import RockManager.util.UtilCommon;


/**
 * 进度指示器，或显示一条信息，有黑色和蓝色两种色彩方案。
 */
public class StatusBar extends Field {

	/**
	 * 现有两套颜色方案， 带有花纹的黑色，蓝色渐变样式。
	 */
	public static int STYLE_BLACK = 0;

	public static int STYLE_BLUE = 1;

	private int colorStyle = 0;

	private Bitmap backImg;

	private static int BACK_FINISHED_COLOR_TOP = 0x5ec9ff;

	private static int BACK_FINISHED_COLOR_BOTTOM = 0x449cca;

	private static int BACK_UNFINISHED_COLOR = 0xdedfde;

	private static int BACK_UNFINISHED_COLOR_BORDER = 0xd1d2d1;

	private static int TEXT_COLOR = 0xffffff;

	private static int TEXT_COLOR_CONTRAST = 0x3b3b3b;

	private static Font TEXT_FONT = Font.getDefault().derive(Font.PLAIN, 27);

	private static int PADDING = 5;

	private String info;

	/**
	 * 完成部分用像素表示的数值。
	 */
	private int finishedInPixel = -1;

	/**
	 * 绘制时的值，在初始时与finishedInPixel相同，但finishedInPixel可能在绘制时变化（paint()可能花费较多时间，
	 * finishedInPixel的set和get可能不在一线程内），所以取一拷贝。
	 */
	private int finishedPixelToDraw;

	private float finished = 1;

	private int assignedWidth = -1;

	private int assignedHeight = -1;


	public StatusBar() {

		this(null);
	}


	public StatusBar(String info) {

		this(info, STYLE_BLACK);
	}


	public StatusBar(int colorStyle) {

		this(null, colorStyle);
	}


	public StatusBar(String info, int colorStyle) {

		setInfo(info);
		this.colorStyle = colorStyle;
	}


	/**
	 * 设置要显示的文字。
	 * 
	 * @param info
	 */
	public void setInfo(String info) {

		if (info == this.info || info != null && info.equals(this.info)) {
			return;
		}
		this.info = info;
		invalidate();
	}


	/**
	 * 设置已完成的进度。
	 * 
	 * @param percent
	 *            0到100.
	 */
	public void setProgress(int percent) {

		setFinished((float) percent / (float) 100);
	}


	/**
	 * 设置已完成的进度。
	 * 
	 * @param percent
	 *            0到100.
	 */
	public void setProgress(float percent) {

		setFinished(percent / (float) 100);
	}


	/**
	 * 通过两数相除设置进度。
	 * 
	 * @param finished
	 * @param total
	 */
	public void setProgress(long finished, long total) {

		setFinished((float) finished / (float) total);
	}


	/**
	 * 设置已完成的百分比。
	 * 
	 * @param percent
	 *            从0到1.
	 */
	public void setFinished(float percent) {

		finished = percent;
		int width = getWidth();
		if (width == 0) {
			finishedInPixel = -1;
			return;
		} else {
			int finishedPixel = MathUtilities.round(width * percent);
			setFinishedPixel(finishedPixel);
		}
	}


	/**
	 * 设置已完成的进度的像素数表示并在需要时重新绘制进度条。此数值与总宽度相比是完成的百分比。
	 * 
	 * @param pixelNumber
	 */
	private void setFinishedPixel(int pixelNumber) {

		int originFinishedInPixel = finishedInPixel;

		if (originFinishedInPixel != pixelNumber) {
			finishedInPixel = pixelNumber;

			if (originFinishedInPixel >= 0) {
				// 原来是-1时会重绘，无需再指定invalidate().
				invalidate();
			}
		}
	}


	protected void layout(int width, int height) {

		int realHeight;
		if (assignedHeight > 0) {
			// 如果指定了高度，则高度是指定的高度和可用高度的较小值。
			realHeight = Math.min(assignedHeight, height);
		} else {
			// 若未指定高度，则高度是默认值和可用高度的较小值。
			realHeight = Math.min(getPreferredHeight(), height);
		}
		int realWidth = (assignedWidth > 0) ? Math.min(assignedWidth, width) : width;
		setExtent(realWidth, realHeight);
		// 在高度已决定的情况下，可以初始化背景图片了。
		createBackImage();
		// 若旋转了屏幕，需指示重新计算finishedInPixel.
		finishedInPixel = -1;
	}


	/**
	 * 设置想要的宽度，但实际的宽度是此值和可用宽度的较小值。
	 * 
	 * @param width
	 */
	public void setWidth(int width) {

		assignedWidth = width;
	}


	/**
	 * 设置想要的高度，但实际的高度是此值和可用高度的较小值。
	 * 
	 * @param height
	 */
	public void setHeight(int height) {

		assignedHeight = height;
	}


	protected void paint(Graphics g) {

		// 小于0则表示在未取得正确宽度的情况下计算出finishedInPixel,需重新计算。
		if (finishedInPixel < 0) {
			setFinished(finished);
			if (finishedInPixel < 0) {
				// 仍不能获得正确宽度。无法绘制，返回。
				return;
			}
		}

		finishedPixelToDraw = finishedInPixel; // 取一拷贝

		// 绘制已完成部分。
		drawFinished(g);

		// 绘制未完成部分
		g.setColor(BACK_UNFINISHED_COLOR);

		g.fillRect(finishedPixelToDraw, 0, getWidth() - finishedPixelToDraw, getHeight());

		if (info == null || info.length() == 0) {
			// 只有没有文字时才绘制边框。
			// 有文字时高度较大，通常作为在底部显示的信息栏，上部背景很浅。没文字时作进度栏，背景是黑色。
			g.setColor(BACK_UNFINISHED_COLOR_BORDER);
			g.drawLine(finishedPixelToDraw, 0, getWidth() - 1, 0);
			g.drawLine(finishedPixelToDraw, getHeight() - 1, getWidth() - 1, getHeight() - 1);
		}

		// 绘制文字
		drawInfoText(g);
	}


	/**
	 * 绘制文字。
	 * 
	 * @param g
	 */
	private void drawInfoText(Graphics g) {

		if (info == null || info.length() == 0) {
			// empty, no need to paint
			return;
		}
		g.setFont(TEXT_FONT);
		int textWidth = TEXT_FONT.getBounds(info);
		int offsetX = UtilCommon.getOffset(getWidth(), textWidth);
		int offsetY = UtilCommon.getOffset(getHeight(), TEXT_FONT.getHeight());
		if (offsetX < 0) {
			// 当文字太宽时使offsetX变为0。同时使用DrawStyle.ELLIPSIS样式进行绘制。
			offsetX = 0;
		}
		// 绘制已完成部分上的覆盖文字。
		// 当已完成部分大于offsetX时文字才有可能出现，才有必要绘制文字。
		if (finishedPixelToDraw > offsetX) {
			XYRect finishedRect = new XYRect();
			finishedRect.width = finishedPixelToDraw;
			finishedRect.height = getHeight();
			g.pushRegion(finishedRect);
			g.setColor(TEXT_COLOR);
			g.drawText(info, offsetX, offsetY, DrawStyle.ELLIPSIS, getWidth());
			g.popContext();
		}
		// 绘制未完成部分上的覆盖文字。
		if (getWidth() - finishedPixelToDraw > offsetX) {
			XYRect unfinishedRect = new XYRect();
			unfinishedRect.x = finishedPixelToDraw;
			unfinishedRect.width = getWidth() - finishedPixelToDraw;
			unfinishedRect.height = getHeight();
			g.pushRegion(unfinishedRect);
			g.setColor(TEXT_COLOR_CONTRAST);
			// 不起作用，不知为何。系统的bug？
			// g.drawText(info, offsetX, offsetY, DrawStyle.RIGHT,
			// unfinishedRect.width);
			// info文字在最左则不应显示部分的宽度。
			int leftOffset = textWidth - (unfinishedRect.width - offsetX);
			g.drawText(info, -leftOffset, offsetY, DrawStyle.ELLIPSIS, getWidth());
			g.popContext();
		}

	}


	/**
	 * 绘制已完成部分。
	 * 
	 * @param g
	 */
	private void drawFinished(Graphics g) {

		XYRect targetRect = new XYRect();
		targetRect.width = finishedPixelToDraw;
		targetRect.height = getHeight();

		UtilCommon.bitmapFill(g, targetRect, backImg, 0, 0);

	}


	/**
	 * 生成进度条已完成部分（或全部）的指示图形。
	 */
	private void createBackImage() {

		if (colorStyle == STYLE_BLACK) {
			backImg = Bitmap.getBitmapResource("img/other/statusBack.png");
		} else {
			int width = 60;
			int height = getHeight();
			backImg = new Bitmap(width, height);
			Graphics g = Graphics.create(backImg);
			int[] xPts = { 0, width, width, 0 };
			int[] yPts = { 0, 0, height, height };
			int[] colors = { BACK_FINISHED_COLOR_TOP, BACK_FINISHED_COLOR_TOP,
					BACK_FINISHED_COLOR_BOTTOM, BACK_FINISHED_COLOR_BOTTOM };
			g.drawShadedFilledPath(xPts, yPts, null, colors, null);
		}
	}


	public int getPreferredHeight() {

		return TEXT_FONT.getHeight() + PADDING * 2;
	}

}
