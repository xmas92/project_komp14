package ast.statement;

import ast.declaration.VariableDeclaration;
import ast.expression.Expression;
import ast.visitor.GenericVisitor;
import ast.visitor.VoidVisitor;

public final class AssignmentStatement extends Statement {
	public String id;
	public Expression index;
	public Expression expr;
	public VariableDeclaration decl;
	public AssignmentStatement(int line, int column,
			String id, Expression index, Expression expr) {
		super(line, column);
		this.id = id;
		this.expr = expr;
		this.index = index;
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
