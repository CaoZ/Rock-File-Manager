
package RockManager.util.ui;

import RockManager.config.ConfigData;
import RockManager.util.FixUtil;
import RockManager.util.OSVersionUtil;
import RockManager.util.UtilCommon;
import RockManager.util.quickExit.QuickExitMenuHandler;
import RockManager.util.quickExit.QuickExitRegistry;
import RockManager.util.quickExit.QuickExitScreen;
import net.rim.device.api.system.Display;
import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.TransitionContext;
import net.rim.device.api.ui.Ui;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.UiEngineInstance;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.container.MainScreen;


/**
 * 有动画效果的MainScreen.
 */
public class AnimatedMainScreen extends MainScreen {

	private boolean hasAppliedAnimation = false;

	private static int menuAnimateTime = -1;

	static {
		if (OSVersionUtil.isOS5()) {
			// 也许是由于机能的问题，在os5上菜单弹出、收起时似乎比os6实际需要的时间长。
			menuAnimateTime = 140;
		} else {
			menuAnimateTime = 180;
		}
	}


	public AnimatedMainScreen() {

		this(0);

	}


	public AnimatedMainScreen(long style) {

		super(style);

		if (this instanceof QuickExitScreen) {
			QuickExitRegistry.addLog((QuickExitScreen) this);
		}

		boolean animationEffect = ConfigData.ANIMATION_EFFECT.booleanValue();

		if (animationEffect == false) {
			return;
		}

		UiEngineInstance uiEngine = Ui.getUiEngineInstance();

		// screen push时动画效果。
		TransitionContext transitionPush = new TransitionContext(TransitionContext.TRANSITION_FADE);
		transitionPush.setIntAttribute(TransitionContext.ATTR_DURATION, 100);
		transitionPush.setIntAttribute(TransitionContext.ATTR_KIND, TransitionContext.KIND_IN);

		uiEngine.setTransition(null, this, UiEngineInstance.TRIGGER_PUSH, transitionPush);

		// screen pop时动画效果。
		if (OSVersionUtil.isOS5()) {
			// os 5 上zoom效果不好，改为fade

			TransitionContext transitionPop = new TransitionContext(TransitionContext.TRANSITION_FADE);
			transitionPop.setIntAttribute(TransitionContext.ATTR_DURATION, 100);
			transitionPop.setIntAttribute(TransitionContext.ATTR_KIND, TransitionContext.KIND_OUT);

			uiEngine.setTransition(this, null, UiEngineInstance.TRIGGER_POP, transitionPop);

		} else {

			TransitionContext transitionPop = new TransitionContext(TransitionContext.TRANSITION_ZOOM);
			transitionPop.setIntAttribute(5, 110); // TransitionContext.ATTR_SCALE
			transitionPop.setIntAttribute(TransitionContext.ATTR_KIND, TransitionContext.KIND_OUT);

			uiEngine.setTransition(this, null, UiEngineInstance.TRIGGER_POP, transitionPop);

		}

		hasAppliedAnimation = true;

	}


	public boolean onMenu(int instance) {

		boolean animationEffect = ConfigData.ANIMATION_EFFECT.booleanValue();

		if (animationEffect == false) {
			return super.onMenu(instance);
		}

		final UiEngineInstance uiEngine = Ui.getUiEngineInstance();

		// 由于在OS 5上有可能连续弹出两个菜单: short menu -> full menu,
		// 而第二个菜单弹出时第一个菜单的onMenuDismissed方法未调用，所以第二个菜单弹出时取得的originPop是错误的。
		// 所以现在设置为null。事实上除了菜单弹出时其它时候无需为其它Screen设置TransitionContext。

		/*
		 * // 原来的Push时效果 final TransitionContext originPush =
		 * uiEngine.getTransition(this, null, UiEngineInstance.TRIGGER_PUSH);
		 */

		// menu弹出动画效果
		TransitionContext transitionPush = new TransitionContext(TransitionContext.TRANSITION_SLIDE);
		transitionPush.setIntAttribute(TransitionContext.ATTR_STYLE, TransitionContext.STYLE_OVER);
		transitionPush.setIntAttribute(TransitionContext.ATTR_DIRECTION, TransitionContext.DIRECTION_UP);
		transitionPush.setIntAttribute(TransitionContext.ATTR_KIND, TransitionContext.KIND_IN);
		transitionPush.setIntAttribute(TransitionContext.ATTR_DURATION, menuAnimateTime);

		final Screen thisScreen = this;

		UiApplication.getUiApplication().invokeLater(new Runnable() {

			public void run() {

				// 还原原来Screen Push时效果.
				uiEngine.setTransition(thisScreen, null, UiEngineInstance.TRIGGER_PUSH, null);
			}
		});

		uiEngine.setTransition(this, null, UiEngineInstance.TRIGGER_PUSH, transitionPush);

		return super.onMenu(instance);

	}


	protected void onMenuDismissed(Menu menu) {

		boolean animationEffect = ConfigData.ANIMATION_EFFECT.booleanValue();

		if (animationEffect == false) {
			return;
		}

		// 在os5上若有从短菜单的"Full Menu"进入完整菜单，完整菜单再关闭时调用此方法时menu为null.
		boolean hasSelected = menu != null && menu.getSelectedItem() != null;

		if (hasSelected) {
			// 如果是选择了某项，取消收起动画效果，立即执行。
			super.onMenuDismissed(menu);
			return;
		}

		final UiEngineInstance uiEngine = Ui.getUiEngineInstance();

		// 由于在OS 5上有可能连续弹出两个菜单: short menu -> full menu,
		// 而第二个菜单弹出时第一个菜单的onMenuDismissed方法未调用，所以第二个菜单弹出时取得的originPop是错误的。
		// 所以现在设置为null。事实上除了菜单弹出时其它时候无需为其它Screen设置TransitionContext。

		/*
		 * final TransitionContext originPop = uiEngine.getTransition(null,
		 * this, UiEngineInstance.TRIGGER_POP);
		 */

		// menu收起动画效果, 在非Storm, Torch机型上可能无效。
		// http://supportforums.blackberry.com/t5/Java-Development/Have-problem-to-create-a-AnimatedMainScreen-the-animated-slide/m-p/1304455
		TransitionContext transitionPop = new TransitionContext(TransitionContext.TRANSITION_SLIDE);
		transitionPop.setIntAttribute(TransitionContext.ATTR_STYLE, TransitionContext.STYLE_OVER);
		transitionPop.setIntAttribute(TransitionContext.ATTR_DIRECTION, TransitionContext.DIRECTION_DOWN);
		transitionPop.setIntAttribute(TransitionContext.ATTR_KIND, TransitionContext.KIND_OUT);
		transitionPop.setIntAttribute(TransitionContext.ATTR_DURATION, menuAnimateTime);

		uiEngine.setTransition(null, this, UiEngineInstance.TRIGGER_POP, transitionPop);

		final Screen thisScreen = this;

		UiApplication.getUiApplication().invokeLater(new Runnable() {

			public void run() {

				uiEngine.setTransition(null, thisScreen, UiEngineInstance.TRIGGER_POP, null);
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

		if (attached == false && hasAppliedAnimation == true) {

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


	public static int getMenuAnimateTime() {

		return menuAnimateTime;
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
