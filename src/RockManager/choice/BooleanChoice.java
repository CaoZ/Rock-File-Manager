
package RockManager.choice;

import RockManager.languages.LangRes;


public class BooleanChoice extends Choice {

	private final boolean value;

	public static final BooleanChoice YES = new BooleanChoice(true);

	public static final BooleanChoice NO = new BooleanChoice(false);


	private BooleanChoice(boolean value) {

		this.value = value;
	}


	public boolean getValue() {

		return value;
	}


	public String toString() {

		if (value == true) {
			return LangRes.get(LangRes.YES);
		} else {
			return LangRes.get(LangRes.NO);
		}

	}

}
