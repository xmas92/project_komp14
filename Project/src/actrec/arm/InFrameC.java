package actrec.arm;

import ir.tree.Exp;
import ir.tree.TEMP;
import actrec.Access;
import actrec.Temp;
import actrec.TempList;
import assem.Instr;
import assem.OPER;

public class InFrameC extends Access {
	public int offset;
	public Temp t;
	public InFrameC(int offset) {
		this.offset = offset;
		this.t = new Temp();
	}
	public Instr init(Temp sp) {
		return new OPER("ldr `d0, [ sp, #`f" + offset + " ]", new TempList(t, null), null);
	}
	@Override
	public Exp unEx(Exp sp) {
		return new TEMP(t);
	}
}
