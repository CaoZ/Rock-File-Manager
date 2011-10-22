
package RockManager.ui.screen.propertyScreen;

import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.component.LabelField;


public class ValueLabel extends LabelField {

	public ValueLabel(String message) {

		super(message, DrawStyle.RIGHT);
	}


	protected void paint(Graphics g) {

		g.setColor(0x111111);
		super.paint(g);
	}

}
