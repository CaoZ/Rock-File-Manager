
package RockManager.ui;

import RockManager.util.ui.GPATools;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.Font;
import net.rim.device.api.util.MathUtilities;


public class MyUI {

	public static final Font SYSTEM_FONT;

	public static final Font MAIN_FONT;

	public static final Font SMALLER_FONT;

	/**
	 * 此时的UI大小相比在9630上8pt字体时放大或缩小的倍数。
	 */
	public static final float UI_RATIO;

	static {

		// 设置字体，稍微比系统字体小些，在我的9630上，系统系统为8pt(27px), 这个程序的字体为25和24。
		float mainFontRatio = 0.9259f; // 25/27;
		float smallerFontRatio = 0.8889f; // 24/27;

		SYSTEM_FONT = Font.getDefault();
		int systemFontHeight = SYSTEM_FONT.getHeight();

		int mainFontHeight = MathUtilities.round((systemFontHeight * mainFontRatio));
		MAIN_FONT = SYSTEM_FONT.derive(Font.PLAIN, mainFontHeight);

		int smallerFontHeight = MathUtilities.round((systemFontHeight * smallerFontRatio));
		SMALLER_FONT = SYSTEM_FONT.derive(Font.PLAIN, smallerFontHeight);

		UI_RATIO = mainFontHeight / 25f;

	}


	/**
	 * 获得与MAIN_FONT相同样式的Font, 高度为MainFont的percent%.
	 * 
	 * @param percent
	 * @return
	 */
	public static Font deriveFont(float ratio) {

		int mainFontHeight = MAIN_FONT.getHeight();
		int newFontHeight = MathUtilities.round(mainFontHeight * ratio);
		return MAIN_FONT.derive(MAIN_FONT.getStyle(), newFontHeight);

	}
	
	/**
	 * 获得适当大小的字体。
	 * @param originSize 在基准设备（9630）上的大小。
	 * @return
	 */
	public static Font deriveFont(int originSize) {
		
		int newFontHeight = deriveSize(originSize);
		return MAIN_FONT.derive(MAIN_FONT.getStyle(), newFontHeight);
	}


	/**
	 * 当前size乘以UI_RATIO的结果。
	 * 
	 * @param originSize
	 * @return
	 */
	public static int deriveSize(int originSize) {

		int targetSize = MathUtilities.round(originSize * UI_RATIO);
		return targetSize;

	}


	/**
	 * 按照UI_RATIO缩放图片。
	 * 
	 * @param originBitmap
	 * @return
	 */
	public static Bitmap deriveImg(Bitmap originBitmap) {

		int originW = originBitmap.getWidth();
		int originH = originBitmap.getHeight();

		int targetW = deriveSize(originW);
		int targetH = deriveSize(originH);

		return GPATools.ResizeTransparentBitmap(originBitmap, targetW, targetH);

	}

}
