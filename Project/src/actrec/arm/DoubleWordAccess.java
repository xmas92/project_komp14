package actrec.arm;

import ir.tree.Exp;
import actrec.Access;

public class DoubleWordAccess extends Access {
	public Access a1, a2;
	public DoubleWordAccess(Access a1, Access a2) {
		this.a1 = a1;
		this.a2 = a2;
	}
	@Override
	public Exp unExLo(Exp fp) {
		return a1.unEx(fp);
	}
	@Override
	public Exp unExHi(Exp fp) {
		return a2.unEx(fp);
	}
}
