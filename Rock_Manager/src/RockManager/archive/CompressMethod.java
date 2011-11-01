
package RockManager.archive;

import RockManager.languages.LangRes;


public class CompressMethod {

	public static final int STORE = 0;

	public static final int FASTEST = 1;

	public static final int FAST = 3;

	public static final int NORMAL = 6;

	public static final int GOOD = 8;

	public static final int BEST = 9;

	private int method;


	public CompressMethod(int method) {

		if (method != STORE && method != FASTEST && method != FAST && method != NORMAL && method != GOOD
				&& method != BEST) {
			throw new IllegalArgumentException("Wrong compress method!");
		}
		this.method = method;
	}


	public int getMethod() {

		return method;
	}


	public String toString() {

		int langKey = -1;

		switch (method) {

			case STORE:
				langKey = LangRes.COMPRESS_METHOD_STORE;
				break;
			case FASTEST:
				langKey = LangRes.COMPRESS_METHOD_FASTEST;
				break;
			case FAST:
				langKey = LangRes.COMPRESS_METHOD_FAST;
				break;
			case NORMAL:
				langKey = LangRes.COMPRESS_METHOD_NORMAL;
				break;
			case GOOD:
				langKey = LangRes.COMPRESS_METHOD_GOOD;
				break;
			case BEST:
				langKey = LangRes.COMPRESS_METHOD_BEST;
				break;

		}

		return LangRes.get(langKey);

	}

}
