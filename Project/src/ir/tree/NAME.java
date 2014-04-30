package ir.tree;

import actrec.Label;

public class NAME extends Exp {
	public Label label;
	public NAME(Label label) {
		this.label = label;
	}
	@Override
	public String toString() {
		return toString(0);
	}
	@Override
	public String toString(int i) {
		String s = PrintIndent(i);
		s += "(NAME " + label.label + ")";
		return s;
	}
	@Override
	public ExpList kids() {
		return null;
	}
	@Override
	public Exp build(ExpList el) {
		return this;
	}
}
