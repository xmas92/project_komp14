package ir.tree;

public class CONST extends Exp {
	public int value;
	public CONST(int value) {
		this.value = value;
	}
	public CONST(String value) {
		this.value = Integer.parseInt(value);
	}
	@Override
	public String toString() {
		return toString(0);
	}
	@Override
	public String toString(int i) {
		String s = PrintIndent(i);
		s += "(CONST " + value + ")";
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
