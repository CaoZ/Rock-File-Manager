
package RockManager.ui.progressPopup;

import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.component.ButtonField;
import RockManager.languages.LangRes;
import RockManager.ui.MyUI;
import RockManager.ui.statusBar.StatusBar;
import RockManager.util.ui.BasePopupScreen;


public class ProgressPopup extends BasePopupScreen implements FieldChangeListener {

	/**
	 * 正在处理的文件，如"happy.cod".
	 */
	private ProgressLabelField progressLabel;

	/**
	 * 进度，0到100.
	 */
	private StatusBar progressBar;

	private ButtonField cancelButton;

	private Runnable cancelRunnable;


	public ProgressPopup() {

		super(VERTICAL_SCROLL | VERTICAL_SCROLLBAR, 0);

		// add progress Label
		progressLabel = new ProgressLabelField();
		progressLabel.setFont(MyUI.deriveFont(0.8f));
		progressLabel.setMargin(0, 0, 13, 0);
		progressLabel.setName("initialing...");
		add(progressLabel);

		// add progress StatusBar;
		progressBar = new StatusBar(StatusBar.STYLE_BLUE);
		progressBar.setHeight(10);
		progressBar.setProgress(0);
		progressBar.setMargin(0, 0, 10, 0);
		add(progressBar);

	}


	/**
	 * 添加取消按钮。
	 */
	private void addCancelButton() {

		// add cancel button
		cancelButton = new ButtonField(LangRes.get(LangRes.BUTTON_LABEL_CANCEL), FIELD_HCENTER
				| ButtonField.CONSUME_CLICK);
		cancelButton.setFont(MyUI.deriveFont(0.8f));

		int marginTop = MyUI.deriveSize(10);
		cancelButton.setMargin(marginTop, 0, 0, 0);

		cancelButton.setChangeListener(this);
		add(cancelButton);

	}


	/**
	 * 设置是否可以中途取消操作。
	 */
	public void setCancelable(boolean value) {

		if (value == true && cancelButton == null) {
			addCancelButton();
		}
	}


	/**
	 * 设置正在处理的文件的名称。
	 * 
	 * @param name
	 */
	public void setProgressName(String name) {

		progressLabel.setName(name);
	}


	/**
	 * 设置进度显示。
	 * 
	 * @param rate
	 *            进度，0-100
	 */
	public void setProgressRate(int rate) {

		progressLabel.setRate(rate);
		progressBar.setProgress(rate);
	}


	public void fieldChanged(Field field, int context) {

		if (field == cancelButton) {
			cancelOperation();
		}
	}


	public void setCancelRunnable(Runnable cancelRunnable) {

		this.cancelRunnable = cancelRunnable;
	}


	/**
	 * 单击了取消按钮。
	 */
	public void cancelOperation() {

		if (cancelRunnable != null) {
			cancelRunnable.run();
		}

	}

}
