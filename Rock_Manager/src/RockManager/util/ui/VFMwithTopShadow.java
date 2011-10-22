
package RockManager.util.ui;

import RockManager.util.UtilCommon;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.XYRect;
import net.rim.device.api.ui.container.VerticalFieldManager;


/**
 * 顶部有个阴影效果的VerticalFieldManager。
 */
public class VFMwithTopShadow extends VerticalFieldManager {

	private Bitmap topShadow;


	public VFMwithTopShadow(long style) {

		super(style);
		topShadow = Bitmap.getBitmapResource("img/other/topShadow.png");
	}


	protected void paint(Graphics g) {

		super.paint(g);
		paintTopShadow(g);
	}


	/**
	 * 绘制顶部的阴影。
	 * 
	 * @param g
	 */
	private void paintTopShadow(Graphics g) {

		XYRect targetRect = new XYRect();
		targetRect.width = getWidth();
		targetRect.height = topShadow.getHeight();

		UtilCommon.bitmapFill(g, targetRect, topShadow, 0, 0);

	}

}
