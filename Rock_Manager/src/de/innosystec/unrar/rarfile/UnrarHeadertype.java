/*
 * Copyright (c) 2007 innoSysTec (R) GmbH, Germany. All rights reserved.
 * Original author: Edmund Wagner
 * Creation date: 22.05.2007
 *
 * Source: $HeadURL$
 * Last changed: $LastChangedDate$
 * 
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
package de.innosystec.unrar.rarfile;

/**
 * DOCUMENT MEjk
 * 
 * @author $LastChangedBy$
 * @version $LastChangedRevision$
 */
public class UnrarHeadertype {

	/**
	 * 
	 */
	public static final byte MainHeader = ((byte) 0x73);

	/**
	 * 
	 */
	public static final byte MarkHeader = ((byte) 0x72);

	/**
	 * 
	 */
	public static final byte FileHeader = ((byte) 0x74);

	/**
	 * 
	 */
	public static final byte CommHeader = ((byte) 0x75);

	/**
	 * 
	 */
	public static final byte AvHeader = ((byte) 0x76);

	/**
	 * 
	 */
	public static final byte SubHeader = ((byte) 0x77);

	/**
	 * 
	 */
	public static final byte ProtectHeader = ((byte) 0x78);

	/**
	 * 
	 */
	public static final byte SignHeader = ((byte) 0x79);

	/**
	 * 
	 */
	public static final byte NewSubHeader = ((byte) 0x7a);

	/**
	 * 
	 */
	public static final byte EndArcHeader = ((byte) 0x7b);

	private byte headerByte;

	private UnrarHeadertype(byte headerByte) {
		this.headerByte = headerByte;
	}

	/**
	 * Return true if the given byte is equal to the enum's byte
	 * 
	 * @param header
	 * @return true if the given byte is equal to the enum's byte
	 */
	public boolean equals(byte header) {
		return headerByte == header;
	}

	/**
	 * the header byte of this enum
	 * 
	 * @return the header byte of this enum
	 */
	public byte getHeaderByte() {
		return headerByte;
	}

}
