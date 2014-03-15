package ast.statement;

import ast.expression.Expression;
import ast.visitor.GenericVisitor;
import ast.visitor.VoidVisitor;

public final class PrintStatement extends Statement {
	public Expression expr;
	public PrintStatement(int line, int column,
			Expression expr) {
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
