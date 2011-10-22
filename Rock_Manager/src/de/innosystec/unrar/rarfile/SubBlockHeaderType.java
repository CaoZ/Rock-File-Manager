/*
 * Copyright (c) 2007 innoSysTec (R) GmbH, Germany. All rights reserved.
 * Original author: Edmund Wagner
 * Creation date: 20.11.2007
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

public class SubBlockHeaderType {

	public static final short EA_HEAD = ((short) 0x100);
	public static final short UO_HEAD = ((short) 0x101);
	public static final short MAC_HEAD = ((short) 0x102);
	public static final short BEEA_HEAD = ((short) 0x103);
	public static final short NTACL_HEAD = ((short) 0x104);
	public static final short STREAM_HEAD = ((short) 0x105);

	private short subblocktype;

	private SubBlockHeaderType(short subblocktype) {
		this.subblocktype = subblocktype;
	}

	/**
	 * Return true if the given value is equal to the enum's value
	 * 
	 * @param subblocktype
	 * @return true if the given value is equal to the enum's value
	 */
	public boolean equals(short subblocktype) {
		return this.subblocktype == subblocktype;
	}

	/**
	 * @return the short representation of this enum
	 */
	public short getSubblocktype() {
		return subblocktype;
	}
}
