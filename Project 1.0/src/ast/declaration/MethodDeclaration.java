package ast.declaration;

import java.util.List;

import ast.Node;
import ast.Parameter;
import ast.expression.Expression;
import ast.statement.StatementBlock;
import ast.type.Type;

public class MethodDeclaration extends Node {

	public MethodDeclaration(int line, int column,
			Type type, String id,
			List<Parameter> parameters,
			StatementBlock block,
			Expression returnexpr) {
		super(line, column);
		// TODO Auto-generated constructor stub
	}

}
