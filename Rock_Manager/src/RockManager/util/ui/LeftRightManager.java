
package RockManager.util.ui;

import RockManager.util.UtilCommon;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.VerticalFieldManager;


public class LeftRightManager extends HorizontalFieldManager {

	private VerticalFieldManager leftVFM;

	private VerticalFieldManager rightVFM;


	public LeftRightManager() {

		leftVFM = new VerticalFieldManager();
		rightVFM = new VerticalFieldManager();

		add(leftVFM);
		add(rightVFM);

	}


	/**
	 * 添加到左侧。
	 * 
	 * @param field
	 */
	public void addToLeft(Field field) {

		leftVFM.add(field);

	}


	/**
	 * 添加到右侧。
	 * 
	 * @param field
	 */
	public void addToRight(Field field) {

		rightVFM.add(field);

	}


	protected void sublayout(int maxWidth, int maxHeight) {

		// 最多只使用85%的宽度，使leftVFM, rightVFM不能完全占据整行，而能左对齐或右对齐。
		int widthForSeparateUse = (int) (maxWidth * 0.85);

		layoutChild(leftVFM, widthForSeparateUse, maxHeight);
		int leftWidth = leftVFM.getWidth();
		int leftHeight = leftVFM.getHeight();

		layoutChild(rightVFM, widthForSeparateUse, maxHeight);
		int rightWidth = rightVFM.getWidth();
		int rightHeight = rightVFM.getHeight();

		int realHeight;

		if (leftWidth + rightWidth > maxWidth) {
			// 宽度：一行显示不开，需分多行

			realHeight = leftHeight + rightHeight;

			int rightStartX = maxWidth - rightWidth; // rightVFM开始的位置x.
			int rightStartY = leftHeight; // rightVFM开始的位置y.

			setPositionChild(leftVFM, 0, 0);
			setPositionChild(rightVFM, rightStartX, rightStartY);

		} else {
			// 宽度：一行即可完全显示

			realHeight = Math.max(leftHeight, rightHeight);

			int leftStartY = UtilCommon.getOffset(realHeight, leftHeight);

			int rightStartX = maxWidth - rightWidth;
			int rightStartY = UtilCommon.getOffset(realHeight, rightHeight);

			// 使两者都垂直居中。
			setPositionChild(leftVFM, 0, leftStartY);
			setPositionChild(rightVFM, rightStartX, rightStartY);

		}

		setExtent(maxWidth, realHeight);

	}

}