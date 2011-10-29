
package RockManager.fileList;

import net.rim.device.api.ui.Keypad;
import RockManager.config.Config;
import RockManager.favouritesList.FavouritesData;
import RockManager.util.KeyUtil;


public class FileListKeyCharHandler {

	public static boolean keyChar(char key, int status, int time, FileListField fileList) {

		if (key == Keypad.KEY_ESCAPE) {

			if (fileList.getKeyword().length() > 0) {
				// 清除搜索条件。
				fileList.setKeyword("");
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

		} else if (KeyUtil.isOnSameKey(key, status, Config.SHORTCUT_KEY_SEARCH)) {

			if (fileList.isSearchable()) {
				fileList.getKeywordField().setFocus();
				return true;
			}

		}

		// 这个快捷键默认是‘r’,在某些机型上可能与搜索快捷键‘s’在一个按键上（如9105），因此不用else if而用if.
		if (KeyUtil.isOnSameKey(key, status, Config.SHORTCUT_KEY_RENAME) && fileList.isRealFileItem()
				&& fileList.isNormalFolder()) {

			fileList.renameFile();
			return true;

		}

		// 显示属性。
		if (KeyUtil.isOnSameKey(key, status, Config.SHORTCUT_KEY_SHOW_PROPERTY) && fileList.shouldShowProperty()) {

			fileList.showProperty();
			return true;

		}

		if ((key == Keypad.KEY_BACKSPACE || key == Keypad.KEY_DELETE) && fileList.isRealFileItem()) {
			if (fileList.isNormalFolder()) {
				// 删除文件。
				fileList.deleteFile();
				return true;
			} else if (fileList.isFavouriteList()) {
				// 删除一收藏条目。
				FavouritesData.delete(fileList.getThisItem());
				return true;
			}
		}

		// 复制
		if (fileList.isClipboardAllowed() && KeyUtil.isOnSameKey(key, status, Config.SHORTCUT_KEY_COPY)
				&& fileList.isRealFileItem()) {

			fileList.copyToClipboard();
			return true;

		}

		// 剪切
		if (fileList.isClipboardAllowed() && KeyUtil.isOnSameKey(key, status, Config.SHORTCUT_KEY_CUT)
				&& fileList.isRealFileItem()) {

			fileList.cutToClipboard();
			return true;

		}

		// 粘贴
		if (fileList.isClipboardAllowed() && KeyUtil.isOnSameKey(key, status, Config.SHORTCUT_KEY_PASTE)) {

			fileList.pasteFromClipboard();
			return true;

		}

		return false;

	}

}
