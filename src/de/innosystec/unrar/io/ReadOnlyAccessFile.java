/*
 * Copyright (c) 2007 innoSysTec (R) GmbH, Germany. All rights reserved.
 * Original author: Edmund Wagner
 * Creation date: 23.05.2007
 *
 * Source: $HeadURL$
 * Last changed: $LastChangedDate$
 * 
 * the unrar licence applies to all junrar source and binary distributions 
 * you are not allowed to use this source to re-create the RAR compression algorithm
 * 
 * Here some html entities which can be used for escaping javadoc tags:
 * "&":  "&#038;" or "&amp;"
 * "<":  "&#060;" or "&lt;"
 * ">":  "&#062;" or "&gt;"
 * "@":  "&#064;" 
 */
package de.innosystec.unrar.io;

import java.io.IOException;
import java.io.InputStream;
import javax.microedition.io.file.FileConnection;
import net.rim.device.api.io.Seekable;

/**
 * DOCUMENT ME
 * 
 * @author $LastChangedBy$
 * @version $LastChangedRevision$
 */
public class ReadOnlyAccessFile implements IReadOnlyAccess {

	private FileConnection fconn;
	private InputStream input;
	private long position = 0;

	public ReadOnlyAccessFile(FileConnection file) throws IOException {
		fconn = file;
		input = fconn.openInputStream();
	}

	public int readFully(byte[] buffer, int count) throws IOException {
		int readed = input.read(buffer, 0, count);
		position += readed;
		return readed;
	}

	public long getPosition() throws IOException {
		return position;
		// return seekable.getPosition();
	}

	public void setPosition(long pos) throws IOException {

		// 5.0版本以上的新功能，Seekable!!
		// 但经过测试，seekable似乎比skip方法慢些。
		// seekable.setPosition(pos);

		if (pos > position) {
			input.skip(pos - position);
		} else if (pos < position) {
			// input.close();
			// input = fconn.openInputStream();
			// 不知道此reset方法是否有效
			// API not clear!!

			// input.reset();
			// input.skip(pos);

			// 使用Seekable来跳到前面。
			Seekable seekable = (Seekable) input;
			seekable.setPosition(pos);
		}

		position = pos;

	}

	public int read() throws IOException {
		int readed = input.read();
		if (readed != -1) {
			position++;
		}
		return readed;
	}

	public int read(byte[] buffer, int off, int count) throws IOException {
		int readed = input.read(buffer, off, count);
		position += readed;
		return readed;
	}

	public void close() throws IOException {
		if (input != null) {
			input.close();
		}
		if (fconn != null) {
			fconn.close();
		}
	}
}
