
package RockManager.util;

import java.util.Vector;
import net.rim.device.api.ui.Font;
import net.rim.device.api.util.CharacterUtilities;


/**
 * 将一段较长的文字按显示宽度分成数行。
 */

public class SplitedText {

	private Vector textVector;

	private int totalLineCount;


	public SplitedText(String text, int displayWidth, Font textFont) {

		textVector = new Vector();
		totalLineCount = 1;

		char[] allChar = text.trim().toCharArray();
		int lineWidth = 0;
		int start = 0;

		for (int i = 0; i < allChar.length; i++) {

			char thisChar = allChar[i];
			int charWidth = textFont.getAdvance(thisChar);
			lineWidth += charWidth;

			if (lineWidth > displayWidth) {

				// 该算为另起一行，但，如果是空格就不再添加到下一行的开头，
				// 如果是字母尝试找到前一个适合位置（非字母或空格）
				// 使用 islower||isupper 检测是否是字母

				totalLineCount++;

				if (CharacterUtilities.isSpaceChar(thisChar)) {

					// good{ }morning -> good{}morning
					textVector.addElement(String.valueOf(allChar, start, i - start));
					start = i + 1;
					lineWidth = 0;

					// 天下{ } 无双 -> 天下{}无双
					// 若下一行行首有空格，则去除
					while (CharacterUtilities.isSpaceChar(allChar[start])) {
						start++;
						i++;
					}

				} else {

					if (UtilCommon.isAlphabet(thisChar) && UtilCommon.isAlphabet(allChar[i - 1])) {

						int j = i - 2;
						int limit = 13;

						boolean find = false;

						// 试图寻找这个单词与前面部分的分割点(非字母处)作为分割之处。
						// 但是搜索时须有一个限度，避免一行的字母过少，例如分割fiddler2gggggggggggggggggggggggggggggggggggggggggggg时。
						while (limit > 0 && j > start && UtilCommon.isAlphabet(allChar[j])) {
							j--;
							limit--;
						}

						if (j > start && limit > 0) {
							find = true;
						}

						if (find) {
							// j:他{的}fans
							// j:good{ }morning
							j++; // j+1, 将这个不是字母的放到第一行
							textVector.addElement(String.valueOf(allChar, start, j - start));
							start = j;
							// i在for循环里马上就要加1，所以此处先减1
							i = start - 1;
							lineWidth = 0;
						} else {

							// go{o}d -> g-{}ood
							// 增添分割号的方式。
							// textVector
							// .addElement(String.valueOf(allChar, start, i -
							// start - 1) + '-');
							// start = i - 1;

							// go{o}d -> go{}od
							// 不增添分隔号的方式。
							textVector.addElement(String.valueOf(allChar, start, i - start));
							start = i;

							i = start - 1;
							lineWidth = 0;
						}

					} else {

						// 天气{不}错
						textVector.addElement(String.valueOf(allChar, start, i - start));
						start = i;
						lineWidth = charWidth;

					}
				}

			}

		}

		textVector.addElement(String.valueOf(allChar, start, allChar.length - start));

	}


	/**
	 * 获取总行数。
	 * 
	 * @return
	 */

	public int getLineCount() {

		return totalLineCount;
	}


	/**
	 * 返回已分好行的Strings.
	 * 
	 * @return
	 */
	public String[] getTextStrings() {

		String[] texts = new String[totalLineCount];

		for (int i = 0; i < textVector.size(); i++) {
			texts[i] = (String) textVector.elementAt(i);
		}

		return texts;

	}


	/**
	 * 返回已分好行的Vector。
	 * 
	 * @return
	 */
	public Vector getTextVector() {

		return textVector;
	}

}
