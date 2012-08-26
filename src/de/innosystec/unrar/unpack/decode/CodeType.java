/*
 * Copyright (c) 2007 innoSysTec (R) GmbH, Germany. All rights reserved.
 * Original author: Edmund Wagner
 * Creation date: 01.06.2007
 *
 * Source: $HeadURL$
 * Last changed: $LastChangedDate$
 * 
 * Here some html entities which can be used for escaping javadoc tags:
 * "&":  "&#038;" or "&amp;"
 * "<":  "&#060;" or "&lt;"
 * ">":  "&#062;" or "&gt;"
 * "@":  "&#064;" 
 */
package de.innosystec.unrar.unpack.decode;

/**
 * DOCUMENT ME
 * 
 * the unrar licence applies to all junrar source and binary distributions you
 * are not allowed to use this source to re-create the RAR compression algorithm
 * 
 * @author $LastChangedBy$
 * @version $LastChangedRevision$
 */
public class CodeType {
	public static final int CODE_HUFFMAN = 0;
	public static final int CODE_LZ = 1;
	public static final int CODE_LZ2 = 2;
	public static final int CODE_REPEATLZ = 3;
	public static final int CODE_CACHELZ = 4;
	public static final int CODE_STARTFILE = 5;
	public static final int CODE_ENDFILE = 6;
	public static final int CODE_VM = 7;
	public static final int CODE_VMDATA = 8;
}
