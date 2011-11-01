
package RockManager.favouritesList;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import RockManager.fileList.FileItem;
import RockManager.fileList.FileNameComparator;
import net.rim.device.api.system.PersistentObject;
import net.rim.device.api.system.PersistentStore;
import net.rim.device.api.util.SimpleSortingVector;


public class FavouritesData {

	private static Vector CHANGE_LISTENERS;

	private static PersistentObject PERSIST;

	private static Hashtable DATA; // key:path value:FileItem

	static {

		// "Rock File Manager FavouriteData V1";
		long key = 0x81000565cb170d13L;
		PERSIST = PersistentStore.getPersistentObject(key);
		Object contents = PERSIST.getContents();

		if (contents instanceof Hashtable) {
			DATA = (Hashtable) contents;
		} else {
			DATA = new Hashtable();
			PERSIST.setContents(DATA);
			saveData();
		}

		CHANGE_LISTENERS = new Vector();

	}


	private static void saveData() {

		PERSIST.commit();
	}


	public static void add(FileItem fileItem) {

		String itemPath = fileItem.getPath();
		SimpleFileItem simpleFileItem = new SimpleFileItem(fileItem);

		DATA.put(itemPath, simpleFileItem);
		saveData();

		favouritesChanged();

	}


	public static FileItem[] listFiles() {

		SimpleSortingVector vector = new SimpleSortingVector();

		Enumeration items = DATA.elements();

		while (items.hasMoreElements()) {
			SimpleFileItem simpleItem = (SimpleFileItem) items.nextElement();
			FileItem fileItem = simpleItem.toFileItem();
			vector.addElement(fileItem);
		}

		vector.setSortComparator(new FileNameComparator());
		vector.reSort();
		FileItem[] files = new FileItem[vector.size()];
		vector.copyInto(files);
		return files;
	}


	/**
	 * 从收藏夹中删除这个条目。
	 */
	public static void delete(FileItem thisItem) {

		String itemPath = thisItem.getPath();

		if (DATA.containsKey(itemPath)) {

			DATA.remove(itemPath);
			saveData();

			favouritesChanged();
		}

	}


	private static void favouritesChanged() {

		for (int i = 0; i < CHANGE_LISTENERS.size(); i++) {
			FavouritesChangedListener thisListener = (FavouritesChangedListener) CHANGE_LISTENERS.elementAt(i);
			thisListener.favouritesChanged();
		}

	}


	public static void addChangeListener(FavouritesChangedListener listener) {

		if (CHANGE_LISTENERS.contains(listener) == false) {
			CHANGE_LISTENERS.addElement(listener);
		}
	}


	public static void removeChangeListener(FavouritesChangedListener listener) {

		CHANGE_LISTENERS.removeElement(listener);
	}

}
