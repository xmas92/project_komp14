package actrec;

public class Label {
	private static int idx = 0;
	public final String label;
	public Label() {
		label = "L"+idx++;
	}
	public Label(String s) {
		label = s;
	}
}
