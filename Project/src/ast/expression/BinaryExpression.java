package ast.expression;

import ast.type.PrimitiveType.Primitive;
import ast.visitor.GenericVisitor;
import ast.visitor.VoidVisitor;

public final class BinaryExpression extends Expression {
	public static enum Operator {
		And, Or, // Boolean algebra
		Less, LessEq, Greater, GreaterEq, Eq, NotEq, // Relations
		Plus, Minus, Times,	// Arithmetic
		 // For Compiler Only (NOT IN LANUGAGE)
		ASR, ADDS, ADC, SUBS, SBC, XOR, // Arithmetic
		LessU, LessEqU, GreaterU, GreaterEqU,// Relations unsigned versions
		;
		public static Operator Unsigned(Operator op) {
			switch (op) {
			case Less:
				return LessU;
			case LessEq:
				return LessEqU;
			case Greater:
				return GreaterU;
			case GreaterEq:
				return GreaterEqU;
			default:
				return null;
			}
		}

		public static Operator Reciprocal(Operator op) {
			switch (op) {
			case Less:
				return GreaterEq;
			case LessU:
				return GreaterEqU;
			case LessEq:
				return Greater;
			case LessEqU:
				return GreaterU;
			case Greater:
				return LessEq;
			case GreaterU:
				return LessEqU;
			case GreaterEq:
				return Less;
			case GreaterEqU:
				return LessU;
			case Eq:
				return NotEq;
			case NotEq:
				return Eq;
			default:
				return null;
			}
		} 
	}
	public Operator op;
	public Expression e1;
	public Expression e2;
	public boolean e1promote;
	public boolean e2promote;
	public Primitive type;
	public Primitive itype;
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
			return ">";
		case GreaterEq:
			return ">=";
		case Less:
			return "<";
		case LessEq:
			return "<=";
		case Minus:
			return "-";
		case Plus:
			return "+";
		case Times:
			return "*";
		case And:
			return "&&";
		case Or:
			return "||";
		case Eq:
			return "==";
		case NotEq:
			return "!=";

		default:
			return "Unknown";
		}
    }
    
    @Override
    public int Precedence() {
    	switch (this.op) {
		case Greater:
		case GreaterEq:
		case Less:
		case LessEq:
			return 9;
		case Minus:
		case Plus:
			return 11;
		case Times:
			return 12;
		case And:
			return 4;
		case Or:
			return 3;
		case Eq:
		case NotEq:
			return 8;

		default:
			return 0;
		}
    }

	@Override
	public boolean IsDoubleWord() {
		return itype == Primitive.Long;
	}
}
