
package RockManager.fileList;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileSystemRegistry;
import net.rim.device.api.io.FileInfo;
import net.rim.device.api.io.file.ExtendedFileConnection;
import net.rim.device.api.system.ControlledAccessException;
import net.rim.device.api.ui.ContextMenu;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.TouchEvent;
import net.rim.device.api.ui.Touchscreen;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.ListField;
import net.rim.device.api.ui.menu.DefaultContextMenuProvider;
import net.rim.device.api.util.MathUtilities;
import net.rim.device.api.util.SimpleSortingVector;
import RockManager.archive.ArchiveListField;
import RockManager.config.ConfigData;
import RockManager.favouritesList.FavouritesListField;
import RockManager.fileClipboard.FileClipboard;
import RockManager.fileHandler.FileHandler;
import RockManager.fileHandler.FileProperty;
import RockManager.fileList.filePicker.FilePicker;
import RockManager.fileList.position.FocusFinder;
import RockManager.fileList.position.PositionData;
import RockManager.fileList.position.PositionLogger;
import RockManager.fileList.searchBox.SearchBox;
import RockManager.languages.LangRes;
import RockManager.ui.ScreenHeightChangeEvent;
import RockManager.ui.oneLineInputField.InputField;
import RockManager.ui.screen.fileScreen.FileScreen;
import RockManager.util.OSVersionUtil;
import RockManager.util.UtilCommon;
import RockManager.util.ui.BaseObjectListField;
import RockManager.util.ui.VFMwithScrollbar;


public class FileListField extends BaseObjectListField implements ScreenHeightChangeEvent, FieldChangeListener {

	/**
	 * 当前目录路径，若listRoots时则为null.
	 */
	protected String folderPath;

	/**
	 * 当前目录的url形式（由FileConnection.getURL()获得）。
	 */
	protected String folderPathTotalURL;

	protected static int ROW_HEIGHT = 40;

	protected AddressBar addressBar = new AddressBar();

	/**
	 * 搜索框。
	 */
	private SearchBox searchBox = new SearchBox(this);

	private static String FILE_PROTOCOL = "file://";

	/**
	 * 存储高亮的那一行与顶部的距离。
	 */
	private PositionLogger positionLogger = new PositionLogger();

	/**
	 * 关键字输入框。
	 */
	private InputField keywordField = searchBox.getInputField();

	/**
	 * 原结果数据，用来筛选出搜索结果。
	 */
	private FileItem[] originData;

	/**
	 * 存储过滤出的结果。
	 */
	private FileItem[] resultData;

	/**
	 * 是否输入了关键字。
	 */
	private boolean keyWordEntered;

	/**
	 * 要搜索的关键字，将输入的内容以空格split的结果。
	 */
	private String[] keywords;

	/**
	 * 上一次搜索时输入的关键字，若本次输入的关键字是上一次关键字的扩充，则可利用上次的结果加快搜索速度。
	 */
	private String lastKeyword;

	/**
	 * 是否允许进行搜索。
	 */
	private boolean isSearchable = true;

	/**
	 * 文字字体。
	 */
	private Font textFont = getFont().derive(Font.PLAIN, 25);

	private FileJournalListener journalListener;

	private FileRootChangeListener rootChangeListener;

	/**
	 * 是否允许复制粘贴等剪贴板操作。
	 */
	private boolean clipboardAllowed;

	/**
	 * 文件选择器模式。
	 */
	private boolean filePickerMode = false;

	private FilePicker filePicker;


	protected FileListField() {

		setRowHeight(ROW_HEIGHT);
		setFont(textFont);

		setEmptyString("", DrawStyle.HCENTER);
		keywordField.setChangeListener(this);
		// 禁止pre-suffix搜索。
		super.setSearchable(false);

	}


	/**
	 * @param initialDir
	 *            初始路径，设置为null则listRoot().
	 */
	public FileListField(String initialDir) {

		this();
		setDirPath(initialDir);
	}


	/**
	 * 向系统注册FileJournalListener。
	 */
	public void registerJournalListener() {

		if (journalListener != null) {
			unRegisterJournalListener();
		}
		journalListener = new FileJournalListener(this);

	}


	/**
	 * 清除已注册的FileJournalListener。
	 */
	public void unRegisterJournalListener() {

		if (journalListener != null) {
			journalListener.unRegister();
		}

	}


	/**
	 * 向系统注册rootChangeListener.
	 */
	public void registerRootChangeListener() {

		if (rootChangeListener != null) {
			unRegisterRootChangeListener();
		}
		rootChangeListener = new FileRootChangeListener(this);

	}


	/**
	 * 清除已注册的rootChangeListener.
	 */
	public void unRegisterRootChangeListener() {

		if (rootChangeListener != null) {
			rootChangeListener.unRegister();
		}

	}


	/**
	 * 设置要转到的新地址。
	 * 
	 * @param newPath
	 */
	protected void setDirPath(String newPath) {

		String lastPath = folderPath;
		folderPath = newPath;

		int oldSize = getSize();

		FileItem[] files = listFiles(); // folderPath在此过程中可能改变。最终folderPath是path形式（由fconn.getPath()获得），而folderPathURL是url形式。
		if (files == null) {
			// 在列出文件的过程中遇到错误了。
			folderPath = lastPath;
			return;
		}

		setOriginData(files);
		set(files);

		// 设置该使哪行具有焦点状态。
		setFocusIndex(false, folderPath, lastPath, files);

		// 设置地址栏显示的地址。
		addressBar.setAddress(folderPath);

		if (journalListener != null) {
			journalListener.setFocusOnPath(folderPathTotalURL);
		}

		resetSearchData();

		checkIfEmpty();

		if (oldSize == getSize()) {
			// 手动重绘，使滚动条出现。
			screenHeightChangeNotify(SCREEN_HEIGHT_NOT_CHANGED);
		}

	}


	/**
	 * 检查此文件夹或压缩文件是否是空的，设置适当的emptyString.
	 */
	protected void checkIfEmpty() {

		if (originData.length == 0) {
			matchEmptyString();
		}

	}


	/**
	 * 搜索数据重置。
	 */
	protected void resetSearchData() {

		keyWordEntered = false;
		resultData = null;
		lastKeyword = "";
		keywords = null;
		setKeyword("");

	}


	/**
	 * 是否输入了关键字。
	 */
	public boolean isKeyWordEntered() {

		return keyWordEntered;
	}


	/**
	 * 获取输入的关键字列表。
	 * 
	 * @return 输入的关键字。可能是null.
	 */
	public String[] getKeywords() {

		return keywords;
	}


	/**
	 * 刷新。
	 */
	public void refresh() {

		logPosition(true);

		String keyword = getKeyword();
		setDirPath(folderPathTotalURL);
		setKeyword(keyword);

		restoreInnerPosition();

	}


	/**
	 * 设置原始数据，可在此数据内进行搜索。
	 * 
	 * @param files
	 */
	protected void setOriginData(FileItem[] files) {

		originData = files;
	}


	/**
	 * 设置该使哪行就有焦点状态。
	 * 
	 * @param isSameDir
	 *            是否是相同的文件夹（如：进行刷新等操作并没有改变文件夹路径），如果是，则newPath和lastPath不会用到。
	 * @param newPath
	 * @param lastPath
	 * @param list
	 */
	protected void setFocusIndex(boolean isSameDir, String newPath, String lastPath, FileItem[] files) {

		if (getManager() == null) {
			// 下面会用到manager, 所以判断是否为null. 为null的情况几乎不会发生。
			return;
		}

		if (!isSameDir && files.length <= 1) {
			return; // only one or nothing, no need to find.
		}

		boolean scrollable = getManager().isStyle(Manager.VERTICAL_SCROLL);

		// 滚动到最上方，以此作为基点，否则若新进入的列表的高度大于manager的高度，或在原文件夹滚动的距离大于要在新文件夹要滚动的距离时，需动态的滚回到顶部。
		if (scrollable) {
			// Manager是否可滚动，在OS 5上需判断，某则会造成错误。
			getManager().setVerticalScroll(0);
		}

		// 返回了上层文件夹。
		// 条件：1. isSameDir需为false 2. lastPath不为null 3.
		// newPath为null(返回了磁盘列出界面)或newPath是lastPath的上级.
		boolean backToParent = !isSameDir && lastPath != null
				&& (newPath == null || UtilCommon.getParentDir(lastPath).equals(newPath));

		int desiredIndex = -1;
		boolean hasLog = false;

		if (backToParent) { // 返回了上级文件夹

			String folderName = UtilCommon.getName(lastPath, false); // 要寻找的文件夹的名称。
			int desiredFileType; // 要寻找的类型
			int searchStart; // 从哪里开始寻找

			hasLog = positionLogger.hasLog(getFolderPath());

			if (hasLog) {

				desiredFileType = positionLogger.getFileType(getFolderPath());
				searchStart = positionLogger.getIndex(getFolderPath());

			} else {

				desiredFileType = FileItem.TYPE_UNKNOWN;
				searchStart = -1;

			}

			desiredIndex = FocusFinder.find(files, folderName, desiredFileType, searchStart);

		} else if (isSameDir) {

			hasLog = positionLogger.hasLog_inner();

			if (hasLog) {

				String lastFocusedName = positionLogger.getLastFocusedName_inner();
				int desiredFileType = positionLogger.getFileType_inner();
				int searchStart = positionLogger.getIndex_inner();

				desiredIndex = FocusFinder.find(files, lastFocusedName, desiredFileType, searchStart);

			}

		}

		if (desiredIndex >= 0) {

			int needScrollDistance = restorPositionNeedScroll(desiredIndex, isSameDir);

			if (needScrollDistance > 0) {
				getManager().setVerticalScroll(needScrollDistance);
			}

			// 设置焦点为返回时文件夹/原来的文件夹。
			setSelectedIndex(desiredIndex);

		}

		if (hasLog && backToParent) {
			positionLogger.removeLog(getFolderPath()); // 与当前目录关联的位置已使用，可以清理了。
		} else if (hasLog && isSameDir) {
			positionLogger.clearPositionData_inner();
		}

	}


	/**
	 * 还原上次记住的位置（文件夹内）。
	 */
	public void restoreInnerPosition() {

		FileItem[] files = keyWordEntered ? resultData : originData;

		setFocusIndex(true, null, null, files);

	}


	/**
	 * 返回需从最上向下滚动的距离。仅是这个list的manager只含有list的情况，也就是getTop()和距离manager底部距离为0的情况，
	 * 若manager在y轴上还包含其它组件，情况会复杂一些，暂未考虑。
	 * 
	 * @param index
	 *            要被focus的项的index。
	 * @param sameDir
	 *            是同层文件夹，还是返回了父文件夹。
	 * @return
	 */
	private int restorPositionNeedScroll(int index, boolean isSameDir) {

		if (getSize() * ROW_HEIGHT < getManager().getVisibleHeight()) {
			// 不会填满整个manager,无需滚动。
			return -1;
		}

		// 与Manager最上端的距离，未滚动
		int distanceInManager = ROW_HEIGHT * index;
		// 与Manager最上端的距离，已滚动
		int distanceWithManager;

		if (isSameDir) {
			distanceWithManager = positionLogger.getDistance_inner();
		} else {
			distanceWithManager = positionLogger.getDistance(getFolderPath());
		}

		int needScroll = 0;

		if (distanceWithManager >= 0) {
			// 若distanceWithManager小于0，说明不能取得数据，不指定需要滚动的距离。
			needScroll = distanceInManager - distanceWithManager;
		}

		int remains = getSize() * ROW_HEIGHT - needScroll;
		if (remains < getManager().getVisibleHeight()) {
			// 若采用此数据滚动后剩下的高度不足以填满整个manager，则减小需滚动的距离。
			// 如在搜索结果中进入了新文件夹，然后返回的情况，此时记录的距离可能是不准确的，故需重新验证。
			needScroll = getSize() * ROW_HEIGHT - getManager().getVisibleHeight();
		}

		// 可滚动最大距离，超过此距离上部无法完全显示。
		int scrollMax = index * ROW_HEIGHT;
		// 可滚动最大距离，小于此距离下部无法完全显示。
		int scrollMin = (index + 1) * ROW_HEIGHT - getManager().getVisibleHeight();

		needScroll = MathUtilities.clamp(scrollMin, needScroll, scrollMax);

		return needScroll;

	}


	/**
	 * 列出当前目录的所有文件。
	 */
	protected FileItem[] listFiles() {

		// 是否显示隐藏文件。
		boolean showHiddenFile = ConfigData.SHOW_HIDDEN_FILE.booleanValue();

		Enumeration allFiles = null; // 文件名（String）的集合，可能包含隐藏文件。
		Enumeration allFilesDetailInfo = null; // 文件信息（FileInfo）的集合，可能包含隐藏文件。由此获得的文件名不包括最后的'/'.
		Enumeration normalFiles = null; // 文件名（String）的集合，不包含隐藏文件。

		if (folderPath != null) {

			ExtendedFileConnection fconn = null;

			try {

				// now basePath is the new path, the folder of all listed files.
				fconn = (ExtendedFileConnection) Connector.open(folderPath);

				// 若分开，先判断是否存在，然后判断是否是folder时曾出现fconn.exists()==true且fconn.isDirectory()==false的情况(文件被另外的程序删除了)。
				// 所以应一步完成。

				if (fconn.isDirectory()) {

					folderPath = FILE_PROTOCOL + fconn.getPath() + fconn.getName();
					folderPathTotalURL = fconn.getURL();

					allFiles = fconn.list("*", showHiddenFile);
					allFilesDetailInfo = fconn.listWithDetails("*", showHiddenFile);

					if (showHiddenFile) {
						// 若不需要显示隐藏文件无需normalFiles这组数据就可完成任务。
						normalFiles = fconn.list();
					}

				} else {

					String targetPath = FILE_PROTOCOL + fconn.getPath() + fconn.getName();
					String message;

					if (fconn.exists() == false) {
						message = LangRes.get(LangRes.UNABLE_OPEN_FOLDER_NOT_EXIST);
					} else {
						message = LangRes.get(LangRes.UNABLE_OPEN_FOLDER_NOT_FOLDER);
					}

					message = UtilCommon.replaceString(message, "{1}", targetPath);

					UtilCommon.trace(message);

					throw new IOException(message);

				}

			} catch (Exception e) {

				// 出现异常，可能是文件夹已被删除或无效路径
				e.printStackTrace();
				return null;

			} finally {

				if (fconn != null) {
					try {
						fconn.close();
					} catch (Exception e) {
					}
				}

			}

		}

		SimpleSortingVector fileVector = new SimpleSortingVector();

		String fileNameFromAll = null;
		String fileNameFromNormal = null;
		FileItem thisItem = null;

		if (folderPath == null) {

			// 列出根目录，listRoots

			folderPathTotalURL = null;
			allFiles = FileSystemRegistry.listRoots();

			while (allFiles.hasMoreElements()) {
				thisItem = new FileItem(FILE_PROTOCOL + '/' + (String) allFiles.nextElement(), FileItem.TYPE_DISK);
				fileVector.addElement(thisItem);
			}

		} else {

			// 在正常文件夹列出的。
			while (allFiles.hasMoreElements()) {

				fileNameFromAll = (String) allFiles.nextElement();
				FileInfo thisFile = null;
				boolean isLocked = false;

				try {
					thisFile = (FileInfo) allFilesDetailInfo.nextElement();
				} catch (ControlledAccessException e) {
					// 不能获得这一项，也就是不能获得大小，最有可能的就是这个文件是lock了。
					isLocked = true;
				}

				if (showHiddenFile) {
					// 是否从normalFiles中读取下一项。
					if (fileNameFromNormal == null) {
						if (normalFiles.hasMoreElements()) {
							fileNameFromNormal = (String) normalFiles.nextElement();
						}
					}
				}

				thisItem = new FileItem(folderPath + fileNameFromAll);

				boolean isHidden = showHiddenFile && !fileNameFromAll.equals(fileNameFromNormal);
				boolean isFile = thisItem.isFile();

				if (isHidden) {
					// 是隐藏文件。
					// 不消除fromNormal，下次继续使用。
					thisItem.setDisplayAttribute_Hide(true);
				} else {
					// 不是隐藏文件。
					// 使fromNormal为null，以便使它可以前进，取得下个元素。
					fileNameFromNormal = null;
				}

				if (isFile && thisFile != null) {
					// 是文件，设置大小。
					long fileSize = thisFile.getFileSize();
					thisItem.setSize(fileSize);
				} else {
					// 不能获得大小，可能是locked.
					thisItem.setIsDRMForwardLocked(isLocked);
				}

				fileVector.addElement(thisItem);

			}

		}

		FileItem[] fileItems = arrangeFiles(fileVector);
		return fileItems;
	}


	/**
	 * 排序，添加返回项，进行整理。
	 * 
	 * @param fileVector
	 * @return
	 */
	protected FileItem[] arrangeFiles(SimpleSortingVector fileVector) {

		FileNameComparator comparator = new FileNameComparator();
		fileVector.setSortComparator(comparator);
		fileVector.reSort();

		// 根据设置是否应向菜单中添加返回项。
		boolean addReturnItem = ConfigData.ADD_RETURN_ITEM.booleanValue();

		if (addReturnItem && shouldHaveReturn()) {
			FileItem returnItem = new FileItem("...", FileItem.TYPE_RETURN);
			fileVector.insertElementAt(returnItem, 0);
		}

		FileItem[] files = new FileItem[fileVector.size()];
		fileVector.copyInto(files);
		return files;
	}


	/**
	 * 依据basePath判断是否应该添加返回项或返回上级目录还是关闭Screen。
	 */
	boolean shouldHaveReturn() {

		// test case
		//
		// String one = null; // false
		// String two = "file:///store/"; // false
		// String three = "file:///store/hello/"; // true
		// String four = ""; // false
		// String five = "hello/"; // true
		// String six = "hello\\"; // true;

		boolean shouldHaveReturn = false;

		if (folderPath != null && folderPath.length() > 0) {

			// "file:///store/hello/"
			if (folderPath.startsWith(FILE_PROTOCOL)) {

				if (filePickerMode) {
					shouldHaveReturn = true;
				} else {
					// "file:///store/hello/"

					int start = FILE_PROTOCOL.length() + 1;
					int end = folderPath.length() - 1;

					String toTest = folderPath.substring(start, end);

					if (toTest.indexOf('/') > 0) {
						shouldHaveReturn = true;
					}
				}

			} else {

				// 压缩文件内格式，"hello/"
				shouldHaveReturn = true;

			}
		}

		return shouldHaveReturn;
	}


	/**
	 * 获取AddressBar.
	 * 
	 * @return
	 */
	public AddressBar getAddressBar() {

		return addressBar;
	}


	/**
	 * 获取SearchBox, 搜索输入栏。
	 * 
	 * @return
	 */
	public SearchBox getSearchBox() {

		return searchBox;
	}


	protected boolean navigationClick(int status, int time) {

		if (isEmpty()) {
			// show nothing.
			return true;
		}

		boolean consumed = false;

		FileItem thisItem = getThisItem();

		switch (thisItem.getType()) {

			case FileItem.TYPE_RETURN:
				doReturnToParent();
				consumed = true;
				break;

			case FileItem.TYPE_DIR:
				doEnterThisDir();
				consumed = true;
				break;

			case FileItem.TYPE_DISK:
				doOpenThisDisk();
				consumed = true;
				break;

			case FileItem.TYPE_FILE:
				if (!filePickerMode) {
					doOpenThisFile();
					consumed = true;
					break;
				} else {
					return false;
				}

		}

		if (consumed) {
			popupMenuFix();
			return true;
		}

		return super.navigationClick(status, time);

	}


	protected boolean touchEvent(TouchEvent message) {

		// 6.0.0.534以下(只确定534无此问题而438有此问题)需修正unclick, unclick应返回true,
		// 否则navigationClick会调用两次。
		if (message.getEvent() == TouchEvent.UNCLICK) {
			if (OSVersionUtil.isOS6() && OSVersionUtil.getRevisionVersion() < 534) {
				return true;
			}
		}
		return super.touchEvent(message);

	}


	/**
	 * 修复某些os 6系统上popup screen的bug.
	 */
	protected void popupMenuFix() {

		if (Touchscreen.isSupported() && OSVersionUtil.isOS6()) {
			// 有此问题的版本: os 6且有触摸屏, 如：6.0.0.534, 6.0.0.570。
			// 在某些特定版本的9800上阻止错误的popup menu的出现。
			getScreen().setContextMenuProvider(new DefaultContextMenuProvider());
			// 还原, 使按住屏幕或按住触控板时可以出现popup menu.
			UiApplication.getUiApplication().invokeLater(new Runnable() {

				public void run() {

					getScreen().setContextMenuProvider(null);
				}
			});
		}

	}


	protected int moveFocus(int amount, int status, int time) {

		// 若切换了行则隐藏提示框。
		int originIndex = getSelectedIndex();
		int remains = super.moveFocus(amount, status, time);
		if (originIndex != getSelectedIndex()) {
			if (getManager() instanceof VFMwithScrollbar) {
				((VFMwithScrollbar) getManager()).hideTip();
			}
		}
		return remains;

	}


	protected void moveFocus(int x, int y, int status, int time) {

		// 若切换了行则隐藏提示框。。
		int originIndex = getSelectedIndex();
		super.moveFocus(x, y, status, time);
		if (originIndex != getSelectedIndex()) {
			if (getManager() instanceof VFMwithScrollbar) {
				((VFMwithScrollbar) getManager()).hideTip();
			}
		}

	}


	public boolean keyChar(char key, int status, int time) {

		boolean consumed = FileListKeyCharHandler.keyChar(key, status, time, this);

		if (consumed) {
			return true;
		} else {
			return super.keyChar(key, status, time);
		}

	}


	/**
	 * 显示属性。
	 */
	void showProperty() {

		FileProperty.showPropertyScreen(this, getThisItem());

	}


	/**
	 * 重命名文件。
	 */
	void renameFile() {

		FileHandler.renameWithUI(getThisItem(), this);

	}


	/**
	 * 删除文件。
	 */
	public void deleteFile() {

		FileHandler.deleteWithUI(getThisItem());

	}


	/**
	 * 将当前高亮项复制到剪贴板。
	 */
	void copyToClipboard() {

		FileClipboard.put(FileClipboard.METHOD_COPY, getThisItem());

	}


	/**
	 * 将当前高亮项剪切到剪贴板。
	 */
	void cutToClipboard() {

		FileClipboard.put(FileClipboard.METHOD_CUT, getThisItem());

	}


	/**
	 * 从剪贴板粘贴文件到当前文件夹。
	 */
	void pasteFromClipboard() {

		FileClipboard.pasteWithUI(this);

	}


	/**
	 * 选中的项项是否是真实的文件项。
	 * 
	 * @return
	 */
	boolean isRealFileItem() {

		FileItem thisItem = getThisItem();
		boolean isRealFileItem = (thisItem != null && thisItem.isRealFile());
		return isRealFileItem;

	}


	/**
	 * 设置要搜索的关键字。
	 * 
	 * @param keyword
	 */
	public void setKeyword(String keyword) {

		String originKeyword = getKeyword();
		if (keyword.equals(originKeyword) == false) {
			keywordField.setText(keyword);
		}
	}


	/**
	 * 在keywordField中的label，搜索提示文字。
	 * 
	 * @param newLabel
	 */
	public void setLabel(String newLabel) {

		searchBox.setLabelText(newLabel);
	}


	/**
	 * 设置是否允许进行搜索。
	 */
	public void setSearchable(boolean searchable) {

		keywordField.setEditable(searchable);
		isSearchable = searchable;
	}


	/**
	 * 是否允许进行搜索。
	 */
	public boolean isSearchable() {

		return isSearchable;
	}


	/**
	 * 获取当前正处于selected状态的行的内容, 若此FileListField为空的，则返回null。
	 * 
	 * @return
	 */
	public FileItem getThisItem() {

		if (isEmpty()) {
			return null;
		} else {
			return (FileItem) get(this, getSelectedIndex());
		}

	}


	/**
	 * 在一个新的Screen打开这个disk.
	 */
	private void doOpenThisDisk() {

		if (filePickerMode) {
			doEnterThisDir();
			return;
		}

		FileItem thisItem = getThisItem();

		String path = thisItem.getRawPath();
		FileScreen fileScreen = new FileScreen(path);
		// 设置地址栏图标为disk图标。
		fileScreen.setIcon(thisItem.getIcon());
		UiApplication.getUiApplication().pushScreen(fileScreen);
	}


	/**
	 * 进入一个子目录。
	 */
	protected void doEnterThisDir() {

		FileItem thisItem = getThisItem();

		logPosition(false);
		// 更新地址栏显示的图标。这要在设置新地址之前完成，因为要取得的图标是要列出的文件的父目录的图标。
		addressBar.setIcon(thisItem.getIcon());
		String newPath = thisItem.getURL();
		setDirPath(newPath);
	}


	/**
	 * 打开这个文件。
	 */
	protected void doOpenThisFile() {

		FileHandler.openFile(getThisItem());
	}


	/**
	 * 记录当前高亮的项目的顶端与Manager的顶端(滚动后)的距离。
	 * 
	 * @param sameDir
	 *            是否是同一层目录，若是，则是因为输入关键字而记录位置，若否，则是因进入子文件夹而记录位置。
	 */
	protected void logPosition(boolean isSameDir) {

		if (isEmpty()) {
			// 此时没必要记录位置，且因为getThisItem()为null也不应记录位置。
			return;
		}

		int distanceWithManager = -1;
		Manager manager = getManager();
		if (manager != null) {
			// 距离Manager最顶端的距离，未滚动
			int distanceInManager = ROW_HEIGHT * getSelectedIndex();
			// 距离Manager最顶端的距离，已滚动
			distanceWithManager = distanceInManager - manager.getVerticalScroll();
		}

		FileItem thisItem = getThisItem();

		if (isSameDir) {
			positionLogger.logDistance_inner(distanceWithManager);
			positionLogger.logIndex_inner(getSelectedIndex());
			positionLogger.logFocusedName_inner(thisItem.getDisplayName());
			positionLogger.logFileType_inner(thisItem.getType());
		} else {
			// 存储此值，而不是Manager滚动的距离，这样即使添加或删除了项目也能保证位置不变。
			PositionData positionData = new PositionData();
			positionData.setDistance(distanceWithManager);
			positionData.setIndex(getSelectedIndex());
			positionData.setFileType(thisItem.getType());
			positionLogger.log(getFolderPath(), positionData);
		}

	}


	/**
	 * 返回上级目录。
	 */
	protected void doReturnToParent() {

		String newPath = UtilCommon.getParentDir(folderPath);

		// 若是"file:///SDCard/"经过处理会变成"file:///"，从而可判断是否是根目录。
		if (newPath.equals(FILE_PROTOCOL + '/')) {
			newPath = null;
		} else {
			// 通常情况下不用设置地址栏的图标，但在返回磁盘根目录时(如："file:///SDCard/")需设置为"磁盘"图标。
			int sepPosition = newPath.lastIndexOf('/', newPath.length() - 2);
			if (sepPosition == FILE_PROTOCOL.lastIndexOf('/') + 1) {
				addressBar.setIcon(FileItem.getDiskIcon());
			}
		}
		setDirPath(newPath);

	}


	public void screenHeightChangeNotify(int context) {

		fieldChangeNotify(context);
	}


	/**
	 * 对每行进行绘制。
	 */
	public void drawListRow(ListField listField, Graphics g, int index, int y, int width) {

		// 绘制背景
		super.drawListRow(listField, g, index, y, width);

		FileListDrawer.drawListRow(this, g, index, y, width, filePickerMode);

	}


	/**
	 * 获取搜索框输入的关键字。
	 * 
	 * @return
	 */
	public String getKeyword() {

		return keywordField.getText();
	}


	/**
	 * 获取keywordField.
	 * 
	 * @return
	 */
	public InputField getKeywordField() {

		return keywordField;
	}


	protected void paint(Graphics g) {

		if (isEmpty()) {
			FileListDrawer.paintEmptyString(this, g);
		} else {
			super.paint(g);
		}

	}


	/**
	 * 重载此方法以完成搜索功能。
	 */
	public void fieldChanged(Field field, int context) {

		if (field != keywordField || originData == null) {
			// 与搜索功能无关或还不能搜索，返回。
			return;
		}

		String keyword = getKeyword().trim().toLowerCase();

		if (keyword.equals(lastKeyword)) {
			// 可能只是增减了空格个数，对结果无影响，返回。
			return;
		}

		if (keyword.length() == 0) {

			boolean lastTimeKeyWordEntered = keyWordEntered;
			keyWordEntered = false;

			if (lastTimeKeyWordEntered == true) {
				// 现在无关键字，之前的状态是输入了关键字，重绘，还原位置。
				set(originData);
				resultData = null;
				restoreInnerPosition();
			}

			if (originData.length == 0) {
				// 没有数据，设置为空时的提示文字。
				matchEmptyString();
			}

			lastKeyword = keyword;

			return;

		} else {
			// 输入了关键字。
			if (keyWordEntered == false) {
				// 第一次输入关键字，记录位置。
				logPosition(true);
			}
			keyWordEntered = true;
			keywords = UtilCommon.splitString(keyword, " ");
		}

		Vector result = new Vector();
		// 是否是上次输入内容的增量形式，如'a'->'ab'
		boolean increased = lastKeyword != null && lastKeyword.length() > 0 && resultData != null
				&& keyword.startsWith(lastKeyword);
		FileItem[] sourceData = increased ? resultData : originData;

		for (int i = 0; i < sourceData.length; i++) {
			FileItem thisItem = sourceData[i];
			if (thisItem.isReturn()) {
				// 返回项不计算内
				continue;
			}

			String name = thisItem.getDisplayName().toLowerCase();
			boolean haveAllKeywords = true;
			for (int j = 0; j < keywords.length; j++) {
				if (name.indexOf(keywords[j]) < 0) {
					// 此条目不包含这个关键字。
					haveAllKeywords = false;
					break;
				}
			}

			if (haveAllKeywords) {
				// 此条目符合关键字匹配
				result.addElement(thisItem);
			}
		}

		resultData = new FileItem[result.size()];
		result.copyInto(resultData);
		set(resultData);

		if (resultData.length == 0) {
			// 搜索结果为空
			matchEmptyString();
		}

		lastKeyword = keyword;

	}


	/**
	 * 根据当前状态设置结果为空时显示的emptyString.
	 */
	private void matchEmptyString() {

		if (keyWordEntered) {
			setEmptyString(LangRes.get(LangRes.SEARCH_RESULT_EMPTY), DrawStyle.HCENTER);
		} else {
			setEmptyString(getNoFileFindString(), DrawStyle.HCENTER);
		}
	}


	/**
	 * 返回当文件夹/根目录/压缩文件为空时的emptyString.
	 * 
	 * @return
	 */
	protected String getNoFileFindString() {

		return LangRes.get(LangRes.FOLDER_IS_EMPTY);
	}


	public boolean isFocusable() {

		if (isEmpty() && getKeyword().length() > 0) {
			return false;
		} else {
			return true;
		}

	}


	/**
	 * 获取文件夹路径。
	 */
	public String getFolderPath() {

		return folderPath;
	}


	/**
	 * 获取文件夹 地址的URL形式(由UtilCommon.toURLForm()获得)。
	 */
	public String getFolderPathURL() {

		return UtilCommon.toURLForm(folderPath);
	}


	/**
	 * 获取文件夹 地址的完全URL形式(由Fconn.getURL()获得)。
	 */
	public String getFolderPathTotalURL() {

		return folderPathTotalURL;
	}


	/**
	 * 清除记录的位置数据（不包括innerPosition的数据）。
	 */
	public void clearPositionData() {

		positionLogger.clearPositionData();
	}


	/**
	 * 重写此方法，让适当的短菜单出现。(这似乎是一个隐藏的系统方法)。
	 * 
	 * @param instance
	 * @return
	 */
	public ContextMenu getContextMenu(int instance) {

		if (instance == 0) {
			return getContextMenu();
		} else {
			ContextMenu contextMenu = ContextMenu.getInstance();
			contextMenu.setTarget(this);
			makeContextMenu(contextMenu);
			return contextMenu;
		}

	}


	protected void makeContextMenu(ContextMenu contextMenu) {

		if (isEmpty()) {
			super.makeContextMenu(contextMenu);
		} else {
			FileListContextMenuHandler.makeContextMenu(contextMenu, this);
		}

	}


	/**
	 * 设置是否允许剪贴板操作。
	 */
	public void setClipboardAllowed(boolean value) {

		clipboardAllowed = value;
	}


	/**
	 * 是否允许剪贴板操作。
	 */
	public boolean isClipboardAllowed() {

		return clipboardAllowed;
	}


	/**
	 * 使具有特定文件名的项在下次refreash时获得焦点。优先级高于logPosition(true)中记录文件名部分。
	 * 可用于重命名文件后使那一项获得焦点。
	 */
	public void setItemToFocus(String fileName, int fileType) {

		positionLogger.logFocusedName_inner_highPriority(fileName);
		positionLogger.logFileType_inner_highPriority(fileType);
	}


	/**
	 * 是否应向菜单中添加"属性"项，或按快捷键后是否应显示属性。
	 * 
	 * @return
	 */
	boolean shouldShowProperty() {

		if (filePickerMode && OSVersionUtil.isOS5()) {
			// 在os
			// 5中在filePickerMode若是显示"属性窗口"后，若关闭"属性窗口"，FilePicker会不可见，需移动才可见，系统bug。
			return false;
		}
		FileItem thisFileItem = getThisItem();
		return thisFileItem != null && thisFileItem.isReturn() == false;
	}


	/**
	 * 是否是普通的文件夹列表。
	 */
	public boolean isNormalFolder() {

		return !isArchiveList() && !isFavouriteList() && folderPath != null;
	}


	/**
	 * 是否是列出了各个磁盘的列表。
	 */
	public boolean isDiskList() {

		return !isArchiveList() && !isFavouriteList() && folderPath == null;
	}


	/**
	 * 是否是压缩文件的列表。
	 */
	public boolean isArchiveList() {

		return this instanceof ArchiveListField;
	}


	/**
	 * 是否是收藏夹列表。
	 */
	public boolean isFavouriteList() {

		return this instanceof FavouritesListField;
	}


	/**
	 * 返回当前状态下的全部文件(不包括返回项)。
	 * 
	 * @return
	 */
	public FileItem[] getAllFiles() {

		FileItem[] results;

		if (keyWordEntered) {
			results = resultData;
		} else {
			results = originData;
		}

		if (results.length >= 1 && results[0].isReturn()) {

			FileItem[] origin = results;
			results = new FileItem[origin.length - 1];

			System.arraycopy(origin, 1, results, 0, results.length);

		}

		return results;

	}


	/**
	 * 将状态置为文件选择器模式。
	 */
	public void activePickerMode() {

		filePickerMode = true;

	}


	/**
	 * 是否是文件选择器模式。
	 * 
	 * @return
	 */
	public boolean isPickerMode() {

		return filePickerMode;
	}


	/**
	 * 设置filePicker.
	 */
	public void setFilePicker(FilePicker filePicker) {

		this.filePicker = filePicker;
	}


	/**
	 * 选中当前目录。
	 */
	public void pickFolder() {

		if (filePicker != null) {
			filePicker.select(getFolderPath());
		}
	}


	/**
	 * 选中焦点所在文件/目录。
	 */
	public void pickFile() {

		if (filePicker != null) {
			String filePath = getThisItem().getPath();
			filePicker.select(filePath);
		}
	}

}
