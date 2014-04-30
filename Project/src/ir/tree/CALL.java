package ir.tree;


public class CALL extends Exp {
	public Exp f;
	public ExpList explist;
	public CALL(Exp f, ExpList explist) {
		this.f = f;
		this.explist = explist;
	}
	@Override
	public String toString() {
		return toString(0);
	}
	@Override
	public String toString(int i) {
		String s = PrintIndent(i);
		s += "(CALL\n";
		s += f.toString(i+1);
		s += "\n";
		for (Exp e : explist)
			s += e.toString(i+1) + "\n";
		s += PrintIndent(i);
		s += ")";
		return s;
	}
	@Override
	public ExpList kids() {
		return new ExpList(f, explist);
	}
	@Override
	public Exp build(ExpList el) {
		return new CALL(el.head, el.tail);
	}
}
