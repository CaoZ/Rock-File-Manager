
package RockManager.ui.screen.propertyScreen;

import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.NullField;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.decor.BackgroundFactory;
import RockManager.fileList.FileItem;
import RockManager.languages.LangRes;
import RockManager.ui.titledPanel.TitledPanel;
import RockManager.util.ui.AnimatedMainScreen;
import RockManager.util.ui.VFMwithScrollbar;


public class BasePropertyScreen extends AnimatedMainScreen {

	private FileItem thisFile;

	private VFMwithScrollbar mainVFM;


	public BasePropertyScreen(FileItem file) {

		super(NO_VERTICAL_SCROLL | NO_SYSTEM_MENU_ITEMS);

		thisFile = file;
		mainVFM = new VFMwithScrollbar();
		getMainManager().add(mainVFM);

		getMainManager().setBackground(BackgroundFactory.createSolidBackground(0xf7f7f7));
		mainVFM.setBackground(BackgroundFactory.createSolidBackground(0xf7f7f7));

		add(new NullField());
		setTitleField();

	}


	/**
	 * 设置标题栏。
	 */
	private void setTitleField() {

		BitmapField iconField = new BitmapField(thisFile.getIcon(), FIELD_VCENTER);

		HorizontalFieldManager titleField = new HorizontalFieldManager();
		titleField.setPadding(2, 4, 2, 4);
		titleField.add(iconField);

		String fileName = thisFile.getDisplayName();

		String properties = " " + LangRes.getString(LangRes.PROPERTIES);

		LabelField label = new LabelField(fileName + properties, FIELD_VCENTER | DrawStyle.ELLIPSIS
				| DrawStyle.TRUNCATE_BEGINNING);

		label.setMargin(0, 20, 0, 6);
		titleField.add(label);

		setTitle(titleField);

	}


	protected FileItem getThisFile() {

		return thisFile;
	}


	protected void addInfoToPanel(TitledPanel panel, Field infoField, int marginBottom) {

		infoField.setMargin(0, 0, marginBottom, 0);
		panel.add(infoField);

	}


	protected void addInfoToPanel(TitledPanel panel, Field infoField) {

		panel.add(infoField);

	}


	/**
	 * 添加到mainVFM(VFMwithScrollbar)
	 */
	public void add(Field field) {

		mainVFM.add(field);
	}

}
