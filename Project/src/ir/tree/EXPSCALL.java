package ir.tree;

public class EXPSCALL extends Stm {
	public CALL c;
	public EXPSCALL(CALL c) {
		this.c = c;
	}
	@Override
	public String toString() {
		return toString(0);
	}

	@Override
	public String toString(int i) {
		String s = PrintIndent(i);
		s += "(EXPSCALL\n";
		s += c.toString(i+1);
		s += "\n";
		s += PrintIndent(i);;
		s += ")";
		return s;
	}

	@Override
	public ExpList kids() {
		return c.kids();
	}

	@Override
	public Stm build(ExpList el) {
		return new EXPS(c.build(el));
	}

}
