package actrec.arm;

import ir.tree.Exp;
import ir.tree.TEMP;
import actrec.Access;
import actrec.Temp;

public class InReg extends Access {
	public Temp t;
	public InReg(Temp t) {
		this.t = t;
	}
	@Override
	public Exp unEx(Exp fp) {
		return new TEMP(t);
	}
}
