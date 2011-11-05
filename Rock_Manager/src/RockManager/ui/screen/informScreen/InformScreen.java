
package RockManager.ui.screen.informScreen;

import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.component.LabelField;
import RockManager.ui.MyUI;
import RockManager.util.ui.BasePopupScreen;
import RockManager.util.ui.SeparatorField;


public abstract class InformScreen extends BasePopupScreen {

	public InformScreen() {

		super(VERTICAL_SCROLL | VERTICAL_SCROLLBAR, DEFAULT_CLOSE);

		addTitleField();

		int marginTop = MyUI.deriveSize(4);
		int marginBottom = MyUI.deriveSize(10);
		SeparatorField sep = new SeparatorField(marginTop, marginBottom);
		add(sep);

		addMainArea();

	}


	private void addTitleField() {

		String title = getTitle();
		LabelField titleLabel = new LabelField(title, FIELD_HCENTER | DrawStyle.HCENTER);
		titleLabel.setFont(getFont().derive(Font.BOLD));
		add(titleLabel);

	}


	protected abstract String getTitle();


	protected abstract void addMainArea();


	protected void addLabelField(String label) {

		LabelField labelField = new LabelField(label);
		labelField.setMargin(1, 0, 1, 0);
		add(labelField);

	}

}
