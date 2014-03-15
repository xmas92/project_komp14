package ast.statement;

import ast.expression.Expression;
import ast.visitor.GenericVisitor;
import ast.visitor.VoidVisitor;

public final class IfStatement extends Statement {
	public Expression cond;
	public Statement thenstmt;
	public Statement elsestmt;
	public IfStatement(int line, int column,
			Expression cond, Statement thenstmt, Statement elsestmt) {
		super(line, column);
		this.cond = cond;
		this.thenstmt = thenstmt;
		this.elsestmt = elsestmt;
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
