
package RockManager.fileList;

import java.io.IOException;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.component.ListField;
import RockManager.archive.ArchiveEntry;
import RockManager.fileHandler.FileSizeString;
import RockManager.languages.LangRes;
import RockManager.util.IOUtil;
import RockManager.util.UtilCommon;


public class FileItem {

	/**
	 * 类型：未知
	 */
	public static final int TYPE_UNKNOWN = -1;

	/**
	 * 类型：文件夹
	 */
	public static final int TYPE_DIR = 0;

	/**
	 * 类型：文件
	 */
	public static final int TYPE_FILE = 1;

	/**
	 * 类型：返回指示项
	 */
	public static final int TYPE_RETURN = 2;

	/**
	 * 类型：根：磁盘/分区/SD卡等
	 */
	public static final int TYPE_DISK = 3;

	/**
	 * 类型：压缩文件中的文件夹
	 */
	public static final int TYPE_DIR_IN_ARCHIVE = 4;

	/**
	 * 类型：压缩文件中的文件
	 */
	public static final int TYPE_FILE_IN_ARCHIVE = 5;

	private static IconSet ICON_SET = new IconSet();

	private int fileType;

	/**
	 * 文件的路径。
	 */
	private String path;

	private boolean isHidden = false;

	private boolean isDRMLocked = false;

	private boolean isEncrypted = false;

	/**
	 * 文件大小的字符串表示，如"10,120 KB"。
	 */
	private String fileSizeString;

	/**
	 * 文件大小，对于磁盘则是可用空间。
	 */
	private long fileSize = -1;

	private ArchiveEntry originArchiveEntry;


	public FileItem(String path, int fileType) {

		this.fileType = fileType;
		this.path = path;

		if (fileType == TYPE_FILE) {
			if (UtilCommon.getSuffix(path).equals("rem")) {
				// 根据文件名判断是否是加密文件。
				setIsEncrypted(true);
			}
		}

	}


	/**
	 * 新建一个FileItem, 自动判断是文件还是目录。只适用于正常文件。 磁盘类型、压缩文件类型请明确指定类型。
	 * 
	 * @param path
	 */
	public FileItem(String path) {

		this(path, UtilCommon.isFolder(path) ? TYPE_DIR : TYPE_FILE);

	}


	/**
	 * 设置文件属性为隐藏，由于是根据已存在的文件创建，无需修改文件属性。
	 */
	public void setDisplayAttribute_Hide(boolean isHidden) {

		this.isHidden = isHidden;
	}


	/**
	 * 设置文件是否是DRMForwardLocked.
	 * 
	 * @param value
	 */
	public void setIsDRMForwardLocked(boolean value) {

		isDRMLocked = value;
	}


	/**
	 * 设置文件是否是加密了的。
	 */
	public void setIsEncrypted(boolean value) {

		isEncrypted = value;
	}


	/**
	 * 返回文件大小。若不能获得文件大小，返回-1。对于disk类型, 返回可用空间。对于压缩文件中的文件，返回未压缩时大小。
	 * 
	 * @return
	 */
	public long getFileSize() {

		return fileSize;
	}


	/**
	 * 获取文件大小的字符串形式，外部调用。
	 * 
	 * @return
	 */
	public String getSizeString() {

		if (fileSizeString == null && fileSize >= 0) {
			fileSizeString = getSizeString(fileSize);
		}

		return fileSizeString;
	}


	/**
	 * 获取文件大小的字符串形式，外部调用。若不能获得则等到获得时主动重绘ListField.
	 * 
	 * @param list
	 * @param index
	 * @return
	 */
	public String getSizeString(final ListField list, final int index) {

		if (fileSizeString == null) {

			if (fileSize >= 0) {

				fileSizeString = getSizeString(fileSize);

			} else {

				new Thread() {

					public void run() {

						readFileSize(list, index);
					}
				}.start();

			}

		}

		return fileSizeString;

	}


	/**
	 * 读取文件大小，若是磁盘，则读取可用空间。<br>
	 * 其它类型可直接获得文件大小，因此主要是磁盘类型使用此方法。
	 * 
	 * @param list
	 * @param index
	 */
	private synchronized void readFileSize(ListField list, int index) {

		if (fileSizeString != null) {
			// 可能已由另一线程获得，返回。
			return;
		}

		FileConnection fconn = null;

		try {
			fconn = (FileConnection) Connector.open(path);
			fileSize = isDisk() ? fconn.availableSize() : fconn.fileSize();
			String sizeString = getSizeString(fileSize);

			if (isDisk()) {
				sizeString += LangRes.get(LangRes.DISK_AVAILABLE_SIZE);
			}

			fileSizeString = sizeString;

			if (list.isVisible()) {
				list.invalidate(index);
			}

		} catch (IOException e) {
		} finally {
			if (fconn != null) {
				try {
					fconn.close();
				} catch (IOException e) {
				}
			}
		}

	}


	/**
	 * 将文件大小(long)转换为对应的字符串形式。例如 "123 KB"
	 * 
	 * @param fileSize
	 * @return
	 */
	private String getSizeString(long fileSize) {

		boolean KBonly = true;
		boolean showDot = false;

		if (isDisk()) {
			KBonly = false;
			showDot = true;
		}

		return FileSizeString.getSizeString(fileSize, KBonly, showDot);
	}


	/**
	 * 设置文件大小, 外部调用。
	 * 
	 * @param size
	 */
	public void setSize(long size) {

		fileSize = size;

	}


	/**
	 * 获取显示样式名称。
	 * 
	 * @return
	 */
	public String getDisplayName() {

		if (path.length() == 0) {
			return path;
		}

		String displayName = null;
		switch (fileType) {
			case TYPE_FILE:
			case TYPE_DIR:
			case TYPE_DISK:
				displayName = UtilCommon.getName(path, true);
				break;
			case TYPE_DIR_IN_ARCHIVE:
				displayName = path.substring(0, path.length() - 1);
				break;
			case TYPE_RETURN:
			case TYPE_FILE_IN_ARCHIVE:
				displayName = path;
				break;
		}
		return displayName;
	}


	/**
	 * 根据当前FileItem的类型返回对应的图标。
	 * 
	 * @return
	 */
	public Bitmap getIcon() {

		Bitmap icon = null;

		switch (fileType) {

			case TYPE_RETURN:
				icon = getIcon("S.return");
				break;

			case TYPE_DIR:
			case TYPE_DIR_IN_ARCHIVE:
				icon = getIcon("S.folder");
				break;

			case TYPE_DISK:
				icon = getIcon("S.disk");
				break;

			case TYPE_FILE:
			case TYPE_FILE_IN_ARCHIVE:
				icon = getIcon(getSuffix());
				break;

		}
		return icon;
	}


	/**
	 * 返回指定扩展名的图标，或文件夹图标等，或不存在此图标返回默认图标。
	 * 
	 * @param type
	 * @return
	 */
	private Bitmap getIcon(String type) {

		return ICON_SET.getIcon(type);
	}


	/**
	 * 返回"磁盘"图标。
	 * 
	 * @return
	 */
	public static Bitmap getDiskIcon() {

		return ICON_SET.getIcon("S.disk");
	}


	/**
	 * 返回“小锁”图标。
	 * 
	 * @return
	 */
	public static Bitmap getLockIcon() {

		return ICON_SET.getIcon("S.lock");
	}


	/**
	 * 返回文件类型。
	 * 
	 * @return
	 */
	public int getType() {

		return fileType;
	}


	/**
	 * 是否是文件夹。
	 * 
	 * @return
	 */
	public boolean isDir() {

		return fileType == TYPE_DIR || fileType == TYPE_DIR_IN_ARCHIVE;
	}


	/**
	 * 是否是磁盘/根目录类型（root）。
	 * 
	 * @return
	 */
	public boolean isDisk() {

		return fileType == TYPE_DISK;
	}


	/**
	 * 是否是文件。
	 * 
	 * @return
	 */
	public boolean isFile() {

		return fileType == TYPE_FILE || fileType == TYPE_FILE_IN_ARCHIVE;
	}


	/**
	 * 是否是实际存在的文件或文件夹，即磁盘上的，非压缩文件中的。
	 * 
	 * @return
	 */
	public boolean isRealFile() {

		return fileType == TYPE_FILE || fileType == TYPE_DIR;
	}


	/**
	 * 是否是压缩文件中的文件或文件夹。
	 * 
	 * @return
	 */
	public boolean isFileInArchive() {

		return fileType == TYPE_FILE_IN_ARCHIVE || fileType == TYPE_DIR_IN_ARCHIVE;
	}


	/**
	 * 是否是返回项。
	 * 
	 * @return
	 */
	public boolean isReturn() {

		return fileType == TYPE_RETURN;
	}


	/**
	 * 是否是隐藏文件。
	 * 
	 * @return
	 */
	public boolean isHidden() {

		return isHidden;
	}


	/**
	 * 是否是DRM locked.
	 * 
	 * @return
	 */
	public boolean isDRMLocked() {

		return isDRMLocked;
	}


	/**
	 * 是否是加密的(文件名以".rem"结束)。
	 * 
	 * @return
	 */
	public boolean isEncrypted() {

		return isEncrypted;
	}


	/**
	 * 获得文件名。
	 * 
	 * @param withSuffix
	 *            是否要后缀。
	 * @return
	 */
	public String getName(boolean withSuffix) {

		return UtilCommon.getName(getPath(), false);
	}


	/**
	 * 返回此项的扩展名(若是文件的话)，若是加密的，则返回原始的扩展名。例如：A.jpg.rem将返回jpg。
	 * 
	 * @return
	 */
	public String getSuffix() {

		return UtilCommon.getSuffix(getPath());
	}


	/**
	 * 返回此项的扩展名的原始形式(若是文件的话)，若是加密的，则返回原始的扩展名。例如：A.JPG.rem将返回JPG。
	 * 
	 * @return
	 */
	public String getOriginSuffix() {

		return UtilCommon.getOriginSuffix(getPath());
	}


	/**
	 * 按"原样"返回扩展名，例如：A.jpg.rem将返回rem。
	 * 
	 * @return
	 */
	public String getRawSuffix() {

		return UtilCommon.getSuffix(path);
	}


	/**
	 * 获取文件绝对路径(若是以".rem"结束不包括最后的".rem")。
	 * 
	 * @return
	 */
	public String getPath() {

		if (isEncrypted) {
			return path.substring(0, path.lastIndexOf('.'));
		} else {
			return path;
		}

	}


	/**
	 * 获取文件绝对路径(若是以".rem"结束也包括最后的".rem")。
	 * 
	 * @return
	 */
	public String getRawPath() {

		return path;
	}


	/**
	 * 获取文件路径的URL编码形式(若是以".rem"结束不包括最后的".rem")。
	 * 
	 * @return
	 */
	public String getURL() {

		return UtilCommon.toURLForm(getPath());
	}


	/**
	 * 获取文件路径的URL编码形式(若是以".rem"结束也包括最后的".rem")。
	 * 
	 * @return
	 */
	public String getRawURL() {

		return UtilCommon.toURLForm(path);
	}


	/**
	 * 返回要显示的文件名。
	 */
	public String toString() {

		return getDisplayName();
	}


	/**
	 * 如果是压缩文件类的项，设置原始的与之关联的ArchiveEntry。
	 * 
	 * @param archiveEntry
	 */
	public void setOriginArchiveEntry(ArchiveEntry archiveEntry) {

		originArchiveEntry = archiveEntry;
	}


	/**
	 * 如果是压缩文件类的项，获取原始的与之关联的ArchiveEntry。
	 * 
	 * @return
	 */
	public ArchiveEntry getOriginArchiveEntry() {

		return originArchiveEntry;
	}


	/**
	 * 此项是否还存在。
	 * 
	 * @return
	 */
	public boolean exists() {

		return IOUtil.isExists(getURL());
	}

}
