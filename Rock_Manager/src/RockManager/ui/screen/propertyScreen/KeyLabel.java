
package RockManager.ui.screen.propertyScreen;

import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.component.LabelField;


public class KeyLabel extends LabelField {

	public KeyLabel(String message) {

		super(message);
	}


	protected void paint(Graphics g) {

		g.setColor(0x111111);
		super.paint(g);
	}

}
