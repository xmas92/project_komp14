package ast.visitor;

import actrec.jvm.JVMStack;
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

public class JVMStackVisitor implements GenericVisitor<Integer, JVMStack> {

	@Override
	public Integer visit(Node n, JVMStack arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer visit(MainClass n, JVMStack arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer visit(Parameter n, JVMStack arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer visit(Program n, JVMStack arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer visit(ClassDeclaration n, JVMStack arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer visit(MethodDeclaration n, JVMStack arg) {
		n.block.accept(this, arg);
		n.returnexpr.accept(this, arg);
		if (n.type.IsDoubleWord()) {
			arg.Push(2);
			arg.Pop(2);
		} else {
			arg.Push(1);
			arg.Pop(1);
		}
		return arg.Max();
	}

	@Override
	public Integer visit(VariableDeclaration n, JVMStack arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer visit(ArrayAccessExpression n, JVMStack arg) {
		n.expr.accept(this, arg);
		arg.Push(1);
		n.index.accept(this, arg);
		arg.Push(1);
		arg.Pop(2);
		return arg.Max();
	}

	@Override
	public Integer visit(BinaryExpression n, JVMStack arg) {
		n.e1.accept(this, arg);
		if (n.itype == Primitive.Long) 
			arg.Push(2);
		else
			arg.Push(1);
		n.e2.accept(this, arg);
		if (n.itype == Primitive.Long) {
			arg.Push(2);
			arg.Pop(4);
		} else	{
			arg.Push(1);
			arg.Pop(2);
		}
		return arg.Max();
	}

	@Override
	public Integer visit(BooleanLiteralExpression n, JVMStack arg) {
		arg.Push(1);
		arg.Pop(1);
		return arg.Max();
	}

	@Override
	public Integer visit(Expression n, JVMStack arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer visit(IdentifierExpression n, JVMStack arg) {
		if (n.decl.type.IsDoubleWord()) {
			arg.Push(2);
			arg.Pop(2);
		} else {
			arg.Push(1);
			arg.Pop(1);
		}
		return arg.Max();
		
	}

	@Override
	public Integer visit(IntegerLiteralExpression n, JVMStack arg) {
		arg.Push(1);
		arg.Pop(1);
		return arg.Max();
	}

	@Override
	public Integer visit(LengthExpression n, JVMStack arg) {
		arg.Push(1);
		arg.Pop(1);
		return arg.Max();
	}

	@Override
	public Integer visit(LongLiteralExpression n, JVMStack arg) {
		arg.Push(2);
		arg.Pop(2);
		return arg.Max();
	}

	@Override
	public Integer visit(MemberCallExpression n, JVMStack arg) {
		n.expr.accept(this, arg);
		arg.Push(1);
		int i = 0;
		for (Expression e : n.exprlist) {
			e.accept(this, arg);
			if (e.IsDoubleWord()) {
				i += 2;
				arg.Push(2);
			} else {
				i += 1;
				arg.Push(1);
			}
		}
		arg.Pop(i+1);
		return arg.Max();
	}

	@Override
	public Integer visit(NewArrayExpression n, JVMStack arg) {
		n.size.accept(this, arg);
		arg.Push(1);
		arg.Pop(1);
		return arg.Max();
	}

	@Override
	public Integer visit(NewClassExpression n, JVMStack arg) {
		arg.Push(2);
		arg.Pop(2);
		return arg.Max();
	}

	@Override
	public Integer visit(ThisExpression n, JVMStack arg) {
		arg.Push(1);
		arg.Pop(1);
		return arg.Max();
	}

	@Override
	public Integer visit(UnaryExpression n, JVMStack arg) {
		n.expr.accept(this, arg);
		arg.Push(1);
		arg.Pop(1);
		return arg.Max();
	}

	@Override
	public Integer visit(AssignmentStatement n, JVMStack arg) {
		arg.Push(1);
		if (n.index != null) {
			n.index.accept(this, arg);
			arg.Push(1);
		}
		n.expr.accept(this, arg);
		if (n.decl.type.IsDoubleWord()) {
			arg.Push(2);
			arg.Pop(2);
		} else {
			arg.Push(1);
			arg.Pop(1);
		}
		arg.Pop(1);
		if (n.index != null) {
			arg.Pop(1);
		}
		return arg.Max();
	}

	@Override
	public Integer visit(IfStatement n, JVMStack arg) {
		n.cond.accept(this, arg);
		arg.Push(1);
		arg.Pop(1);
		n.thenstmt.accept(this, arg);
		if (n.elsestmt != null) {
			n.elsestmt.accept(this, arg);
		}
		return arg.Max();
	}

	@Override
	public Integer visit(PrintStatement n, JVMStack arg) {
		arg.Push(1);
		n.expr.accept(this, arg);
		arg.Pop(1);
		return arg.Max();
	}

	@Override
	public Integer visit(Statement n, JVMStack arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer visit(StatementBlock n, JVMStack arg) {
		for (Statement s : n.statements) {
			s.accept(this, arg);
		}
		return arg.Max();
	}

	@Override
	public Integer visit(WhileStatement n, JVMStack arg) {
		n.cond.accept(this, arg);
		n.loop.accept(this, arg);
		return arg.Max();
	}

	@Override
	public Integer visit(Type n, JVMStack arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer visit(PrimitiveType n, JVMStack arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer visit(ClassType n, JVMStack arg) {
		// TODO Auto-generated method stub
		return null;
	}

}
