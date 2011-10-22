
package RockManager.ui.screen.propertyScreen;

import net.rim.device.api.i18n.DateFormat;
import net.rim.device.api.ui.Field;
import net.sf.zipme.ZipEntry;
import RockManager.archive.ArchiveEntry;
import RockManager.fileHandler.FileSizeString;
import RockManager.fileHandler.fileCounter.FileCountResult;
import RockManager.fileHandler.fileCounter.FileCounter;
import RockManager.fileList.FileItem;
import RockManager.fileList.FileListField;
import RockManager.languages.LangRes;
import RockManager.ui.titledPanel.TitledPanel;
import RockManager.util.ui.LeftRightManager;
import de.innosystec.unrar.rarfile.FileHeader;


public class FileInArchivePropertyScreen extends BasePropertyScreen {

	private FileListField parentFileList;

	private long originSize;

	private long packedSize;


	public FileInArchivePropertyScreen(FileListField parentFileList, FileItem file) {

		super(file);

		this.parentFileList = parentFileList;

		addBasicInfoPanel();

	}


	private void addBasicInfoPanel() {

		TitledPanel basicInfoPanel = new TitledPanel(LangRes.getString(LangRes.BASIC_INFO));
		basicInfoPanel.setPadding(9, 10, 9, 10);

		addInfoToPanel(basicInfoPanel, getFileNameArea(), 7);

		addInfoToPanel(basicInfoPanel, getLocationArea(), 7);

		addInfoToPanel(basicInfoPanel, getOriginSizeArea(), 7);

		addInfoToPanel(basicInfoPanel, getPackedSizeArea(), 7);

		addInfoToPanel(basicInfoPanel, getCompressRatioArea(), 7);

		if (getThisFile().isDir()) {
			addInfoToPanel(basicInfoPanel, getFolderCountArea(), 7);
		}

		addInfoToPanel(basicInfoPanel, getFileTimeArea());

		add(basicInfoPanel);

	}


	private Field getFileNameArea() {

		String fileName = getThisFile().getDisplayName();

		LeftRightManager fileNameArea = new LeftRightManager();

		fileNameArea.addToLeft(new KeyLabel(LangRes.getString(LangRes.NAME)));
		fileNameArea.addToRight(new ValueLabel(fileName));

		return fileNameArea;
	}


	private Field getLocationArea() {

		String fileLocation = parentFileList.getAddressBar().getAddress();

		LeftRightManager fileLocationArea = new LeftRightManager();

		fileLocationArea.addToLeft(new KeyLabel(LangRes.getString(LangRes.LOCATION)));
		fileLocationArea.addToRight(new ValueLabel(fileLocation));

		return fileLocationArea;

	}


	private Field getOriginSizeArea() {

		ArchiveEntry originEntry = getThisFile().getOriginArchiveEntry();

		originSize = getEntrySize(originEntry, true);

		String originSizeString = FileSizeString.getSizeString(originSize, false, true);

		LeftRightManager originSizeArea = new LeftRightManager();

		originSizeArea.addToLeft(new KeyLabel(LangRes.getString(LangRes.SIZE)));
		originSizeArea.addToRight(new ValueLabel(originSizeString));

		return originSizeArea;

	}


	private Field getPackedSizeArea() {

		ArchiveEntry originEntry = getThisFile().getOriginArchiveEntry();

		packedSize = getEntrySize(originEntry, false);

		String packedSizeString = FileSizeString.getSizeString(packedSize, false, true);

		LeftRightManager packedSizeArea = new LeftRightManager();

		packedSizeArea.addToLeft(new KeyLabel(LangRes.getString(LangRes.PACKED_SIZE)));
		packedSizeArea.addToRight(new ValueLabel(packedSizeString));

		return packedSizeArea;

	}


	private Field getCompressRatioArea() {

		float compressRatio = 1;

		if (originSize != 0) {
			compressRatio = (float) packedSize / (float) originSize;
		}

		// 即便只压缩了一点，也是99%, 而不是100%
		String compressRatioString = (int) (compressRatio * 100) + " %";

		LeftRightManager compressRatioArea = new LeftRightManager();

		compressRatioArea.addToLeft(new KeyLabel(LangRes.getString(LangRes.COMPRESSION_RATIO)));
		compressRatioArea.addToRight(new ValueLabel(compressRatioString));

		return compressRatioArea;

	}


	private Field getFolderCountArea() {

		ArchiveEntry folderEntry = getThisFile().getOriginArchiveEntry();
		FileCountResult countResult = FileCounter.countFolder(folderEntry);

		LeftRightManager folderCountArea = new LeftRightManager();

		folderCountArea.addToLeft(new KeyLabel(LangRes.getString(LangRes.CONTAINS)));
		folderCountArea.addToRight(new ValueLabel(countResult.toString()));

		return folderCountArea;

	}


	private Field getFileTimeArea() {

		Object originDataEntry = getThisFile().getOriginArchiveEntry().getOriginDataEntry();

		long modifiedTime = 0;

		if (originDataEntry instanceof ZipEntry) {
			modifiedTime = ((ZipEntry) originDataEntry).getTime();
		} else if (originDataEntry instanceof FileHeader) {
			FileHeader header = (FileHeader) originDataEntry;
			modifiedTime = header.getMTime().getTime();
		}

		String fileTimeString = null;

		if (modifiedTime > 0) {
			DateFormat formater = DateFormat.getInstance(DateFormat.DATETIME_DEFAULT);
			fileTimeString = formater.formatLocal(modifiedTime);
		} else {
			// can't get the right time.
			fileTimeString = "-";
		}

		LeftRightManager fileTimeArea = new LeftRightManager();

		fileTimeArea.addToLeft(new KeyLabel(LangRes.getString(LangRes.MODIFIED)));
		fileTimeArea.addToRight(new ValueLabel(fileTimeString));

		return fileTimeArea;

	}


	private long getEntrySize(ArchiveEntry entry, boolean originSize) {

		long size = 0;

		if (originSize) {
			// 需要的是未压缩大小
			size = entry.getFileSize();
		} else {
			// 需要的是压缩后大小
			size = entry.getPackedSize();
		}

		return size;

	}

}
