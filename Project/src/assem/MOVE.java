package assem;

import actrec.Temp;
import actrec.TempList;

public class MOVE extends Instr {
	public Temp	dst;
	public Temp	src;

	public MOVE(String a, Temp d, Temp s) {
		assem = a;
		dst = d;
		src = s;
	}

	public TempList uses() {
		if (src == null) return null;

		return new TempList(src, null);
	}

	public TempList defines() {
		if (dst == null) return null;

		return new TempList(dst, null);
	}

	public Targets jumps() {
		return null;
	}

	@Override
	public boolean ChangeUse(Temp gtemp, Temp vi) {
		if (src.equals(gtemp)) {
			src = vi;
			return true;
		}
		return false;
	}

	@Override
	public boolean ChangeDef(Temp gtemp, Temp vi) {
		if (dst.equals(gtemp)) {
			dst = vi;
			return true;
		}
		return false;
	}

	@Override
	public void CoalesceTemp(Temp gtemp, Temp gtemp2) {
		if (src.equals(gtemp)) {
			src = gtemp2;
		}
		if (dst.equals(gtemp)) {
			dst = gtemp2;
		}
	}

}