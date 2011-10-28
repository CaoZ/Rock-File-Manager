
package RockManager.util;

import java.util.Vector;
import net.rim.device.api.io.URI;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Display;
import net.rim.device.api.system.EventLogger;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.VirtualKeyboard;
import net.rim.device.api.ui.XYDimension;
import net.rim.device.api.ui.XYEdges;
import net.rim.device.api.ui.XYRect;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.util.MathUtilities;


public class UtilCommon {

	private static long LOG_GUID = 0x829155f422cfffcdL;

	private static boolean LOG_REGISTERED = false;


	/**
	 * 弹出窗口，显示指定的信息。
	 * 
	 * @param object
	 */
	public static void trace(final Object object) {

		UiApplication.getUiApplication().invokeLater(new Runnable() {

			public void run() {

				String message = (object == null) ? "[NULL]" : object.toString();
				Dialog.alert(message);
			}
		});
	}


	/**
	 * 弹出窗口，显示指定的信息。
	 */
	public static void trace(final int intValue) {

		trace(new Integer(intValue));
	}


	/**
	 * 弹出窗口，显示指定信息。
	 */
	public static void alert(final String message, final boolean doModal) {

		Bitmap alertBitmap = Bitmap.getPredefinedBitmap(Bitmap.EXCLAMATION);
		final Dialog dialog = new Dialog(Dialog.D_OK, message, Dialog.OK, alertBitmap, 0);

		UiApplication.getUiApplication().invokeAndWait(new Runnable() {

			public void run() {

				if (doModal) {
					dialog.doModal();
				} else {
					dialog.show();
				}

			}
		});

	}


	/**
	 * 返回两数差平均值。
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public static int getOffset(int x, int y) {

		return MathUtilities.round((x - y) / 2f);
	}


	/**
	 * 返回扩展名的(小写)，如Tom.mp3返回mp3，Tom(无后缀)返回""
	 * 
	 * @param path
	 * @return 扩展名的小写形式。
	 */
	public static String getSuffix(String path) {

		return getOriginSuffix(path).toLowerCase();
	}


	/**
	 * 返回扩展名的 原始形式，如Tom.mP3返回mP3，Tom(无后缀)返回""
	 * 
	 * @param path
	 * @return 扩展名的原始形式，大小写不变。
	 */
	public static String getOriginSuffix(String path) {

		int lastPosition = path.lastIndexOf('.');
		if (lastPosition < 0) {
			return "";
		} else {
			return path.substring(lastPosition + 1);
		}
	}


	/**
	 * 根据路径判断(最后一个字符是否是'/'或'\')是否是文件夹。
	 * 
	 * @param fileName
	 * @return
	 */
	public static boolean isFolder(String fileName) {

		return fileName != null && (fileName.endsWith("/") || fileName.endsWith("\\"));

	}


	/**
	 * 返回文件名，适用于没使用URI(URL)编码的情况。<br>
	 * 如Tom.mp3返回Tom，Tom(无后缀)返回Tom， Video/Tom.avi returns Tom.avi。
	 * 若是文件夹时返回文件夹名（最后两个'/'之间的内容）。<br>
	 * 如Video/将返回Video。Video/Funny/返回Funny。
	 * 
	 * @param path
	 * @param withExtension
	 *            是否包含扩展名(当path是文件夹路径而不是文件路径时会忽略该项)
	 * @return 文件名
	 */

	public static String getName(String path, boolean withExtension) {

		if (path == null || path.length() == 0) {
			return "";
		}
		boolean isFolder = isFolder(path);
		if (isFolder) {
			path = path.substring(0, path.length() - 1);
		}
		String name = URI.getFile(path);
		if (isFolder || withExtension) {
			return name;
		}
		int lastPosition = name.lastIndexOf('.');
		if (lastPosition < 0) {
			return name;
		} else {
			return name.substring(0, lastPosition);
		}
	}


	/**
	 * 把给定的字符串按给定的分隔符分割成字符串数组，连续的分割符将视为一个，开头和结尾的分割符将被删除，String.split()。
	 * <p>
	 * 例如："###12##34###56####78#"按"#"分割的结果为{"12", "34", "56", "78"}, <br>
	 * 而按"##"分割的结果为{"#12", "34", "#56", "78#"}。
	 * <p>
	 * 简单的例子"12 34 56"按" "将被分割成：{"12", "34", "56"}。<br>
	 * "123"按" "将被分割成{"123"}(长度为1的数组)。<br>
	 * ""(空字符串)按" "将被分割成{}(长度为0的数组)。
	 * 
	 * @param str
	 * @param separator
	 * @return
	 */
	public static String[] splitString(String str, String separator) {

		StringBuffer sb = new StringBuffer(str);
		Vector vector = new Vector();
		String sbToString = sb.toString(); // 经过Debug发现，sb.toString()较慢，应尽量减少sb.toString()的调用次数。

		// 若字符串以分割符结尾，则删除这些分隔符。
		while (sbToString.endsWith(separator)) {
			sb.delete(sb.length() - separator.length(), sb.length());
			sbToString = sb.toString();
		}

		boolean reachEnd = (sb.length() == 0);

		while (!reachEnd) {

			// 若字符串以分割符开头，删除这些分隔符。
			while (sbToString.startsWith(separator)) {
				sb.delete(0, separator.length());
				sbToString = sb.toString();
			}
			// 寻找下一个分割符的位置，若没有则视为在字符串的结尾。
			int separatorPosition = sbToString.indexOf(separator);
			if (separatorPosition < 0) {
				separatorPosition = sb.length();
				reachEnd = true;
			}

			vector.addElement(sbToString.substring(0, separatorPosition));

			if (!reachEnd) {
				sb.delete(0, separatorPosition + separator.length());
				sbToString = sb.toString();
			}

		}

		String[] strings = new String[vector.size()];
		vector.copyInto(strings);
		return strings;

	}


	/**
	 * 将（255,255,255）样式换算成0xFFFFFF样式颜色。
	 * 
	 * @param R
	 * @param G
	 * @param B
	 * @return 0x样式颜色的数值部分。
	 */
	public static int RGBColor(int R, int G, int B) {

		return (R << 16) | (G << 8) | B;
	}


	/**
	 * 以'/'或'\\'作为路径分隔符，返回当前路径的父目录，若无法取得则返回空字符串.
	 * <p>
	 * "file:///SDCard/happy.cod" -> "file:///SDCard/" <br>
	 * "file:///SDCard/dir/" -> "file:///SDCard/" <br>
	 * "file:///SDCard/" -> "file:///" <br>
	 * "file" -> ""
	 * 
	 * @param path
	 * @see #getParentDir(String)
	 */
	public static String getParentDir(String path) {

		int lastPosition = path.lastIndexOf('/', path.length() - 2) + 1;
		if (lastPosition == 0) {
			lastPosition = path.lastIndexOf('\\', path.length() - 2) + 1;
		}
		return path.substring(0, lastPosition);
	}


	/**
	 * 以'\'或'/'作为路径分隔符，获取最后一级文件或子目录的路径。文件系统和zip文件内使用'/'作为分隔符，而rar文件使用'\'作为分隔符。
	 * <p>
	 * "file:///SDCard/dir/" -> "dir/" <br>
	 * "SDCard/file.txt" -> "file.txt"
	 * 
	 * @param path
	 * @return
	 */
	public static String getFullFileName(String path) {

		// '/'的情况占绝大多数
		int lastPosition = path.lastIndexOf('/', path.length() - 2) + 1;
		if (lastPosition == 0) {
			lastPosition = path.lastIndexOf('\\', path.length() - 2) + 1;
		}
		return path.substring(lastPosition, path.length());
	}


	/**
	 * 在str中替换第一次出现的origin为replace.
	 * <p>
	 * 例："Good morning everyone", " ", "#" -> "Good#morning everyone"
	 * 
	 * @param str
	 * @param origin
	 * @param replace
	 * @return
	 */
	public static String replaceString(String str, String origin, String replace) {

		int firstTime = str.indexOf(origin);

		if (firstTime < 0) {
			// nothing to replace
			return str;
		}

		StringBuffer sb = new StringBuffer();
		sb.append(str.substring(0, firstTime));
		sb.append(replace);
		sb.append(str.substring(firstTime + origin.length()));

		return sb.toString();

	}


	/**
	 * 在str中替换所有出现的origin为replace.
	 * <p>
	 * 例："Good morning everyone", " ", "#" -> "Good#morning#everyone"
	 * 
	 * @param str
	 * @param origin
	 * @param replace
	 * @return
	 */
	public static String replaceAllString(String str, String origin, String replace) {

		int index = -1;
		// knownIndex, 记录已检查了的位置。
		int knownIndex = 0;
		StringBuffer sb = new StringBuffer(str);
		while ((index = sb.toString().indexOf(origin, knownIndex)) >= 0) {
			knownIndex = index + replace.length();
			sb.delete(index, index + origin.length());
			sb.insert(index, replace);
		}
		return sb.toString();
	}


	/**
	 * 获取设备的分辨率（top-side-up）.
	 * 
	 * @return
	 */
	public static XYDimension getDeviceResolution() {

		VirtualKeyboard vk = null;
		int vkOriginState = 0;

		if (VirtualKeyboard.isSupported()) {
			// 隐藏键盘。
			Screen screen = UiApplication.getUiApplication().getActiveScreen();
			if (screen != null) {
				vk = screen.getVirtualKeyboard();
				vkOriginState = vk.getVisibility();
				vk.setVisibility(VirtualKeyboard.HIDE_FORCE);
			}
		}

		int width = Display.getWidth();
		int height = Display.getHeight();

		if (VirtualKeyboard.isSupported()) {
			int bigger = Math.max(width, height);
			int smaller = Math.min(width, height);

			width = smaller;
			height = bigger;

			if (vk != null) {
				vk.setVisibility(vkOriginState);
			}

		}

		return new XYDimension(width, height);

	}


	/**
	 * 在Event Log中记录事件。
	 * 
	 * @param info
	 */
	public static void log(String info) {

		if (LOG_REGISTERED == false) {
			EventLogger.register(LOG_GUID, "#RockManager#", EventLogger.VIEWER_STRING);
		}

		EventLogger.logEvent(LOG_GUID, info.getBytes(), EventLogger.ERROR);

	}


	/**
	 * 使用bitmap填充某个区域。
	 * 
	 * @param g
	 * @param dest
	 *            目标区域。
	 * @param bitmap
	 * @param left
	 *            左侧的边
	 * @param right
	 *            右侧的边
	 */
	public static void bitmapFill(Graphics g, final XYRect target, Bitmap bitmap, int left, int right) {

		if (left > 0 || right > 0) {
			// 确保值是正确的、在范围内的。
			left = Math.min(left, Math.min(target.width, bitmap.getWidth()) / 2);
			right = Math.min(right, Math.min(target.width, bitmap.getWidth()) - left);
		}

		XYRect leftRect = null;

		if (left > 0) {

			leftRect = new XYRect();
			leftRect.x = target.x;
			leftRect.y = target.y;
			leftRect.width = left;
			leftRect.height = Math.min(bitmap.getHeight(), target.height);

		}

		XYRect rightRect = null;

		if (right > 0) {

			rightRect = new XYRect();
			rightRect.x = target.x + target.width - right;
			rightRect.y = target.y;
			rightRect.width = right;
			rightRect.height = Math.min(bitmap.getHeight(), target.height);

		}

		XYRect mainRect = new XYRect();

		mainRect.x = target.x + left;
		mainRect.y = target.y;
		mainRect.width = Math.min(bitmap.getWidth(), target.width) - left - right;
		mainRect.height = Math.min(bitmap.getHeight(), target.height);

		while (true) {

			if (leftRect != null) {
				g.drawBitmap(leftRect, bitmap, 0, 0);
			}

			mainRect.x = target.x + left; // mainRect初始位置复原

			while (true) {
				g.drawBitmap(mainRect, bitmap, left, 0);
				int distance = target.x + target.width - right - mainRect.x - mainRect.width;
				if (distance <= 0) {
					break;
				} else {
					mainRect.x += mainRect.width;
					if (mainRect.width > distance) {
						// 不用完整的画出整个图像，只需画出一部分。
						mainRect.width = distance;
					}
				}
			}

			if (rightRect != null) {
				g.drawBitmap(rightRect, bitmap, bitmap.getWidth() - right, 0);
			}

			int distanceY = target.y + target.height - mainRect.y - mainRect.height;

			if (distanceY <= 0) {

				break;

			} else {

				mainRect.y += mainRect.height;

				if (leftRect != null) {
					leftRect.y += leftRect.height;
				}

				if (rightRect != null) {
					rightRect.y += rightRect.height;
				}

				if (mainRect.height > distanceY) {

					mainRect.height = distanceY;

					if (leftRect != null) {
						leftRect.height = distanceY;
					}

					if (rightRect != null) {
						rightRect.height = distanceY;
					}

				}

			}

		}

	}


	/**
	 * 使用bitmap填充某一区域。
	 * 
	 * @param g
	 * @param target
	 * @param bitmap
	 * @param borders
	 */
	public static void bitmapFill(Graphics g, XYRect target, Bitmap bitmap, XYEdges borders) {

		int top = borders.top;
		int right = borders.right;
		int bottom = borders.bottom;
		int left = borders.left;

		if (top > 0) {

			XYRect targetTop = new XYRect(target);
			targetTop.height = top;

			XYRect bitmapTopRect = new XYRect(0, 0, bitmap.getWidth(), top);
			Bitmap topBitmap = getPartBitmap(bitmap, bitmapTopRect);

			bitmapFill(g, targetTop, topBitmap, left, right);

		}

		XYRect targetCenter = new XYRect(target);
		targetCenter.y = target.y + top;
		targetCenter.height = target.height - top - bottom;

		XYRect bitmapCenterRect = new XYRect(0, top, bitmap.getWidth(), bitmap.getHeight() - top - bottom);
		Bitmap centerBitmap = getPartBitmap(bitmap, bitmapCenterRect);

		bitmapFill(g, targetCenter, centerBitmap, left, right);

		if (bottom > 0) {

			XYRect targetBottom = new XYRect(target);
			targetBottom.y = target.y + target.height - bottom;
			targetBottom.height = bottom;

			XYRect bitmapBottomRect = new XYRect(0, bitmap.getHeight() - bottom, bitmap.getWidth(), bottom);
			Bitmap bottomBitmap = getPartBitmap(bitmap, bitmapBottomRect);

			bitmapFill(g, targetBottom, bottomBitmap, left, right);

		}

	}


	/**
	 * 获取bitmap的某一部分。
	 * 
	 * @param origin
	 * @param destRect
	 * @return
	 */
	public static Bitmap getPartBitmap(Bitmap origin, XYRect destRect) {

		Bitmap target = new Bitmap(destRect.width, destRect.height);
		int[] imageData = new int[destRect.width * destRect.height];

		origin.getARGB(imageData, 0, destRect.width, destRect.x, destRect.y, destRect.width, destRect.height);
		target.setARGB(imageData, 0, destRect.width, 0, 0, destRect.width, destRect.height);

		return target;

	}


	/**
	 * 检测是否为英文字母。CharacterUtilities.isLetter()把中文字符也算为true，如果只想判定是否为英文字母，使用此方法。
	 * 
	 * @param aChar
	 * @return
	 */

	public static boolean isAlphabet(char aChar) {

		if (Character.isLowerCase(aChar) || Character.isUpperCase(aChar)) {
			return true;
		} else {
			return false;
		}
	}


	/**
	 * 将文件URL转换为文件地址形式(仅将"%25"转为百分号'%')。
	 * 
	 * @param uri
	 * @return
	 */
	public static String URLtoPath(String url) {

		return replaceAllString(url, "%25", "%");

	}


	/**
	 * 将文件地址转换为文件URL形式(仅将百分号'%'转为"%25")。
	 * 
	 * @param filePath
	 * @return
	 */
	public static String toURLForm(String filePath) {

		return replaceAllString(filePath, "%", "%25");

	}


	/**
	 * 返回e.getMessage。若errorMessage为null, 返回"[no error message available]"
	 * 
	 * @param e
	 * @return
	 */
	public static String getErrorMessage(Exception e) {

		String errorMessage = e.getMessage();
		if (errorMessage == null) {
			errorMessage = "[no error message available]";
		}
		return errorMessage;

	}


	/**
	 * 限制菜单的最短宽度。有时菜单太小了，既不好看，也不方便点选。
	 */
	public static void setMenuMinWidth(Menu menu, int minWidth) {

		int count = menu.getSize();

		for (int i = 0; i < count; i++) {

			MenuItem thisItem = menu.getItem(i);
			if (thisItem.isSeparator()) {
				continue;
			}

			String text = thisItem.toString();
			Font menuFont = menu.getFont();

			int originWidth = menuFont.getBounds(text);

			if (originWidth >= minWidth) {
				break;
			}

			int spaceWidth = menuFont.getAdvance(' ');
			int spaceToAdd = (int) Math.ceil((minWidth - originWidth) / spaceWidth);

			StringBuffer newLabel = new StringBuffer(text);

			for (int j = 0; j < spaceToAdd; j++) {
				newLabel.append(' ');
			}

			thisItem.setText(newLabel.toString());
			break;

		}

	}

}
