package RockManager.ui.titledPanel;

import RockManager.util.UtilCommon;
import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.XYRect;

public class EmptyIndicator extends Field {

	private static EncodedImage backImage = EncodedImage
			.getEncodedImageResource("img/titledPanel/blackBack.png");

	public EmptyIndicator() {
		this(0);
	}

	public EmptyIndicator(long style) {
		super(FIELD_HCENTER | style);
		setFont(getFont().derive(Font.PLAIN, 25));
	}

	protected void layout(int width, int height) {
		setExtent((int) (width * 0.95), backImage.getHeight());
	}

	protected void paint(Graphics g) {

		// 绘制背景
		int backHalfWidth = backImage.getWidth() / 2;
		XYRect dest = new XYRect(0, 0, backHalfWidth, backImage.getHeight());
		g.setGlobalAlpha((int) (255 * 0.6));
		g.drawImage(dest, backImage, 0, 0, 0);
		g.setColor(0);
		g.fillRect(dest.width, 0, getWidth() - backImage.getWidth(), backImage.getHeight());
		dest.x = getWidth() - backHalfWidth;
		g.drawImage(dest, backImage, 0, backHalfWidth, 0);

		// 绘制文字
		g.setGlobalAlpha(250);
		g.setColor(Color.WHITE);
		String text = "Empty";
		int offsetX = UtilCommon.getOffset(getWidth(), getFont().getBounds(text));
		int offsetY = UtilCommon.getOffset(getHeight(), getFont().getHeight());
		g.drawText(text, offsetX, offsetY);
	}
}
