
package RockManager.fileList;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.XYRect;
import RockManager.util.UtilCommon;
import RockManager.util.ui.VFMwithScrollbar;


public class FileListDrawer {

	/**
	 * 正常颜色.
	 */
	private static final int FONT_COLOR = 0x111111;

	/**
	 * 较浅颜色。
	 */
	private static final int FONT_COLOR_LIGHT = 0x6d6d6d;

	/**
	 * 高亮时颜色。
	 */
	private static final int FONT_COLOR_HIGHLIGHT = 0xffffff;

	/**
	 * 高亮时较浅颜色。
	 */
	private static final int FONT_COLOR_LIGHT_HIGHLIGHT = 0xe7e7e7;


	/**
	 * 对每行进行绘制。
	 */
	public static void drawListRow(FileListField listField, Graphics g, int index, int y, int width, boolean pickerMode) {

		FileItem thisItem = (FileItem) listField.get(listField, index);

		// ****绘制文件图标**** //
		Bitmap fileIcon = thisItem.getIcon();
		int iconY = y + UtilCommon.getOffset(listField.getRowHeight(), fileIcon.getHeight());
		int x = 9;
		XYRect fileIconDest = new XYRect(x, iconY, fileIcon.getWidth(), fileIcon.getHeight());

		if (thisItem.isHidden()) {
			// 是隐藏文件，用0.5的透明度绘制图标。

			if (pickerMode) {
				g.setGlobalAlpha((int) (255 * 0.7));
			} else {
				g.setGlobalAlpha((int) (255 * 0.55));
			}
		}

		g.drawBitmap(fileIconDest, fileIcon, 0, 0);

		if (thisItem.isDRMLocked() || thisItem.isEncrypted()) {
			// 绘制小锁图标
			Bitmap lockIcon = FileItem.getLockIcon();
			XYRect lockIconDest = new XYRect(fileIconDest.x + 18, iconY + 15, lockIcon.getWidth(), lockIcon.getHeight());
			g.drawBitmap(lockIconDest, lockIcon, 0, 0);
		}

		if (thisItem.isHidden()) {
			// 是隐藏文件，设置绘制文字的透明度

			if (pickerMode) {
				g.setGlobalAlpha((int) (255 * 0.75));
			} else {
				if (listField.isFocused(index)) {
					g.setGlobalAlpha((int) (255 * 0.75));
				} else {
					g.setGlobalAlpha((int) (255 * 0.6));
				}
			}

		}

		int textY = y + UtilCommon.getOffset(listField.getRowHeight(), g.getFont().getHeight());
		// 使文件名与图标隔开6个像素。
		x = x + fileIconDest.width + 6;
		// 文件名可用的最大宽度。
		int nameMaxWidth = width - x - 20;

		// ****绘制文件大小，并以此计算出绘制文件名时可用的宽度。**** //

		if (pickerMode == false) {

			String sizeString = null;

			if (thisItem.isFile()) {
				sizeString = thisItem.getSizeString();
			} else if (thisItem.isDisk()) {
				sizeString = thisItem.getSizeString(listField, index);
			}

			if (sizeString != null) {

				g.setColor(listField.isFocused(index) ? FONT_COLOR_LIGHT_HIGHLIGHT : FONT_COLOR_LIGHT);

				int sizeStringWidth = g.drawText(sizeString, 0, textY, DrawStyle.RIGHT, width - 11);
				nameMaxWidth = nameMaxWidth - 5 - sizeStringWidth; // 文件名和文件大小相隔5个像素。

			}

		}

		// ****绘制文件名称**** //

		drawName(listField, g, index, thisItem.getDisplayName(), x, textY, nameMaxWidth, pickerMode);

	}


	/**
	 * 绘制文件名称，并高亮搜索出的关键字。
	 * 
	 * @param g
	 * @param index
	 * @param name
	 * @param x
	 * @param y
	 * @param pickerMode
	 * @param width
	 */
	private static void drawName(FileListField listField, Graphics g, int index, String name, int x, int y,
			int maxWidth, boolean pickerMode) {

		boolean thisFocused = listField.isFocused(index);
		int needWidth = -1;

		Font textFont = g.getFont();

		if (listField.isKeyWordEntered()) {
			// 绘制关键字的亮色背景
			int originAlpha = g.getGlobalAlpha();
			g.setGlobalAlpha(0);
			int drawedWidth = g.drawText(name, x, y, DrawStyle.ELLIPSIS, maxWidth);
			g.setGlobalAlpha(originAlpha);
			needWidth = textFont.getAdvance(name);

			if (drawedWidth < needWidth) {
				// 没画完整，最后画了省略号。
				int dotsWidth = textFont.getAdvance("…");
				drawedWidth = drawedWidth - dotsWidth;
			}

			int backMaxX = x + drawedWidth;

			g.setColor(thisFocused ? 0x31b210 : 0xffef63);
			String lowerName = name.toLowerCase();

			String[] keywords = listField.getKeywords();

			for (int i = 0; i < keywords.length; i++) {

				int keyWordStart = 0;
				int keyWordEnd = 0;

				while ((keyWordStart = lowerName.indexOf(keywords[i], keyWordEnd)) >= 0) {

					keyWordEnd = keyWordStart + keywords[i].length();

					int startX = x + textFont.getAdvance(name.substring(0, keyWordStart));

					if (startX >= backMaxX) {
						break;
					}

					// 使高亮色彩从文字开始位置左移1像素并使宽度扩大2像素
					startX--;

					int widthX = textFont.getAdvance(name.substring(keyWordStart, keyWordEnd)) + 2;
					widthX = Math.min(widthX, backMaxX + 1 - startX);

					g.fillRoundRect(startX, y - 2, widthX, textFont.getHeight() + 4, 8, 8);

				}

			}
		}

		// 绘制文件名
		if (thisFocused) {
			g.setColor(FONT_COLOR_HIGHLIGHT);
		} else {
			if (pickerMode) {
				g.setColor(FONT_COLOR_LIGHT_HIGHLIGHT);
			} else {
				g.setColor(FONT_COLOR);
			}
		}

		int drawedWidth = g.drawText(name, x, y, DrawStyle.ELLIPSIS, maxWidth);

		if (thisFocused) {

			if (needWidth < 0) {
				needWidth = textFont.getAdvance(name);
			}

			if (drawedWidth < needWidth) {
				// 文件名没画完整，请求显示tip框.

				Manager manager = listField.getManager();

				if (manager instanceof VFMwithScrollbar) {

					String tipInfo = listField.getThisItem().getDisplayName();
					XYRect focusRect = new XYRect();
					listField.getFocusRect(focusRect);
					Font smallerFont = textFont.derive(Font.PLAIN, textFont.getHeight() - 1);

					((VFMwithScrollbar) manager).showTip(tipInfo, focusRect, smallerFont, listField);

				}

			}

		}

	}


	/**
	 * 当文件夹或搜索结构为空时，显示"空文件夹"样式的提示文字。
	 * 
	 * @param listField
	 * @param g
	 * @param emptyString
	 */
	public static void paintEmptyString(FileListField listField, Graphics g) {

		// 没有文件项找到，绘制emptyString
		int width = listField.getWidth();
		int height = listField.getHeight();
		Font font = listField.getFont();
		int fontHeight = font.getHeight() - 1;
		// 将字体缩小一号
		font = font.derive(Font.PLAIN, fontHeight);
		g.setFont(font);

		// 浅色
		g.setColor(FileListDrawer.FONT_COLOR_LIGHT);

		String emptyString = listField.getEmptyString();

		int offsetX = UtilCommon.getOffset(width, font.getBounds(emptyString));
		// 与底部对齐，给上部留出更多空间
		int offsetY = height - fontHeight;
		g.drawText(emptyString, offsetX, offsetY);

	}

}
