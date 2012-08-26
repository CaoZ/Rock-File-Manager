
package RockManager.fileClipboard;

import RockManager.ui.MyUI;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Graphics;


public class FileClipboardIndicator extends Field {

	private static final Bitmap ICON_COPY;

	private static final Bitmap ICON_CUT;

	static {
		Bitmap copy = Bitmap.getBitmapResource("img/clipboard/copy.png");
		ICON_COPY = MyUI.deriveImg(copy);
		Bitmap cut = Bitmap.getBitmapResource("img/clipboard/cut.png");
		ICON_CUT = MyUI.deriveImg(cut);
	}


	public FileClipboardIndicator() {

		this(0);

	}


	public FileClipboardIndicator(long style) {

		super(style);

	}


	public int getPreferredWidth() {

		return ICON_COPY.getWidth();

	}


	public int getPreferredHeight() {

		return ICON_COPY.getHeight();

	}


	protected void layout(int width, int height) {

		setExtent(ICON_COPY.getWidth(), ICON_COPY.getHeight());

	}


	protected void paint(Graphics g) {

		if (FileClipboard.isEmpty()) {
			return;
		}

		switch (FileClipboard.getMethod()) {

			case FileClipboard.METHOD_COPY:
				g.drawBitmap(0, 0, getWidth(), getHeight(), ICON_COPY, 0, 0);
				break;

			case FileClipboard.METHOD_CUT:
				g.drawBitmap(0, 0, getWidth(), getHeight(), ICON_CUT, 0, 0);
				break;

		}

	}


	protected void onDisplay() {

		FileClipboard.addToIndicatorList(this);

		super.onDisplay();

	}


	protected void onUndisplay() {

		FileClipboard.removeFromIndicatorList(this);

		super.onUndisplay();

	}


	public void invalidate() {

		super.invalidate();

	}

}
