
package RockManager.fileList;

import net.rim.device.api.util.Comparator;
import RockManager.util.UtilCommon;


/**
 * 用于文件名排序 , 排序原则：目录在前，文件在后
 */
public class FileNameComparator implements Comparator {

	public int compare(Object o1, Object o2) {

		FileItem f1 = (FileItem) o1;
		FileItem f2 = (FileItem) o2;
		String s1 = f1.getDisplayName();
		String s2 = f2.getDisplayName();
		boolean d1 = f1.isDir();
		boolean d2 = f2.isDir();
		if ((d1 && d2) || (!d1 && !d2))
			// 改为具有数字检测比较功能的比较函数
			return compareFullFileName(s1, s2);
		else if (d1 && !d2)
			return -1;
		else
			return 1;
	}


	/**
	 * 比较文件名。
	 * 
	 * @param s1
	 * @param s2
	 * @return
	 */
	private int compareFullFileName(String s1, String s2) {
		
		// TODO: 若是文件名已 . 开头, 则这种排序会忽略点. 比如  a, .b, c, .b 那项应在最前, 而现在 .b 那项在中间了. 

		String[] s1Parts = UtilCommon.splitString(s1, ".");
		String[] s2Parts = UtilCommon.splitString(s2, ".");

		for (int i = 0; i < s1Parts.length && i < s2Parts.length; i++) {
			int result = compareFileName(s1Parts[i], s2Parts[i]);
			if (result != 0) {
				// 已比较出结果。
				return result;
			}
		}

		// 其中一个已到达末尾，且是另一个的前缀(前面部分都相同)，它应排在前面。
		return s1Parts.length - s2Parts.length;

	}


	/**
	 * 支持含数字的比较形式，例如2会排在10后面。<br>
	 * 注:虽然都是分析整数部分的数值，但为了避免数值超出范围，应使用Float.parseFloat.
	 * 
	 * @param s1
	 * @param s2
	 * @return
	 */
	private int compareFileName(String s1, String s2) {

		int l1 = s1.length();
		int l2 = s2.length();
		boolean bothDigit = false;
		int digitCount = 0;
		for (int i = 0; i < l1 && i < l2; i++) {
			char c1 = s1.charAt(i);
			char c2 = s2.charAt(i);
			boolean digit1 = isDigitalNumber(c1);
			boolean digit2 = isDigitalNumber(c2);
			if (digit1 && digit2) {
				// 都是数字，先不比较
				digitCount++;
				bothDigit = true;
				continue;
			} else {
				if (bothDigit) {
					// 在这之前是，现在可能不是了，并且以前的字符都相等
					float number1, number2;
					if (digit1) {
						// 仍是数字
						number1 = readFloarStartLeastLength(s1, i - digitCount, digitCount);
					} else {
						// 不是数字了
						number1 = readFloatStartEnd(s1, i - digitCount, i);
					}
					if (digit2) {
						// 仍是数字
						number2 = readFloarStartLeastLength(s2, i - digitCount, digitCount);
					} else {
						// 不是数字了
						number2 = readFloatStartEnd(s2, i - digitCount, i);
					}
					if (number1 == number2) {
						// 相同
						// 重置数据
						bothDigit = false;
						digitCount = 0;
						// 继续比较下一字符
						continue;
					} else {
						// 数字不同，根据数字的值比较
						return (int) (number1 - number2);
					}

				} else {
					// 一直不是数字
					// 都转为大写形式, 否则B会排在a的前面。
					c1 = Character.toUpperCase(c1);
					c2 = Character.toUpperCase(c2);
					if (c1 == c2) {
						// 两字符相同，继续比较下一字符
						continue;
					} else {
						return c1 - c2;
					}

				}
			}
		}
		// 已有的都比完了，且都相同或还是数字
		// 如：1、2；00001、2；23BCD、13
		if (bothDigit) {
			int start = Math.min(l1, l2) - digitCount;
			float number1 = readFloarStartLeastLength(s1, start, digitCount);
			float number2 = readFloarStartLeastLength(s2, start, digitCount);
			if (number1 == number2) {
				// 数字相同，根据文件名长度比较。
				return l1 - l2;
			} else {
				return (int) (number1 - number2);
			}
		} else {
			return l1 - l2;
		}

	}


	/**
	 * 从给定的开始位置到结束位置读取数值。
	 * 
	 * @param s
	 * @param start
	 * @param end
	 * @return
	 */
	private float readFloatStartEnd(String s, int start, int end) {

		String digitS = s.substring(start, end);
		return Float.parseFloat(digitS);
	}


	/**
	 * 从给定的开始位置读取数值，直到字符串结束或数值部分结束。
	 * 
	 * @param s
	 * @param start
	 * @param leastLength
	 * @return
	 */
	private float readFloarStartLeastLength(String s, int start, int leastLength) {

		int end = start + leastLength;
		for (; end < s.length(); end++) {
			char c = s.charAt(end);
			// 不能使用Character.isDigit(char), 例如对于罗马数字'Ⅻ', 它会返回true,
			// 但在Integer.parseInt(String)中却分析不出来，这应是一个bug, 在Java SE上没有此问题。
			if (isDigitalNumber(c)) {
				continue;
			} else {
				break;
			}
		}
		return readFloatStartEnd(s, start, end);
	}


	/**
	 * 判断是否是罗马型数字
	 * 
	 * @param c
	 * @return
	 */
	private boolean isDigitalNumber(char c) {

		return (c >= '0' && c <= '9') || (c >= '０' && c <= '９');
	}
}
