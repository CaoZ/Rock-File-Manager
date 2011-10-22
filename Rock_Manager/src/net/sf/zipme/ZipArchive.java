/* ZipArchive.java
   Copyright (C) 2007 Akihiko Kusanagi

This file is derived from ZipFile.java. */

/* ZipFile.java
 Copyright (C) 2001, 2002, 2003, 2004, 2005, 2006
 Free Software Foundation, Inc.

 This file is part of GNU Classpath.

 GNU Classpath is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2, or (at your option)
 any later version.

 GNU Classpath is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with GNU Classpath; see the file COPYING.  If not, write to the
 Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 02110-1301 USA.

 Linking this library statically or dynamically with other modules is
 making a combined work based on this library.  Thus, the terms and
 conditions of the GNU General Public License cover the whole
 combination.

 As a special exception, the copyright holders of this library give you
 permission to link this library with independent modules to produce an
 executable, regardless of the license terms of these independent
 modules, and to copy and distribute the resulting executable under
 terms of your choice, provided that you also meet, for each linked
 independent module, the terms and conditions of the license of that
 module.  An independent module is a module which is not derived from
 or based on this library.  If you modify this library, you may extend
 this exception to your version of the library, but you are not
 obligated to do so.  If you do not wish to do so, delete this
 exception statement from your version. */

package net.sf.zipme;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import net.rim.device.api.io.Seekable;


/**
 * This class represents a Zip archive. You can ask for the contained entries,
 * or get an input stream for a file entry. The entry is automatically
 * decompressed. This class is thread safe: You can open input streams for
 * arbitrary entries in different threads.
 * 
 * @author Jochen Hoenicke
 * @author Artur Biesiadowski
 */
public class ZipArchive implements ZipConstants {

	/**
	 * This field isn't defined in the JDK's ZipConstants, but should be.
	 */
	static final int ENDNRD = 4;

	// The index into the buffer to start reading bytes from.
	private int off;

	// The number of bytes to read from the buffer.
	private int len;

	// The entries of this zip archive when initialized and not yet closed.
	private Hashtable entries;

	private InputStream input;

	private UnzipCallback unzipCallback;


	/**
	 * Opens a Zip archive reading the given InputStream.
	 * 
	 * @exception IOException
	 *                if a i/o error occured.
	 * @exception ZipException
	 *                if the stream doesn't contain a valid zip archive.
	 */
	public ZipArchive(InputStream in, long fileSize) throws ZipException {

		off = 0;
		len = (int) fileSize;
		input = in;
		checkZipArchive();
	}


	/**
	 * 通过文件头判断是否是有效的zip压缩文件。
	 * 
	 * @throws ZipException
	 */
	private void checkZipArchive() throws ZipException {

		boolean valid = false;
		byte[] buf = new byte[4];
		try {
			input.read(buf);
			// int sig = buf[0] & 0xFF | ((buf[1] & 0xFF) << 8) | ((buf[2] &
			// 0xFF) << 16)
			// | ((buf[3] & 0xFF) << 24);
			// valid = sig == LOCSIG;
			valid = (buf[0] == 0x50 && buf[1] == 0x4b);

		} catch (IOException e) {
			e.printStackTrace();
		}

		if (valid == false) {
			throw new ZipException("Not a valid zip archive");
		}
	}


	/**
	 * Read the central directory of a zip archive and fill the entries array.
	 * This is called exactly once when first needed. It is called while holding
	 * the lock on <code>raf</code>.
	 * 
	 * @exception IOException
	 *                if a i/o error occured.
	 * @exception ZipException
	 *                if the central directory is malformed
	 */
	private void readEntries() throws ZipException, IOException {

		/*
		 * Search for the End Of Central Directory. When a zip comment is
		 * present the directory may start earlier. Note that a comment has a
		 * maximum length of 64K, so that is the maximum we search backwards.
		 */
		PartialInputStream inp = new PartialInputStream(input, 4096, len);
		int pos = len - ENDHDR;
		int top = Math.max(0, pos - 65536);
		do {
			if (pos < top)
				throw new ZipException("central directory not found, probably not a zip archive");
			inp.seek(off + pos--);
		} while (inp.readLeInt() != ENDSIG);

		if (inp.skip(ENDTOT - ENDNRD) != ENDTOT - ENDNRD)
			throw new EOFException();
		int count = inp.readLeShort();
		if (inp.skip(ENDOFF - ENDSIZ) != ENDOFF - ENDSIZ)
			throw new EOFException();
		int centralOffset = inp.readLeInt();

		entries = new Hashtable(count + count / 2);
		// 定位到最后的目录段。
		inp.seek(off + centralOffset);

		for (int i = 0; i < count; i++) {
			if (inp.readLeInt() != CENSIG)
				throw new ZipException("Wrong Central Directory signature");

			inp.skip(4);

			int flags = inp.readLeShort();
			int method = inp.readLeShort();
			int dostime = inp.readLeInt();
			int crc = inp.readLeInt();
			int csize = inp.readLeInt();
			int size = inp.readLeInt();
			int nameLen = inp.readLeShort();
			int extraLen = inp.readLeShort();
			int commentLen = inp.readLeShort();
			inp.skip(8);
			int offset = inp.readLeInt();
			String name = inp.readString(nameLen, flags);

			ZipEntry entry = new ZipEntry(name);
			entry.setMethod(method);
			entry.setCrc(crc & 0xffffffffL);
			entry.setSize(size & 0xffffffffL);
			entry.setCompressedSize(csize & 0xffffffffL);
			entry.setDOSTime(dostime);
			if (extraLen > 0) {
				byte[] extra = new byte[extraLen];
				inp.readFully(extra);
				entry.setExtra(extra);
			}
			if (commentLen > 0) {
				entry.setComment(inp.readString(commentLen, flags));
			}
			entry.offset = offset;
			entries.put(name, entry);

			if (unzipCallback != null) {
				unzipCallback.volumeProgressChanged(i + 1, count);
			}

		}
	}


	/**
	 * Returns an enumeration of all Zip entries in this Zip archive.
	 */
	public Enumeration entries() {

		try {
			return getEntries().elements();
		} catch (IOException ioe) {
			return (new Hashtable()).elements();
		}
	}


	/**
	 * Reads entries when necessary.
	 * 
	 * @exception IOException
	 *                when the entries could not be read.
	 */
	private Hashtable getEntries() throws IOException {

		if (entries == null)
			readEntries();

		return entries;
	}


	/**
	 * Searches for a zip entry in this archive with the given name.
	 * 
	 * @param name
	 *            the name. May contain directory components separated by
	 *            slashes ('/').
	 * @return the zip entry, or null if no entry with that name exists.
	 */
	public ZipEntry getEntry(String name) {

		try {
			Hashtable entries = getEntries();
			ZipEntry entry = (ZipEntry) entries.get(name);
			// If we didn't find it, maybe it's a directory.
			if (entry == null && !name.endsWith("/"))
				entry = (ZipEntry) entries.get(name + '/');
			return entry != null ? new ZipEntry(entry, name) : null;
		} catch (IOException ioe) {
			return null;
		}
	}


	/**
	 * Creates an input stream reading the given zip entry as uncompressed data.
	 * Normally zip entry should be an entry returned by getEntry() or
	 * entries(). This implementation returns null if the requested entry does
	 * not exist. This decision is not obviously correct, however, it does
	 * appear to mirror Sun's implementation, and it is consistant with their
	 * javadoc. On the other hand, the old JCL book, 2nd Edition, claims that
	 * this should return a "non-null ZIP entry". We have chosen for now ignore
	 * the old book, as modern versions of Ant (an important application) depend
	 * on this behaviour. See discussion in this thread:
	 * http://gcc.gnu.org/ml/java-patches/2004-q2/msg00602.html
	 * 
	 * @param entry
	 *            the entry to create an InputStream for.
	 * @return the input stream, or null if the requested entry does not exist.
	 * @exception IOException
	 *                if a i/o error occured.
	 * @exception ZipException
	 *                if the Zip archive is malformed.
	 */
	public InputStream getInputStream(ZipEntry entry) throws IOException {

		Hashtable entries = getEntries();
		String name = entry.getName();
		ZipEntry zipEntry = (ZipEntry) entries.get(name);
		if (zipEntry == null)
			return null;

		PartialInputStream inp = new PartialInputStream(input, 4096, len);
		inp.seek(off + zipEntry.offset);

		if (inp.readLeInt() != LOCSIG)
			throw new ZipException("Wrong Local header signature: " + name);

		inp.skip(4);

		if (zipEntry.getMethod() != inp.readLeShort())
			throw new ZipException("Compression method mismatch: " + name);

		inp.skip(16);

		int nameLen = inp.readLeShort();
		int extraLen = inp.readLeShort();
		inp.skip(nameLen + extraLen);

		inp.setLength((int) zipEntry.getCompressedSize());

		int method = zipEntry.getMethod();
		switch (method) {
			case ZipOutputStream.STORED:
				return inp;
			case ZipOutputStream.DEFLATED:
				inp.addDummyByte();
				final Inflater inf = new Inflater(true);
				final int sz = (int) entry.getSize();
				return new InflaterInputStream(inp, inf) {

					public int available() throws IOException {

						if (sz == -1)
							return super.available();
						if (super.available() != 0)
							return sz - inf.getTotalOut();
						return 0;
					}
				};
			default:
				throw new ZipException("Unknown compression method " + method);
		}
	}


	/**
	 * Returns the number of entries in this zip archive.
	 */
	public int size() {

		try {
			return getEntries().size();
		} catch (IOException ioe) {
			return 0;
		}
	}

	private static final class PartialInputStream extends InputStream {

		// We may need to supply an extra dummy byte to our reader.
		// See Inflater. We use a count here to simplify the logic
		// elsewhere in this class. Note that we ignore the dummy
		// byte in methods where we know it is not needed.
		private int dummyByteCount;

		private Seekable seekable;

		private InputStream input;

		private byte[] buffer;

		private long bufferOffset;

		private int pos;

		private long end;


		public PartialInputStream(InputStream input, int bufferSize, long fileSize) {

			this.input = input;
			seekable = (Seekable) input;
			buffer = new byte[bufferSize];
			bufferOffset = -buffer.length;
			pos = buffer.length;
			end = fileSize;
		}


		void setLength(int length) {

			end = bufferOffset + pos + length;
		}


		private void fillBuffer() throws IOException {

			synchronized (input) {
				long len = end - bufferOffset;
				if (len == 0 && dummyByteCount > 0) {
					buffer[0] = 0;
					dummyByteCount = 0;
				} else {
					seekable.setPosition(bufferOffset);
					input.read(buffer, 0, Math.min(buffer.length, (int) len));
				}
			}
		}


		public int available() {

			long amount = end - (bufferOffset + pos);
			if (amount > Integer.MAX_VALUE)
				return Integer.MAX_VALUE;
			return (int) amount;
		}


		public int read() throws IOException {

			if (bufferOffset + pos >= end + dummyByteCount) {
				return -1;
			}
			if (pos == buffer.length) {
				bufferOffset += buffer.length;
				pos = 0;
				fillBuffer();
			}
			return buffer[pos++] & 0xFF;

		}


		public int read(byte[] b, int off, int len) throws IOException {

			if (len > end + dummyByteCount - (bufferOffset + pos)) {
				len = (int) (end + dummyByteCount - (bufferOffset + pos));
				if (len == 0)
					return -1;
			}

			int totalBytesRead = Math.min(buffer.length - pos, len);
			System.arraycopy(buffer, pos, b, off, totalBytesRead);
			pos += totalBytesRead;
			off += totalBytesRead;
			len -= totalBytesRead;

			while (len > 0) {
				bufferOffset += buffer.length;
				pos = 0;
				fillBuffer();
				int remain = Math.min(buffer.length, len);
				System.arraycopy(buffer, pos, b, off, remain);
				pos += remain;
				off += remain;
				len -= remain;
				totalBytesRead += remain;
			}

			return totalBytesRead;
		}


		public long skip(long amount) throws IOException {

			if (amount < 0)
				return 0;
			if (amount > end - (bufferOffset + pos))
				amount = end - (bufferOffset + pos);
			seek(bufferOffset + pos + amount);
			return amount;
		}


		void seek(long newpos) throws IOException {

			long offset = newpos - bufferOffset;
			if (offset >= 0 && offset <= buffer.length) {
				pos = (int) offset;
			} else {
				bufferOffset = newpos;
				pos = 0;
				fillBuffer();
			}
		}


		void readFully(byte[] buf) throws IOException {

			if (read(buf, 0, buf.length) != buf.length)
				throw new EOFException();
		}


		void readFully(byte[] buf, int off, int len) throws IOException {

			if (read(buf, off, len) != len)
				throw new EOFException();
		}


		int readLeShort() throws IOException {

			int result;
			if (pos + 1 < buffer.length) {
				result = ((buffer[pos + 0] & 0xff) | (buffer[pos + 1] & 0xff) << 8);
				pos += 2;
			} else {
				int b0 = read();
				int b1 = read();
				if (b1 == -1)
					throw new EOFException();
				result = (b0 & 0xff) | (b1 & 0xff) << 8;
			}
			return result;
		}


		int readLeInt() throws IOException {

			int result;
			if (pos + 3 < buffer.length) {
				result = (((buffer[pos + 0] & 0xff) | (buffer[pos + 1] & 0xff) << 8) | ((buffer[pos + 2] & 0xff) | (buffer[pos + 3] & 0xff) << 8) << 16);
				pos += 4;
			} else {
				int b0 = read();
				int b1 = read();
				int b2 = read();
				int b3 = read();
				if (b3 == -1)
					throw new EOFException();
				result = (((b0 & 0xff) | (b1 & 0xff) << 8) | ((b2 & 0xff) | (b3 & 0xff) << 8) << 16);
			}
			return result;
		}


		String readString(int length, int flags) throws IOException {

			if (length > end - (bufferOffset + pos))
				throw new EOFException();

			byte[] b = new byte[length];
			readFully(b);

			// 使中文文件名不乱码
			String decoded = FileNameFix.getString(b, flags);
			return decoded;
		}


		public void addDummyByte() {

			dummyByteCount = 1;
		}
	}


	public void setCallBack(UnzipCallback callback) {

		unzipCallback = callback;
	}
}
