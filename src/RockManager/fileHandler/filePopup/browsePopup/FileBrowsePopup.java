
package RockManager.fileHandler.filePopup.browsePopup;

import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import RockManager.fileHandler.FileHandler;
import RockManager.fileHandler.filePopup.BaseFilePopup;
import RockManager.fileList.filePicker.FilePicker;
import RockManager.languages.LangRes;
import RockManager.ui.oneLineInputField.FileNameInputField;
import RockManager.ui.oneLineInputField.WrappedOneLineInputArea;
import RockManager.util.MarginPaddingUtil;
import RockManager.util.UtilCommon;


/**
 * 文件路径输入选择弹出窗口。 包含一个地址输入框及一个Browse按钮，按按钮可调出FilePicker。
 */
public class FileBrowsePopup extends BaseFilePopup {

	private String defaultDestinationPath;

	private String selectedPath;


	public FileBrowsePopup() {

		// empty
	}


	public FileBrowsePopup(String title) {

		setTitle(title);
	}


	protected void addMainArea() {

		LabelField destinationPath = new LabelField(LangRes.get(LangRes.LABEL_DESTINATION_PATH));
		// 设置左边距。因为输入框有一定边框，使这个label右移4以跟输入框左对齐。
		MarginPaddingUtil.setMarginLeft(destinationPath, 4);
		add(destinationPath);

		// 地址输入框
		final WrappedOneLineInputArea locationArea = createLocationArea();

		// "浏览"按钮
		final ButtonField browseButton = createBrowseButton();

		HorizontalFieldManager addressArea = new HorizontalFieldManager() {

			protected void sublayout(int maxWidth, int maxHeight) {

				layoutChild(browseButton, maxWidth, maxHeight);
				int buttonWidth = browseButton.getWidth();
				int buttonHeight = browseButton.getHeight();

				layoutChild(locationArea, maxWidth - buttonWidth, maxHeight);
				int locationWidth = locationArea.getWidth();
				int locationHeight = locationArea.getHeight();

				int realHeight = Math.max(buttonHeight, locationHeight);

				int locationY = UtilCommon.getOffset(realHeight, locationHeight);
				setPositionChild(locationArea, 0, locationY);

				int buttonY = UtilCommon.getOffset(realHeight, buttonHeight);
				setPositionChild(browseButton, locationWidth, buttonY);

				setExtent(maxWidth, realHeight);

			};
		};

		addressArea.add(locationArea);
		addressArea.add(browseButton);

		add(addressArea);

	}


	/**
	 * 创建地址输入框。
	 */
	private WrappedOneLineInputArea createLocationArea() {

		WrappedOneLineInputArea locationArea = new WrappedOneLineInputArea(FIELD_VCENTER);
		locationArea.activeDefaultBorder();

		FileNameInputField inputField = new FileNameInputField();
		locationArea.replaceInputField(inputField);
		setInputField(inputField);

		return locationArea;

	}


	/**
	 * 设置默认路径，呼出FilePicker时的默认地址(若输入框中地址不可用时地址)。
	 * 
	 * @param destinationPath
	 */
	public void setDefaultDestinationPath(String destinationPath) {

		defaultDestinationPath = destinationPath;
		if (isEmptyInput()) {
			setDestinationPath(destinationPath);
		}
	}


	/**
	 * 设置输入框地址。
	 * 
	 * @param destinationPath
	 */
	public void setDestinationPath(String destinationPath) {

		setInputText(destinationPath);
	}


	/**
	 * 创建"浏览"按钮。
	 * 
	 * @return
	 */
	private ButtonField createBrowseButton() {

		ButtonField browseButton = new ButtonField(LangRes.get(LangRes.BUTTON_LABEL_BROWSE), FIELD_VCENTER
				| ButtonField.CONSUME_CLICK);

		browseButton.setChangeListener(new FieldChangeListener() {

			public void fieldChanged(Field field, int context) {

				String initialURL = UtilCommon.toURLForm(getInputedText());
				if (UtilCommon.isFolder(initialURL) == false) {
					// 可能是个文件，转到上级。
					initialURL = UtilCommon.getParentDir(initialURL);
				}
				if (FileHandler.isFolderExists(initialURL) == false) {
					initialURL = defaultDestinationPath;
				}

				FilePicker filePicker = new FilePicker(initialURL);
				final String selectedPath = filePicker.show();

				if (selectedPath != null) {

					// invokeLater:
					// 若不是用invokeLater在除storm和torch以外的机型上可能导致FilePicker关闭动画的消失。
					UiApplication.getUiApplication().invokeLater(new Runnable() {

						public void run() {

							setDestinationPath(selectedPath);
						}
					});

				}

			}
		});

		return browseButton;

	}


	protected void showTextEmptyError() {

		UtilCommon.trace(LangRes.get(LangRes.ERR_PATH_CANNOT_EMPTY));
	}


	/**
	 * 显示此popup。
	 * 
	 * @return 输入框中地址，若取消了选择则返回null.
	 */
	public String show() {

		UiApplication.getUiApplication().pushModalScreen(this);
		return selectedPath;

	}


	protected void doOperation() {

		selectedPath = getInputedText();
		close();

	}

}
