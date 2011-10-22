
package RockManager.archive;

import java.io.OutputStream;
import javax.microedition.io.file.FileConnection;


public class Util {

	/**
	 * 安全的关闭IO.
	 * 
	 * @param os
	 * @param fconn
	 */
	static void closeConnection(OutputStream os, FileConnection fconn) {

		if (os != null) {
			try {
				os.close();
			} catch (Exception e) {
			}
		}

		if (fconn != null) {
			try {
				fconn.close();
			} catch (Exception e) {
			}
		}

	}

}
