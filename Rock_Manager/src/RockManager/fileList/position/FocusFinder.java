
package RockManager.fileList.position;

import RockManager.fileList.FileItem;


public class FocusFinder {

	/**
	 * 从start开始向上下搜索，找出与name相符的项，找不到则返回start.
	 */
	public static int find(FileItem[] files, String desiredName, int desiredFileType, int start) {

		int desiredIndex = start;

		// 从指定的位置开始渐次向两边搜索，很可能在指定位置就找到了。若没找到，可能是添加或删除了文件，也可能在最近的几次搜索中就找到。
		// **** 搜索开始 ****//

		int min = 0;
		int max = files.length - 1;

		if (min > max || desiredName == null) {
			// invalid value
			return desiredIndex;
		}

		boolean reachTop = false;
		boolean reachEnd = false;

		if (start < min || start > max) {
			start = (min + max) / 2;
		}

		int smaller = start;
		int bigger = start + 1;

		for (;;) {

			int[] processQueue = new int[2];
			int processLength = 0;

			if (!reachTop && smaller >= min) {
				processQueue[processLength] = smaller;
				processLength++;
				smaller--;
			} else {
				reachTop = true;
			}

			if (!reachEnd && bigger <= max) {
				processQueue[processLength] = bigger;
				processLength++;
				bigger++;
			} else {
				reachEnd = true;
			}

			boolean find = false;

			for (int i = 0; i < processLength; i++) {

				int index = processQueue[i];
				FileItem thisItem = files[index];

				if (desiredFileType != FileItem.TYPE_UNKNOWN && thisItem.getType() != desiredFileType) {
					continue;
				}

				// 这个文件夹或文件的名字。
				String realName = thisItem.getDisplayName();

				if (realName.equals(desiredName)) {
					desiredIndex = index;
					find = true;
					break;
				}

			}

			if (find || reachTop && reachEnd) {
				break;
			}

		}

		return desiredIndex;

	}

}
