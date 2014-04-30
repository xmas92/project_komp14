package ir.tree;

import actrec.Label;

public class LABEL extends Stm {
	public Label l;
	public LABEL(Label l) {
		this.l = l;
	}
	@Override
	public String toString() {
		return toString(0);
	}
	@Override
	public String toString(int i) {
		String s = PrintIndent(i);
		s += "(LABEL " + l.label + ")";
		return s;
	}
	@Override
	public ExpList kids() {
		return null;
	}
	@Override
	public Stm build(ExpList el) {
		return this;
	}
}
