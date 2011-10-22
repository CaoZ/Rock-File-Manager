
package RockManager.fileHandler.fileCounter;

import RockManager.languages.LangRes;
import RockManager.util.UtilCommon;


public class FileCountResult {

	private int fileNumber;

	private int folderNumber;


	public FileCountResult() {

		// empty
	}


	public FileCountResult(int fileNumber, int folderNumber) {

		this.fileNumber = fileNumber;
		this.folderNumber = folderNumber;
	}


	public void plusFileNumber() {

		fileNumber++;

	}


	public void plusFolderNumber() {

		folderNumber++;

	}


	public void appendResult(FileCountResult countResult) {

		fileNumber += countResult.fileNumber;
		folderNumber += countResult.folderNumber;

	}


	/**
	 * 返回总项数，文件数与文件夹数之和。
	 * 
	 * @return
	 */
	public int getTotalNumber() {

		return fileNumber + folderNumber;
	}


	public String toString() {

		String resultString = LangRes.getString(LangRes.FILE_COUNT_RESULT);
		resultString = UtilCommon.replaceString(resultString, "{1}", Integer.toString(fileNumber));
		resultString = UtilCommon.replaceString(resultString, "{2}", Integer.toString(folderNumber));
		return resultString;
	}

}
