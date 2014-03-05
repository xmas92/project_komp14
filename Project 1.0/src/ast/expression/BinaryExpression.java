package ast.expression;

public final class BinaryExpression extends Expression {
	public static enum Operator {
		And, Or, 
		Less, LessEq, Greater, GreaterEq, Eq, NotEq,
		Plus, Minus, Times
	}
	public BinaryExpression(int line, int column,
			Operator op, Expression e1, Expression e2) {
		super(line, column);
		// TODO Auto-generated constructor stub
	}
}
