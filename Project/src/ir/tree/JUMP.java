package ir.tree;

import actrec.Label;

public class JUMP extends Stm {
	public Label l;
	public JUMP(Label l) {
		this.l = l;
	}
	@Override
	public String toString() {
		return toString(0);
	}
	@Override
	public String toString(int i) {
		String s = PrintIndent(i);
		s += "(JUMP " + l.label + ")";
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
