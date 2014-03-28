package ast.statement;

import ast.Node;
import ast.visitor.GenericVisitor;
import ast.visitor.VoidVisitor;

public abstract class Statement extends Node {

	public Statement(int line, int column) {
		super(line, column);
	}
	
	public Statement(int beginLine, int beginColumn, int endLine, int endColumn) {
		super(beginLine, beginColumn, endLine, endColumn);
	}
	
	@Override
    public <A> void accept(VoidVisitor<A> v, A arg) {
        v.visit(this, arg);
    }

    @Override
    public <R, A> R accept(GenericVisitor<R, A> v, A arg) {
        return v.visit(this, arg);
    }


}
