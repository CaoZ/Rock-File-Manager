
package RockManager.archive;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import net.sf.zipme.ZipEntry;
import net.sf.zipme.ZipOutputStream;
import RockManager.archive.indicator.ArchiveIndicator;
import RockManager.fileHandler.FileHandler;
import RockManager.util.IOUtil;
import RockManager.util.UtilCommon;


public class ZipUtil {

	/**
	 * 压缩文件或文件夹。
	 * 
	 * @param originFileURL
	 *            要压缩的文件位置。
	 * @param saveURL
	 *            Zip文件位置。
	 * @param compressMethod
	 *            压缩方法(存储，最快，标准等)。
	 * @param compressIndicator
	 * @throws Exception
	 */
	public static void compress(String originFileURL, String saveURL, int compressMethod,
			ArchiveIndicator compressIndicator) throws Exception {

		try {
			FileHandler.createTargetFile(saveURL);
		} catch (Exception e) {
			throw e;
		}

		FileConnection originFileConn = null;
		FileConnection fconn = null;
		ZipOutputStream zos = null;

		try {

			fconn = (FileConnection) Connector.open(saveURL);
			OutputStream os = fconn.openOutputStream();
			zos = new ZipOutputStream(os);
			// "方式"是程序中的叫法，如"存储", "最快", "标准"等，实际上对应zip中的level.
			zos.setLevel(compressMethod);

			originFileConn = (FileConnection) Connector.open(originFileURL);

			if (compressIndicator != null) {

				long totalSize = 0;
				if (originFileConn.isDirectory()) {
					totalSize = originFileConn.directorySize(true);
				} else {
					totalSize = originFileConn.fileSize();
				}
				compressIndicator.setTotalSize(totalSize);

			}

			compress(originFileConn, zos, "", compressIndicator);

			// 压缩成功完成。
			if (compressIndicator != null) {
				compressIndicator.setProgressRate(100);
			}

		} catch (Exception e) {
			throw e;
		} finally {
			IOUtil.closeConnection(originFileConn);
			IOUtil.closeStream(zos);
			IOUtil.closeConnection(fconn);
		}

	}


	/**
	 * 压缩文件或文件夹。
	 * 
	 * @param fconn
	 *            源文件/文件夹的FileConnection.
	 * @param zos
	 *            ZipOutputStream.
	 * @param baseDir
	 *            相对的父级路径。
	 * @param compressIndicator
	 * @throws IOException
	 */
	private static void compress(FileConnection fconn, ZipOutputStream zos, String baseDir,
			ArchiveIndicator compressIndicator) throws  Exception {

		String fileName = fconn.getName();

		if (compressIndicator != null) {
			compressIndicator.setProgressName(fileName);
		}

		if (fconn.isDirectory()) {
			// 是文件夹，建立文件夹节点并压缩所有子文件/文件夹。

			String folderPath = "file://" + fconn.getPath() + fileName;
			String folderPathURL = UtilCommon.toURLForm(folderPath);
			String newBaseDir = baseDir + fileName;
			ZipEntry dirEntry = new ZipEntry(newBaseDir);
			zos.putNextEntry(dirEntry);
			zos.closeEntry();

			Enumeration allFiles = fconn.list("*", true);

			while (allFiles.hasMoreElements()) {

				String thisFileName = (String) allFiles.nextElement();
				String thisFileURL = folderPathURL + UtilCommon.toURLForm(thisFileName);
				compress(thisFileURL, zos, newBaseDir, compressIndicator);

			}

		} else {

			// 是文件，压缩此文件。
			compressFile(fconn, zos, baseDir + fileName, compressIndicator);

		}

	}


	/**
	 * 压缩文件到Zip中。
	 * 
	 * @param originFileURL
	 *            要压缩的文件位置。
	 * @param zos
	 *            ZipOutputStream.
	 * @param baseDir
	 *            相对的父级路径。
	 * @throws IOException
	 */
	private static void compress(String originFileURL, ZipOutputStream zos, String baseDir,
			ArchiveIndicator compressIndicator) throws Exception {

		FileConnection fconn = null;

		try {

			fconn = (FileConnection) Connector.open(originFileURL);
			compress(fconn, zos, baseDir, compressIndicator);

		} catch (Exception e) {
			throw e;
		} finally {
			IOUtil.closeConnection(fconn);
		}

	}


	/**
	 * 压缩一个文件到Zip中。
	 * 
	 * @param fconn
	 *            原文件的FileConnection.
	 * @param zos
	 *            ZipOutputStream.
	 * @param name
	 *            此ZipEntry的完整名称。
	 * @param compressIndicator
	 * @throws IOException
	 */
	private static void compressFile(FileConnection fconn, ZipOutputStream zos, String name,
			ArchiveIndicator compressIndicator) throws IOException {

		ZipEntry fileEntry = new ZipEntry(name);
		zos.putNextEntry(fileEntry);

		InputStream is = fconn.openInputStream();
		int readCount = -1;
		byte[] buffer = new byte[10240];

		try {

			while ((readCount = is.read(buffer)) > 0) {

				if (compressIndicator != null) {
					compressIndicator.increaseRead(readCount);
				}
				zos.write(buffer, 0, readCount);
			}

		} catch (Exception e) {
		} finally {
			is.close();
		}

		zos.closeEntry();

	}

}
