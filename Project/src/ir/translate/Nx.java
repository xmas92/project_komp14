package ir.translate;

import ir.tree.Exp;
import ir.tree.Stm;
import actrec.Label;

public class Nx extends Tr {
	public Stm s;
	public Nx(Stm s) {
		this.s = s;
	}
	@Override
	public Exp unEx() {
		// Will never happen... right?
		return null;
	}

	@Override
	public Stm unNx() {
		return s;
	}

	@Override
	public Stm unCx(Label t, Label f) {
		// Will never happen... right?
		return null;
	}
	@Override
	public Exp unExLo() {
		// Will never happen... right?
		return null;
	}
	@Override
	public Exp unExHi() {
		// Will never happen... right?
		return null;
	}

}
