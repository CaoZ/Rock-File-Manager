
package RockManager.fileHandler.fileCounter;

import java.util.Enumeration;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import RockManager.archive.ArchiveEntry;
import RockManager.util.UtilCommon;


/**
 * 计算一个文件夹内含有多少文件。
 */
public class FileCounter {

	/**
	 * 计算一个文件夹内含有多少文件。
	 */
	public static FileCountResult countFolder(String folderURL) {

		FileCountResult result = new FileCountResult();

		FileConnection fconn = null;
		try {
			fconn = (FileConnection) Connector.open(folderURL, Connector.READ);
			if (fconn.isDirectory()) {
				Enumeration allFiles = fconn.list("*", true);
				while (allFiles.hasMoreElements()) {

					String thisFileName = (String) allFiles.nextElement();
					if (UtilCommon.isFolder(thisFileName)) {
						result.plusFolderNumber();
						// 除了子文件夹自身算一个文件外，再加上子文件夹内的文件数量。
						String subFolderURL = folderURL + UtilCommon.toURLForm(thisFileName);
						result.appendResult(countFolder(subFolderURL));
					} else {
						// is file
						result.plusFileNumber();
					}

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (fconn != null) {
				try {
					fconn.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		return result;

	}


	public static FileCountResult countFolder(ArchiveEntry folderEntry) {

		FileCountResult result = new FileCountResult();

		ArchiveEntry[] allFiles = folderEntry.getFiles();

		for (int i = 0; i < allFiles.length; i++) {
			if (allFiles[i].isDir()) {
				result.plusFolderNumber();
				result.appendResult(countFolder(allFiles[i]));
			} else {
				result.plusFileNumber();
			}
		}

		return result;

	}

}
