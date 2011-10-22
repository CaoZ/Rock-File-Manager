/*
 * Copyright (c) 2007 innoSysTec (R) GmbH, Germany. All rights reserved.
 * Original author: Edmund Wagner
 * Creation date: 31.05.2007
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
package de.innosystec.unrar.unpack.vm;

/**
 * DOCUMENT ME
 * 
 * @author $LastChangedBy$
 * @version $LastChangedRevision$
 */
public class VMStandardFilters {

	public static final int VMSF_NONE = 0;
	public static final int VMSF_E8 = 1;
	public static final int VMSF_E8E9 = 2;
	public static final int VMSF_ITANIUM = 3;
	public static final int VMSF_RGB = 4;
	public static final int VMSF_AUDIO = 5;
	public static final int VMSF_DELTA = 6;
	public static final int VMSF_UPCASE = 7;

	private int filter;

	public VMStandardFilters(int filter) {
		this.filter = filter;
	}

	/**
	 * int, one of VMStandardFilters
	 * 
	 * @return
	 */
	public int getFilter() {
		return filter;
	}

	public boolean equals(int filter) {
		return this.filter == filter;
	}

}
