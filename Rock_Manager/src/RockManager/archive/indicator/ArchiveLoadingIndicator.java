package RockManager.archive.indicator;

import javax.microedition.io.file.FileConnection;
import net.sf.zipme.UnzipCallback;
import RockManager.ui.statusBar.StatusBar;
import de.innosystec.unrar.UnrarCallback;

/**
 * 压缩文件载入指示器，在屏幕底部的StatusBar显示进度。
 * 
 */
public class ArchiveLoadingIndicator implements UnrarCallback, UnzipCallback {

	private StatusBar statusBar;

	public ArchiveLoadingIndicator(StatusBar statusBar) {
		this.statusBar = statusBar;
	}

	public boolean isNextVolumeReady(FileConnection nextVolume) {
		// UnrarCallback
		return false;
	}

	public void volumeProgressChanged(long current, long total) {
		statusBar.setProgress(current, total);
	}

	/**
	 * 设置进度。
	 * 
	 * @param percent
	 *            0到1.
	 */
	public void setProgress(float percent) {
		statusBar.setProgress(percent);
	}

}
