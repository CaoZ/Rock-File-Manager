
package RockManager.archive;

import java.io.OutputStream;
import java.util.Vector;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import RockManager.archive.indicator.ArchiveIndicator;
import RockManager.fileHandler.FileHandler;
import RockManager.ui.progressPopup.ProgressPopup;
import RockManager.util.IOUtil;
import RockManager.util.UtilCommon;
import de.innosystec.unrar.Archive;
import de.innosystec.unrar.rarfile.FileHeader;


public class RarUtil {

	public static void extractAll(Archive archiveRAR, String targetURL, ProgressPopup indicator) throws Exception {

		Vector headerVector = archiveRAR.getFileHeaders();
		FileHeader[] headers = new FileHeader[headerVector.size()];
		headerVector.copyInto(headers);

		long totalPackedSize = getPackedSize(headers);
		long totalRead = 0;

		ArchiveIndicator extractIndicator = new ArchiveIndicator();
		extractIndicator.setDisplay(indicator);
		extractIndicator.setTotalSize(totalPackedSize);

		archiveRAR.setExtractIndicator(extractIndicator);

		for (int i = 0; i < headers.length; i++) {

			FileHeader thisHeader = headers[i];

			String name = thisHeader.isUnicode() ? thisHeader.getFileNameW() : thisHeader.getFileNameString();
			name = UtilCommon.replaceAllString(name, ArchiveEntry.RAR_SEPARATOR, "/");
			if (thisHeader.isDirectory()) {
				name += "/";
			}

			// 设置进度指示窗口要显示的文件名。
			extractIndicator.setProgressName(UtilCommon.getFullFileName(name));

			String thisFileURL = targetURL + UtilCommon.toURLForm(name);

			try {
				FileHandler.createTargetFile(thisFileURL);
			} catch (Exception e) {
				throw e; // failed to create file...
				// end extract progress
			}

			if (thisHeader.isDirectory()) {
				// 若是目录无需进一步操作。
				continue;
			}

			FileConnection fconn = null;
			OutputStream os = null;

			try {

				fconn = (FileConnection) Connector.open(thisFileURL);
				os = fconn.openOutputStream();

				archiveRAR.extractFile(thisHeader, os);

			} catch (Exception e) {
			} finally {

				// 即使解压失败也能给定正确的进度。
				totalRead += thisHeader.getPackSize();
				extractIndicator.setTotalRead(totalRead);

				IOUtil.closeStream(os);
				IOUtil.closeConnection(fconn);

			}

		}

	}


	public static void extractEntry() {

	}


	/**
	 * 计算压缩后大小。
	 * 
	 * @param headers
	 * @return
	 */
	private static long getPackedSize(FileHeader[] headers) {

		long packedSize = 0;

		for (int i = 0; i < headers.length; i++) {
			if (headers[i].isDirectory() == false) {
				packedSize += headers[i].getPackSize();
			}
		}

		return packedSize;

	}

}
