package ir.tree;

public class SEQ extends Stm {
	public Stm s1, s2;
	public SEQ(Stm s1, Stm s2) {
		this.s1 = s1;
		this.s2 = s2;
	}

	@Override
	public String toString() {
		return toString(0);
	}
	
	@Override
	public String toString(int i) {
		String s = PrintIndent(i);
		s += "(SEQ\n";
		s += s1.toString(i+1);
		s += "\n";
		s += s2.toString(i+1);
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
	public Stm build(ExpList el) {
		// Should not happen
		throw new Error();
	}
}
