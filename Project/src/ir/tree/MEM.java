package ir.tree;

public class MEM extends Exp {
	public Exp e;
	public MEM(Exp e) {
		this.e = e;
	}
	@Override
	public String toString() {
		return toString(0);
	}
	@Override
	public String toString(int i) {
		String s = PrintIndent(i);
		s += "(MEM\n";
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
	public Exp build(ExpList el) {
		return new MEM(el.head);
	}
}
