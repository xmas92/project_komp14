package ast.statement;

import ast.expression.Expression;
import ast.visitor.GenericVisitor;
import ast.visitor.VoidVisitor;

public final class WhileStatement extends Statement {
	public Expression cond;
	public Statement loop;
	public WhileStatement(int line, int column,
			Expression cond, Statement loop) {
		super(line, column);
		this.cond = cond;
		this.loop = loop;
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
