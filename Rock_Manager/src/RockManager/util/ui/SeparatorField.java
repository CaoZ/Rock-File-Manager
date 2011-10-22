
package RockManager.util.ui;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Display;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.XYRect;
import RockManager.util.UtilCommon;


public class SeparatorField extends Field {

	private Bitmap backImg;

	/**
	 * 是否是真彩色，若只能显示65536色的话对于这个图像缩放后不好看。
	 */
	private boolean trueColorSupport;

	private static final int MARGIN_DEFAULT_X = 0;

	private static final int MARGIN_DEFAULT_Y = 3;


	public SeparatorField() {

		this(MARGIN_DEFAULT_Y, MARGIN_DEFAULT_Y);

	}


	public SeparatorField(int marginTop, int marginBottom) {

		trueColorSupport = Display.getNumColors() > 65536;

		setMargin(marginTop, MARGIN_DEFAULT_X, marginBottom, MARGIN_DEFAULT_X);

	}


	protected void layout(int width, int height) {

		// 图片未载入，或宽度变化，
		if (backImg == null || getWidth() != width) {

			String backImgPath = "img/other/separatorLine_{SIZE}.png";
			String imgSize = width <= 360 ? "360" : "480";

			backImgPath = UtilCommon.replaceString(backImgPath, "{SIZE}", imgSize);

			backImg = Bitmap.getBitmapResource(backImgPath);

			if (trueColorSupport && backImg.getWidth() != width) {
				// 支持真彩，进行缩放。
				backImg = GPATools.ResizeTransparentBitmap(backImg, width, backImg.getHeight(),
						Bitmap.FILTER_LANCZOS, Bitmap.SCALE_STRETCH);
			}

		}

		height = backImg.getHeight();
		setExtent(width, height);

	}


	protected void paint(Graphics g) {

		if (trueColorSupport) {
			g.drawBitmap(0, 0, getWidth(), getHeight(), backImg, 0, 0);
		} else {
			XYRect target = new XYRect(0, 0, getWidth(), getHeight());
			int left;
			if (target.width > backImg.getWidth()) {
				// 图片不足以完全覆盖区域，给中间至少留1像素。
				left = (backImg.getWidth() - 1) / 2;
			} else {
				left = backImg.getWidth() / 2;
			}
			int right = left;
			UtilCommon.bitmapFill(g, target, backImg, left, right);
		}

	}

}
