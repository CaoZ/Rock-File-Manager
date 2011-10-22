
package RockManager.util.ui;

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

		// 最多只使用88%的宽度，使leftVFM, ightVFM不能完全占据整行，而能左对齐或右对齐。
		int widthForSeparateUse = (int) (maxWidth * 0.85);

		layoutChild(leftVFM, widthForSeparateUse, maxHeight);
		int leftWidth = leftVFM.getWidth();
		int leftHeight = leftVFM.getHeight();

		layoutChild(rightVFM, widthForSeparateUse, maxHeight);
		int rightWidth = rightVFM.getWidth();
		int rightHeight = rightVFM.getHeight();

		setPositionChild(leftVFM, 0, 0);

		if (leftWidth + rightWidth > maxWidth) {

			// 宽度：一行显示不开，需分多行

			int rightStartX = maxWidth - rightWidth;
			int rightStartY = leftHeight;
			setPositionChild(rightVFM, rightStartX, rightStartY);

			int realHeight = leftHeight + rightHeight;
			setExtent(maxWidth, realHeight);

		} else {

			// 宽度：一行即可完全显示

			int rightStartX = maxWidth - rightWidth;
			setPositionChild(rightVFM, rightStartX, 0);

			int realHeight = Math.max(leftHeight, rightHeight);
			setExtent(maxWidth, realHeight);

		}

	}
}