package ast.expression;

import ast.visitor.GenericVisitor;
import ast.visitor.VoidVisitor;

public final class NewClassExpression extends Expression {
	public String id;
	public NewClassExpression(int line, int column, String id) {
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

}
