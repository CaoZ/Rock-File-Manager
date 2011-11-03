
package RockManager.fileClipboard;

import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import RockManager.fileList.FileItem;
import RockManager.languages.LangRes;
import RockManager.ui.MyUI;
import RockManager.util.MarginPaddingUtil;
import RockManager.util.UtilCommon;
import RockManager.util.ui.BaseButtonField;
import RockManager.util.ui.BasePopupScreen;
import RockManager.util.ui.LeftRightManager;
import RockManager.util.ui.SeparatorField;


public class FileClipboardPopup extends BasePopupScreen implements FieldChangeListener {

	private BaseButtonField buttonOK;

	private BaseButtonField buttonClear;


	public FileClipboardPopup() {

		super(VERTICAL_SCROLL | VERTICAL_SCROLLBAR, DEFAULT_CLOSE);

		setTitle(LangRes.get(LangRes.MENU_TITLE_CLIPBOARD));

		FileItem thisItem = FileClipboard.get();

		addFileNameArea(thisItem);
		addFileLocationArea(thisItem);

		// 添加一分隔线条。
		int marginTop = MyUI.deriveSize(15);
		int marginBottom = MyUI.deriveSize(10);

		SeparatorField sep = new SeparatorField(marginTop, marginBottom);
		add(sep);

		addButtons();

	}


	private void addFileNameArea(FileItem thisItem) {

		String fileName = thisItem.getDisplayName();

		LeftRightManager fileNameArea = new LeftRightManager();

		fileNameArea.addToLeft(new LabelField(LangRes.get(LangRes.NAME)));
		fileNameArea.addToRight(new LabelField(fileName));

		add(fileNameArea);

	}


	private void addFileLocationArea(FileItem thisItem) {

		String fileLocation = UtilCommon.getParentDir(thisItem.getRawPath());

		LeftRightManager fileLocationArea = new LeftRightManager();

		fileLocationArea.addToLeft(new LabelField(LangRes.get(LangRes.LOCATION)));
		fileLocationArea.addToRight(new LabelField(fileLocation, DrawStyle.RIGHT));

		MarginPaddingUtil.setMarginTop(fileLocationArea, 5);

		add(fileLocationArea);

	}


	private void addButtons() {

		// 添加按钮。
		HorizontalFieldManager buttons = new HorizontalFieldManager(FIELD_HCENTER);

		buttonOK = new BaseButtonField(LangRes.get(LangRes.BUTTON_LABEL_OK_S), ButtonField.CONSUME_CLICK);
		buttonOK.setChangeListener(this);

		buttonClear = new BaseButtonField(LangRes.get(LangRes.BUTTON_LABEL_CLEAR_S), ButtonField.CONSUME_CLICK);
		buttonClear.setChangeListener(this);

		MarginPaddingUtil.setMarginRight(buttonOK, 10);
		buttons.add(buttonOK);
		buttons.add(buttonClear);
		add(buttons);

		registerHotKey(buttonOK, LangRes.get(LangRes.BUTTON_LABEL_OK));
		registerHotKey(buttonClear, LangRes.get(LangRes.BUTTON_LABEL_CLEAR_S));

	}


	public void fieldChanged(Field field, int context) {

		if (field == buttonOK) {
			close();
		} else if (field == buttonClear) {
			close();
			FileClipboard.clear();
		}

	}

}
