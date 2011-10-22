package RockManager.fileList;

import java.util.Vector;
import net.rim.device.api.collection.util.UnsortedReadableList;
import net.rim.device.api.ui.component.KeywordProvider;
import net.rim.device.api.util.SimpleSortingVector;
import net.rim.device.api.util.StringUtilities;

public class FileList extends UnsortedReadableList implements KeywordProvider {

	private Vector files;

	public FileList(SimpleSortingVector fileVector) {
		files = fileVector;
		loadFrom(fileVector.elements());
	}

	public void insertElement(int index, Object element) {
		insertAt(index, element);
	}

	public String[] getKeywords(Object element) {
		if (element instanceof FileItem) {
			String name = ((FileItem) element).getDisplayName();
			return StringUtilities.stringToWords(name);
		} else {
			return null;
		}
	}

	public FileItem elementAt(int index) {
		return (FileItem) files.elementAt(index);
	}

}
