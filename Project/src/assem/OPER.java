package assem;

import actrec.Label;
import actrec.Temp;
import actrec.TempList;

public class OPER extends Instr {
	public TempList	dst;
	public TempList	src;
	public Targets	jump;

	public OPER(String a, TempList d, TempList s, Label j) {
		assem = a;
		dst = d;
		src = s;
		jump = new Targets(j);
	}
	public OPER(String a, TempList d, TempList s, Label t, Label f) {
		assem = a;
		dst = d;
		src = s;
		jump = new Targets(t,f);
	}

	public OPER(String a, TempList d, TempList s) {
		assem = a;
		dst = d;
		src = s;
		jump = null;
	}

	public TempList uses() {
		return src;
	}

	public TempList defines() {
		return dst;
	}

	public Targets jumps() {
		return jump;
	}
	@Override
	public boolean ChangeUse(Temp gtemp, Temp vi) {
		return src.swap(gtemp, vi);
	}
	@Override
	public boolean ChangeDef(Temp gtemp, Temp vi) {
		return dst.swap(gtemp, vi);
	}
	@Override
	public void CoalesceTemp(Temp gtemp, Temp gtemp2) {
		if (src != null)
			src.swapAll(gtemp, gtemp2);
		if (dst != null)
			dst.swapAll(gtemp, gtemp2);
	}

}