package ast;

import java.util.List;

import ast.declaration.ClassDeclaration;

public final class Program extends Node {

	public Program(int line, int column, 
			MainClass mainclass, List<ClassDeclaration> classdeclarations) {
		super(line, column);
	}

}
