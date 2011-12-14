
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

		int backImgWidth = backWhenEmpty.getWidth();
		int backImgHeight = backWhenEmpty.getHeight();

		int backImgHalfWidth = (int) Math.ceil(backImgWidth / 2f);

		int backPaddingX = UtilCommon.getOffset(getWidth(), (int) (getWidth() * 0.95));
		// 取到padding后再计算totalWidth, 因为总宽度减去2个padding后不一定等于总宽度的95%（奇偶等原因）。
		int backTotalWidth = getWidth() - backPaddingX * 2;
		int backOffsetY = UtilCommon.getOffset(getHeight(), backImgHeight);

		// 开始绘制
		g.setGlobalAlpha((int) (255 * 0.6));
		// 图片左侧
		XYRect backImgLeftRect = new XYRect(backPaddingX, backOffsetY, backImgHalfWidth, backImgHeight);
		g.drawBitmap(backImgLeftRect, backWhenEmpty, 0, 0);

		// 图片右侧
		XYRect backImgRightRect = new XYRect(backImgLeftRect);
		backImgRightRect.x = getWidth() - backPaddingX - backImgHalfWidth;
		g.drawBitmap(backImgRightRect, backWhenEmpty, backImgWidth - backImgHalfWidth, 0);

		// 黑色填充
		XYRect blackRect = new XYRect(backImgLeftRect);
		blackRect.x = backImgLeftRect.x + backImgLeftRect.width;
		blackRect.width = backTotalWidth - backImgLeftRect.width - backImgRightRect.width;

		g.setColor(0);
		g.fillRect(blackRect.x, blackRect.y, blackRect.width, blackRect.height);

		// 绘制文字
		g.setGlobalAlpha(250);
		g.setColor(Color.WHITE);
		String text = "Empty";
		int offsetX = UtilCommon.getOffset(getWidth(), getFont().getAdvance(text));
		int offsetY = UtilCommon.getOffset(getHeight(), getFont().getHeight());
		g.drawText(text, offsetX, offsetY);

	}

}
