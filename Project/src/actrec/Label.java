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
	@Override
	public int hashCode() {
		return label.hashCode();
	}
	@Override
	public boolean equals(Object o) {
		if (o instanceof Label)
			return ((Label)o).label.equals(label);
		return false;
	}
}
