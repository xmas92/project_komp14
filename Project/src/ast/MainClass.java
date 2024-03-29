package ast;

import actrec.Frame;
import ast.declaration.ClassDeclaration;
import ast.statement.StatementBlock;
import ast.visitor.GenericVisitor;
import ast.visitor.VoidVisitor;

public class MainClass extends ClassDeclaration {
	public String input;
	public StatementBlock block;
	public Frame frame;
	public MainClass(int line, int column, StatementBlock block, String id, String input) {
		super(line, column,null,null,id,null);
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
