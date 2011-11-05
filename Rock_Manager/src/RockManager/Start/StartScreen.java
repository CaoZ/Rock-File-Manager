
package RockManager.Start;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.component.StandardTitleBar;
import net.rim.device.api.ui.decor.BackgroundFactory;
import RockManager.config.Config;
import RockManager.config.OptionsScreen;
import RockManager.favouritesList.FavouritesListField;
import RockManager.fileList.FileListField;
import RockManager.languages.LangRes;
import RockManager.stat.Stat;
import RockManager.ui.ScreenHeightChangeEvent;
import RockManager.ui.ScreenHeightChangeListener;
import RockManager.ui.screen.informScreen.AboutScreen;
import RockManager.ui.screen.informScreen.KeyboardShortcutsHelpScreen;
import RockManager.ui.titledPanel.TitledPanel;
import RockManager.util.CapabilityUtil;
import RockManager.util.ui.AnimatedMainScreen;
import RockManager.util.ui.VFMwithScrollbar;


public class StartScreen extends AnimatedMainScreen implements ScreenHeightChangeListener {

	private VFMwithScrollbar vfm;

	private FileListField diskList;


	public StartScreen() {

		super(NO_VERTICAL_SCROLL | NO_SYSTEM_MENU_ITEMS);

		String title = "Rock File Manager";
		if (Config.DEBUG_MODE) {
			title += " [DEBUG]";
		}

		StandardTitleBar titleBar = new StandardTitleBar();
		titleBar.addTitle(title);
		titleBar.addNotifications();
		titleBar.addSignalIndicator();
		setTitle(titleBar);

		// MainManager也设置背景色，保证拖动时不会露出白色（在触摸屏机型上即使下面没内容了还可以向下拉，类似弹性缓冲效果）。
		getMainManager().setBackground(BackgroundFactory.createSolidBackground(0xf7f7f7));

		vfm = new VFMwithScrollbar(USE_ALL_WIDTH);
		vfm.setBackground(BackgroundFactory.createSolidBackground(0xf7f7f7));

		addDeviceItem();
		addFavouriteItem();

		add(vfm);

		UiApplication.getUiApplication().invokeLater(new Runnable() {

			public void run() {

				new Stat();

			}
		});

	}


	private void addDeviceItem() {

		TitledPanel device = new TitledPanel(LangRes.get(LangRes.MY_DEVICE));
		device.setIcon(Bitmap.getBitmapResource("img/icons/device.png"));
		device.setPadding(5, 4, 5, 4);
		device.setChangeListener(this);

		diskList = new FileListField(null);
		diskList.setChangeListener(this); // 高度改变时(如SD卡插入拔出)通知Screen重新布局。
		diskList.setSearchable(false);
		diskList.setClipboardAllowed(false);
		diskList.registerRootChangeListener();

		device.add(diskList);
		vfm.add(device);

	}


	private void addFavouriteItem() {

		TitledPanel favourite = new TitledPanel(LangRes.get(LangRes.FAVOURITE));
		favourite.setIcon(Bitmap.getBitmapResource("img/icons/favourite.png"));
		favourite.setPadding(5, 4, 5, 4);
		favourite.setChangeListener(this);

		FavouritesListField favouriteList = new FavouritesListField();
		favouriteList.setChangeListener(this);

		favourite.add(favouriteList);

		if (favouriteList.isEmpty()) {
			// 添加某个元素后favourite的状态将变为展开的。但如果列表中没元素，应将favourite重置为收缩的。
			favourite.toggleStatus();
		}

		vfm.add(favourite);

	}


	protected void makeMenu(Menu menu, int instance) {

		menu.add(MenuItem.separator(4000));

		addConfigMenu(menu, 4001);

		if (CapabilityUtil.isPhysicalKeyboardSupported()) {
			// 拥有物理键盘，添加"键盘快捷键"菜单。
			addShortcutsMenu(menu, 4002);
		}

		addAboutMenu(menu, 4003);

		menu.add(MenuItem.separator(4010));

		super.makeMenu(menu, instance);

	}


	/**
	 * 添加"设置"菜单项。
	 */
	private void addConfigMenu(Menu menu, int ordinal) {

		String configLabel = LangRes.get(LangRes.MENU_TITLE_OPTIONS);

		MenuItem config = new MenuItem(configLabel, ordinal, ordinal) {

			public void run() {

				UiApplication.getUiApplication().pushScreen(new OptionsScreen());
			}
		};
		menu.add(config);

	}


	/**
	 * 添加"键盘快捷键"菜单。
	 */
	private void addShortcutsMenu(Menu menu, int ordinal) {

		String shortCutLable = LangRes.get(LangRes.MENU_KEYBOARD_SHORTCUTS);

		MenuItem shortCutKeyHelp = new MenuItem(shortCutLable, ordinal, ordinal) {

			public void run() {

				UiApplication.getUiApplication().invokeLater(new Runnable() {

					public void run() {

						UiApplication.getUiApplication().pushScreen(new KeyboardShortcutsHelpScreen());
					}
				});
			}
		};
		menu.add(shortCutKeyHelp);

	}


	/**
	 * 关于菜单。
	 */
	private void addAboutMenu(Menu menu, int ordinal) {

		String aboutLable = LangRes.get(LangRes.MENU_ABOUT);

		MenuItem about = new MenuItem(aboutLable, ordinal, ordinal) {

			public void run() {

				UiApplication.getUiApplication().invokeLater(new Runnable() {

					public void run() {

						UiApplication.getUiApplication().pushScreen(new AboutScreen());
					}
				});
			}
		};
		menu.add(about);

	}


	protected boolean onSavePrompt() {

		// 避免保存对话框的出现。
		return true;

	}


	protected void onUiEngineAttached(boolean attached) {

		if (attached) {
			diskList.setFocus();
		}

		super.onUiEngineAttached(attached);

	}


	public void close() {

		diskList.unRegisterRootChangeListener();
		super.close();

	}


	public void fieldChanged(Field field, int context) {

		// 其它组件高度改变时会有HEIGHT_CHANGED,
		// 而FileListField正相反，高度变化时系统自动通知，不变时会有HEIGHT_CHANGED的手动通知，原意是更改目录时高度不变也让滚动条出现。
		if (context == ScreenHeightChangeEvent.SCREEN_HEIGHT_CHANGED || field instanceof FileListField
				&& context != ScreenHeightChangeEvent.SCREEN_HEIGHT_NOT_CHANGED) {
			updateLayout();
		}

	}

}
