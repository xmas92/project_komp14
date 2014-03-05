package ast.expression;

public class UnaryExpression extends Expression {
	public static enum Operator {
		Not
	}
	public UnaryExpression(int line, int column, 
			Operator op, Expression expr) {
		super(line, column);
		// TODO Auto-generated constructor stub
	}

}
