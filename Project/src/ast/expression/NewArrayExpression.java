package ast.expression;

import compiler.util.Report;

import ast.type.PrimitiveType.Primitive;
import ast.visitor.GenericVisitor;
import ast.visitor.VoidVisitor;

public class NewArrayExpression extends Expression {
	public Expression size;
	public Primitive primitive;
	public NewArrayExpression(int line, int column, 
			Expression size, Primitive primative, int dim) {
		super(line, column);
		this.size = size;
		this.primitive = primative;
		if (dim != 1) {
			Report.ExitWithError("MultiDim Array Creation Not Allowed. (NewArrayExpression) (%d:%d)",
					this.getBeginLine(), this.getBeginColumn());
		}
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

	@Override
	public boolean IsDoubleWord() {
		return false;
	} 
}
