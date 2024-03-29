package ast.expression;

import ast.type.PrimitiveType.Primitive;
import ast.visitor.GenericVisitor;
import ast.visitor.VoidVisitor;

public final class ArrayAccessExpression extends Expression {
	public Expression expr;
	public Expression index;
	public Primitive type;
	public boolean ArrayBoundException;
	public boolean StaticCheck;
	public ArrayAccessExpression(int line, int column,
			Expression expr, Expression index) {
		super(line, column);
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
    
    @Override
    public int Precedence() {
    	return 15;
    }

	@Override
	public boolean IsDoubleWord() {
		return type == Primitive.LongArr;
	}
}
