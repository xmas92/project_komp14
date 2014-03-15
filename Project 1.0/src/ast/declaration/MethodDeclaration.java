package ast.declaration;

import java.util.List;

import ast.Node;
import ast.Parameter;
import ast.expression.Expression;
import ast.statement.StatementBlock;
import ast.type.Type;
import ast.visitor.GenericVisitor;
import ast.visitor.VoidVisitor;

public class MethodDeclaration extends Node {
	public Type type;
	public String id;
	public List<Parameter> parameters;
	public StatementBlock block;
	public Expression returnexpr;
	public MethodDeclaration(int line, int column,
			Type type, String id,
			List<Parameter> parameters,
			StatementBlock block,
			Expression returnexpr) {
		super(line, column);
		this.type = type;
		this.id = id;
		this.parameters = parameters;
		this.block = block;
		this.returnexpr = returnexpr;
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
