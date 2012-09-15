
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

	private FileItem[] itemsToCompress;

	private FileListField parentFileList;


	public CreateArchivePopup(FileListField parentFileList) {

		if (parentFileList.isMultiSelecting()) {
			itemsToCompress = parentFileList.getSelectedFiles();
		} else {
			itemsToCompress = new FileItem[1];
			itemsToCompress[0] = parentFileList.getThisItem();
		}
		this.parentFileList = parentFileList;

		setTitle(LangRes.get(LangRes.TITLE_CREATE_ARCHIVE));

		String parentPath = parentFileList.getFolderPath();

		setDestinationPath(parentPath + getPreferedArchiveName());
		setDefaultDestinationPath(parentPath);

		focusOKButton();

	}


	private String getPreferedArchiveName() {

		String archive_name;
		String suffix = ".zip";

		if (itemsToCompress.length == 1) {
			// 只有一个文件, 以文件名组成压缩文件名.
			String itemPath = itemsToCompress[0].getPath();
			archive_name = UtilCommon.getName(itemPath, false);
		} else {
			// 两个或以上文件, 以父文件夹名组成压缩文件名.
			String parent_path = parentFileList.getFolderPath();
			archive_name = UtilCommon.getName(parent_path, false);
		}

		return archive_name + suffix;

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

		LabelField methodLabel = new LabelField(LangRes.get(LangRes.LABEL_COMPRESS_METHOD));
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

		if (parentFileList.isMultiSelecting()) {
			parentFileList.leaveMultiSelectMode();
		}

		final String saveURL = UtilCommon.toURLForm(getInputedText());
		CompressMethod method = (CompressMethod) compressMethodChoiceField.getChoice(compressMethodChoiceField
				.getSelectedIndex());
		final int compressMethod = method.getMethod();

		UiApplication.getUiApplication().invokeLater(new Runnable() {

			public void run() {

				// popup中有一个将在invokeLater中执行的线程，故popup的构造要放在此invokeLater中。
				FileCompressProgressPopup progressPopup = new FileCompressProgressPopup(itemsToCompress, saveURL,
						compressMethod, parentFileList);
				UiApplication.getUiApplication().pushScreen(progressPopup);

			}
		});

	}
}
