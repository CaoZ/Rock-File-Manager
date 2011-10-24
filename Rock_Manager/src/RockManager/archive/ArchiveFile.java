
package RockManager.archive;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import net.rim.device.api.io.Seekable;
import net.sf.zipme.UnzipCallback;
import net.sf.zipme.ZipArchive;
import net.sf.zipme.ZipEntry;
import net.sf.zipme.ZipInputStream;
import RockManager.archive.indicator.ExtractIndicator;
import RockManager.fileHandler.FileHandler;
import RockManager.ui.progressPopup.ProgressPopup;
import RockManager.util.UtilCommon;
import de.innosystec.unrar.Archive;
import de.innosystec.unrar.UnrarCallback;
import de.innosystec.unrar.exception.RarException;
import de.innosystec.unrar.rarfile.FileHeader;


/**
 * ArchiveEntry之上的一个包装。
 */
public class ArchiveFile {

	/**
	 * 压缩文件的父路径, 即所在的文件夹。
	 */
	private String parentPath;

	/**
	 * 文件名称，不包括后缀名。
	 */
	private String fileName;

	/**
	 * 文件名称，包括后缀名。
	 */
	private String fullFileName;

	/**
	 * 压缩文件类型。
	 */
	private FileConnection archiveFile;

	private ArchiveEntry rootEntry;

	private ArchiveEntry activeEntry;

	public static int TYPE_ZIP = ArchiveEntry.TYPE_ZIP;

	public static int TYPE_RAR = ArchiveEntry.TYPE_RAR;

	/**
	 * 原压缩文件打开的InputStream, 现在只有zip型使用到了这个流。
	 */
	private InputStream archiveInput;

	/**
	 * rar型用到的archive.
	 */
	private Archive archiveRAR;

	/**
	 * zip型用到的archive.
	 */
	private ZipArchive archiveZip;

	private String subDirPath;


	/**
	 * 通过给定的文件路径创建ArchiveFile.
	 * 
	 * @param url
	 * @throws IOException
	 */
	public ArchiveFile(String fileURL) throws IOException {

		try {
			archiveFile = (FileConnection) Connector.open(fileURL);
		} catch (Exception e) {
			e.printStackTrace();
			throw new IOException("Unable to open the archive, " + e.toString());
		}

		parentPath = "file://" + archiveFile.getPath();
		fullFileName = archiveFile.getName();

		String suffix = UtilCommon.getSuffix(fullFileName);

		if (suffix.equals("rem")) {
			// 如"A.rar.rem"
			String nameWithOutRem = UtilCommon.getName(fullFileName, false);
			suffix = UtilCommon.getSuffix(nameWithOutRem);
			fileName = UtilCommon.getName(nameWithOutRem, false);
		} else {
			// 如"A.rar"
			fileName = UtilCommon.getName(fullFileName, false);
		}

		// 将cod文件也视为zip型压缩文件。
		if (suffix.equals("zip") || suffix.equals("cod")) {
			rootEntry = new ArchiveEntry(TYPE_ZIP);
		} else if (suffix.equals("rar")) {
			rootEntry = new ArchiveEntry(TYPE_RAR);
		}

	}


	/**
	 * 初始化压缩文件，可能很快，也可能耗时。
	 * 
	 * @param indicator
	 *            进度指示器。
	 * @throws IOException
	 * @throws RarException
	 */
	public void initialArchive(Object indicator) throws IOException {

		try {
			if (isZipArchive()) {
				// ZIP archive
				initialZipArchive(indicator);
			} else if (isRarArchive()) {
				// RAR archive
				initialRarArchive(indicator);
			}
		} catch (IOException e) {
			// 初始化失败
			// 清理，关闭文件流，关闭文件连接
			close();
			throw e;
		}

	}


	/**
	 * 初始化一个zip压缩文件。耗时操作。
	 * 
	 * @param archiveFile
	 * @throws IOException
	 */
	private void initialZipArchive(Object indicator) throws IOException {

		// 注意：此处打开了一个文件流。
		archiveInput = archiveFile.openInputStream();

		try {
			archiveZip = new ZipArchive(archiveInput, archiveFile.fileSize());
		} catch (IOException initialFailedException) {
			// 不能初始化压缩文件，可能是无效的压缩文件。
			throw initialFailedException;
		}

		if (indicator instanceof UnzipCallback) {
			UnzipCallback callback = (UnzipCallback) indicator;
			archiveZip.setCallBack(callback);
		}

		Enumeration entries = archiveZip.entries(); // 主要的耗时操作。
		archiveZip.setCallBack(null); // loading indicator以后没用了，clean.

		while (entries.hasMoreElements()) {

			ZipEntry thisEntry = (ZipEntry) entries.nextElement();
			String name = thisEntry.getName();

			ArchiveEntry addedEntry = rootEntry.addFile(name);

			if (addedEntry != null) {
				// 添加了一条新纪录或取得了已有的项。

				addedEntry.setOriginDataEntry(thisEntry);

				if (thisEntry.isDirectory() == false) {
					// 是文件，设置大小。
					long fileSize = thisEntry.getSize();
					addedEntry.setFileSize(fileSize);
				}

			}

		}

	}


	/**
	 * 初始化一个rar压缩文件。耗时操作。
	 * 
	 * @param archiveFile
	 * @throws IOException
	 * @throws RarException
	 */
	private void initialRarArchive(Object indicator) throws IOException {

		UnrarCallback callback = null;
		if (indicator instanceof UnrarCallback) {
			callback = (UnrarCallback) indicator;
		}

		archiveRAR = new Archive(archiveFile, callback);

		Enumeration headers = archiveRAR.getFileHeaders().elements(); // 主要的耗时操作。
		archiveRAR.setUnrarCallback(null); // loading indicator以后没用了，clean.

		while (headers.hasMoreElements()) {

			FileHeader thisHeader = (FileHeader) headers.nextElement();
			String name = thisHeader.isUnicode() ? thisHeader.getFileNameW() : thisHeader.getFileNameString();
			if (thisHeader.isDirectory()) {
				// 补全最后一个'\'，这样基本屏蔽了和zip压缩文件的差异，为操作提供了方便。
				name += "\\";
			}

			ArchiveEntry addedEntry = rootEntry.addFile(name);

			if (addedEntry != null) {
				// 添加了一条新纪录或取得了已有的项。

				addedEntry.setOriginDataEntry(thisHeader);

				if (thisHeader.isDirectory() == false) {
					// 是文件，设置大小。
					long fileSize = thisHeader.getFullUnpackSize();
					addedEntry.setFileSize(fileSize);
				}

			}

		}

	}


	/**
	 * 解压全部文件到指定路径。
	 * 
	 * @param targetPath
	 *            目标路径，不存在将被创建。
	 * @param indicator
	 *            一个弹出窗口，作为进度指示器。
	 * @throws IOException
	 */
	public void extractAll(String targetURL, ProgressPopup indicator) throws Exception {

		FileHandler.createTargetFolder(targetURL);

		if (isZipArchive()) {
			// zip型压缩文件
			extractZipAll(targetURL, indicator);
		} else if (isRarArchive()) {
			// rar型压缩文件
			extractRarAll(targetURL, indicator);
		}

	}


	/**
	 * 解压整个zip文件。
	 * 
	 * @param targetURL
	 * @param indicator
	 * @throws Exception
	 */
	private void extractZipAll(String targetURL, ProgressPopup indicator) throws Exception {

		long fileSize = archiveFile.fileSize();

		// 注意：此处打开了一个文件流。
		if (archiveInput != null) {
			// 若zip文件已经初始化了，关闭原来的流并重新打开，使流定位到文件的开头。
			archiveInput.close();
		}
		archiveInput = archiveFile.openInputStream();

		Seekable seekableStream = null;
		if (archiveInput instanceof Seekable) {
			seekableStream = (Seekable) archiveInput;
		}

		ZipInputStream zipInput = new ZipInputStream(archiveInput);
		byte[] buffer = new byte[10240];
		int readCount = -1;
		ZipEntry thisEntry = null;
		long positionNow = 0;

		while ((thisEntry = zipInput.getNextEntry()) != null) {
			// 切换到了下一个文件。

			String name = thisEntry.getName();
			String thisFileURL = targetURL + UtilCommon.toURLForm(name);

			try {
				FileHandler.createTargetFile(thisFileURL);
			} catch (Exception e) {
				throw e; // failed to create file...
				// end extract progress
			}

			if (thisEntry.isDirectory()) {
				// 若是目录无需进一步操作了。
				continue;
			}

			FileConnection thisFileConnection = null;
			OutputStream os = null;

			try {

				thisFileConnection = (FileConnection) Connector.open(thisFileURL);
				os = thisFileConnection.openOutputStream();
				// 设置显示的文件名，直接从zip中获得的文件名可能包含文件夹结构，如"happy/happy.cod".
				indicator.setProgressName(UtilCommon.getFullFileName(name));

				while ((readCount = zipInput.read(buffer)) > 0) {

					if (seekableStream != null) {
						positionNow = seekableStream.getPosition();
						// 显示目前的进度
						indicator.setProgressRate((int) (positionNow * 100 / fileSize));
					}

					os.write(buffer, 0, readCount);

				}

			} catch (Exception e) {
			} finally {
				Util.closeConnection(os, thisFileConnection);
			}

		}

		int finalRate = (int) (positionNow * 100 / fileSize);
		if (finalRate < 100) {
			// 由于获取进度是由在文件中的位置除以文件总长度获得的，所以即使全部解压完成，显示的进度可能也不是100%(大多是98,99)。
			// 设置显示的进度为100%。
			indicator.setProgressRate(100);
		}

		// 全部读取完毕，关闭流。(如果调用ArchiveFile.close(), 此处不关闭也可)
		zipInput.close();

	}


	/**
	 * 解压整个rar文件。
	 * 
	 * @param targetURL
	 * @param indicator
	 * @throws Exception
	 */
	private void extractRarAll(String targetURL, ProgressPopup indicator) throws Exception {

		if (archiveRAR == null) {
			archiveRAR = new Archive(archiveFile);
		}

		RarUtil.extractAll(archiveRAR, targetURL, indicator);

	}


	/**
	 * 解压一个entry, entry可能是文件或文件夹。
	 * 
	 * @param entry
	 * @param targetURL
	 * @throws Exception
	 */
	public void extractEntry(ArchiveEntry entry, String targetURL, ExtractIndicator indicator) throws Exception {

		String entryName = UtilCommon.getFullFileName(entry.getName());

		if (entry.isDir()) { // entry是文件夹

			if (entry.isRarEntry()) {
				entryName = UtilCommon.replaceString(entryName, ArchiveEntry.RAR_SEPARATOR, "/");
			}

			indicator.setProgressName(entryName);

			String folderURL = targetURL + UtilCommon.toURLForm(entryName);

			try {
				FileHandler.createTargetFolder(folderURL);
			} catch (Exception e) {
				throw e; // failed to create file...
				// end extract progress
			}

			ArchiveEntry[] entries = entry.getFiles();

			for (int i = 0; i < entries.length; i++) {
				// 解压所有子文件。
				extractEntry(entries[i], folderURL, indicator);
			}

			return;

		}

		// entry是文件

		String fileURL = targetURL + UtilCommon.toURLForm(entryName);

		indicator.setProgressName(entryName);

		try {
			FileHandler.createTargetFile(fileURL);
		} catch (Exception e) {
			throw e; // failed to create file...
			// end extract progress
		}

		if (entry.isZipEntry()) {
			extractZipEntry((ZipEntry) entry.getOriginDataEntry(), fileURL, indicator);
		} else {
			extractRarEntry((FileHeader) entry.getOriginDataEntry(), fileURL, indicator);
		}

	}


	/**
	 * 解压一个zip的entry.
	 * 
	 * @param entry
	 * @param targetURL
	 * @param indicator
	 */
	private void extractZipEntry(ZipEntry entry, String targetURL, ExtractIndicator indicator) {

		FileConnection targetConn = null;
		OutputStream os = null;

		try {

			targetConn = (FileConnection) Connector.open(targetURL);
			os = targetConn.openOutputStream();

			InputStream zipIn = archiveZip.getInputStream(entry);

			byte[] buffer = new byte[10240];
			int readCount = -1;

			while ((readCount = zipIn.read(buffer)) > 0) {
				indicator.increaseRead(readCount);
				os.write(buffer, 0, readCount);
			}

		} catch (Exception e) {
		} finally {
			Util.closeConnection(os, targetConn);
		}

	}


	/**
	 * 解压一个rar的entry.
	 * 
	 * @param entry
	 * @param targetURL
	 * @param indicator
	 */
	private void extractRarEntry(FileHeader entry, String targetURL, ExtractIndicator indicator) {

		FileConnection targetConn = null;
		OutputStream os = null;

		try {

			targetConn = (FileConnection) Connector.open(targetURL);
			os = targetConn.openOutputStream();

			archiveRAR.setExtractIndicator(indicator);
			archiveRAR.extractFile(entry, os);

		} catch (Exception e) {
		} finally {
			Util.closeConnection(os, targetConn);
		}

	}


	/**
	 * 返回文件打开是否较耗时。
	 * 
	 * @return
	 */
	public boolean isTooBig() {

		boolean tooBig = false;
		long fileSize = 0;
		try {
			fileSize = archiveFile.fileSize();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (isRarArchive()) {
			// RAR文件
			// 打开RAR文件获取目录结构差不多需要遍历整个文件，若RAR文件大于1024K,则认为是较大的，打开较耗时的。
			if (fileSize > 1024 * 1024) {
				tooBig = true;
			}
		} else if (isZipArchive()) {
			// 打开Zip文件获取目录结构不需遍历整个文件，仅与里面的文件个数有关（需遍历目录段），认为大于3M的Zip文件打开较耗时。
			if (fileSize > 1024 * 1024 * 3) {
				tooBig = true;
			}
		}
		return tooBig;
	}


	/**
	 * 设置子目录路径，若为空则为根目录。
	 * 
	 * @param newPath
	 */
	public void setSubDirPath(String newPath) {

		boolean rootDir = (newPath.length() == 0);
		boolean backToParent = !rootDir && newPath.length() < subDirPath.length();
		boolean enterSubDir = !rootDir && !backToParent && newPath.length() > subDirPath.length();

		if (rootDir) {
			// 压缩文件根目录.
			activeEntry = rootEntry;
		} else {
			if (backToParent) {
				// 返回上级目录。
				activeEntry = activeEntry.getParentEntry();
			} else if (enterSubDir) {
				// 进入子目录。
				String subDirPath = UtilCommon.getFullFileName(newPath);
				activeEntry = activeEntry.getEntry(subDirPath);
			} else {
				// 目录未变
			}
		}

		subDirPath = newPath;
	}


	/**
	 * 返回地址栏显示的压缩文件路径形式。 若是根目录显示压缩文件名称，否则显示压缩文件名称加压缩文件内的路径。
	 * 
	 * @return
	 */
	public String getDisplayPath() {

		if (subDirPath == null || subDirPath.length() == 0) {
			return fullFileName;
		} else {
			return fullFileName + getSeparator() + subDirPath;
		}
	}


	/**
	 * 返回当前目录下的文件、文件夹。在setSubDirPath后调用此函数。
	 * 
	 * @return
	 */
	public ArchiveEntry[] getFiles() {

		return activeEntry.getFiles();
	}


	/**
	 * 返回压缩文件名称。
	 * 
	 * @param withSuffix
	 *            是否带后缀名（.zip或.rar）
	 * @return
	 */
	public String getName(boolean withSuffix) {

		if (withSuffix) {
			return fullFileName;
		} else {
			return fileName;
		}
	}


	/**
	 * 返回父文件夹的路径。
	 */
	public String getParentPath() {

		return parentPath;
	}


	/**
	 * 返回压缩文件类型，zip或rar.
	 * 
	 * @return
	 */
	public int getType() {

		return rootEntry.getType();
	}


	/**
	 * 是否是zip型压缩文件，如.zip、.cod。
	 * 
	 * @return
	 */
	public boolean isZipArchive() {

		return rootEntry.getType() == TYPE_ZIP;
	}


	/**
	 * 是否是rar型压缩文件，如.rar。
	 * 
	 * @return
	 */
	public boolean isRarArchive() {

		return rootEntry.getType() == TYPE_RAR;
	}


	public String getSeparator() {

		return rootEntry.getSeparator();
	}


	public char getSeparatorChar() {

		return getSeparator().charAt(0);
	}


	/**
	 * 关闭压缩文件的流，释放文件连接。 对于zip文件，需关闭输入流及文件连接。
	 * 对于rar文件，只需调用它的close方法，它会自动关闭流和文件连接，并完成其他清理工作。
	 */
	public void close() {

		switch (rootEntry.getType()) {

			case ArchiveEntry.TYPE_ZIP:

				if (archiveInput != null) {
					try {
						archiveInput.close();
					} catch (Exception e) {
					}
				}
				break;

			case ArchiveEntry.TYPE_RAR:

				if (archiveRAR != null) {
					try {
						archiveRAR.close();
					} catch (Exception e) {
					}
				}
				break;
		}

		try {
			// 若在初始化完成前遇到错误要关闭，则即使是rar型文件也要手动关闭文件连接，因为archiveRAR可能还未创建。
			archiveFile.close();
		} catch (Exception e) {
		}

	}

}
