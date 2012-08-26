
package RockManager.fileList.position;

public class PositionData {

	/**
	 * 高亮项与manager上部的滚动后的距离。
	 */
	private int distance;

	/**
	 * 高亮项的index。
	 */
	private int index;

	private int fileType;


	public void setDistance(int distance) {

		this.distance = distance;
	}


	public int getDistance() {

		return distance;
	}


	public void setIndex(int index) {

		this.index = index;
	}


	public int getIndex() {

		return index;
	}


	public void setFileType(int type) {

		this.fileType = type;
	}


	public int getFileType() {

		return fileType;
	}

}
