
package RockManager.fileList;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Random;
import RockManager.util.UtilCommon;
import RockManager.util.ui.GPATools;
import net.rim.device.api.io.LineReader;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.component.Dialog;


public class IconSet {

	private static LimitedHashTable CACHE_SET = new LimitedHashTable(20);

	/**
	 * 将格式与文件地址相对应的表。
	 */
	private static Hashtable PATH_SET = new Hashtable();

	private static int desiredSize = -1;


	public IconSet() {

		InputStream input = getClass().getResourceAsStream("/img/iconSet/iconSet.list");
		if (input != null) {
			LineReader reader = new LineReader(input);
			while (true) {
				try {
					String path = new String(reader.readLine());
					parseTypes(path.trim());
				} catch (EOFException e) {
					break;
				} catch (IOException e) {
					break;
				}
			}
			try {
				input.close();
			} catch (IOException e) {
			}
		}

	}


	/**
	 * 分析文件名，获得图标所代表的文件格式，如"cod_jad.png"是cod与jad文件的图标。
	 * 
	 * @param path
	 */
	private void parseTypes(String path) {

		if (path.toLowerCase().endsWith(".png")) {
			path = path.substring(0, path.length() - 4);
			String[] types = UtilCommon.splitString(path, "_");
			for (int i = 0; i < types.length; i++) {
				PATH_SET.put(types[i], path + ".png");
			}
		}
	}


	/**
	 * 根据文件名获得对应的图标。
	 * 
	 * @param type
	 * @return
	 */
	public Bitmap getIcon(String type) {

		String path = (String) PATH_SET.get(type);
		if (path == null) {
			return getUnknownIcon();
		}

		if (CACHE_SET.containsKey(path) == false) {
			addToCache(path);
		}

		return (Bitmap) CACHE_SET.get(path);
	}


	/**
	 * 返回未知文件类型图标。
	 * 
	 * @return
	 */
	public Bitmap getUnknownIcon() {

		if (PATH_SET.containsKey("S.unknown") == false) {
			Dialog.alert("Hey, I can't find 'S.unknown.png', I have to leave.");
			System.exit(1);
		}
		return getIcon("S.unknown");
	}


	/**
	 * 将此路径图标加入缓存。
	 * 
	 * @param type
	 */
	private void addToCache(String path) {

		String fullPath = "img/iconSet/" + path;
		Bitmap icon = Bitmap.getBitmapResource(fullPath);
		// 虽然列表中有，可能文件实际不存在。
		if (icon == null) {
			icon = getUnknownIcon();
		}

		// 如有必要，调整大小。
		if (desiredSize > 0) {
			icon = GPATools.ResizeTransparentBitmap(icon, desiredSize, desiredSize);
		}

		CACHE_SET.put(path, icon);

	}


	public void setDesiredSize(int iconSize) {

		desiredSize = iconSize;

	}

}


/**
 * 有容量大小限制的HashTable.
 */
class LimitedHashTable extends Hashtable {

	private int maxSize;


	public LimitedHashTable(int maxSize) {

		super();
		this.maxSize = maxSize;
	}


	public Object put(Object key, Object value) {

		if (size() == maxSize && containsKey(key) == false) {
			int indexToRemove = new Random().nextInt(maxSize);
			Enumeration keys = keys();
			for (int i = 0; i < indexToRemove; i++) {
				keys.nextElement();
			}
			Object keyToRemove = keys.nextElement();
			remove(keyToRemove);
		}

		return super.put(key, value);
	}

}
