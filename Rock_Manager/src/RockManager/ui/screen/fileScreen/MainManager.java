
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


	public void addToContent(Field field) {

		mainVFM.add(field);
	}


	public void showCount() {

		UiApplication.getUiApplication().invokeLater(new Runnable() {

			public void run() {

				if (!isCountShown()) {
					countLabel = new FileCountLabel();
					add(countLabel);
				}
			}
		});

	}


	public void hideCount() {

		UiApplication.getUiApplication().invokeLater(new Runnable() {

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
