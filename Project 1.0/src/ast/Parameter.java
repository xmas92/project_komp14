package ast;

import ast.declaration.VariableDeclaration;
import ast.type.Type;
import ast.visitor.GenericVisitor;
import ast.visitor.VoidVisitor;

public class Parameter extends VariableDeclaration {
	public Parameter(int line, int column, Type type, String id) {
		super(line, column, type, id);
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
