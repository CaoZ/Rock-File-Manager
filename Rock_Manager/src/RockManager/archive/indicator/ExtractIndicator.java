
package RockManager.archive.indicator;

import RockManager.ui.progressPopup.ProgressPopup;


public class ExtractIndicator {

	/**
	 * 所有压缩文件压缩后的总大小。
	 */
	private long totalSize;

	/**
	 * 所有压缩文件已读取(解压)的大小。
	 */
	private long totalRead;

	private ProgressPopup display;


	public void setTotalSize(long size) {

		totalSize = size;

	}


	public void setTotalRead(long totalReadSize) {

		if (totalRead != totalReadSize) {
			totalRead = totalReadSize;
			setRate();
		}

	}


	/**
	 * 增加已读取大小。
	 * 
	 * @param curRead
	 */
	public void increaseRead(long curRead) {

		if (curRead > 0) {
			totalRead += curRead;
			setRate();
		}

	}


	private void setRate() {

		if (totalSize == 0) {
			display.setProgressRate(100);
		} else {
			display.setProgressRate((int) (totalRead * 100 / totalSize));
		}

	}


	/**
	 * 设置正在做的工作的名称。
	 * 
	 * @param name
	 */
	public void setProgressName(String name) {

		display.setProgressName(name);

	}


	public void setDisplay(ProgressPopup display) {

		this.display = display;
	}

}
