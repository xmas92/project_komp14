package ir.tree;

public class EXPS extends Stm {
	public Exp e;
	public EXPS(Exp e) {
		this.e = e;
	}
	@Override
	public String toString() {
		return toString(0);
	}
	@Override
	public String toString(int i) {
		String s = PrintIndent(i);
		s += "(EXPS\n";
		s += e.toString(i+1);
		s += "\n";
		s += PrintIndent(i);
		s += ")";
		return s;
	}
	@Override
	public ExpList kids() {
		return new ExpList(e, null);
	}
	@Override
	public Stm build(ExpList el) {
		return new EXPS(el.head);
	}
}
