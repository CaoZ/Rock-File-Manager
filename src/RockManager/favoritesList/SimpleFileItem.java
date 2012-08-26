
package RockManager.favoritesList;

import RockManager.fileList.FileItem;
import net.rim.device.api.util.Persistable;


public class SimpleFileItem implements Persistable {

	private String path;

	private int fileType;

	private boolean isDRMLocked;

	private boolean isEncrypted;

	private long fileSize;


	public SimpleFileItem(FileItem fileItem) {

		path = fileItem.getRawPath();
		fileType = fileItem.getType();
		isDRMLocked = fileItem.isDRMLocked();
		isEncrypted = fileItem.isEncrypted();
		fileSize = fileItem.getFileSize();
	}


	public FileItem toFileItem() {

		FileItem fileItem = new FileItem(path, fileType);
		fileItem.setIsDRMForwardLocked(isDRMLocked);
		fileItem.setIsEncrypted(isEncrypted);
		fileItem.setSize(fileSize);
		return fileItem;
	}

}
