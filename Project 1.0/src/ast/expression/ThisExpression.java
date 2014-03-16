package ast.expression;

import ast.visitor.GenericVisitor;
import ast.visitor.VoidVisitor;

public class ThisExpression extends Expression {

	public ThisExpression(int line, int column) {
		super(line, column);
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

}
