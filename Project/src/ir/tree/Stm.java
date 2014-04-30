package ir.tree;

public abstract class Stm {
	
	public static Stm NOOP = new EXPS(new CONST(0));

	public abstract String toString();
	public abstract String toString(int i);
	protected String PrintIndent(int i) {
		String s = "";
		for (int t = 0; t < i; t++)
			s += "  ";
		return s;
	}
	public abstract ExpList kids();
	public abstract Stm build(ExpList el);
}
