
package RockManager.ui.screen.propertyScreen;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import RockManager.fileHandler.FileSizeString;
import RockManager.fileList.FileItem;
import net.rim.device.api.ui.UiApplication;


public class FileSizeLabel extends ValueLabel {

	public FileSizeLabel(final FileItem fileItem) {

		super("-");

		if (fileItem.isFile()) {

			setFileSize(fileItem.getFileSize());

		} else if (fileItem.isDir()) {

			setText("...");

			UiApplication.getUiApplication().invokeLater(new Runnable() {

				public void run() {

					Thread countThread = createCountThread(fileItem.getURL());
					countThread.start();

				}

			});
		}

	}


	private void setFileSize(long fileSize) {

		final String fileSizeString;

		if (fileSize >= 0) {
			fileSizeString = FileSizeString.getSizeString(fileSize, false, true);
		} else {
			// can't get the right size
			fileSizeString = "-";
		}

		UiApplication.getUiApplication().invokeAndWait(new Runnable() {

			public void run() {

				setText(fileSizeString);

			}
		});

	}


	private Thread createCountThread(final String folderURL) {

		Thread countThread = new Thread() {

			public void run() {

				FileConnection fconn = null;
				try {
					fconn = (FileConnection) Connector.open(folderURL);
					long folderSize = fconn.directorySize(true);
					setFileSize(folderSize);
				} catch (Exception e) {
					setFileSize(-1);
				} finally {
					if (fconn != null) {
						try {
							fconn.close();
						} catch (Exception e) {
						}
					}
				}

			}

		};

		return countThread;

	}

}
