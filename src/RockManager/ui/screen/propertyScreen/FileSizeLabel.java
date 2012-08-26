
package RockManager.ui.screen.propertyScreen;

import RockManager.fileHandler.FileSizeString;
import RockManager.fileList.FileItem;
import RockManager.util.IOUtil;
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

				long folderSize = IOUtil.getFileSize(folderURL);
				setFileSize(folderSize);
			}

		};

		return countThread;

	}

}
