
package RockManager.util.ui;

import RockManager.config.ConfigData;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.TransitionContext;
import net.rim.device.api.ui.Ui;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.UiEngineInstance;
import net.rim.device.api.ui.XYEdges;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.decor.Border;
import net.rim.device.api.ui.decor.BorderFactory;


public class BaseDialog extends Dialog {

	private boolean hasAppliedAnimation = false;


	public BaseDialog(int type, String message, int defaultChoice, Bitmap bitmap, long style) {

		super(type, message, defaultChoice, bitmap, style);
		applyOwnTheme();
	}


	public BaseDialog(String message, Object[] choices, int[] values, int defaultChoice, Bitmap bitmap) {

		super(message, choices, values, defaultChoice, bitmap);
		applyOwnTheme();
	}


	public BaseDialog(String message, Object[] choices, int[] values, int defaultChoice, Bitmap bitmap, long style) {

		super(message, choices, values, defaultChoice, bitmap, style);
		applyOwnTheme();
	}


	public BaseDialog(String message, Object[] choices, int[] values, Object defaultObject, Bitmap bitmap) {

		super(message, choices, values, defaultObject, bitmap);
		applyOwnTheme();
	}


	public BaseDialog(String message, Object[] choices, int[] values, Object defaultObject, Bitmap bitmap, long style) {

		super(message, choices, values, defaultObject, bitmap, style);
		applyOwnTheme();
	}


	protected void applyOwnTheme() {

		XYEdges edges = new XYEdges(31, 31, 31, 31);
		Bitmap borderImg = Bitmap.getBitmapResource("img/other/popupBack.png");
		Border border = BorderFactory.createBitmapBorder(edges, borderImg);
		setBorder(border);
		setPadding(10, 8, 8, 8);

		if (ConfigData.ANIMATION_EFFECT.booleanValue() == false) {
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

}
