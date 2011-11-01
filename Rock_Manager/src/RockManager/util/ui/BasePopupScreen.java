
package RockManager.util.ui;

import java.util.Enumeration;
import java.util.Hashtable;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Display;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.TransitionContext;
import net.rim.device.api.ui.Ui;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.UiEngineInstance;
import net.rim.device.api.ui.XYEdges;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;
import net.rim.device.api.ui.decor.Border;
import net.rim.device.api.ui.decor.BorderFactory;
import RockManager.config.ConfigData;
import RockManager.util.FixUtil;
import RockManager.util.KeyUtil;
import RockManager.util.UtilCommon;
import RockManager.util.quickExit.QuickExitMenuHandler;
import RockManager.util.quickExit.QuickExitRegistry;
import RockManager.util.quickExit.QuickExitScreen;


public class BasePopupScreen extends PopupScreen {

	/**
	 * 标题性文字，如“正在解压...”、“正在复制...”、“重命名...”等。
	 */
	private LabelField titleField;

	private Hashtable hotKeyTable;

	private boolean hasAppliedAnimation = false;


	public BasePopupScreen(long managerStyle, long screenStyle) {

		// 必要时可以滚动
		super(new VerticalFieldManager(managerStyle), screenStyle);

		XYEdges edges = new XYEdges(31, 31, 31, 31);
		Bitmap borderImg = Bitmap.getBitmapResource("img/other/popupBack.png");
		Border border = BorderFactory.createBitmapBorder(edges, borderImg);
		setBorder(border);
		setPadding(10, 8, 7, 8);

		if (this instanceof QuickExitScreen) {
			QuickExitRegistry.addLog((QuickExitScreen) this);
		}

		boolean animationEffect = ConfigData.ANIMATION_EFFECT.booleanValue();

		if (animationEffect == false) {
			return;
		}

		// 动画push效果。
		TransitionContext transitionPush = new TransitionContext(TransitionContext.TRANSITION_ZOOM);
		transitionPush.setIntAttribute(TransitionContext.ATTR_DURATION, 200);
		transitionPush.setIntAttribute(5, 50); // TransitionContext.ATTR_SCALE
		Ui.getUiEngineInstance().setTransition(null, this, UiEngineInstance.TRIGGER_PUSH, transitionPush);

		// 动画pop效果。
		TransitionContext transitionPop = new TransitionContext(TransitionContext.TRANSITION_ZOOM);
		transitionPop.setIntAttribute(TransitionContext.ATTR_DURATION, 200);
		transitionPop.setIntAttribute(TransitionContext.ATTR_KIND, TransitionContext.KIND_OUT);
		transitionPop.setIntAttribute(5, 30);// TransitionContext.ATTR_SCALE
		Ui.getUiEngineInstance().setTransition(this, null, UiEngineInstance.TRIGGER_POP, transitionPop);

		hasAppliedAnimation = true;

	}


	protected void useSystemTheme() {

		setBorder(null);
		setPadding(0, 0, 0, 0);

	}


	/**
	 * 设置标题。
	 * 
	 * @param title
	 */
	public void setTitle(String title) {

		// 在9800中setText需获得eventLock, 而9780中不需要，奇怪！
		synchronized (UiApplication.getEventLock()) {

			if (titleField == null) {
				titleField = new LabelField();
				titleField.setFont(getFont().derive(Font.PLAIN, 24));
				titleField.setMargin(0, 0, 20, 0);
				getDelegate().insert(titleField, 0);
			}
			titleField.setText(title);

		}

	}


	public LabelField getTitleField() {

		return titleField;
	}


	/**
	 * 分析按钮label中带下划线的字母，为按钮注册快捷键。
	 * 
	 * @param button
	 */
	public void registerHotKey(BaseButtonField button, String labelWithoutShortcut) {

		String label = button.getLabel();
		if (label == null || label.length() == 0) {
			return;
		}

		int position = label.indexOf('\u0332');

		if (position > 0) {

			if (hotKeyTable == null) {
				hotKeyTable = new Hashtable();
			}

			String labelWithShortcut = label;

			button.setLabel(labelWithShortcut, labelWithoutShortcut);
			button.activeTextChangeMode();

			char hotKey = Character.toLowerCase(label.charAt(position - 1));
			hotKeyTable.put(new Character(hotKey), button);

		}

	}


	protected boolean keyChar(char ch, int status, int time) {

		boolean consumed = super.keyChar(ch, status, time);

		if (consumed == false && hotKeyTable != null) {

			Enumeration keys = hotKeyTable.keys();

			while (keys.hasMoreElements()) {
				Character thisKey = (Character) keys.nextElement();
				if (KeyUtil.isOnSameKey(ch, status, thisKey.charValue())) {

					BaseButtonField button = (BaseButtonField) hotKeyTable.get(thisKey);
					button.click();
					consumed = true;

					break;

				}

			}

		}

		return consumed;

	}


	public boolean onMenu(int instance) {

		boolean animationEffect = ConfigData.ANIMATION_EFFECT.booleanValue();

		if (animationEffect == false) {
			return super.onMenu(instance);
		}

		UiEngineInstance uiEngine = Ui.getUiEngineInstance();

		// 原来的Push时效果。
		TransitionContext originPush = uiEngine.getTransition(this, null, UiEngineInstance.TRIGGER_PUSH);

		// menu弹出动画效果
		TransitionContext transitionPush = new TransitionContext(TransitionContext.TRANSITION_SLIDE);
		transitionPush.setIntAttribute(TransitionContext.ATTR_STYLE, TransitionContext.STYLE_OVER);
		transitionPush.setIntAttribute(TransitionContext.ATTR_DIRECTION, TransitionContext.DIRECTION_UP);
		transitionPush.setIntAttribute(TransitionContext.ATTR_KIND, TransitionContext.KIND_IN);
		transitionPush.setIntAttribute(TransitionContext.ATTR_DURATION, AnimatedMainScreen.getMenuAnimateTime());

		uiEngine.setTransition(this, null, UiEngineInstance.TRIGGER_PUSH, transitionPush);

		boolean created = super.onMenu(instance);

		// 还原原来Screen Push时效果.
		uiEngine.setTransition(this, null, UiEngineInstance.TRIGGER_PUSH, originPush);

		return created;

	}


	protected void onMenuDismissed(Menu menu) {

		boolean animationEffect = ConfigData.ANIMATION_EFFECT.booleanValue();

		if (animationEffect == false) {
			return;
		}

		boolean hasSelected = (menu.getSelectedItem() != null);

		if (hasSelected) {
			// 如果是选择了某项，取消收起动画效果，立即执行。
			super.onMenuDismissed(menu);
			return;
		}

		final UiEngineInstance uiEngine = Ui.getUiEngineInstance();

		final TransitionContext originPop = uiEngine.getTransition(null, this, UiEngineInstance.TRIGGER_POP);

		// menu收起动画效果, 在非Storm, Torch机型上可能无效。
		// http://supportforums.blackberry.com/t5/Java-Development/Have-problem-to-create-a-AnimatedMainScreen-the-animated-slide/m-p/1304455
		TransitionContext transitionPop = new TransitionContext(TransitionContext.TRANSITION_SLIDE);
		transitionPop.setIntAttribute(TransitionContext.ATTR_STYLE, TransitionContext.STYLE_OVER);
		transitionPop.setIntAttribute(TransitionContext.ATTR_DIRECTION, TransitionContext.DIRECTION_DOWN);
		transitionPop.setIntAttribute(TransitionContext.ATTR_KIND, TransitionContext.KIND_OUT);
		transitionPop.setIntAttribute(TransitionContext.ATTR_DURATION, AnimatedMainScreen.getMenuAnimateTime());

		uiEngine.setTransition(null, this, UiEngineInstance.TRIGGER_POP, transitionPop);

		final Screen thisScreen = this;

		UiApplication.getUiApplication().invokeLater(new Runnable() {

			public void run() {

				uiEngine.setTransition(null, thisScreen, UiEngineInstance.TRIGGER_POP, originPop);

			}
		});

		super.onMenuDismissed(menu);

	}


	protected void makeMenu(Menu menu, int instance) {

		FixUtil.fixVirtualKeyboardMenuItem(menu, this);
		super.makeMenu(menu, instance);

		QuickExitMenuHandler.handleExitMenuItem(menu);

		UtilCommon.setMenuMinWidth(menu, Display.getWidth() / 3);
	}


	protected void onUiEngineAttached(boolean attached) {

		if (attached == false && hasAppliedAnimation) {

			final Screen thisScreen = this;

			UiApplication.getUiApplication().invokeLater(new Runnable() {

				public void run() {

					// 清理内存
					// http://supportforums.blackberry.com/t5/Java-Development/Clear-Screen-Transitions/ta-p/572506

					Ui.getUiEngineInstance().setTransition(null, thisScreen, UiEngineInstance.TRIGGER_PUSH, null);
					Ui.getUiEngineInstance().setTransition(thisScreen, null, UiEngineInstance.TRIGGER_POP, null);

				}

			});
		}

		super.onUiEngineAttached(attached);

	}


	public void close() {

		if (this instanceof QuickExitScreen) {
			QuickExitRegistry.removeLog((QuickExitScreen) this);
		}

		// 在invokeLater中执行。作用：使从菜单中选择"关闭"来关闭本Screen时也有动画效果(等待Menu关闭后再关闭本Screen)。某则不会出现动画效果，原因是Menu也是一种Screen(DefaultMenuScreen)。
		UiApplication.getUiApplication().invokeLater(new Runnable() {

			public void run() {

				superDotClose();
			}
		});

	}


	private void superDotClose() {

		super.close();

	}

}
