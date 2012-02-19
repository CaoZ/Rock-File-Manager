
package RockManager.ui.screen.fileScreen;

import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import RockManager.languages.LangRes;
import RockManager.ui.MyUI;
import RockManager.util.UtilCommon;


public class FileCountLabel extends Field {

	private int count;

	private Font textFont = MyUI.deriveFont(22);

	private int displayHeight = MyUI.deriveSize(30);

	private String info;


	public FileCountLabel() {

		reset();
	}


	public void reset() {

		count = 0;
		generateInfo();
	}


	protected void layout(int width, int height) {

		setExtent(width, displayHeight);
	}


	public void setCount(int count) {

		this.count = count;
		generateInfo();
		invalidate();
	}


	private void generateInfo() {

		String template = LangRes.get(LangRes.TEXT_N_SELECTED);
		info = UtilCommon.replaceString(template, "{1}", Integer.toString(count));
	}


	protected void paint(Graphics g) {

		if (info == null) {
			return;
		}
		int textWidth = textFont.getBounds(info);
		int textHeight = textFont.getHeight();
		int blackWidth = textWidth + MyUI.deriveSize(18);

		g.setGlobalAlpha(120);
		g.setColor(0);
		int blackOffsetX = UtilCommon.getOffset(getWidth(), blackWidth);
		int arcSize = MyUI.deriveSize(9);
		// 增加高度，使下部圆角消失。另外加上一些数值是为了防止因次像素平滑（虽然暂时BB没这种设计）而可能会出现的一些黑色不完整。
		g.fillRoundRect(blackOffsetX, 0, blackWidth, displayHeight + arcSize + 10, arcSize, arcSize);

		g.setColor(0xffffff);
		g.setFont(textFont);
		g.setGlobalAlpha(240);

		int offsetX = UtilCommon.getOffset(getWidth(), textWidth);
		int offsetY = UtilCommon.getOffset(getHeight(), textHeight);

		g.drawText(info, offsetX, offsetY);

	}

}
