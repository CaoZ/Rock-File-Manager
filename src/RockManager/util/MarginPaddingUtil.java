
package RockManager.util;

import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.XYEdges;


public class MarginPaddingUtil {

	/**
	 * 为field设置margin.
	 * 
	 * @param field
	 * @param position
	 *            位置，各个位置以空格分隔，如"top right bottom left"、"top"
	 * @param value
	 *            值，各个位置以空格分隔，与position一一对应，如"15 0 10 0"、"8"
	 */

	public static void setMargin(Field field, String position, String value) {

		XYEdges margin = new XYEdges();
		field.getMargin(margin);

		String[] positions = UtilCommon.splitString(position, " ");
		String[] values = UtilCommon.splitString(value, " ");

		for (int i = 0; i < positions.length && i < values.length; i++) {

			String thisPosition = positions[i];
			int thisValue;
			try {
				thisValue = Integer.parseInt(values[i]);
			} catch (NumberFormatException e) {
				continue;
			}

			if (thisPosition.equalsIgnoreCase("top")) {
				margin.top = thisValue;
			} else if (thisPosition.equalsIgnoreCase("right")) {
				margin.right = thisValue;
			} else if (thisPosition.equalsIgnoreCase("bottom")) {
				margin.bottom = thisValue;
			} else if (thisPosition.equalsIgnoreCase("left")) {
				margin.left = thisValue;
			}

		}

		field.setMargin(margin);

	}


	/**
	 * 单独设置margin top.
	 * 
	 * @param field
	 * @param value
	 */
	public static void setMarginTop(Field field, int value) {

		setMargin(field, "top", Integer.toString(value));
	}


	/**
	 * 单独设置margin right.
	 * 
	 * @param field
	 * @param value
	 */
	public static void setMarginRight(Field field, int value) {

		setMargin(field, "right", Integer.toString(value));
	}


	/**
	 * 单独设置margin bottom.
	 * 
	 * @param field
	 * @param value
	 */
	public static void setMarginBottom(Field field, int value) {

		setMargin(field, "bottom", Integer.toString(value));
	}


	/**
	 * 单独设置margin left.
	 * 
	 * @param field
	 * @param value
	 */
	public static void setMarginLeft(Field field, int value) {

		setMargin(field, "left", Integer.toString(value));
	}


	/**
	 * 为field设置padding.
	 * 
	 * @param field
	 * @param position
	 *            位置，各个位置以空格分隔，如"top right bottom left"、"top"
	 * @param value
	 *            值，各个位置以空格分隔，与position一一对应，如"15 0 10 0"、"8"
	 */

	public static void setPadding(Field field, String position, String value) {

		XYEdges padding = new XYEdges();
		field.getPadding(padding);

		String[] positions = UtilCommon.splitString(position, " ");
		String[] values = UtilCommon.splitString(value, " ");

		for (int i = 0; i < positions.length && i < values.length; i++) {

			String thisPosition = positions[i];
			int thisValue;
			try {
				thisValue = Integer.parseInt(values[i]);
			} catch (NumberFormatException e) {
				continue;
			}

			if (thisPosition.equalsIgnoreCase("top")) {
				padding.top = thisValue;
			} else if (thisPosition.equalsIgnoreCase("right")) {
				padding.right = thisValue;
			} else if (thisPosition.equalsIgnoreCase("bottom")) {
				padding.bottom = thisValue;
			} else if (thisPosition.equalsIgnoreCase("left")) {
				padding.left = thisValue;
			}

		}

		field.setPadding(padding);

	}


	/**
	 * 单独设置padding top.
	 * 
	 * @param field
	 * @param value
	 */
	public static void setPaddingTop(Field field, int value) {

		setPadding(field, "top", Integer.toString(value));
	}


	/**
	 * 单独设置padding right.
	 * 
	 * @param field
	 * @param value
	 */
	public static void setPaddingRight(Field field, int value) {

		setPadding(field, "right", Integer.toString(value));
	}


	/**
	 * 单独设置padding bottom.
	 * 
	 * @param field
	 * @param value
	 */
	public static void setPaddingBottom(Field field, int value) {

		setPadding(field, "bottom", Integer.toString(value));
	}


	/**
	 * 单独设置padding left.
	 * 
	 * @param field
	 * @param value
	 */
	public static void setPaddingLeft(Field field, int value) {

		setPadding(field, "left", Integer.toString(value));
	}

}
