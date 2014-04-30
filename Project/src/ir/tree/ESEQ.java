package ir.tree;

public class ESEQ extends Exp {
	public Stm s;
	public Exp e;
	public ESEQ(Stm s, Exp e) {
		this.s = s;
		this.e = e;
	}
	@Override
	public String toString() {
		return toString(0);
	}
	@Override
	public String toString(int i) {
		String s = PrintIndent(i);
		s += "(ESEQ\n";
		s += this.s.toString(i+1);
		s += "\n";
		s += e.toString(i+1);
		s += "\n";
		s += PrintIndent(i);
		s += ")";
		return s;
	}
	@Override
	public ExpList kids() {
		// Should not happen
		throw new Error();
	}
	@Override
	public Exp build(ExpList el) {
		// Should not happen
		throw new Error();
	}
}
