
package RockManager.config;

import net.rim.device.api.ui.Touchscreen;
import net.rim.device.api.ui.component.CheckboxField;
import net.rim.device.api.ui.component.NullField;
import net.rim.device.api.ui.component.StandardTitleBar;
import net.rim.device.api.ui.decor.BackgroundFactory;
import RockManager.languages.LangRes;
import RockManager.ui.titledPanel.TitledPanel;
import RockManager.util.MarginPaddingUtil;
import RockManager.util.ui.AnimatedMainScreen;
import RockManager.util.ui.MyStyle;
import RockManager.util.ui.VFMwithScrollbar;


public class OptionsScreen extends AnimatedMainScreen {

	private VFMwithScrollbar mainVFM;

	private CheckboxField showHidden;

	private CheckboxField addReturn;

	private CheckboxField animationEffect;


	public OptionsScreen() {

		super(NO_VERTICAL_SCROLL | NO_SYSTEM_MENU_ITEMS);

		StandardTitleBar titleBar = new StandardTitleBar();
		titleBar.addTitle(LangRes.get(LangRes.MENU_TITLE_OPTIONS));
		titleBar.addNotifications();
		titleBar.addSignalIndicator();
		setTitle(titleBar);

		add(new NullField());

		// MainManager也设置背景色，保证拖动时不会露出白色（在触摸屏机型上即使下面没内容了还可以向下拉，类似弹性缓冲效果）。
		getMainManager().setBackground(BackgroundFactory.createSolidBackground(0xf7f7f7));

		mainVFM = new VFMwithScrollbar(USE_ALL_WIDTH);
		mainVFM.setBackground(BackgroundFactory.createSolidBackground(0xf7f7f7));

		addDisplayOptions();

		add(mainVFM);

	}


	private void addDisplayOptions() {

		TitledPanel displayOptions = new TitledPanel(LangRes.get(LangRes.OPTIONS_DISPLAY));
		displayOptions.setPadding(5, 10, 5, 10);

		addShowHidden(displayOptions);
		addAddReturn(displayOptions);
		addAnimationEffect(displayOptions);

		mainVFM.add(displayOptions);

	}


	private void addShowHidden(TitledPanel displayOptions) {

		String label = LangRes.get(LangRes.OPTIONS_SHOW_HIDDEN_FILES);
		boolean value = ConfigData.SHOW_HIDDEN_FILE.booleanValue();

		CheckboxField checkBox = new CheckboxField(label, value, MyStyle.LABEL_BOX_CHECKBOX);
		setUpDownPadding(checkBox);
		displayOptions.add(checkBox);

		showHidden = checkBox;

	}


	private void addAddReturn(TitledPanel displayOptions) {

		String label = LangRes.get(LangRes.OPTIONS_SHOW_RETURN_ITEM);
		boolean value = ConfigData.ADD_RETURN_ITEM.booleanValue();

		CheckboxField checkBox = new CheckboxField(label, value, MyStyle.LABEL_BOX_CHECKBOX);
		setUpDownPadding(checkBox);
		displayOptions.add(checkBox);

		addReturn = checkBox;

	}


	private void addAnimationEffect(TitledPanel displayOptions) {

		String label = LangRes.get(LangRes.OPTIONS_ANIMATION_EFFECT);
		boolean value = ConfigData.ANIMATION_EFFECT.booleanValue();

		CheckboxField checkBox = new CheckboxField(label, value, MyStyle.LABEL_BOX_CHECKBOX);
		setUpDownPadding(checkBox);
		displayOptions.add(checkBox);

		animationEffect = checkBox;

	}


	private void setUpDownPadding(CheckboxField checkboxField) {

		if (Touchscreen.isSupported() == false) {
			MarginPaddingUtil.setPadding(checkboxField, "top bottom", "2 2");
		}

	}


	protected boolean onSave() {

		boolean isShowHidden = showHidden.getChecked();
		boolean isAddReturn = addReturn.getChecked();
		boolean isAnimationEffect = animationEffect.getChecked();

		ConfigData.SHOW_HIDDEN_FILE.setBooleanValue(isShowHidden);
		ConfigData.ADD_RETURN_ITEM.setBooleanValue(isAddReturn);
		ConfigData.ANIMATION_EFFECT.setBooleanValue(isAnimationEffect);

		ConfigData.savaConfig();

		return true;

	}

}
