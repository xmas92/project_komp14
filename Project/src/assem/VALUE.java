package assem;

import actrec.Temp;
import actrec.TempList;

public class VALUE extends Instr {
	public String value;

	public VALUE(String a, String v) {
		assem = a;
		value = v;
	}

	public TempList uses() {
		return null;
	}

	public TempList defines() {
		return null;
	}

	public Targets jumps() {
		return null;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof VALUE) {
			return ((VALUE)obj).assem.equals(assem);
		}

		return false;
	}

	@Override
	public int hashCode() {
		return assem.hashCode();
	}

	@Override
	public boolean ChangeUse(Temp gtemp, Temp vi) {
		return false;
	}

	@Override
	public boolean ChangeDef(Temp gtemp, Temp vi) {
		return false;
	}

	@Override
	public void CoalesceTemp(Temp gtemp, Temp gtemp2) {
	}
}