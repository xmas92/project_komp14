package ast.expression;

import ast.visitor.GenericVisitor;
import ast.visitor.VoidVisitor;

public class UnaryExpression extends Expression {
	public static enum Operator {
		Not
	}
	public Operator op;
	public Expression expr;
	public UnaryExpression(int line, int column, 
			Operator op, Expression expr) {
		super(line, column);
		this.op = op;
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
