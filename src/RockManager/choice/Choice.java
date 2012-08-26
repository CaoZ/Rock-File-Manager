
package RockManager.choice;

public class Choice {

	private String label;


	public Choice(String label) {

		this.label = label;
	}


	public Choice() {

		this(null);
	}


	public void setLabel(String label) {

		this.label = label;
	}


	public String toString() {

		if (label != null) {
			return label;
		} else {
			return super.toString();
		}
	}

}
