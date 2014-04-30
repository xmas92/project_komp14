package actrec;

import ir.tree.Exp;

public abstract class Access {
	public ir.tree.Exp unEx(ir.tree.Exp fp) {
		throw new Error();
	}

	public Exp unExLo(Exp fp) {
		throw new Error();
	}

	public Exp unExHi(Exp fp) {
		throw new Error();
	}

	public int GetOffset() {
		throw new Error();
	}
}
