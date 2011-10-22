
package RockManager.fileHandler;

import net.rim.device.api.util.MathUtilities;


/**
 * 处理文件大小转换为字符串。
 */
public class FileSizeString {

	private static int SIZE_B = 1;

	private static int SIZE_1000B = 1000 * SIZE_B;

	private static int SIZE_KB = 1024 * SIZE_B;

	private static int SIZE_1000KB = 1000 * SIZE_KB;

	private static int SIZE_MB = 1024 * SIZE_KB;

	private static int SIZE_1000MB = 1000 * SIZE_MB;

	private static int SIZE_GB = 1024 * SIZE_MB;


	/**
	 * 获取文件大小的字符串形式。例如 "123 KB"
	 * 
	 * @param size
	 *            文件大小，字节
	 * @param KBonly
	 *            是否只以KB作单位
	 * @param showDot
	 *            是否允许显示小数
	 * @return
	 */
	public static String getSizeString(long size, boolean KBonly, boolean showDot) {

		// 关于文件大小的单位：若大于1000MB, 使用GB
		// 若大于1000KB, 使用MB
		// 否则使用KB.

		// 关于小数：若整数位大于等于3位, 即>=100, 不显示小数，如900 KB
		// 若整数位为2位，即10-99，显示一位小数，如86.5 KB
		// 若整数位为1为，即0-9，显示二位小数，如1.23 GB

		// 若是显示硬盘可用空间，可使用 MB, 显示小数。
		// 若是列表中显示文件大小，1000MB以下使用KB, 不显示小数, 否则使用GB, 显示小数。

		String unit; // 单位
		int multiple; // 比率
		int decimalCount = 0; // 小数位数

		if (!KBonly && size < SIZE_1000B) {
			unit = " B";
			multiple = SIZE_B;
		} else if (KBonly || size < SIZE_1000KB) {
			unit = " KB";
			multiple = SIZE_KB;
		} else if (size < SIZE_1000MB) {
			unit = " MB";
			multiple = SIZE_MB;
		} else {
			unit = " GB";
			multiple = SIZE_GB;
		}

		// 整数部分
		int numberInteger = (int) (size / multiple);
		// 小数部分的整数值，整数部分的余数
		int partDecimal = (int) (size % multiple);
		// 小数部分
		int numberDecimal = 0;

		if (showDot) {
			decimalCount = getDecimalCount(numberInteger, multiple);
		}

		if (decimalCount > 0) {
			// 显示小数
			int decimalMultiple = (int) MathUtilities.pow(10, decimalCount);
			numberDecimal = MathUtilities.round(partDecimal / (float) multiple * decimalMultiple);
			if (Integer.toString(numberDecimal).length() > decimalCount) {
				// 得到100，但最大为99，应向整数位进一位。如：9.999MB不应表示为9.99MB, 而是10.0MB.
				numberInteger++;
				numberDecimal = 0;
				// 重新计算所需的小数位数。9.99 -> 10.0
				decimalCount = getDecimalCount(numberInteger, multiple);
			}
		} else {
			// 不显示小数
			if (partDecimal > 0) {
				numberInteger++;
			}
		}

		StringBuffer sb = getCommaFormSB(numberInteger);

		// 添加小数点及后面部分和单位
		if (decimalCount > 0) {
			sb.append('.');
			int zeroPaddingCount = decimalCount - Integer.toString(numberDecimal).length();
			for (int i = 0; i < zeroPaddingCount; i++) {
				// 若小数部分位数不足，进行填充。
				sb.append('0');
			}
			sb.append(numberDecimal);
		}
		sb.append(unit);

		return sb.toString();

	}


	/**
	 * 计算给定的数显示时所需的小数位数。
	 * 
	 * @param number
	 * @param multiple
	 * @return
	 */
	private static int getDecimalCount(int number, int multiple) {

		// 目标形式：1.35, 9.23, 18.2, 99.6 , 100, 900

		if (multiple == SIZE_B) {
			return 0;
		}

		if (number >= 0 && number <= 9) {
			return 2;
		} else if (number >= 10 && number <= 99) {
			return 1;
		} else {
			return 0;
		}
	}


	/**
	 * 获取添加了分隔符形式的数字的字符串。
	 * 
	 * @param number
	 * @return
	 */
	public static String getCommaFormString(long number) {

		return getCommaFormSB(number).toString();

	}


	/**
	 * 获取添加了分隔符形式的数字的StringBuffer。
	 * 
	 * @param number
	 * @return
	 */
	private static StringBuffer getCommaFormSB(long number) {

		StringBuffer sb = new StringBuffer(Long.toString(number));
		addComma(sb);
		return sb;

	}


	/**
	 * 添加分隔符。
	 * 
	 * @param integerString
	 *            一串数字的字符串形式。
	 * @return
	 */
	private static StringBuffer addComma(StringBuffer integerString) {

		// 需添加分隔符的个数
		int commaCount = (integerString.length() - 1) / 3;

		for (int i = 1; i <= commaCount; i++) {
			// 添加分隔符
			int position = integerString.length() - (3 * i + i - 1);
			integerString.insert(position, ',');
		}

		return integerString;

	}

}
