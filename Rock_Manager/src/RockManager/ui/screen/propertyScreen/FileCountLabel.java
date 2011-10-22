
package RockManager.ui.screen.propertyScreen;

import RockManager.fileHandler.fileCounter.FileCountResult;
import RockManager.fileHandler.fileCounter.FileCounter;
import net.rim.device.api.ui.UiApplication;


public class FileCountLabel extends ValueLabel {

	public FileCountLabel(final String folderURL) {

		super("...");

		UiApplication.getUiApplication().invokeLater(new Runnable() {

			public void run() {

				Thread countThread = createCountThread(folderURL);
				countThread.start();

			}
		});

	}


	private Thread createCountThread(final String folderURL) {

		Thread countThread = new Thread() {

			public void run() {

				final FileCountResult countResult = FileCounter.countFolder(folderURL);

				UiApplication.getUiApplication().invokeAndWait(new Runnable() {

					public void run() {

						setText(countResult.toString());

					}
				});

			}
		};

		return countThread;

	}

}
