
package RockManager.fileList.position;

import java.util.Hashtable;
import RockManager.fileList.FileItem;


/**
 * 用于记录高亮项（焦点所在）的位置。
 */

public class PositionLogger {

	private Hashtable positionLog;

	private InnerPositionData innerPositionData;


	public PositionLogger() {

		positionLog = new Hashtable();
		innerPositionData = new InnerPositionData();
	}


	/**
	 * 获取与folderPath相关联的位置数据，若不存在，返回0。
	 */
	public int getDistance(String folderPath) {

		folderPath = checkIfRoot(folderPath);

		Object positionData = positionLog.get(folderPath);
		if (positionData instanceof PositionData) {
			return ((PositionData) positionData).getDistance();
		} else {
			return -1;
		}

	}


	/**
	 * 获取与folderPath相关的index数据，若不存在，返回0。
	 */
	public int getIndex(String folderPath) {

		folderPath = checkIfRoot(folderPath);

		Object positionData = positionLog.get(folderPath);
		if (positionData instanceof PositionData) {
			return ((PositionData) positionData).getIndex();
		} else {
			return -1;
		}

	}


	/**
	 * 获取与folderPath相关的fileType数据，若不存在，返回FileItem.TYPE_UNKNOWN。
	 */
	public int getFileType(String folderPath) {

		folderPath = checkIfRoot(folderPath);

		Object positionData = positionLog.get(folderPath);
		if (positionData instanceof PositionData) {
			return ((PositionData) positionData).getFileType();
		} else {
			return FileItem.TYPE_UNKNOWN;
		}

	}


	/**
	 * 删除与folderPath相关的记录。
	 */
	public void removeLog(String folderPath) {

		folderPath = checkIfRoot(folderPath);
		positionLog.remove(folderPath);

	}


	public void log(String folderPath, PositionData positionData) {

		folderPath = checkIfRoot(folderPath);
		positionLog.put(folderPath, positionData);

	}


	/**
	 * 记录本文件夹内focused项的距离。
	 */
	public void logDistance_inner(int distanceWithManager) {

		innerPositionData.setTopDistance(distanceWithManager);

	}


	/**
	 * 返回上次本文件夹内focused的距离。
	 */
	public int getDistance_inner() {

		return innerPositionData.getTopDistance();

	}


	/**
	 * 记录本文件夹内focused项的位置。
	 */
	public void logIndex_inner(int selectedIndex) {

		innerPositionData.setSelectedIndex(selectedIndex);

	}


	/**
	 * 返回上次本文件夹内focused的位置。
	 */
	public int getIndex_inner() {

		return innerPositionData.getSelectedIndex();

	}


	/**
	 * 清除记录的位置数据（不包括innerPosition的数据）。
	 */
	public void clearPositionData() {

		positionLog.clear();

	}


	/**
	 * 清除记录的本文件夹内的位置数据。
	 */
	public void clearPositionData_inner() {

		innerPositionData.resetData();

	}


	/**
	 * 记录高亮项的地址。
	 */
	public void logFocusedName_inner(String name) {

		innerPositionData.setFocusedName(name);

	}


	/**
	 * 记录高亮项的地址（较高优先级）。
	 */
	public void logFocusedName_inner_highPriority(String fileName) {

		innerPositionData.setFocusedName_highPriority(fileName);

	}


	/**
	 * 返回已记录的高亮项的文件或文件夹名。
	 */
	public String getLastFocusedName_inner() {

		return innerPositionData.getFocusedName();

	}


	public void logFileType_inner(int type) {

		innerPositionData.setFileType(type);

	}


	public void logFileType_inner_highPriority(int type) {

		innerPositionData.setFileType_highPriority(type);

	}


	public int getFileType_inner() {

		return innerPositionData.getFileType();

	}


	/**
	 * 返回对于folderPath是否存在数据。
	 * 
	 * @param folderPath
	 * @return
	 */
	public boolean hasLog(String folderPath) {

		folderPath = checkIfRoot(folderPath);
		return positionLog.containsKey(folderPath);
	}


	/**
	 * 返回是否存在本文件夹的位置数据。
	 * 
	 * @return
	 */
	public boolean hasLog_inner() {

		return innerPositionData.hasLog();
	}


	private String checkIfRoot(String folderPath) {

		if (folderPath != null) {
			return folderPath;
		} else {
			return "root";
		}

	}

}
