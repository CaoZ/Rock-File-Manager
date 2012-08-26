
package RockManager.ui.titledPanel;

import RockManager.ui.ScreenHeightChangeEvent;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.XYEdges;
import net.rim.device.api.ui.container.VerticalFieldManager;
import net.rim.device.api.ui.decor.Border;
import net.rim.device.api.ui.decor.BorderFactory;


public class TitledPanel extends VerticalFieldManager implements FieldChangeListener, ScreenHeightChangeEvent {

	private int width;

	private static Border BORDER;

	private VerticalFieldManager vfm;

	private TitleBar titleBar;

	private XYEdges paddings;

	static {
		XYEdges edges = new XYEdges(7, 6, 7, 6);
		Bitmap borderImage = Bitmap.getBitmapResource("img/titledPanel/border.png");
		BORDER = BorderFactory.createBitmapBorder(edges, borderImage);
	}


	public TitledPanel(String title) {

		this(0, title, 0);
	}


	public TitledPanel(long style, String title, int width) {

		super(style);
		setWidth(width);
		titleBar = new TitleBar(title);
		titleBar.setChangeListener(this);
		super.add(titleBar);
		setBorder(BORDER);
		// 整个panel与父容器的margin
		// 当设置为2,4,4,4时，单击收起时有抖动的现象，而其它值时不会，bug
		setMargin(3, 4, 2, 4);
	}


	public void setIcon(Bitmap icon) {

		titleBar.setIcon(icon);
	}


	public void setWidth(int width) {

		this.width = width;
	}


	public void add(Field field) {

		titleBar.setStatusExpanded();
		if (vfm == null) {
			// 之所以在此处添加vfm，是让没内容时下部没setPadding造成的白边。
			addVFM();
		}
		vfm.add(field);
	}


	private void addVFM() {

		vfm = new VerticalFieldManager(Field.USE_ALL_WIDTH);
		// 只有上边框
		XYEdges edges = new XYEdges(1, 0, 0, 0);
		int borderColor = 0xc2c2c2;
		XYEdges colors = new XYEdges(borderColor, 0, 0, 0);
		Border border = BorderFactory.createSimpleBorder(edges, colors, Border.STYLE_SOLID);
		vfm.setBorder(border);
		if (paddings != null) {
			setPadding(paddings);
		}
		super.add(vfm);
	}


	/**
	 * 为vfm设置padding,如果此时vfm不存在则会将padding的值存储，等vfm创建时在给vfm赋值。
	 */
	public void setPadding(int top, int right, int bottom, int left) {

		if (vfm == null) {
			paddings = new XYEdges(top, right, bottom, left);
		} else {
			vfm.setPadding(top, right, bottom, left);
		}

	}


	/**
	 * 为vfm设置padding,如果此时vfm不存在则会将padding的值存储，等vfm创建时在给vfm赋值。
	 */
	public void setPadding(XYEdges paddings) {

		if (vfm == null) {
			this.paddings = paddings;
		} else {
			vfm.setPadding(paddings);
		}
	}


	/**
	 * 收缩。
	 */
	public void setStatusShrinked() {

		titleBar.setStatusShrinked();
		delete(vfm);
		// updateLayout, not work
		// updateLayout();
		screenHeightChangeNotify(SCREEN_HEIGHT_CHANGED);
	}


	public void delete(Field field) {

		if (field != null && field.getManager() == this) {
			super.delete(field);
		}
	}


	public int getPreferredWidth() {

		if (width > 0) {
			int margin = getMarginLeft() + getMarginRight();
			return width + margin;
		} else {
			return super.getPreferredWidth();
		}
	}


	public void toggleStatus() {

		boolean expanded = titleBar.toggleStatus();
		if (expanded) {
			// 状态已改为expanded，添加vfm。
			if (vfm == null) {
				// 没有内容，添加空白指示。
				setPadding(9, 5, 9, 5);
				add(new EmptyIndicator());
			} else {
				super.add(vfm);
			}
		} else {
			// 该变为收缩状态
			delete(vfm);
		}
		// updateLayout, not work
		// updateLayout();
		screenHeightChangeNotify(SCREEN_HEIGHT_CHANGED);
	}


	public void fieldChanged(Field field, int context) {

		if (field == titleBar) {
			toggleStatus();
		}
	}


	public void screenHeightChangeNotify(int context) {

		fieldChangeNotify(context);
	}

}