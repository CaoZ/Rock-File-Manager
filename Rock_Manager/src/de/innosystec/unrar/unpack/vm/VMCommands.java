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
public class VMCommands {

	public static final int VM_MOV = 0;
	public static final int VM_CMP = 1;
	public static final int VM_ADD = 2;
	public static final int VM_SUB = 3;
	public static final int VM_JZ = 4;
	public static final int VM_JNZ = 5;
	public static final int VM_INC = 6;
	public static final int VM_DEC = 7;
	public static final int VM_JMP = 8;
	public static final int VM_XOR = 9;
	public static final int VM_AND = 10;
	public static final int VM_OR = 11;
	public static final int VM_TEST = 12;
	public static final int VM_JS = 13;
	public static final int VM_JNS = 14;
	public static final int VM_JB = 15;
	public static final int VM_JBE = 16;
	public static final int VM_JA = 17;
	public static final int VM_JAE = 18;
	public static final int VM_PUSH = 19;
	public static final int VM_POP = 20;
	public static final int VM_CALL = 21;
	public static final int VM_RET = 22;
	public static final int VM_NOT = 23;
	public static final int VM_SHL = 24;
	public static final int VM_SHR = 25;
	public static final int VM_SAR = 26;
	public static final int VM_NEG = 27;
	public static final int VM_PUSHA = 28;
	public static final int VM_POPA = 29;
	public static final int VM_PUSHF = 30;
	public static final int VM_POPF = 31;
	public static final int VM_MOVZX = 32;
	public static final int VM_MOVSX = 33;
	public static final int VM_XCHG = 34;
	public static final int VM_MUL = 35;
	public static final int VM_DIV = 36;
	public static final int VM_ADC = 37;
	public static final int VM_SBB = 38;
	public static final int VM_PRINT = 39;
	public static final int VM_MOVB = 40;
	public static final int VM_MOVD = 41;
	public static final int VM_CMPB = 42;
	public static final int VM_CMPD = 43;
	public static final int VM_ADDB = 44;
	public static final int VM_ADDD = 45;
	public static final int VM_SUBB = 46;
	public static final int VM_SUBD = 47;
	public static final int VM_INCB = 48;
	public static final int VM_INCD = 49;
	public static final int VM_DECB = 50;
	public static final int VM_DECD = 51;
	public static final int VM_NEGB = 52;
	public static final int VM_NEGD = 53;
	public static final int VM_STANDARD = 54;

	private int vmCommand;

	public VMCommands(int vmCommand) {
		this.vmCommand = vmCommand;
	}

	public int getVMCommand() {
		return vmCommand;
	}

	public boolean equals(int vmCommand) {
		return this.vmCommand == vmCommand;
	}

}
