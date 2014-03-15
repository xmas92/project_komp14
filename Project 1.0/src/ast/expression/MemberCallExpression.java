package ast.expression;

import java.util.List;

import ast.declaration.MethodDeclaration;
import ast.visitor.GenericVisitor;
import ast.visitor.VoidVisitor;

public final class MemberCallExpression extends Expression {
	public Expression expr;
	public String id;
	public List<Expression> exprlist;
	public MethodDeclaration decl;
	public MemberCallExpression(int line, int column,
			Expression expr, String id, List<Expression> exprlist) {
		super(line, column);
		this.expr = expr;
		this.id = id;
		this.exprlist = exprlist;
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
