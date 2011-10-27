
package RockManager.fileHandler.filePopup.browsePopup;

import RockManager.archive.CompressMethod;
import RockManager.fileList.FileItem;
import RockManager.fileList.FileListField;
import RockManager.languages.LangRes;
import RockManager.ui.progressPopup.FileCompressProgressPopup;
import RockManager.util.UtilCommon;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.ObjectChoiceField;


/**
 * 创建压缩文件窗口。
 */
public class CreateArchivePopup extends FileBrowsePopup {

	private ObjectChoiceField compressMethodChoiceField;

	private FileItem itemToCompress;

	private FileListField parentFileList;


	public CreateArchivePopup(FileItem itemToCompress, FileListField parentFileList) {

		this.itemToCompress = itemToCompress;
		this.parentFileList = parentFileList;
		setTitle(LangRes.getString(LangRes.TITLE_CREATE_ARCHIVE));

		String itemPath = itemToCompress.getPath();
		String parentPath = UtilCommon.getParentDir(itemPath);

		setDestinationPath(parentPath + getPreferedArchiveName());
		setDefaultDestinationPath(parentPath);

		focusOKButton();

	}


	private String getPreferedArchiveName() {

		String itemPath = itemToCompress.getPath();
		String itemName = UtilCommon.getName(itemPath, false);
		String suffix = ".zip";

		return itemName + suffix;

	}


	public void setDestinationPath(String destinationPath) {

		if (UtilCommon.isFolder(destinationPath)) {
			// 设置的是个目录。
			destinationPath += getPreferedArchiveName();
		}
		super.setDestinationPath(destinationPath);
	}


	protected void addMainArea() {

		super.addMainArea();
		addCompressMethodArea();

	}


	private void addCompressMethodArea() {

		LabelField methodLabel = new LabelField(LangRes.getString(LangRes.LABEL_COMPRESS_METHOD));
		// 设置左边距。因为上面的输入框有一定边框，使这个label右移4以跟输入框左对齐。
		methodLabel.setMargin(4, 0, 3, 4);
		add(methodLabel);

		CompressMethod method_store = new CompressMethod(CompressMethod.STORE);
		CompressMethod method_fastest = new CompressMethod(CompressMethod.FASTEST);
		CompressMethod method_fast = new CompressMethod(CompressMethod.FAST);
		CompressMethod method_normal = new CompressMethod(CompressMethod.NORMAL);
		CompressMethod method_good = new CompressMethod(CompressMethod.GOOD);
		CompressMethod method_best = new CompressMethod(CompressMethod.BEST);

		CompressMethod[] methods = { method_store, method_fastest, method_fast, method_normal, method_good, method_best };

		compressMethodChoiceField = new ObjectChoiceField("", methods, 1, FIELD_LEFT) {

			protected void layout(int width, int height) {

				setMinimalWidth(width - 62); // 似乎61是最小值，小于此值方法会无效。
				super.layout(width, height);
			}

		};

		add(compressMethodChoiceField);

	}


	protected void doOperation() {

		close();

		String originFileURL = itemToCompress.getURL();
		String saveURL = UtilCommon.toURLForm(getInputedText());
		CompressMethod method = (CompressMethod) compressMethodChoiceField.getChoice(compressMethodChoiceField
				.getSelectedIndex());
		int compressMethod = method.getMethod();
		final FileCompressProgressPopup progressPopup = new FileCompressProgressPopup(originFileURL, saveURL,
				compressMethod, parentFileList);

		UiApplication.getUiApplication().invokeLater(new Runnable() {

			public void run() {

				UiApplication.getUiApplication().pushScreen(progressPopup);

			}
		});

	}
}
