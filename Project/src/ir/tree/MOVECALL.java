package ir.tree;

public class MOVECALL extends Stm {
	public TEMP t;
	public CALL c;
	
	public MOVECALL(TEMP t, CALL c) {
		this.t = t;
		this.c = c;
	}

	@Override
	public String toString() {
		return toString(0);
	}

	@Override
	public String toString(int i) {
		String s = PrintIndent(i);
		s += "(MOVECALL\n";
		s += t.toString(i+1);
		s += "\n";
		s += c.toString(i+1);
		s += "\n";
		s += PrintIndent(i);
		s += ")";
		return s;
	}

	@Override
	public ExpList kids() {
		return c.kids();
	}

	@Override
	public Stm build(ExpList el) {
		return new MOVE(t,c.build(el));
	}

}
