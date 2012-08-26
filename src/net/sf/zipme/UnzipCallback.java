package net.sf.zipme;

public interface UnzipCallback {
	void volumeProgressChanged(long current, long total);
}
