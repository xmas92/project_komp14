package ast.expression;

import ast.type.PrimitiveType.Primitive;
import ast.visitor.GenericVisitor;
import ast.visitor.VoidVisitor;

public class NewArrayExpression extends Expression {
	public Expression size;
	public Primitive primitive;
	public NewArrayExpression(int line, int column, 
			Expression size, Primitive primative) {
		super(line, column);
		this.size = size;
		this.primitive = primative;
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
    	return 13;
    }

}
