
package RockManager.archive;

import java.util.Enumeration;
import java.util.Hashtable;
import de.innosystec.unrar.rarfile.FileHeader;
import net.sf.zipme.ZipEntry;


public class ArchiveEntry {

	public static final int TYPE_ZIP = 0;

	public static final int TYPE_RAR = 1;

	/**
	 * 用于ZIPME的路径分隔符形式, '/'.
	 */
	public static final String ZIP_SEPARATOR = "/";

	/**
	 * 用于UNRAR的路径分隔符形式, '\'.
	 */
	public static final String RAR_SEPARATOR = "\\";

	/**
	 * 文件完整文件名。若包括分隔符则说明这是个文件夹。
	 */
	private String name;

	/**
	 * 它的子文件（若它本身是文件为null,若本身是文件夹不为null）
	 */
	private Hashtable files;

	/**
	 * 压缩文件类型，rar或zip.
	 */
	private int type;

	/**
	 * 分割符形式。
	 */
	private String separator;

	/**
	 * 它的父文件夹。
	 */
	private ArchiveEntry parentEntry;

	/**
	 * 未压缩时文件大小。
	 */
	private long fileSize = -1;

	/**
	 * 压缩后文件大小或文件夹大小。
	 */
	private long packedSize = -1;

	/**
	 * 与之关联的原始类型压缩文件记录项，应为FileHeader(rar中)或ZipEntry(zip中).
	 */
	private Object originDataEntry;


	public ArchiveEntry(int type) {

		this(null, type);
	}


	public ArchiveEntry(String name, int type) {

		this.name = name;
		setType(type);
	}


	public void setType(int type) {

		this.type = type;
		if (type == TYPE_ZIP) {
			separator = ZIP_SEPARATOR;
		} else {
			separator = RAR_SEPARATOR;
		}
	}


	/**
	 * 返回文件类型，TYPE_ZIP或TYPE_RAR.
	 * 
	 * @return
	 */
	public int getType() {

		return type;
	}


	/**
	 * 设置文件类型entry的大小。
	 * 
	 * @param size
	 */
	public void setFileSize(long size) {

		fileSize = size;
	}


	/**
	 * 获取文件或文件夹的未压缩时大小。
	 * 
	 * @return
	 */
	public long getFileSize() {

		if (fileSize < 0) {
			readFileSize();
		}
		return fileSize;

	}


	private void readFileSize() {

		long size = 0;

		if (isDir()) {
			ArchiveEntry[] entries = getFiles();
			for (int i = 0; i < entries.length; i++) {
				size += entries[i].getFileSize();
			}
		} else { // 是文件。这不会发生，因为若是文件的话在初始化时就已经获得大小了。
		}

		fileSize = size;

	}


	public void setParentEntry(ArchiveEntry entry) {

		parentEntry = entry;
	}


	public ArchiveEntry getParentEntry() {

		return parentEntry;
	}


	/**
	 * 向此entry中添加文件，最终文件会添加到合适的位置。
	 * 
	 * @param name
	 * @return 新添加的entry, 如要添加的项是目录项，且此项已经存在了，则返回已有的此项.
	 */
	public ArchiveEntry addFile(String name) {

		if (files == null) {
			files = new Hashtable();
		}
		int position = subDirPosition(name);
		if (position > 0) {
			String subDirName = name.substring(0, position);
			ArchiveEntry subDirEntry = getEntry(subDirName);
			return subDirEntry.addFile(name.substring(position));
		} else {
			// 添加一个文件项，可能是文件或目录。
			if (isDir(name) && files.containsKey(name)) {
				// 是目录且已存在。
				ArchiveEntry entry = (ArchiveEntry) files.get(name);
				return entry;
			} else {
				// 是文件则一定不存在，或是目录且不存在。
				ArchiveEntry entry = new ArchiveEntry(name, type);
				entry.setParentEntry(this);
				// 最终所有的文件类型的entry都是由此添加的。
				files.put(name, entry);
				return entry;
			}
		}
	}


	/**
	 * 获取目录类型entry的。若不存在则创建一个, 加入files, 并返回它。
	 * 
	 * @return
	 */
	public ArchiveEntry getEntry(String key) {

		ArchiveEntry entry;
		if (files.containsKey(key)) {
			entry = (ArchiveEntry) files.get(key);
		} else {
			entry = new ArchiveEntry(key, type);
			entry.setParentEntry(this);
			// 添加一目录项
			files.put(key, entry);
		}
		return entry;
	}


	public boolean hasFiles() {

		return files != null && files.size() > 0;
	}


	/**
	 * 获取当前entry下的所有文件。
	 * 
	 * @return
	 */
	public ArchiveEntry[] getFiles() {

		ArchiveEntry[] elements = null;
		if (files != null) {
			int size = files.size();
			elements = new ArchiveEntry[size];
			int index = 0;
			Enumeration fileEnum = files.elements();
			while (fileEnum.hasMoreElements()) {
				elements[index] = (ArchiveEntry) fileEnum.nextElement();
				index++;
			}
		}
		if (elements == null) {
			elements = new ArchiveEntry[0];
		}
		return elements;
	}


	/**
	 * 获取当前文件夹下一个子文件夹内的内容。不用加路径分隔符。
	 * 
	 * @param dirName
	 * @return
	 */
	public ArchiveEntry[] getFiles(String dirName) {

		ArchiveEntry[] elements = null;
		if (files != null) {
			ArchiveEntry file = (ArchiveEntry) files.get(dirName + separator);
			if (file != null) {
				elements = file.getFiles();
			}
		}
		if (elements == null) {
			elements = new ArchiveEntry[0];
		}
		return elements;
	}


	/**
	 * @return 文件完整文件名。若包括分隔符则说明这是个文件夹。
	 */
	public String getName() {

		if (name == null) {
			return "";
		} else {
			return name;
		}
	}


	public String getPath() {

		String path;

		if (originDataEntry instanceof ZipEntry) {
			path = ((ZipEntry) originDataEntry).getName();
		} else if (originDataEntry instanceof FileHeader) {
			FileHeader fileHeader = (FileHeader) originDataEntry;
			path = fileHeader.isUnicode() ? fileHeader.getFileNameW() : fileHeader.getFileNameString();
			if (fileHeader.isDirectory()) {
				path += separator;
			}
		} else {
			// originDataEntry可能为null, 此时为rootEntry.
			path = "";
		}

		return path;

	}


	/**
	 * 指定的名称是否是一个目录项。
	 * 
	 * @param name
	 * @return
	 */
	private boolean isDir(String name) {

		return name.endsWith(separator);
	}


	/**
	 * 当前项是否是目录项。
	 * 
	 * @return
	 */
	public boolean isDir() {

		return name.endsWith(separator);
	}


	public boolean isZipEntry() {

		return type == TYPE_ZIP;
	}


	public boolean isRarEntry() {

		return type == TYPE_RAR;
	}


	/**
	 * 若有第二层目录，返回Separator('/'或'\')的位置，否则返回-1.
	 * 如：Hello/World/m.mp3返回后的subString(0,position)为"Hello"
	 * 
	 * @param name
	 * @return
	 */
	private int subDirPosition(String name) {

		int index = name.indexOf(separator);
		if (index > 0 && index != name.length() - 1) {
			return index + 1;
		} else {
			return -1;
		}
	}


	/**
	 * 返回此种类型的压缩文件的路径分隔符。
	 * 
	 * @return
	 */
	public String getSeparator() {

		return separator;
	}


	/**
	 * 设置与之关联的原始类型压缩文件记录项，应为FileHeader(rar中)或ZipEntry(zip中)。
	 */
	public void setOriginDataEntry(Object originEntry) {

		originDataEntry = originEntry;

	}


	/**
	 * 获取与之关联的原始类型压缩文件记录项。
	 * 
	 * @return 类型为FileHeader(rar中)或ZipEntry(zip中)。
	 */
	public Object getOriginDataEntry() {

		return originDataEntry;

	}


	/**
	 * 此项压缩后的大小。若是文件夹，则是文件夹压缩后的大小。
	 */
	public long getPackedSize() {

		if (packedSize < 0) {
			readPackedSize();
		}

		return packedSize;

	}


	private void readPackedSize() {

		long size = 0;

		if (isDir()) { // folder
			ArchiveEntry[] entries = getFiles();
			for (int i = 0; i < entries.length; i++) {
				size += entries[i].getPackedSize();
			}
		} else { // file
			if (isZipEntry()) { // zip
				ZipEntry entry = (ZipEntry) originDataEntry;
				size = entry.getCompressedSize();
			} else { // rar
				FileHeader header = (FileHeader) originDataEntry;
				size = header.getPackSize();
			}
		}

		packedSize = size;

	}

}
