
package RockManager.ui.screen.fileScreen;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.container.AbsoluteFieldManager;
import net.rim.device.api.ui.decor.Background;
import net.rim.device.api.ui.decor.BackgroundFactory;
import RockManager.util.ui.VFMwithTopShadow;


public class MainManager extends AbsoluteFieldManager {

	private VFMwithTopShadow mainVFM;

	private FileCountLabel countLabel;


	/**
	 * 显示文件的主要区域，顶部有阴影效果，并带有多选时的计数标签。
	 */
	public MainManager() {

		mainVFM = new VFMwithTopShadow(USE_ALL_WIDTH | USE_ALL_HEIGHT);

		Bitmap rgb248 = Bitmap.getBitmapResource("img/other/248back.png");
		Background background = BackgroundFactory.createBitmapBackground(rgb248, Background.POSITION_X_LEFT,
				Background.POSITION_Y_TOP, Background.REPEAT_BOTH);
		mainVFM.setBackground(background);

		add(mainVFM);

	}


	protected void sublayout(int width, int height) {

		layoutChild(mainVFM, width, height);

		if (isCountShown()) {
			layoutChild(countLabel, width, height);
			int labelY = height - countLabel.getHeight();
			setPositionChild(countLabel, 0, labelY);
		}

		setExtent(width, height);

	}


	/**
	 * 将field添加到mainVFM中。
	 * 
	 * @param field
	 */
	public void addToContent(Field field) {

		mainVFM.add(field);
	}


	/**
	 * 显示多选计数标签。
	 */
	public void showCount() {

		// 的updateLabel()会调用updateCount()方法，所以要求此处是invokeAndWait()。
		// 若要使用invokeLater(), 需将updateCount()方法也放在invokeLater()中。
		UiApplication.getUiApplication().invokeAndWait(new Runnable() {

			public void run() {

				if (!isCountShown()) {
					countLabel = new FileCountLabel();
					add(countLabel);
				}
			}
		});

	}


	/**
	 * 隐藏多选计数标签。
	 */
	public void hideCount() {

		// 的updateLabel()会调用updateCount()方法，所以要求此处是invokeAndWait()。
		// 若要使用invokeLater(), 需将updateCount()方法也放在invokeLater()中。
		UiApplication.getUiApplication().invokeAndWait(new Runnable() {

			public void run() {

				if (isCountShown()) {
					delete(countLabel);
					countLabel = null;
				}
			}
		});

	}


	/**
	 * 更新数字。
	 * 
	 * @param count
	 */
	public void updateCount(int count) {

		countLabel.setCount(count);
	}


	/**
	 * 计数标签是否显示了。
	 * 
	 * @return
	 */
	public boolean isCountShown() {

		if (countLabel == null) {
			return false;
		}
		if (countLabel.getIndex() == -1) {
			return false;
		}
		return true;
	}

}
