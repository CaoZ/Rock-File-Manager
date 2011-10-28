/*
 * Copyright (c) 2007 innoSysTec (R) GmbH, Germany. All rights reserved.
 * Original author: Edmund Wagner
 * Creation date: 22.05.2007
 *
 * Source: $HeadURL$
 * Last changed: $LastChangedDate$
 *
 * the unrar licence applies to all junrar source and binary distributions
 * you are not allowed to use this source to re-create the RAR compression
 * algorithm
 *
 * Here some html entities which can be used for escaping javadoc tags:
 * "&":  "&#038;" or "&amp;"
 * "<":  "&#060;" or "&lt;"
 * ">":  "&#062;" or "&gt;"
 * "@":  "&#064;"
 */

package de.innosystec.unrar;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Vector;
import javax.microedition.io.file.FileConnection;
import RockManager.ui.progressPopup.ProgressIndicator;
import de.innosystec.unrar.exception.RarException;
import de.innosystec.unrar.exception.RarExceptionType;
import de.innosystec.unrar.io.IReadOnlyAccess;
import de.innosystec.unrar.io.ReadOnlyAccessFile;
import de.innosystec.unrar.rarfile.AVHeader;
import de.innosystec.unrar.rarfile.BaseBlock;
import de.innosystec.unrar.rarfile.BlockHeader;
import de.innosystec.unrar.rarfile.CommentHeader;
import de.innosystec.unrar.rarfile.EAHeader;
import de.innosystec.unrar.rarfile.EndArcHeader;
import de.innosystec.unrar.rarfile.FileHeader;
import de.innosystec.unrar.rarfile.MacInfoHeader;
import de.innosystec.unrar.rarfile.MainHeader;
import de.innosystec.unrar.rarfile.MarkHeader;
import de.innosystec.unrar.rarfile.ProtectHeader;
import de.innosystec.unrar.rarfile.SignHeader;
import de.innosystec.unrar.rarfile.SubBlockHeader;
import de.innosystec.unrar.rarfile.SubBlockHeaderType;
import de.innosystec.unrar.rarfile.UnixOwnersHeader;
import de.innosystec.unrar.rarfile.UnrarHeadertype;
import de.innosystec.unrar.unpack.ComprDataIO;
import de.innosystec.unrar.unpack.Unpack;


/**
 * The Main Rar Class; represents a rar Archive
 * 
 * @author $LastChangedBy$
 * @version $LastChangedRevision$
 */
public class Archive {

	private FileConnection file;

	private ReadOnlyAccessFile rof;

	private UnrarCallback unrarCallback;

	private final ComprDataIO dataIO;

	private final Vector headers = new Vector();

	private MarkHeader markHead = null;

	private MainHeader newMhd = null;

	private EndArcHeader endHeader = null;

	private Unpack unpack;

	/** Archive data CRC. */
	private long arcDataCRC = 0xffffffff;

	private int currentHeaderIndex;

	private boolean encrypted = false;

	private int sfxSize = 0;

	/** Size of packed data in current file. */
	private long totalPackedSize = 0L;


	// /** Number of bytes of compressed data read from current file. */
	// private long totalPackedRead = 0L;

	public Archive(FileConnection file) throws IOException {

		this(file, null);
	}


	/**
	 * create a new archive object using the given file
	 * 
	 * @param file
	 *            the file to extract
	 * @throws RarException
	 */
	public Archive(FileConnection file, UnrarCallback callback) throws IOException {

		unrarCallback = callback;
		setFile(file);
		dataIO = new ComprDataIO(this);
	}


	public FileConnection getFile() {

		return file;
	}


	void setFile(FileConnection file) throws IOException {

		this.file = file;
		totalPackedSize = 0L;
		// totalPackedRead = 0L;
		close();
		rof = new ReadOnlyAccessFile(file);
		try {
			readHeaders();
		} catch (Exception e) {
			close();
			throw new IOException("Exception in archive constructor, maybe file is encrypted or corrupted.");
			// "exception in archive constructor maybe file is encrypted "
			// + "or currupt", e);
			// ignore exceptions to allow exraction of working files in
			// corrupt archive
		}
		// Calculate size of packed data

		for (int i = 0; i < headers.size(); i++) {
			BaseBlock block = (BaseBlock) headers.elementAt(i);
			if (block.getHeaderType() == UnrarHeadertype.FileHeader) {
				totalPackedSize += ((FileHeader) block).getFullPackSize();
			}
		}

		/*
		 * if (unrarCallback != null) {
		 * unrarCallback.volumeProgressChanged(totalPackedRead,
		 * totalPackedSize); }
		 */
	}


	// public void bytesReadRead(int count) {
	//
	// if (count > 0) {
	// totalPackedRead += count;
	// if (unrarCallback != null) {
	// unrarCallback.volumeProgressChanged(totalPackedRead, totalPackedSize);
	// }
	// }
	// }

	public IReadOnlyAccess getRof() {

		return rof;
	}


	/**
	 * @return returns all file headers of the archive
	 */
	public Vector getFileHeaders() {

		Vector vector = new Vector();
		for (int i = 0; i < headers.size(); i++) {
			BaseBlock block = (BaseBlock) headers.elementAt(i);
			if (block.getHeaderType() == UnrarHeadertype.FileHeader) {
				vector.addElement(block);
			}
		}
		return vector;
	}


	public FileHeader nextFileHeader() {

		int n = headers.size();
		while (currentHeaderIndex < n) {
			BaseBlock block = (BaseBlock) headers.elementAt(currentHeaderIndex++);
			if (block.getHeaderType() == UnrarHeadertype.FileHeader) {
				return (FileHeader) block;
			}
		}
		return null;
	}


	public UnrarCallback getUnrarCallback() {

		return unrarCallback;
	}


	public void setUnrarCallback(UnrarCallback callback) {

		unrarCallback = callback;
	}


	/**
	 * @return whether the archive is encrypted
	 */
	public boolean isEncrypted() {

		if (newMhd != null) {
			return newMhd.isEncrypted();
		} else {
			throw new NullPointerException("mainheader is null");
		}
	}


	/**
	 * Read the headers of the archive
	 * 
	 * @throws RarException
	 */
	private void readHeaders() throws IOException, RarException {

		markHead = null;
		newMhd = null;
		endHeader = null;
		headers.removeAllElements();
		currentHeaderIndex = 0;
		int toRead = 0;

		long fileLength = this.file.fileSize();

		while (true) {
			int size = 0;
			long newpos = 0;
			byte[] baseBlockBuffer = new byte[BaseBlock.BaseBlockSize];

			long position = rof.getPosition();

			// Weird, but is trying to read beyond the end of the file
			if (position >= fileLength) {
				break;
			}

			// logger.info("\n--------reading header--------");
			size = rof.readFully(baseBlockBuffer, BaseBlock.BaseBlockSize);
			if (size == 0) {
				break;
			}
			BaseBlock block = new BaseBlock(baseBlockBuffer);

			block.setPositionInFile(position);

			switch (block.getHeaderType()) {

				case UnrarHeadertype.MarkHeader:
					markHead = new MarkHeader(block);
					if (!markHead.isSignature()) {
						throw new RarException(RarExceptionType.badRarArchive);
					}
					headers.addElement(markHead);
					// headers.add(markHead);
					// markHead.print();
					break;

				case UnrarHeadertype.MainHeader:
					int mainHeaderSize = 0;
					toRead = block.hasEncryptVersion() ? MainHeader.mainHeaderSizeWithEnc : MainHeader.mainHeaderSize;
					byte[] mainbuff = new byte[toRead];
					mainHeaderSize = rof.readFully(mainbuff, toRead);
					MainHeader mainhead = new MainHeader(block, mainbuff);
					headers.addElement(mainhead);
					this.newMhd = mainhead;
					if (newMhd.isEncrypted()) {
						throw new RarException(RarExceptionType.rarEncryptedException);
					}
					// mainhead.print();
					break;

				case UnrarHeadertype.SignHeader:
					int signHeaderSize = 0;
					toRead = SignHeader.signHeaderSize;
					byte[] signBuff = new byte[toRead];
					signHeaderSize = rof.readFully(signBuff, toRead);
					SignHeader signHead = new SignHeader(block, signBuff);
					headers.addElement(signHead);
					// logger.info("HeaderType: SignHeader");

					break;

				case UnrarHeadertype.AvHeader:
					int avHeaderSize = 0;
					toRead = AVHeader.avHeaderSize;
					byte[] avBuff = new byte[toRead];
					avHeaderSize = rof.readFully(avBuff, toRead);
					AVHeader avHead = new AVHeader(block, avBuff);
					headers.addElement(avHead);
					// logger.info("headertype: AVHeader");
					break;

				case UnrarHeadertype.CommHeader:
					int commHeaderSize = 0;
					toRead = CommentHeader.commentHeaderSize;
					byte[] commBuff = new byte[toRead];
					commHeaderSize = rof.readFully(commBuff, toRead);
					CommentHeader commHead = new CommentHeader(block, commBuff);
					headers.addElement(commHead);
					// logger.info("method: "+commHead.getUnpMethod()+"; 0x"+
					// Integer.toHexString(commHead.getUnpMethod()));
					newpos = commHead.getPositionInFile() + commHead.getHeaderSize();
					rof.setPosition(newpos);
					break;

				case UnrarHeadertype.EndArcHeader:
					toRead = 0;
					if (block.hasArchiveDataCRC()) {
						toRead += EndArcHeader.endArcArchiveDataCrcSize;
					}
					if (block.hasVolumeNumber()) {
						toRead += EndArcHeader.endArcVolumeNumberSize;
					}
					EndArcHeader endArcHead;
					if (toRead > 0) {
						int endArcHeaderSize = 0;
						byte[] endArchBuff = new byte[toRead];
						endArcHeaderSize = rof.readFully(endArchBuff, toRead);
						endArcHead = new EndArcHeader(block, endArchBuff);
						// logger.info("HeaderType: endarch\ndatacrc:"+
						// endArcHead.getArchiveDataCRC());
					} else {
						// logger.info("HeaderType: endarch - no Data");
						endArcHead = new EndArcHeader(block, null);
					}
					headers.addElement(endArcHead);
					this.endHeader = endArcHead;
					// logger.info("\n--------end header--------");
					return;

				default:
					byte[] blockHeaderBuffer = new byte[BlockHeader.blockHeaderSize];
					int bhsize = rof.readFully(blockHeaderBuffer, BlockHeader.blockHeaderSize);
					BlockHeader blockHead = new BlockHeader(block, blockHeaderBuffer);

					switch (blockHead.getHeaderType()) {
						case UnrarHeadertype.NewSubHeader:
						case UnrarHeadertype.FileHeader:
							toRead = blockHead.getHeaderSize() - BlockHeader.BaseBlockSize
									- BlockHeader.blockHeaderSize;
							byte[] fileHeaderBuffer = new byte[toRead];
							int fhsize = rof.readFully(fileHeaderBuffer, toRead);

							FileHeader fh = new FileHeader(blockHead, fileHeaderBuffer);
							// if (DEBUG) {
							// fh.print();
							// }
							headers.addElement(fh);
							newpos = fh.getPositionInFile() + fh.getHeaderSize() + fh.getFullPackSize();
							rof.setPosition(newpos);
							if (unrarCallback != null) {
								unrarCallback.volumeProgressChanged(newpos, fileLength);
							}
							break;

						case UnrarHeadertype.ProtectHeader:
							toRead = blockHead.getHeaderSize() - BlockHeader.BaseBlockSize
									- BlockHeader.blockHeaderSize;
							byte[] protectHeaderBuffer = new byte[toRead];
							int phsize = rof.readFully(protectHeaderBuffer, toRead);
							ProtectHeader ph = new ProtectHeader(blockHead, protectHeaderBuffer);

							// logger.info("totalblocks"+ph.getTotalBlocks());
							newpos = ph.getPositionInFile() + ph.getHeaderSize();
							rof.setPosition(newpos);
							break;

						case UnrarHeadertype.SubHeader: {
							byte[] subHeadbuffer = new byte[SubBlockHeader.SubBlockHeaderSize];
							int subheadersize = rof.readFully(subHeadbuffer, SubBlockHeader.SubBlockHeaderSize);
							SubBlockHeader subHead = new SubBlockHeader(blockHead, subHeadbuffer);

							switch (subHead.getSubType()) {
								case SubBlockHeaderType.MAC_HEAD: {
									byte[] macHeaderbuffer = new byte[MacInfoHeader.MacInfoHeaderSize];
									int macheadersize = rof.readFully(macHeaderbuffer, MacInfoHeader.MacInfoHeaderSize);
									MacInfoHeader macHeader = new MacInfoHeader(subHead, macHeaderbuffer);
									headers.addElement(macHeader);

									break;
								}
									// TODO implement other subheaders
								case SubBlockHeaderType.BEEA_HEAD:
									break;
								case SubBlockHeaderType.EA_HEAD: {
									byte[] eaHeaderBuffer = new byte[EAHeader.EAHeaderSize];
									int eaheadersize = rof.readFully(eaHeaderBuffer, EAHeader.EAHeaderSize);
									EAHeader eaHeader = new EAHeader(subHead, eaHeaderBuffer);
									headers.addElement(eaHeader);

									break;
								}
								case SubBlockHeaderType.NTACL_HEAD:
									break;
								case SubBlockHeaderType.STREAM_HEAD:
									break;
								case SubBlockHeaderType.UO_HEAD:
									toRead = subHead.getHeaderSize();
									toRead -= BaseBlock.BaseBlockSize;
									toRead -= BlockHeader.blockHeaderSize;
									toRead -= SubBlockHeader.SubBlockHeaderSize;
									byte[] uoHeaderBuffer = new byte[toRead];
									int uoHeaderSize = rof.readFully(uoHeaderBuffer, toRead);
									UnixOwnersHeader uoHeader = new UnixOwnersHeader(subHead, uoHeaderBuffer);
									headers.addElement(uoHeader);
									break;
								default:
									break;
							}

							break;
						}
						default:
							throw new RarException(RarExceptionType.notRarArchive);

					}
			}
			// logger.info("\n--------end header--------");
		}
	}


	/**
	 * Extract the file specified by the given header and write it to the
	 * supplied output stream
	 * 
	 * @param header
	 *            the header to be extracted
	 * @param os
	 *            the outputstream
	 * @throws RarException
	 */
	public void extractFile(FileHeader hd, OutputStream os) throws RarException {

		if (!headers.contains(hd)) {
			throw new RarException(RarExceptionType.headerNotInArchive);
		}
		try {
			doExtractFile(hd, os);
		} catch (Exception e) {
			if (e instanceof RarException) {
				throw (RarException) e;
			} else {
				throw new RarException(e);
			}
		}
	}


	private void doExtractFile(FileHeader hd, OutputStream os) throws Exception {

		dataIO.init(os);
		dataIO.init(hd);
		dataIO.setUnpFileCRC(this.isOldFormat() ? 0 : 0xffFFffFF);
		if (unpack == null) {
			unpack = new Unpack(dataIO);
		}
		if (!hd.isSolid()) {
			unpack.init(null);
		}
		unpack.setDestSize(hd.getFullUnpackSize());
		try {
			unpack.doUnpack(hd.getUnpVersion(), hd.isSolid());
			// Verify file CRC
			hd = dataIO.getSubHeader();
			long actualCRC = hd.isSplitAfter() ? ~dataIO.getPackedCRC() : ~dataIO.getUnpFileCRC();
			int expectedCRC = hd.getFileCRC();
			if (actualCRC != expectedCRC) {
				throw new RarException(RarExceptionType.crcError);
			}
			// if (!hd.isSplitAfter()) {
			// // Verify file CRC
			// if(~dataIO.getUnpFileCRC() != hd.getFileCRC()){
			// throw new RarException(RarExceptionType.crcError);
			// }
			// }
		} catch (Exception e) {
			unpack.cleanUp();
			throw e;
		}
	}


	/**
	 * @return returns the main header of this archive
	 */
	public MainHeader getMainHeader() {

		return newMhd;
	}


	/**
	 * @return whether the archive is old format
	 */
	public boolean isOldFormat() {

		return markHead.isOldFormat();
	}


	/** Close the underlying compressed file. */
	public void close() throws IOException {

		if (rof != null) {
			rof.close();
			rof = null;
		}
		if (unpack != null) {
			unpack.cleanUp();
		}
	}


	public void setExtractIndicator(ProgressIndicator indicator) {

		dataIO.setExtractIndicator(indicator);

	}
	
}
