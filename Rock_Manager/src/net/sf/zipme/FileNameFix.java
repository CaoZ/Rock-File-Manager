
package net.sf.zipme;

import java.io.UnsupportedEncodingException;


public class FileNameFix {

	private static String GBK = "GBK";

	private static String GB2312 = "GB2312";

	private static String UTF8 = "UTF-8";

	private static boolean supportGBK = true;

	private static boolean supportGB2312 = true;

	static {
		// 判断是否支持指定的字符编码，没找到API,只能这样判断了，囧。
		// 黑莓默认支持UTF-8,但似乎支持GBK等需安装额外的语言支持包，也就是说，美国的黑莓不可能支持GBK等，东亚版的支持。
		// 根据以前的此时，黑莓5不支持GBK,支持GB2312,黑莓6支持GBK.
		// 黑莓默认的编码可能是ISO-8859-1
		//
		// 若写new byte[0],有（极小）可能返回空字符串（保不齐程序判断数组长度是0，不解码直接返回空字符串了）
		byte[] bytes = new byte[2];
		try {
			new String(bytes, GBK);
		} catch (UnsupportedEncodingException e) {
			supportGBK = false;
		}
		try {
			new String(bytes, GB2312);
		} catch (UnsupportedEncodingException e) {
			supportGB2312 = false;
		}
	}


	/**
	 * 判断是否使用UTF-8编码的。
	 * 
	 * @param flags
	 * @return
	 */
	public static boolean isUseUTF8(int flags) {

		// 按照标准，正常应是在11位（从0开始的第12位）上判断，但整个lib的高位与低位都是反过来的，所以和0x08相与。
		return (flags & 0x08) > 0;
	}


	/**
	 * 根据flags的指示，使用对应的编码方式解码。
	 * 
	 * @param bytes
	 * @param flags
	 * @return
	 */
	public static String getString(byte[] bytes, int flags) {

		String str = null;

		if (isUseUTF8(flags)) {
			// 使用了UTF-8.
			try {
				str = new String(bytes, UTF8);
			} catch (UnsupportedEncodingException e) {
				str = new String(bytes);
			}
		} else {
			// 使用了本地区编码，在国内大部分是GBK.
			try {
				if (supportGBK) {
					str = new String(bytes, GBK);
				} else if (supportGB2312) {
					str = new String(bytes, GB2312);
				} else {
					str = new String(bytes, UTF8);
				}
			} catch (UnsupportedEncodingException e) {
				str = new String(bytes);
			}
		}

		return str;

	}


	// CZTODO 若以gbk的bytes当做文件名，需更改zipentry的flags。
	public static byte[] getGBKBytes(String name) {

		byte[] bytes = null;
		try {
			if (supportGBK) {
				bytes = name.getBytes(GBK);
			} else if (supportGB2312) {
				bytes = name.getBytes(GB2312);
			} else {
				bytes = name.getBytes(UTF8);
			}
		} catch (UnsupportedEncodingException e) {
			bytes = name.getBytes();
		}
		return bytes;
	}

}
