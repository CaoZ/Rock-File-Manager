
package RockManager.fileList;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Graphics;
import RockManager.languages.LangRes;
import RockManager.util.UtilCommon;
import RockManager.util.ui.GPATools;


public class AddressBar extends Field {

	/**
	 * 图标。
	 */
	private Bitmap icon;

	/**
	 * 原始路径。
	 */
	private String rawAddress;

	/**
	 * 各层路径。
	 */
	private String[] paths;

	/**
	 * 各层路径的长度。
	 */
	private int[] pathLengths;

	public static final int PADDING_TOP = 4;

	public static final int PADDING_LEFT = 6;

	private int barHeight;

	private Bitmap separator;

	private Bitmap leftIndicator;

	private boolean drawIcon = true;


	public AddressBar() {

		barHeight = getFont().getHeight() + PADDING_TOP;

		separator = Bitmap.getBitmapResource("img/other/separator.png");
		leftIndicator = Bitmap.getBitmapResource("img/other/leftIndicator.png");

	}


	public void setDrawIcon(boolean value) {

		drawIcon = value;
	}


	/**
	 * 设置地址栏要显示的图标。
	 * 
	 * @param icon
	 */
	public void setIcon(Bitmap icon) {

		if (!drawIcon || icon == null) {
			// 若不绘制图标，无需设置图标。
			return;
		}

		int iconW = barHeight - PADDING_TOP;
		int iconH = iconW;

		Bitmap processedIcon = GPATools.ResizeTransparentBitmap(icon, iconW, iconH);

		if (processedIcon.equals(this.icon)) {
			return;
		}

		this.icon = processedIcon;
		invalidate();

	}


	/**
	 * 设置地址栏要显示的路径。
	 * 
	 * @param address
	 */
	public void setAddress(String address) {

		if (address == null) {
			// 当是listRoots的DiskList时。
			address = LangRes.get(LangRes.MY_DEVICE);
		}

		if (address.equals(rawAddress)) {
			return;
		}

		rawAddress = address;

		parsePath(rawAddress);
		invalidate();
	}


	/**
	 * 获取显示的地址。
	 * 
	 * @return
	 */
	public String getAddress() {

		if (rawAddress == null) {
			return "";
		} else {
			return rawAddress;
		}
	}


	/**
	 * 分析原始路径，整理出所需信息。
	 * 
	 * @param path
	 */
	private void parsePath(String path) {

		String fileProtocol = "file:///";

		// 可能的值：
		// ""
		// file:///SDCard/
		// RARTest.rar
		// RARTest.rar\dir\
		// ZipTest.zip
		// ZipTest.zip/dir/

		if (path.startsWith(fileProtocol)) {
			path = path.substring(fileProtocol.length());
		}

		paths = null;

		if (path.length() > 0) {
			char separator = path.charAt(path.length() - 1);
			if (separator == '/' || separator == '\\') {
				path = path.substring(0, path.length() - 1);
				paths = UtilCommon.splitString(path, String.valueOf(separator));
			}
		}

		if (paths == null) {
			paths = new String[1];
			paths[0] = path;
		}

		pathLengths = new int[paths.length];
		for (int i = 0; i < paths.length; i++) {
			pathLengths[i] = getFont().getBounds(paths[i]);
		}

	}


	public int getPreferredHeight() {

		return barHeight;
	}


	protected void layout(int width, int height) {

		setExtent(width, barHeight);
	}


	protected void paint(Graphics g) {

		int x = PADDING_LEFT;
		if (drawIcon && icon != null) {
			x = drawIcon(g, x);
		}
		if (paths != null && paths.length > 0) {
			drawAddress(g, x);
		}
	}


	/**
	 * 绘制图标，返回x位置。
	 * 
	 * @param g
	 * @return
	 */
	private int drawIcon(Graphics g, int startX) {

		int iconWidth = icon.getWidth();
		int iconHeight = icon.getHeight();
		g.drawBitmap(startX, PADDING_TOP, iconWidth, iconHeight, icon, 0, 0);
		return startX + iconWidth + 5;
	}


	private void drawAddress(Graphics g, int x) {

		int start = x; // 开始位置
		int end = getWidth() - 10; // 结束位置，离开右侧一定距离，使之不太靠边。
		int availableWidth = end - start; // 可用宽度

		g.setColor(0xe7e7e7);

		if (paths.length == 1) {
			// 只有一层路径时的情况。
			g.setColor(0xffffff);
			g.drawText(paths[0], start, PADDING_TOP, DrawStyle.ELLIPSIS, availableWidth);
			return;
		}

		// ****绘制路径大于1层时的情况**** //

		int sepMarginX = 10;
		int sepWidth = separator.getWidth() + sepMarginX * 2;
		int sepHeight = separator.getHeight();
		int sepOffsetY = PADDING_TOP + UtilCommon.getOffset(barHeight - PADDING_TOP, separator.getHeight());

		int widthOfTotal = 0;

		// 计算总宽度。
		for (int i = 0; i < pathLengths.length; i++) {
			widthOfTotal += pathLengths[i];
			if (i < pathLengths.length - 1) {
				widthOfTotal += sepWidth;
			}
		}

		boolean drawAll = widthOfTotal <= availableWidth;

		if (drawAll) {
			// 可以完整的画出所有内容。

			for (int i = 0; i < paths.length; i++) {

				if (i == paths.length - 1) {
					g.setColor(0xffffff);
				}

				x += g.drawText(paths[i], x, PADDING_TOP);

				if (i < paths.length - 1) {
					g.drawBitmap(x + sepMarginX, sepOffsetY, sepWidth - sepMarginX * 2, sepHeight, separator, 0, 0);
					x += sepWidth;
				}

			}
			return;

		}

		// ****绘制不能完整画出所有路径时的情况**** //

		int indicatorOffsetY = PADDING_TOP + UtilCommon.getOffset(barHeight - PADDING_TOP, leftIndicator.getHeight());

		g.drawBitmap(start, indicatorOffsetY, leftIndicator.getWidth(), leftIndicator.getHeight(), leftIndicator, 0, 0);
		x += leftIndicator.getWidth() + sepMarginX;
		start = x;
		availableWidth = end - start;

		int widthOfLast = pathLengths[paths.length - 2] + sepWidth + pathLengths[paths.length - 1];
		boolean drawLastTwoFull = widthOfLast <= availableWidth;

		if (drawLastTwoFull) {
			// 可以至少完整的画出最后两层。
			// 由于不能完整画出所有层，但是能完整的画出最后两层加leftIndicator, 所有至少有三层。

			int startIndex = paths.length - 3;

			for (; startIndex >= 0; startIndex--) {
				widthOfLast += pathLengths[startIndex] + sepWidth;
				if (widthOfLast > availableWidth) {
					// 画不出此层了。
					startIndex++;
					break;
				}
			}

			for (int i = startIndex; i < paths.length; i++) {
				// 从startIndex开始，完整的画出后面的内容。

				if (i == paths.length - 1) {
					g.setColor(0xffffff);
				}

				x += g.drawText(paths[i], x, PADDING_TOP);

				if (i < paths.length - 1) {
					g.drawBitmap(x + sepMarginX, sepOffsetY, sepWidth - sepMarginX * 2, sepHeight, separator, 0, 0);
					x += sepWidth;
				}

			}

			return;

		}

		// **** 绘制不能完整的画出两层时的情况 **** //

		String levelOne = paths[paths.length - 2];
		String levelTwo = paths[paths.length - 1];
		int levelOneWidth = pathLengths[paths.length - 2];
		int levelTwoWidth = pathLengths[paths.length - 1];

		int levelOneLeastWidth = levelOneWidth;

		if (levelOne.length() > 3) {

			String levelOneEllipsis = levelOne.substring(0, 3) + "…";
			int levelOneEllipsisWidth = getFont().getBounds(levelOneEllipsis);
			levelOneLeastWidth = Math.min(levelOneLeastWidth, levelOneEllipsisWidth);

		}

		boolean drawLastOneFull = levelOneLeastWidth + sepWidth + levelTwoWidth <= availableWidth;

		if (drawLastOneFull) {
			// 可完整的画出最后一层。

			int widthForLevelOne = availableWidth - sepWidth - levelTwoWidth;
			x += g.drawText(levelOne, start, PADDING_TOP, DrawStyle.ELLIPSIS, widthForLevelOne);
			g.drawBitmap(x + sepMarginX, sepOffsetY, sepWidth, sepHeight, separator, 0, 0);
			x += sepWidth;
			g.setColor(0xffffff);
			g.drawText(levelTwo, x, PADDING_TOP);

			return;

		} else {
			// 两层都不能完整画出。

			x += g.drawText(levelOne, start, PADDING_TOP, DrawStyle.ELLIPSIS, levelOneLeastWidth);
			g.drawBitmap(x + sepMarginX, sepOffsetY, sepWidth, sepHeight, separator, 0, 0);
			x += sepWidth;
			int widthForLevelTwo = end - x;
			g.setColor(0xffffff);
			g.drawText(levelTwo, x, PADDING_TOP, DrawStyle.ELLIPSIS, widthForLevelTwo);

		}

	}

}
