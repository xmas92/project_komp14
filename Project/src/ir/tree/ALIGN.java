package ir.tree;

public class ALIGN extends Exp {

	@Override
	public String toString() {
		return toString(0);
	}

	@Override
	public String toString(int i) {
		String s = PrintIndent(i);
		s += "(Align)";
		return s;
	}

	@Override
	public ExpList kids() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Exp build(ExpList el) {
		return this;
	}

}
