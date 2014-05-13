package ir.tree;

public class COMPILERCONST extends Exp {
	public int value;
	public char c;
	public COMPILERCONST(int value, char c) {
		this.value = value;
		this.c = c;
	}
	public COMPILERCONST(String value, char c) {
		this.value = Integer.parseInt(value);
		this.c = c;
	}
	@Override
	public String toString() {
		return toString(0);
	}
	@Override
	public String toString(int i) {
		String s = PrintIndent(i);
		s += "(COMPILERCONST " + c + value + ")";
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
