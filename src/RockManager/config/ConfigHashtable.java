
package RockManager.config;

import java.util.Hashtable;
import net.rim.device.api.system.PersistentObject;
import net.rim.device.api.system.PersistentStore;
import net.rim.device.api.util.Persistable;


public class ConfigHashtable extends Hashtable implements Persistable {

	private int persistKey;


	public static ConfigHashtable getTable(String configKey) {

		int key = configKey.hashCode();

		PersistentObject persist = PersistentStore.getPersistentObject(key);
		Object contents = persist.getContents();

		if (contents instanceof ConfigHashtable) {
			return (ConfigHashtable) contents;
		} else {
			ConfigHashtable table = new ConfigHashtable();
			table.setPersistKey(key);
			persist.setContents(table);
			persist.commit();
			return table;
		}

	}


	private void setPersistKey(int key) {

		persistKey = key;

	}


	public void read(ConfigElement configElement) {

		String key = configElement.key;
		Object value;

		if (containsKey(key)) {
			value = get(key);
		} else {
			value = configElement.defaultValue;
		}

		configElement.setValue(value);

	}


	public void write(ConfigElement configElement) {

		// 写入条件：
		// 若table中有此值且值不同或table中无此值且值不同于默认值。

		String key = configElement.key;
		boolean needUpdate = false;

		if (containsKey(key)) {
			Object valueInTable = get(key);
			if (valueInTable.equals(configElement.value()) == false) {
				needUpdate = true;
			}
		} else {
			if (configElement.defaultValue.equals(configElement.value()) == false) {
				needUpdate = true;
			}
		}

		if (needUpdate) {
			put(key, configElement.value());
			PersistentObject persist = PersistentStore.getPersistentObject(persistKey);
			persist.commit();
		}

	}

}
