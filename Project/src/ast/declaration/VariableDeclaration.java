package ast.declaration;

import actrec.Access;
import ast.Node;
import ast.type.Type;
import ast.visitor.GenericVisitor;
import ast.visitor.VoidVisitor;

public class VariableDeclaration extends Node {
	public Type type;
	public String id;
	public Access access;
	public boolean isField;
	public boolean CONST = true;
	public VariableDeclaration(int line, int column,
			Type type, String id) {
		super(line, column);
		this.type = type;
		this.id = id;
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
