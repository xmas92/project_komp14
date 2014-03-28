package ast.visitor;

import ast.MainClass;
import ast.Node;
import ast.Parameter;
import ast.Program;
import ast.declaration.ClassDeclaration;
import ast.declaration.MethodDeclaration;
import ast.declaration.VariableDeclaration;
import ast.expression.ArrayAccessExpression;
import ast.expression.BinaryExpression;
import ast.expression.BooleanLiteralExpression;
import ast.expression.Expression;
import ast.expression.IdentifierExpression;
import ast.expression.IntegerLiteralExpression;
import ast.expression.LengthExpression;
import ast.expression.LongLiteralExpression;
import ast.expression.MemberCallExpression;
import ast.expression.NewArrayExpression;
import ast.expression.NewClassExpression;
import ast.expression.ThisExpression;
import ast.expression.UnaryExpression;
import ast.statement.AssignmentStatement;
import ast.statement.IfStatement;
import ast.statement.PrintStatement;
import ast.statement.Statement;
import ast.statement.StatementBlock;
import ast.statement.WhileStatement;
import ast.type.ClassType;
import ast.type.PrimitiveType;
import ast.type.PrimitiveType.Primitive;
import ast.type.Type;

public class JVMStackVisitor implements GenericVisitor<Integer, Object> {

	@Override
	public Integer visit(Node n, Object arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer visit(MainClass n, Object arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer visit(Parameter n, Object arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer visit(Program n, Object arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer visit(ClassDeclaration n, Object arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer visit(MethodDeclaration n, Object arg) {
		int i1 = n.block.accept(this, arg);
		int i2 = n.returnexpr.accept(this, arg);
		return (i1 < i2)?i2:i1;
	}

	@Override
	public Integer visit(VariableDeclaration n, Object arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer visit(ArrayAccessExpression n, Object arg) {
		int i1 = n.expr.accept(this, arg);
		int i2 = 1 + n.index.accept(this, arg);
		return (i1 < i2)?i2:i1;
	}

	@Override
	public Integer visit(BinaryExpression n, Object arg) {
		int i1 = n.e1.accept(this, arg);
		int i2 = 1 + n.e2.accept(this, arg);
		i2 = (i2 < ((n.type == Primitive.Long)?4:2)?4:i2);
		return (i1 < i2)?i2:i1;
	}

	@Override
	public Integer visit(BooleanLiteralExpression n, Object arg) {
		return 1;
	}

	@Override
	public Integer visit(Expression n, Object arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer visit(IdentifierExpression n, Object arg) {
		return n.decl.type.IsDoubleWord()?2:1;
	}

	@Override
	public Integer visit(IntegerLiteralExpression n, Object arg) {
		return 1;
	}

	@Override
	public Integer visit(LengthExpression n, Object arg) {
		return 1;
	}

	@Override
	public Integer visit(LongLiteralExpression n, Object arg) {
		return 2;
	}

	@Override
	public Integer visit(MemberCallExpression n, Object arg) {
		int i1 = n.expr.accept(this, arg);
		int i = 1;
		for (Expression e : n.exprlist) {
			int i2 = i + e.accept(this, arg);
			i += 2; // TODO: Over shooting fix so it is only 1 if not long
			i1 = (i1 < i2)?i2:i1;
		}
		return i1;
	}

	@Override
	public Integer visit(NewArrayExpression n, Object arg) {
		return n.size.accept(this, arg);
	}

	@Override
	public Integer visit(NewClassExpression n, Object arg) {
		return 2;
	}

	@Override
	public Integer visit(ThisExpression n, Object arg) {
		return 1;
	}

	@Override
	public Integer visit(UnaryExpression n, Object arg) {
		return n.expr.accept(this, arg);
	}

	@Override
	public Integer visit(AssignmentStatement n, Object arg) {
		int i = n.decl.isField?1:0;
		if (n.index == null)
			return i + n.expr.accept(this, arg);
		int i1 = ++i + n.expr.accept(this, arg);
		int i2 = ++i + n.index.accept(this, arg);
		return (i1 < i2)?i2:i1;
	}

	@Override
	public Integer visit(IfStatement n, Object arg) {
		int i1 = n.cond.accept(this, arg);
		int i2 = n.thenstmt.accept(this, arg);
		if (n.elsestmt != null) {
			i1 = (i1 < i2)?i2:i1;
			i2 = n.elsestmt.accept(this, arg);
		}
		return (i1 < i2)?i2:i1;
	}

	@Override
	public Integer visit(PrintStatement n, Object arg) {
		return 1 + n.expr.accept(this, arg);
	}

	@Override
	public Integer visit(Statement n, Object arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer visit(StatementBlock n, Object arg) {
		int i1 = 0;
		for (Statement s : n.statements) {
			int i2 = s.accept(this, arg);
			i1 = (i1 < i2)?i2:i1;
		}
		return i1;
	}

	@Override
	public Integer visit(WhileStatement n, Object arg) {
		int i1 = n.cond.accept(this, arg);
		int i2 = n.loop.accept(this, arg);
		return (i1 < i2)?i2:i1;
	}

	@Override
	public Integer visit(Type n, Object arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer visit(PrimitiveType n, Object arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer visit(ClassType n, Object arg) {
		// TODO Auto-generated method stub
		return null;
	}

}
