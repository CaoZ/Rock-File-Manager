package de.innosystec.unrar;

public class Enum {

	private final String name;

	public final String name() {
		return name;
	}

	public Enum(String name) {
		this.name = name;
	}

	public String toString() {
		return name;
	}

	public final boolean equals(Object other) {
		return this == other;
	}

	public final int hashCode() {
		return super.hashCode();
	}

}
