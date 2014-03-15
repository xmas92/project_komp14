package ast.visitor;

import ast.*;
import ast.declaration.*;
import ast.expression.*;
import ast.statement.*;
import ast.type.*;

public interface VoidVisitor<T> {
	public void visit(Node n, T arg);
	
	// ast
	public void visit(MainClass n, T arg);
	public void visit(Parameter n, T arg);
	public void visit(Program n, T arg);
	
	// ast.declaration
	public void visit(ClassDeclaration n, T arg);
	public void visit(MethodDeclaration n, T arg);
	public void visit(VariableDeclaration n, T arg);
	
	// ast.expression
	public void visit(ArrayAccessExpression n, T arg);
	public void visit(BinaryExpression n, T arg);
	public void visit(BooleanLiteralExpression n, T arg);
	public void visit(Expression n, T arg);
	public void visit(IdentifierExpression n, T arg);
	public void visit(IntegerLiteralExpression n, T arg);
	public void visit(LengthExpression n, T arg);
	public void visit(LongLiteralExpression n, T arg);
	public void visit(MemberCallExpression n, T arg);
	public void visit(NewArrayExpression n, T arg);
	public void visit(NewClassExpression n, T arg);
	public void visit(ThisExpression n, T arg);
	public void visit(UnaryExpression n, T arg);
	
	// ast.statement
	public void visit(AssignmentStatement n, T arg);
	public void visit(IfStatement n, T arg);
	public void visit(PrintStatement n, T arg);
	public void visit(Statement n, T arg);
	public void visit(StatementBlock n, T arg);
	public void visit(WhileStatement n, T arg);
	
	// ast.type
	public void visit(Type n, T arg);
	public void visit(PrimitiveType n, T arg);
	public void visit(ClassType n, T arg);
}
