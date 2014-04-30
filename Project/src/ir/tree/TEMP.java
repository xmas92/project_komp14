package ir.tree;

import actrec.Temp;

public class TEMP extends Exp {
	public Temp temp;
	public TEMP(Temp temp) {
		this.temp = temp;
	}
	@Override
	public String toString() {
		return toString(0);
	}
	@Override
	public String toString(int i) {
		String s = PrintIndent(i);
		s += "(TEMP t" + temp.temp + ")";
		return s;
	}
	@Override
	public ExpList kids() {
		return null;
	}
	@Override
	public Exp build(ExpList el) {
		return this;
	}
	
}
