package ast.statement;

import java.util.List;

import ast.declaration.VariableDeclaration;
import ast.visitor.GenericVisitor;
import ast.visitor.VoidVisitor;

public class StatementBlock extends Statement {
	public List<VariableDeclaration> variabledeclarations;
	public List<Statement> statements;
	public StatementBlock(int line, int column,
			List<VariableDeclaration> variabledeclarations,
			List<Statement> statements) {
		super(line, column);
		this.variabledeclarations = variabledeclarations;
		this.statements = statements;
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
