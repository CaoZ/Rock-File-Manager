package de.innosystec.unrar;

import de.innosystec.unrar.rarfile.FileHeader;
import de.innosystec.unrar.unpack.*;
import java.io.IOException;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

/**
 * 
 * @author alban
 */
public class Volume {

	private Volume() {
	}

	public static boolean mergeArchive(Archive archive, ComprDataIO dataIO) throws IOException {
		FileHeader hd = dataIO.getSubHeader();
		if (hd.getUnpVersion() >= 20 && hd.getFileCRC() != 0xffffffff
				&& dataIO.getPackedCRC() != ~hd.getFileCRC()) {
			System.err.println("Data Bad CRC");
		}

		boolean oldNumbering = !archive.getMainHeader().isNewNumbering() || archive.isOldFormat();
		String nextName = nextVolumeName(archive.getFile().getPath(), oldNumbering);
		// File nextVolume = new File(nextName);
		FileConnection nextVolume = (FileConnection) Connector.open(nextName);
		UnrarCallback callback = archive.getUnrarCallback();
		if ((callback != null) && !callback.isNextVolumeReady(nextVolume)) {
			return false;
		}
		if (!nextVolume.exists()) {
			return false;
		}
		archive.setFile(nextVolume);
		hd = archive.nextFileHeader();
		if (hd == null) {
			return false;
		}
		dataIO.init(hd);
		return true;
	}

	public static String nextVolumeName(String arcName, boolean oldNumbering) {
		if (!oldNumbering) {
			// part1.rar, part2.rar, ...
			int len = arcName.length();
			int indexR = len - 1;
			while ((indexR >= 0) && !isDigit(arcName.charAt(indexR))) {
				indexR--;
			}
			int index = indexR + 1;
			int indexL = indexR - 1;
			while ((indexL >= 0) && isDigit(arcName.charAt(indexL))) {
				indexL--;
			}
			if (indexL < 0) {
				return null;
			}
			indexL++;
			StringBuffer buffer = new StringBuffer(len);
			// buffer.append(arcName, 0, indexL);
			buffer.append(arcName.substring(0, indexL));
			char[] digits = new char[indexR - indexL + 1];
			arcName.getChars(indexL, indexR + 1, digits, 0);
			indexR = digits.length - 1;
			while ((indexR >= 0) && (++digits[indexR]) == '9' + 1) {
				digits[indexR] = '0';
				indexR--;
			}
			if (indexR < 0) {
				buffer.append('1');
			}
			buffer.append(digits);
			// buffer.append(arcName, index, len);
			buffer.append(arcName.substring(index, len));
			return buffer.toString();
		} else {
			// .rar, .r00, .r01, ...
			int len = arcName.length();
			if ((len <= 4) || (arcName.charAt(len - 4) != '.')) {
				return null;
			}
			StringBuffer buffer = new StringBuffer();
			int off = len - 3;
			// buffer.append(arcName, 0, off);
			buffer.append(arcName.substring(0, off));
			if (!isDigit(arcName.charAt(off + 1)) || !isDigit(arcName.charAt(off + 2))) {
				buffer.append("r00");
			} else {
				char[] ext = new char[3];
				arcName.getChars(off, len, ext, 0);
				int i = ext.length - 1;
				while ((++ext[i]) == '9' + 1) {
					ext[i] = '0';
					i--;
				}
				buffer.append(ext);
			}
			return buffer.toString();
		}
	}

	private static boolean isDigit(char c) {
		return (c >= '0') && (c <= '9');
	}
}
