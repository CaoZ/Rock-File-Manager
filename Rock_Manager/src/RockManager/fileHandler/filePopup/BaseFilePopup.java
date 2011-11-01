
package RockManager.fileHandler.filePopup;

import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import RockManager.languages.LangRes;
import RockManager.ui.oneLineInputField.InputField;
import RockManager.ui.oneLineInputField.WrappedOneLineInputArea;
import RockManager.util.MarginPaddingUtil;
import RockManager.util.UtilCommon;
import RockManager.util.ui.BaseButtonField;
import RockManager.util.ui.BasePopupScreen;
import RockManager.util.ui.SeparatorField;


public abstract class BaseFilePopup extends BasePopupScreen implements FieldChangeListener {

	private InputField inputField;

	private BaseButtonField buttonOK;

	private BaseButtonField buttonCancel;


	/**
	 * 重命名文件、创建文件夹等弹出窗口。
	 * 
	 * @param fileItem
	 * @param fileListField
	 */
	public BaseFilePopup() {

		super(VERTICAL_SCROLL | VERTICAL_SCROLLBAR, DEFAULT_CLOSE | DEFAULT_MENU | NO_SYSTEM_MENU_ITEMS);

		addMainArea();

		// 添加一分隔线条。
		SeparatorField sep = new SeparatorField(15, 10);
		add(sep);

		addButtons();

	}


	/**
	 * 添加主要的内容，这个过程中inputField会创建。
	 */
	protected void addMainArea() {

		HorizontalFieldManager mainHFM = new HorizontalFieldManager();
		mainHFM.add(new LabelField(LangRes.get(LangRes.NAME), FIELD_VCENTER));

		WrappedOneLineInputArea inputArea = new WrappedOneLineInputArea(FIELD_VCENTER | USE_ALL_WIDTH);
		inputArea.activeDefaultBorder();

		setInputField(inputArea.getInputField());

		mainHFM.add(inputArea);
		add(mainHFM);

	}


	/**
	 * 设置inputField, 并对这个inputField注册监听器。
	 * 
	 * @param inputField
	 */
	protected void setInputField(InputField newInputField) {

		if (inputField != null) {
			inputField.getManager().replace(inputField, newInputField);
		}
		inputField = newInputField;
		inputField.setChangeListener(this);

	}


	protected InputField getInputField() {

		return inputField;
	}


	/**
	 * 添加按钮。
	 */
	private void addButtons() {

		HorizontalFieldManager buttons = new HorizontalFieldManager(FIELD_HCENTER);

		buttonOK = new BaseButtonField(LangRes.get(LangRes.BUTTON_LABEL_OK_S), ButtonField.CONSUME_CLICK);

		buttonOK.setChangeListener(this);

		buttonCancel = new BaseButtonField(LangRes.get(LangRes.BUTTON_LABEL_CANCEL_S), ButtonField.CONSUME_CLICK);

		buttonCancel.setChangeListener(this);

		MarginPaddingUtil.setMarginRight(buttonOK, 10);
		buttons.add(buttonOK);
		buttons.add(buttonCancel);
		add(buttons);

		registerHotKey(buttonOK, LangRes.get(LangRes.BUTTON_LABEL_OK));
		registerHotKey(buttonCancel, LangRes.get(LangRes.BUTTON_LABEL_CANCEL));

	}


	/**
	 * 设置输入框输入的文字。
	 */
	protected void setInputText(String text) {

		inputField.setText(text);

	}


	protected boolean isEmptyInput() {

		return getInputedText().length() == 0;
	}


	/**
	 * 获取输入的文字（已trim）。
	 * 
	 * @return
	 */
	protected String getInputedText() {

		return inputField.getText().trim();
	}


	public void fieldChanged(Field field, int context) {

		if (field == buttonOK || field == inputField && context == InputField.ENTER_PRESSED) {

			if (isEmptyInput()) {
				// 输入框为空，显示此错误并使输入框重新获得焦点。
				showTextEmptyError();
				focusInputField();
				return;

			} else {
				doOperation();
			}

		} else if (field == buttonCancel) {
			close();
		}

	}


	/**
	 * 点击确定按钮后，确保输入的文件名不为空时要执行的操作。
	 */
	protected abstract void doOperation();


	/**
	 * 若点击了确定按钮，但输入框没有文字，显示此错误。
	 */
	protected void showTextEmptyError() {

		UtilCommon.trace(LangRes.get(LangRes.ERR_FILENAME_CANNOT_EMPTY));
	}


	/**
	 * 使inputField获得焦点。
	 */
	public void focusInputField() {

		// invokeLater: 在错误提示框出现后再移动焦点到输入框，否则错误提示框还未出现输入框又变蓝了。
		UiApplication.getUiApplication().invokeLater(new Runnable() {

			public void run() {

				inputField.setFocus();
			}
		});
	}


	/**
	 * 使确定按钮获得焦点。
	 */
	public void focusOKButton() {

		buttonOK.setFocus();
	}


	protected void makeMenu(Menu menu, int instance) {

		// 主要目的是在输入框单击时出现的菜单中加入"确定", "取消"两项。
		menu.add(MenuItem.separator(100));
		addMenuItemOK(menu, 101);
		addMenuItemCancel(menu, 102);
		menu.add(MenuItem.separator(110));

		super.makeMenu(menu, instance);

	}


	private void addMenuItemOK(Menu menu, int ordinal) {

		String text = LangRes.get(LangRes.BUTTON_LABEL_OK);
		MenuItem ok = new MenuItem(text, ordinal, ordinal) {

			public void run() {

				buttonOK.click();
			}
		};
		menu.add(ok);

	}


	private void addMenuItemCancel(Menu menu, int ordinal) {

		String text = LangRes.get(LangRes.BUTTON_LABEL_CANCEL);
		MenuItem cancel = new MenuItem(text, ordinal, ordinal) {

			public void run() {

				buttonCancel.click();
			}
		};
		menu.add(cancel);

	}

}
