package ast.type;

public final class PrimitiveType extends Type {

	public static enum Primitive {
		Int, IntArr, Long, LongArr, Boolean
	}
	
	public PrimitiveType(int line, int column, Primitive primative) {
		super(line, column);
		// TODO Auto-generated constructor stub
	}

}
