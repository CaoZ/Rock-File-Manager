
package RockManager.favoritesList;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.XYRect;
import net.rim.device.api.ui.component.Dialog;
import RockManager.fileList.FileItem;
import RockManager.fileList.FileListField;
import RockManager.languages.LangRes;
import RockManager.ui.MyUI;
import RockManager.ui.screen.fileScreen.FileScreen;
import RockManager.util.UtilCommon;
import RockManager.util.ui.BaseDialog;


public class FavoritesListField extends FileListField implements FavoritesChangedListener {

	private Bitmap backWhenEmpty;

	private int originRowHeight;


	public FavoritesListField() {

		originRowHeight = getRowHeight();

		setSearchable(false);
		setClipboardAllowed(false);
		listFavorites();

	}


	private void listFavorites() {

		FileItem[] items = listFiles();
		set(items);
	}


	private boolean testIfExist() {

		FileItem thisItem = getThisItem();

		boolean exists = thisItem.exists();

		if (exists == false) {

			String deleteConfirmAsk = LangRes.get(LangRes.FAVORITE_OLD_DELETE_ASK);
			String message = UtilCommon.replaceString(deleteConfirmAsk, "{1}", thisItem.getDisplayName());
			Bitmap bitmap = Bitmap.getPredefinedBitmap(Bitmap.QUESTION);

			BaseDialog deleteConfirm = new BaseDialog(Dialog.D_YES_NO, message, Dialog.YES, bitmap, 0);

			int answer = deleteConfirm.doModal();

			if (answer == Dialog.YES) {
				FavoritesData.delete(thisItem);
			}

		}

		return exists;

	}


	protected void doOpenThisFile() {

		boolean exists = testIfExist();

		if (exists == false) {
			return;
		}

		super.doOpenThisFile();
	}


	protected void doEnterThisDir() {

		boolean exists = testIfExist();

		if (exists == false) {
			return;
		}

		FileItem thisItem = getThisItem();

		String folderURL = thisItem.getURL();
		FileScreen fileScreen = new FileScreen(folderURL);
		fileScreen.setIcon(thisItem.getIcon());
		UiApplication.getUiApplication().pushScreen(fileScreen);

	}


	public void refresh() {

		int selectedInex = getSelectedIndex();
		listFavorites();
		setSelectedIndex(selectedInex);
	}


	protected FileItem[] listFiles() {

		return FavoritesData.listFiles();
	}


	public void favoritesChanged() {

		refresh();
	}


	public boolean isFocusable() {

		if (isEmpty()) {
			return false;
		} else {
			return true;
		}

	}


	protected void onDisplay() {

		if (isVisible()) {
			// 如果是第一次显示，isVisible()会是false, 无需刷新数据，因为刚刚在构造函数中列出数据了。
			refresh();
		}
		FavoritesData.addChangeListener(this);
		super.onDisplay();
	}


	protected void onUndisplay() {

		FavoritesData.removeChangeListener(this);
		super.onUndisplay();
	}


	protected void paint(Graphics g) {

		if (isEmpty()) {
			drawEmptyIndicator(g);
		} else {
			super.paint(g);
		}

	}


	public void set(Object[] list) {

		if (list.length == 0) {
			// 当为空时绘制时使高度变为42，使TitledPaned内部高度加起来是52.
			// 与TitledPaned因无内容绘制EmptyIndicator时高度一致。
			setRowHeight(MyUI.deriveSize(42));
		} else {
			setRowHeight(originRowHeight);
		}

		super.set(list);

	}


	private void drawEmptyIndicator(Graphics g) {

		if (backWhenEmpty == null) {
			Bitmap back = Bitmap.getBitmapResource("img/titledPanel/blackBack.png");
			backWhenEmpty = MyUI.deriveImg(back);
		}

		// 绘制背景
		int backTotalWidth = (int) (getWidth() * 0.95);
		int backHalfWidth = backWhenEmpty.getWidth() / 2;

		int blackOffsetX = UtilCommon.getOffset(getWidth(), backTotalWidth);
		int blackOffsetY = UtilCommon.getOffset(getHeight(), backWhenEmpty.getHeight());

		XYRect dest = new XYRect(blackOffsetX, blackOffsetY, backHalfWidth, backWhenEmpty.getHeight());
		g.setGlobalAlpha((int) (255 * 0.6));
		g.drawBitmap(dest, backWhenEmpty, 0, 0);

		g.setColor(0);
		g.fillRect(dest.x + dest.width, blackOffsetY, backTotalWidth - backWhenEmpty.getWidth(),
				backWhenEmpty.getHeight());

		dest.x = dest.x + backTotalWidth - backHalfWidth;
		g.drawBitmap(dest, backWhenEmpty, backHalfWidth, 0);

		// 绘制文字
		g.setGlobalAlpha(250);
		g.setColor(Color.WHITE);
		String text = "Empty";
		int offsetX = UtilCommon.getOffset(getWidth(), getFont().getBounds(text));
		int offsetY = UtilCommon.getOffset(getHeight(), getFont().getHeight());
		g.drawText(text, offsetX, offsetY);

	}

}
