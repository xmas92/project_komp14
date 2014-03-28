package ast.expression;

import ast.Node;
import ast.visitor.GenericVisitor;
import ast.visitor.VoidVisitor;

public abstract class Expression extends Node {
	public Expression(int line, int column) {
		super(line, column);
	}
	
	@Override
    public <A> void accept(VoidVisitor<A> v, A arg) {
        v.visit(this, arg);
    }

    @Override
    public <R, A> R accept(GenericVisitor<R, A> v, A arg) {
        return v.visit(this, arg);
    }

    public int Precedence() {
    	return 0;
    }
}
