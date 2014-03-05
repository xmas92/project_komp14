package ast.statement;

import ast.Node;

public abstract class Statement extends Node {

	public Statement(int line, int column) {
		super(line, column);
		// TODO Auto-generated constructor stub
	}
	
	public Statement(int beginLine, int beginColumn, int endLine, int endColumn) {
		super(beginLine, beginColumn, endLine, endColumn);
		// TODO Auto-generated constructor stub
	}


}
