package ast.statement;

import ast.expression.Expression;

public final class IfStatement extends Statement {

	public IfStatement(int line, int column,
			Expression cond, Statement thenstmt, Statement elsestmt) {
		super(line, column);
		// TODO Auto-generated constructor stub
	}

}
