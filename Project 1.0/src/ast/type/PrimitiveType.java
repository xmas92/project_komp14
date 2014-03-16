package ast.type;

import ast.visitor.GenericVisitor;
import ast.visitor.VoidVisitor;

public final class PrimitiveType extends Type {

	public static enum Primitive {
		Int, IntArr, Long, LongArr, Boolean
	}
	public Primitive primitive;
	public PrimitiveType(int line, int column, Primitive primitive) {
		super(line, column);
		this.primitive = primitive;
	}
	
	@Override
    public <A> void accept(VoidVisitor<A> v, A arg) {
        v.visit(this, arg);
    }

    @Override
    public <R, A> R accept(GenericVisitor<R, A> v, A arg) {
        return v.visit(this, arg);
    }
	
	@Override
	public boolean equals(Object t) {
		if (t == null) return false;
		if (t instanceof PrimitiveType) {
			return ((PrimitiveType)t).primitive == primitive;
		}
		return false;
	}

	@Override
	public boolean IsType(Primitive p) {
		return p == this.primitive;
	}

	@Override
	public String toString() {
		switch (primitive) {
		case Boolean:
			return "Boolean";
		case Int:
			return "Int";
		case IntArr:
			return "IntArray";
		case Long:
			return "Long";
		case LongArr:
			return "LongArray";

		default:
			return "Unkown";
		}
	}

	@Override
	public boolean IsDoubleWord() {
		return this.primitive == Primitive.Long;
	}
}
