
package RockManager.util;

import java.io.InputStream;
import java.io.OutputStream;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import net.rim.device.api.util.MathUtilities;


public class IOUtil {

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
			fconn = (FileConnection) Connector.open(fileURL);
			exists = fconn.exists();
		} catch (Exception e) {
		} finally {
			closeConnection(fconn);
		}
		return exists;
	}


	public static int getBufferSize(long totalSize, int min, int max) {

		int bufferSize = (int) (totalSize / 20);
		return MathUtilities.clamp(min, bufferSize, max);
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
			fconn = (FileConnection) Connector.open(fileURL);
			if (fconn.isDirectory()) {
				fileSize = fconn.directorySize(true);
			} else {
				fileSize = fconn.fileSize();
			}
		} catch (Exception e) {
		} finally {
			closeConnection(fconn);
		}

		return fileSize;
	}

}
