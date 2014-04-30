package ir.translate;

import actrec.Label;
import ir.tree.Exp;
import ir.tree.Stm;

public abstract class Tr {
	public abstract Exp unEx();
	public abstract Stm unNx();
	public abstract Stm unCx(Label t, Label f);
	public abstract Exp unExLo();
	public abstract Exp unExHi();
	public boolean HasHighValue() {
		return unExHi() != null;
	}
}
