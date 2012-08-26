
package RockManager.fileList.position;

public class InnerPositionData {

	private int topDistance;

	private int selectedIndex;

	private int fileType;

	private int fileType_highPriority;

	private String focusedName;

	private String focusedName_highPriority;


	public InnerPositionData() {

		resetData();
	}


	public void resetData() {

		topDistance = -1;
		selectedIndex = -1;
		fileType = -1;
		fileType_highPriority = -1;
		focusedName = null;
		focusedName_highPriority = null;

	}


	public int getTopDistance() {

		return topDistance;
	}


	public void setTopDistance(int topDistance) {

		this.topDistance = topDistance;
	}


	public int getSelectedIndex() {

		return selectedIndex;
	}


	public void setSelectedIndex(int selectedIndex) {

		this.selectedIndex = selectedIndex;
	}


	public int getFileType() {

		if (fileType_highPriority >= 0) {
			return fileType_highPriority;
		} else {
			return fileType;
		}

	}


	public void setFileType(int fileType) {

		this.fileType = fileType;
	}


	public void setFileType_highPriority(int fileType_highPriority) {

		this.fileType_highPriority = fileType_highPriority;
	}


	public String getFocusedName() {

		if (focusedName_highPriority != null) {
			return focusedName_highPriority;
		} else {
			return focusedName;
		}

	}


	public void setFocusedName(String focusedName) {

		this.focusedName = focusedName;
	}


	public void setFocusedName_highPriority(String focusedName_highPriority) {

		this.focusedName_highPriority = focusedName_highPriority;
	}


	public boolean hasLog() {

		return focusedName != null || focusedName_highPriority != null;
	}

}
