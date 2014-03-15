package ast;

import ast.statement.StatementBlock;
import ast.visitor.GenericVisitor;
import ast.visitor.VoidVisitor;

public class MainClass extends Node {
	public String id;
	public String input;
	public StatementBlock block;
	public MainClass(int line, int column, StatementBlock block, String id, String input) {
		super(line, column);
		this.id = id;
		this.block = block;
		this.input = input;
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
