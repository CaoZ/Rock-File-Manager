
package RockManager.ui.titledPanel;

import RockManager.ui.MyUI;
import RockManager.util.UtilCommon;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.XYRect;


public class EmptyIndicator extends Field {

	private static Bitmap BACK_IMG;

	static {
		Bitmap back = Bitmap.getBitmapResource("img/titledPanel/blackBack.png");
		BACK_IMG = MyUI.deriveImg(back);
	}


	public EmptyIndicator() {

		this(0);
	}


	public EmptyIndicator(long style) {

		super(FIELD_HCENTER | style);
	}


	protected void layout(int width, int height) {

		setExtent((int) (width * 0.95), BACK_IMG.getHeight());
	}


	protected void paint(Graphics g) {

		// 绘制背景
		int backHalfWidth = BACK_IMG.getWidth() / 2;
		XYRect dest = new XYRect(0, 0, backHalfWidth, BACK_IMG.getHeight());
		g.setGlobalAlpha((int) (255 * 0.6));
		g.drawBitmap(dest, BACK_IMG, 0, 0);
		g.setColor(0);
		g.fillRect(dest.width, 0, getWidth() - BACK_IMG.getWidth(), BACK_IMG.getHeight());
		dest.x = getWidth() - backHalfWidth;
		g.drawBitmap(dest, BACK_IMG, backHalfWidth, 0);

		// 绘制文字
		g.setGlobalAlpha(250);
		g.setColor(Color.WHITE);
		String text = "Empty";
		int offsetX = UtilCommon.getOffset(getWidth(), getFont().getBounds(text));
		int offsetY = UtilCommon.getOffset(getHeight(), getFont().getHeight());
		g.drawText(text, offsetX, offsetY);
	}

}
