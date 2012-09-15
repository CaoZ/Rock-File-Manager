
package RockManager.util;

import java.io.InputStream;
import java.io.OutputStream;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import RockManager.fileList.FileItem;
import net.rim.device.api.util.MathUtilities;


public class IOUtil {

	private static final int BUFFERSIZE_MIN = 1024 * 10;

	private static final int BUFFERSIZE_MAX = 1024 * 400;


	/**
	 * 安全的关闭FileConnection.
	 */
	public static void closeConnection(FileConnection fconn) {

		if (fconn != null) {
			try {
				fconn.close();
			} catch (Exception e) {
			}
		}

	}


	/**
	 * 安全的关闭InputStream.
	 */
	public static void closeStream(InputStream is) {

		if (is != null) {
			try {
				is.close();
			} catch (Exception e) {
			}
		}

	}


	/**
	 * 安全的关闭OutputStream.
	 */
	public static void closeStream(OutputStream os) {

		if (os != null) {
			try {
				os.close();
			} catch (Exception e) {
			}
		}

	}


	/**
	 * 目标文件/文件夹是否存在。
	 */
	public static boolean isExists(String fileURL) {

		FileConnection fconn = null;
		boolean exists = false;
		try {
			fconn = (FileConnection) Connector.open(fileURL, Connector.READ);
			exists = fconn.exists();
		} catch (Exception e) {
		} finally {
			closeConnection(fconn);
		}
		return exists;
	}


	/**
	 * 根据文件总大小获取适当的缓冲大小, 使进度条不至于一次跳跃太多.
	 * 
	 * @param totalSize
	 * @return
	 */
	public static int getBufferSize(long totalSize) {

		int bufferSize = (int) (totalSize / 20);
		return MathUtilities.clamp(BUFFERSIZE_MIN, bufferSize, BUFFERSIZE_MAX);
	}


	/**
	 * 获取文件或文件夹大小。
	 * 
	 * @param fconn
	 * @return
	 */
	public static long getFileSize(FileConnection fconn) {

		long fileSize = -1;
		try {
			if (fconn.isDirectory()) {
				fileSize = fconn.directorySize(true);
			} else {
				fileSize = fconn.fileSize();
			}
		} catch (Exception e) {
			// can't get fileSize
		}
		return fileSize;
	}


	/**
	 * 获取文件夹或文件的大小。
	 * 
	 * @param fileURL
	 * @return
	 */
	public static long getFileSize(String fileURL) {

		long fileSize = -1;
		FileConnection fconn = null;
		try {
			fconn = (FileConnection) Connector.open(fileURL, Connector.READ);
			fileSize = getFileSize(fconn);
		} catch (Exception e) {
		} finally {
			closeConnection(fconn);
		}

		return fileSize;
	}


	/**
	 * 获取多项文件/文件夹的总大小.
	 * 
	 * @param item_to_paste
	 * @return
	 */
	public static long getFileSize(FileItem[] items) {

		long file_size = -1;
		for (int i = 0; i < items.length; i++) {
			FileItem a_item = items[i];
			if (a_item.isFile()) {
				file_size += a_item.getFileSize();
			} else if (a_item.isDir()) {
				String folder_url = a_item.getURL();
				file_size += getFileSize(folder_url);
			}
		}
		return file_size;
	}

}
