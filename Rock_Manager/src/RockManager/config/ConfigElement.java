
package RockManager.config;

public class ConfigElement {

	final String key;

	final Object defaultValue;

	private Object value;


	public ConfigElement(String key, Object defaultValue) {

		this.key = key;
		this.defaultValue = defaultValue;

	}


	public void setValue(Object value) {

		this.value = value;
	}


	public Object value() {

		return value;
	}


	public boolean booleanValue() {

		return ((Boolean) value).booleanValue();
	}


	public void setBooleanValue(boolean value) {

		this.value = new Boolean(value);
	}

}
