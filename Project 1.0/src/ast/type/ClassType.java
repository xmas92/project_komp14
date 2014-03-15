package ast.type;

import ast.visitor.GenericVisitor;
import ast.visitor.VoidVisitor;

public final class ClassType extends Type {
	public String id;
	public ClassType(int line, int column, String id) {
		super(line, column);
		this.id = id;
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
		if (t instanceof ClassType) {
			if (((ClassType)t).id == null) return id == null;
			return ((ClassType)t).id == id;
		}
		return false;
	}

	@Override
	public boolean IsType(String id) {
		return this.id.equals(id);
	}
	

	@Override
	public String toString() {
		return this.id;
	}
}
