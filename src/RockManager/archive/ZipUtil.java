
package RockManager.archive;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import net.sf.zipme.ZipEntry;
import net.sf.zipme.ZipOutputStream;
import RockManager.fileHandler.FileHandler;
import RockManager.fileList.FileItem;
import RockManager.ui.progressPopup.ProgressIndicator;
import RockManager.util.IOUtil;
import RockManager.util.UtilCommon;


public class ZipUtil {

	/**
	 * 压缩文件或文件夹。
	 * 
	 * @param items_to_compress
	 *            要压缩的文件项。
	 * @param saveURL
	 *            Zip文件位置。
	 * @param compressMethod
	 *            压缩方法(存储，最快，标准等)。
	 * @param compressIndicator
	 * @throws Exception
	 */
	public static void compress(FileItem[] items_to_compress, String saveURL, int compressMethod,
			ProgressIndicator compressIndicator) throws Exception {

		try {
			FileHandler.createTargetFile(saveURL);
		} catch (Exception e) {
			throw e;
		}

		FileConnection zip_conn = null;
		ZipOutputStream zip_os = null;

		try {

			zip_conn = (FileConnection) Connector.open(saveURL);
			OutputStream os = zip_conn.openOutputStream();
			zip_os = new ZipOutputStream(os);
			// "方式"是程序中的叫法，如"存储", "最快", "标准"等，实际上对应zip中的level.
			zip_os.setLevel(compressMethod);

			long totalSize = IOUtil.getFileSize(items_to_compress);
			compressIndicator.setTotalSize(totalSize);

			int bufferSize = IOUtil.getBufferSize(totalSize);
			byte[] buffer = new byte[bufferSize];

			for (int i = 0; i < items_to_compress.length; i++) {

				String file_url = items_to_compress[i].getURL();
				FileConnection origin_connection = (FileConnection) Connector.open(file_url, Connector.READ);
				compress(origin_connection, zip_os, "", buffer, compressIndicator);
				IOUtil.closeConnection(origin_connection);

			}

			// 压缩成功完成。
			if (compressIndicator != null) {
				compressIndicator.setProgressRate(100);
			}

		} catch (Exception e) {
			throw e;
		} finally {
			IOUtil.closeStream(zip_os);
			IOUtil.closeConnection(zip_conn);
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
	 * @param buffer
	 * @param compressIndicator
	 * @throws IOException
	 */
	private static void compress(FileConnection fconn, ZipOutputStream zos, String baseDir, byte[] buffer,
			ProgressIndicator compressIndicator) throws Exception {

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
				compress(thisFileURL, zos, newBaseDir, buffer, compressIndicator);

			}

		} else {

			// 是文件，压缩此文件。
			compressFile(fconn, zos, baseDir + fileName, buffer, compressIndicator);

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
	private static void compress(String originFileURL, ZipOutputStream zos, String baseDir, byte[] buffer,
			ProgressIndicator compressIndicator) throws Exception {

		FileConnection fconn = null;

		try {

			fconn = (FileConnection) Connector.open(originFileURL, Connector.READ);
			compress(fconn, zos, baseDir, buffer, compressIndicator);

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
	 * @param buffer
	 * @param compressIndicator
	 * @throws IOException
	 */
	private static void compressFile(FileConnection fconn, ZipOutputStream zos, String name, byte[] buffer,
			ProgressIndicator compressIndicator) throws IOException {

		ZipEntry fileEntry = new ZipEntry(name);
		zos.putNextEntry(fileEntry);

		InputStream is = fconn.openInputStream();
		int readCount = -1;

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
