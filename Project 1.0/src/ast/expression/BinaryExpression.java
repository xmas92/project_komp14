package ast.expression;

import compiler.util.Report;

import ast.type.PrimitiveType;
import ast.type.PrimitiveType.Primitive;
import ast.visitor.GenericVisitor;
import ast.visitor.VoidVisitor;

public final class BinaryExpression extends Expression {
	public static enum Operator {
		And, Or, 
		Less, LessEq, Greater, GreaterEq, Eq, NotEq,
		Plus, Minus, Times
	}
	public Operator op;
	public Expression e1;
	public Expression e2;
	public BinaryExpression(int line, int column,
			Operator op, Expression e1, Expression e2) {
		super(line, column);
		this.op = op;
		this.e1 = e1;
		this.e2 = e2;
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
    public String toString() {
    	switch (this.op) {
		case Greater:
			return "Greater";
		case GreaterEq:
			return "GreaterEq";
		case Less:
			return "Less";
		case LessEq:
			return "LessEq";
		case Minus:
			return "Minus";
		case Plus:
			return "Plus";
		case Times:
			return "Times";
		case And:
			return "Times";
		case Or:
			return "Or";
		case Eq:
			return "Eq";
		case NotEq:
			return "NotEq";

		default:
			return "Unkown";
		}
    }
}
