
package RockManager.util;

import java.io.InputStream;
import java.io.OutputStream;
import javax.microedition.io.file.FileConnection;


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

}
