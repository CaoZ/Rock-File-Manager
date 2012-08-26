
package RockManager.fileList.searchBox;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.TouchEvent;
import net.rim.device.api.ui.component.BitmapField;


public class ClearIcon extends BitmapField {

	private boolean pressed;


	public ClearIcon(Bitmap bitmap, long style) {

		super(bitmap, style);
	}


	protected boolean touchEvent(TouchEvent message) {

		boolean inRegion = isInregion(message);

		switch (message.getEvent()) {

			case TouchEvent.DOWN:
				if (inRegion) {
					setPressed(true);
				}
				break;

			case TouchEvent.MOVE:
				setPressed(inRegion);
				break;

			case TouchEvent.UP:
				// 在上面松开算作单击，如同鼠标在一个按钮上释放当做单击一样。
				fieldChangeNotify(TouchEvent.CLICK);
			case TouchEvent.UNCLICK:
			case TouchEvent.CANCEL:
				setPressed(false);
				break;

			// CLICK时必有UP,所以不再单独处理CLICK
			// case TouchEvent.CLICK:
			// fieldChangeNotify(TouchEvent.CLICK);
			// break;

		}

		return super.touchEvent(message);

	}


	private void setPressed(boolean value) {

		if (pressed != value) {
			pressed = value;
			invalidate();
		}
	}


	/**
	 * 触摸操作是否在这个区域内。
	 * 
	 * @param message
	 * @return
	 */
	private boolean isInregion(TouchEvent message) {

		int x = message.getX(1);
		int y = message.getY(1);
		if (x < 0 || x > getWidth() || y < 0 || y > getHeight()) {
			return false;
		} else {
			return true;
		}
	}


	protected void paint(Graphics g) {

		if (pressed) {
			g.setGlobalAlpha((int) (255 * 0.5));
		}
		super.paint(g);
	}

}
