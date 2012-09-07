
package RockManager.fileList;

import net.rim.device.api.ui.Keypad;
import RockManager.config.ShortCutKeyConfig;
import RockManager.favoritesList.FavoritesData;
import RockManager.util.KeyUtil;


/**
 * 处理按键操作。
 */
public class FileListKeyCharHandler {

	public static boolean keyChar(char key, int status, int time, FileListField fileList) {

		if (key == Keypad.KEY_ESCAPE) {

			if (fileList.getKeyword().length() > 0) {
				// 正在搜索，清除搜索条件。

				if (fileList.isFocused()) {
					// 输入框没有获得焦点，记录当前文件列表高亮项的位置。
					fileList.getSearchBox().hideClearIcon();
					FileItem focusedItem = fileList.getThisItem();
					if (focusedItem != null) {
						fileList.setItemToFocus(focusedItem.getDisplayName(), focusedItem.getType());
					}
				}

				fileList.setKeyword("");
				return true;
			}

			if (fileList.isMultiSelecting()) {
				// 正在多选，退出多选模式。
				fileList.leaveMultiSelectMode();
				return true;
			}

			// 判断按escape是返回上级目录还是关闭screen(交给上级处理)
			// 同样可根据刚才是否添加了返回项判断。
			if (fileList.shouldHaveReturn()) {
				fileList.doReturnToParent();
				if (fileList.isFocused() == false) {
					// 若刚才焦点在搜索框，使列表重新获得焦点。
					fileList.setFocus();
				}
				return true;
			}

		} else if (KeyUtil.isOnSameKey(key, status, ShortCutKeyConfig.SEARCH)) {

			if (fileList.isSearchable()) {
				fileList.getKeywordField().setFocus();
				return true;
			}

		}

		// 这个快捷键默认是‘r’,在某些机型上可能与搜索快捷键‘s’在一个按键上（如9105），因此不用else if而用if.
		if (KeyUtil.isOnSameKey(key, status, ShortCutKeyConfig.RENAME) && fileList.isRealFileItem()
				&& fileList.isNormalFolder()) {

			fileList.renameFile();
			return true;

		}

		// 显示属性。
		if (KeyUtil.isOnSameKey(key, status, ShortCutKeyConfig.SHOW_PROPERTY) && fileList.shouldShowProperty()) {

			fileList.showProperty();
			return true;

		}

		if ((key == Keypad.KEY_BACKSPACE || key == Keypad.KEY_DELETE) && fileList.isRealFileItem()) {
			if (fileList.isNormalFolder()) {
				// 删除文件。
				fileList.deleteFile();
				return true;
			} else if (fileList.isFavoriteList()) {
				// 删除一收藏条目。
				FavoritesData.delete(fileList.getThisItem());
				return true;
			}
		}

		// 复制
		if (fileList.isClipboardAllowed() && KeyUtil.isOnSameKey(key, status, ShortCutKeyConfig.COPY)
				&& fileList.isRealFileItem()) {

			fileList.copyToClipboard();
			return true;

		}

		// 剪切
		if (fileList.isClipboardAllowed() && KeyUtil.isOnSameKey(key, status, ShortCutKeyConfig.CUT)
				&& fileList.isRealFileItem()) {

			fileList.cutToClipboard();
			return true;

		}

		// 粘贴
		if (fileList.isClipboardAllowed() && KeyUtil.isOnSameKey(key, status, ShortCutKeyConfig.PASTE)) {

			fileList.pasteFromClipboard();
			return true;

		}

		// 多选模式
		if (KeyUtil.isOnSameKey(key, status, ShortCutKeyConfig.MULTI_SELECT_MODE)) {

			if (fileList.canMultiSelect()) {
				if (fileList.isMultiSelecting()) {
					// 正在多选，退出多选模式。
					fileList.leaveMultiSelectMode();
				} else {
					// 进入多选模式
					fileList.enterMultiSelectMode();
				}
				return true;
			}

		}

		return false;

	}
}
