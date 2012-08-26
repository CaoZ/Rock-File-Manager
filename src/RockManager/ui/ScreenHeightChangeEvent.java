package RockManager.ui;

public interface ScreenHeightChangeEvent {

	public static final int SCREEN_HEIGHT_CHANGED = "SCREEN_HEIGHT_CHANGED".hashCode();
	/**
	 * 有些情况下高度不变时反而要通知，比如在FileListField(KeywordFilterField)中，
	 * 切换目录时若高度变化了会自动产生FieldChangeNotify事件，而切换目录若高度不变则需手动触发一个，以便重新绘制滚动条。
	 */
	public static final int SCREEN_HEIGHT_NOT_CHANGED = "SCREEN_HEIGHT_NOT_CHANGED".hashCode();

	public void screenHeightChangeNotify(int context);

}
