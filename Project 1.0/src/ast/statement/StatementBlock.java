package ast.statement;

import java.util.List;

import ast.declaration.VariableDeclaration;

public class StatementBlock extends Statement {

	public StatementBlock(int line, int column,
			List<VariableDeclaration> variabledeclarations,
			List<Statement> statements) {
		super(line, column);
		// TODO Auto-generated constructor stub
	}

}
