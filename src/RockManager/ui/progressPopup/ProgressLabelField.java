
package RockManager.ui.progressPopup;

import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.util.MathUtilities;


/**
 * 显示正在处理的步骤的名称（如文件名）及总进度。
 */

public class ProgressLabelField extends Field {

	private String progressName;

	private int progressRate;


	public void setName(String name) {

		if (name.equals(progressName) == false) {
			progressName = name;
			invalidate();
		}

	}


	public void setRate(int rate) {

		rate = MathUtilities.clamp(0, rate, 100);

		if (rate != progressRate) {
			progressRate = rate;
			invalidate();
		}

	}


	protected void layout(int width, int height) {

		height = Math.min(getFont().getHeight(), height);
		setExtent(width, height);
	}


	protected void paint(Graphics g) {

		String rate = progressRate + " %";
		int rateWidth = getFont().getBounds(rate);
		// 设置nameWidth的最大宽度。
		int nameWidth = getWidth() - rateWidth - getFont().getHeight();
		g.drawText(progressName, 0, 0, DrawStyle.ELLIPSIS, nameWidth);
		g.drawText(rate, 0, 0, DrawStyle.RIGHT, getWidth());
	}
}
