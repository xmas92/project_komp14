package ast.expression;

import ast.visitor.GenericVisitor;
import ast.visitor.VoidVisitor;

public final class BooleanLiteralExpression extends Expression {
	public boolean value;
	public BooleanLiteralExpression(int line, int column, boolean value) {
		super(line, column);
		this.value = value;
		setData(new Boolean(value));
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
    	return 16;
    }
    
	@Override
	public boolean IsDoubleWord() {
		return false;
	}
}
