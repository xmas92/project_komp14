package ast.expression;

import ast.visitor.GenericVisitor;
import ast.visitor.VoidVisitor;

public class IntegerLiteralExpression extends Expression {
	public String value;
	public IntegerLiteralExpression(int line, int column, String value) {
		super(line, column);
		this.value = value;
		this.setData(new Integer(value));
	}
	
	public IntegerLiteralExpression(int l, int c, int i) {
		super(l,c);
		this.value = Integer.toString(i);
		this.setData(new Integer(i));
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
