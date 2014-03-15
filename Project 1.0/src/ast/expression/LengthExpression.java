package ast.expression;

import ast.visitor.GenericVisitor;
import ast.visitor.VoidVisitor;

public final class LengthExpression extends Expression {
	public Expression expr;
	public LengthExpression(int line, int column, Expression expr) {
		super(line, column);
		this.expr = expr;
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
