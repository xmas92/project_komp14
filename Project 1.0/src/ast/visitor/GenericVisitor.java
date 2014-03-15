package ast.visitor;

import ast.*;
import ast.declaration.*;
import ast.expression.*;
import ast.statement.*;
import ast.type.*;

public interface GenericVisitor<T1, T2> {
	public T1 visit(Node n, T2 arg);
	
	// ast
	public T1 visit(MainClass n, T2 arg);
	public T1 visit(Parameter n, T2 arg);
	public T1 visit(Program n, T2 arg);
	
	// ast.declaration
	public T1 visit(ClassDeclaration n, T2 arg);
	public T1 visit(MethodDeclaration n, T2 arg);
	public T1 visit(VariableDeclaration n, T2 arg);
	
	// ast.expression
	public T1 visit(ArrayAccessExpression n, T2 arg);
	public T1 visit(BinaryExpression n, T2 arg);
	public T1 visit(BooleanLiteralExpression n, T2 arg);
	public T1 visit(Expression n, T2 arg);
	public T1 visit(IdentifierExpression n, T2 arg);
	public T1 visit(IntegerLiteralExpression n, T2 arg);
	public T1 visit(LengthExpression n, T2 arg);
	public T1 visit(LongLiteralExpression n, T2 arg);
	public T1 visit(MemberCallExpression n, T2 arg);
	public T1 visit(NewArrayExpression n, T2 arg);
	public T1 visit(NewClassExpression n, T2 arg);
	public T1 visit(ThisExpression n, T2 arg);
	public T1 visit(UnaryExpression n, T2 arg);
	
	// ast.statement
	public T1 visit(AssignmentStatement n, T2 arg);
	public T1 visit(IfStatement n, T2 arg);
	public T1 visit(PrintStatement n, T2 arg);
	public T1 visit(Statement n, T2 arg);
	public T1 visit(StatementBlock n, T2 arg);
	public T1 visit(WhileStatement n, T2 arg);
	
	// ast.type
	public T1 visit(Type n, T2 arg);
	public T1 visit(PrimitiveType n, T2 arg);
	public T1 visit(ClassType n, T2 arg);
	
}
