package ast.declaration;

import java.util.List;

import ast.Node;

public final class ClassDeclaration extends Node {

	public ClassDeclaration(int line, int column,
			List<VariableDeclaration> variabledeclatartions,
			List<MethodDeclaration> methoddeclarations,
			String id, String extendsID) {
		super(line, column);
		// TODO Auto-generated constructor stub
	}

}
