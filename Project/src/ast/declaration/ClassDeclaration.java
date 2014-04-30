package ast.declaration;

import java.util.List;

import actrec.Record;
import ast.Node;
import ast.visitor.GenericVisitor;
import ast.visitor.VoidVisitor;

public class ClassDeclaration extends Node {
	public List<VariableDeclaration> variabledeclatartions;
	public List<MethodDeclaration> methoddeclarations;
	public String id;
	public String extendsID;
	public Record record;
	public ClassDeclaration(int line, int column,
			List<VariableDeclaration> variabledeclatartions,
			List<MethodDeclaration> methoddeclarations,
			String id, String extendsID) {
		super(line, column);
		this.variabledeclatartions = variabledeclatartions;
		this.methoddeclarations = methoddeclarations;
		this.id = id;
		this.extendsID = extendsID;
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
