package ast;

import java.util.List;

import ast.declaration.ClassDeclaration;
import ast.visitor.GenericVisitor;
import ast.visitor.VoidVisitor;

public final class Program extends Node {
	public MainClass mc;
	public List<ClassDeclaration> cds;
	public Program(int line, int column, 
			MainClass mainclass, List<ClassDeclaration> classdeclarations) {
		super(line, column);
		this.mc = mainclass;
		this.cds = classdeclarations;
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
