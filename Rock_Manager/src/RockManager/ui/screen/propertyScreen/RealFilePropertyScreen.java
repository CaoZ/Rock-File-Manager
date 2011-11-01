
package RockManager.ui.screen.propertyScreen;

import java.io.IOException;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import net.rim.device.api.i18n.DateFormat;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Touchscreen;
import net.rim.device.api.ui.component.CheckboxField;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import RockManager.fileList.FileItem;
import RockManager.languages.LangRes;
import RockManager.ui.titledPanel.TitledPanel;
import RockManager.util.MarginPaddingUtil;
import RockManager.util.UtilCommon;
import RockManager.util.ui.LeftRightManager;
import RockManager.util.ui.MyStyle;


/**
 * 显示文件属性的Screen(实际存在的文件/文件夹，非压缩文件中的文件/文件夹).
 */
public class RealFilePropertyScreen extends BasePropertyScreen {

	private FileConnection fconn;

	private CheckboxField isReadOnlyCheckbox;

	private CheckboxField isHiddenCheckbox;

	private boolean originIsReadOnly;

	private boolean originIsHidden;


	public RealFilePropertyScreen(FileItem file) {

		super(file);

		createConnect();

		addBasicInfo();
		addAttributeInfo();

		closeConnect();

	}


	/**
	 * 建立FileConnection连接。
	 */
	private void createConnect() {

		try {
			fconn = (FileConnection) Connector.open(getThisFile().getURL());
		} catch (Exception e) {
			// 不能建立文件连接
			e.printStackTrace();
		}

	}


	/**
	 * 关闭FileConnection连接。
	 */
	private void closeConnect() {

		if (fconn != null) {
			try {
				fconn.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}


	/**
	 * 添加基本信息。
	 */
	private void addBasicInfo() {

		TitledPanel basicInfoPanel = new TitledPanel(LangRes.get(LangRes.BASIC_INFO));
		basicInfoPanel.setPadding(9, 10, 9, 10);

		addInfoToPanel(basicInfoPanel, getFileNameArea(), 7);

		addInfoToPanel(basicInfoPanel, getFileLocationArea(), 7);

		addInfoToPanel(basicInfoPanel, getFileSizeArea(), 7);

		if (fconn != null && fconn.isDirectory()) {
			addInfoToPanel(basicInfoPanel, getFolderCountArea(), 7);
		}

		addInfoToPanel(basicInfoPanel, getFileTimeArea());

		add(basicInfoPanel);

	}


	private Field getFileNameArea() {

		String fileName = getThisFile().getDisplayName();

		LeftRightManager fileNameArea = new LeftRightManager();

		fileNameArea.addToLeft(new KeyLabel(LangRes.get(LangRes.NAME)));
		fileNameArea.addToRight(new ValueLabel(fileName));

		return fileNameArea;

	}


	private Field getFileLocationArea() {

		String fileLocation = UtilCommon.getParentDir(getThisFile().getRawPath());

		LeftRightManager fileLocationArea = new LeftRightManager();

		fileLocationArea.addToLeft(new KeyLabel(LangRes.get(LangRes.LOCATION)));
		fileLocationArea.addToRight(new ValueLabel(fileLocation));

		return fileLocationArea;

	}


	private Field getFileSizeArea() {

		LeftRightManager fileSizeArea = new LeftRightManager();

		fileSizeArea.addToLeft(new KeyLabel(LangRes.get(LangRes.SIZE)));
		fileSizeArea.addToRight(new FileSizeLabel(getThisFile()));

		return fileSizeArea;

	}


	private Field getFileTimeArea() {

		long time = -1;
		try {
			time = fconn.lastModified();
		} catch (Exception e) {
		}

		String fileTimeString = null;

		if (time > 0) {
			DateFormat formater = DateFormat.getInstance(DateFormat.DATETIME_DEFAULT);
			fileTimeString = formater.formatLocal(time);
		} else {
			// can't get the right time.
			fileTimeString = "-";
		}

		LeftRightManager fileTimeArea = new LeftRightManager();

		fileTimeArea.addToLeft(new KeyLabel(LangRes.get(LangRes.MODIFIED)));
		fileTimeArea.addToRight(new ValueLabel(fileTimeString));

		return fileTimeArea;

	}


	private Field getFolderCountArea() {

		LeftRightManager folderCountArea = new LeftRightManager();

		folderCountArea.addToLeft(new KeyLabel(LangRes.get(LangRes.CONTAINS)));
		folderCountArea.addToRight(new FileCountLabel(fconn.getURL()));

		return folderCountArea;

	}


	private void addAttributeInfo() {

		TitledPanel attributeInfoPanel = new TitledPanel(LangRes.get(LangRes.ATTRIBUTES));
		attributeInfoPanel.setPadding(4, 10, 4, 10);

		attributeInfoPanel.add(getAttributeArea());

		add(attributeInfoPanel);

	}


	private Field getAttributeArea() {

		try {
			originIsReadOnly = (fconn.canWrite() == false);
			originIsHidden = fconn.isHidden();
		} catch (Exception e) {
		}

		isReadOnlyCheckbox = new CheckboxField(LangRes.get(LangRes.READ_ONLY), originIsReadOnly,
				MyStyle.NO_USE_ALL_WIDTH);
		isHiddenCheckbox = new CheckboxField(LangRes.get(LangRes.HIDDEN), originIsHidden, MyStyle.NO_USE_ALL_WIDTH);

		HorizontalFieldManager hfm = new HorizontalFieldManager() {

			// 重写layout, 因为在某些机器上CheckBox会占用全部宽度，如9780.
			protected void sublayout(int maxWidth, int maxHeight) {

				int checkBoxWidth = (int) (maxWidth * 0.46);

				layoutChild(isReadOnlyCheckbox, checkBoxWidth, maxHeight);
				setPositionChild(isReadOnlyCheckbox, 0, 0);

				layoutChild(isHiddenCheckbox, checkBoxWidth, maxHeight);
				setPositionChild(isHiddenCheckbox, checkBoxWidth, 0);

				int height = Math.max(isReadOnlyCheckbox.getHeight(), isHiddenCheckbox.getHeight());

				setExtent(maxWidth, height);

			}

		};

		hfm.add(isReadOnlyCheckbox);
		hfm.add(isHiddenCheckbox);

		if (Touchscreen.isSupported() == false) {
			// 增加高度。不支持触屏的机器上复选框高度较小，导致与边框的距离太小。
			MarginPaddingUtil.setMargin(hfm, "top bottom", "5 5");
		}

		return hfm;

	}


	protected boolean onSave() {

		boolean isReadOnly = isReadOnlyCheckbox.getChecked();
		boolean isHidden = isHiddenCheckbox.getChecked();

		boolean attributeReadOnlyChanged = (isReadOnly != originIsReadOnly);
		boolean attributeHiddenChanged = (isHidden != originIsHidden);

		if (attributeHiddenChanged == false && attributeReadOnlyChanged == false) {
			return true;
		}

		createConnect();

		try {
			if (attributeHiddenChanged) {
				fconn.setHidden(isHidden);
			}
			if (attributeReadOnlyChanged) {
				fconn.setWritable(!isReadOnly);
			}
		} catch (Exception e) {
			// 不能修改文件属性。
			UtilCommon.trace("Unable to modify file attributes: " + e.getMessage());
			e.printStackTrace();
		}

		closeConnect();

		return true;

	}

}
