package ast.visitor;

import java.util.LinkedList;
import java.util.List;

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
import ast.type.Type;
import ast.type.PrimitiveType.Primitive;

public class OptimizeExpressionPassOneVisitor implements
		GenericVisitor<Expression, Boolean> {

	@Override
	public Expression visit(Node n, Boolean arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Expression visit(MainClass n, Boolean arg) {
		n.block.accept(this, arg);
		return null;
	}

	@Override
	public Expression visit(Parameter n, Boolean arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Expression visit(Program n, Boolean arg) {
		n.mc.accept(this, false);
		for (ClassDeclaration cd : n.cds)
			cd.accept(this, false);
		return null;
	}

	@Override
	public Expression visit(ClassDeclaration n, Boolean arg) {
		for (VariableDeclaration vd : n.variabledeclatartions)
			vd.accept(this, arg);
		for (MethodDeclaration md : n.methoddeclarations)
			md.accept(this, arg);
		return null;
	}

	@Override
	public Expression visit(MethodDeclaration n, Boolean arg) {
		n.block.accept(this, arg);
		n.returnexpr = n.returnexpr.accept(this, arg);
		return null;
	}

	@Override
	public Expression visit(VariableDeclaration n, Boolean arg) {
		if (n.type.IsType(Primitive.IntArr)
				|| n.type.IsType(Primitive.LongArr) || n.isField
				|| !Type.IsPrimative(n.type))
			n.CONST = false ;
		return null;
	}

	@Override
	public Expression visit(ArrayAccessExpression n, Boolean arg) {
		n.expr = n.expr.accept(this, arg);
		n.index = n.index.accept(this, arg);
		return n;
	}

	public int Value(IntegerLiteralExpression n) {
		return Integer.parseInt(n.value);
	}

	public long Value(LongLiteralExpression n) {
		return Long.parseLong(n.value.substring(0, n.value.length() - 1));
	}

	@Override
	public Expression visit(BinaryExpression n, Boolean arg) {
		n.e1 = n.e1.accept(this, arg);
		n.e2 = n.e2.accept(this, arg);
		int l = n.getBeginLine();
		int c = n.getBeginColumn();
		switch (n.op) {
		case And:
			if (n.e1 instanceof BooleanLiteralExpression
					&& n.e2 instanceof BooleanLiteralExpression)
				return new BooleanLiteralExpression(l, c,
						((BooleanLiteralExpression) n.e1).value
								&& ((BooleanLiteralExpression) n.e2).value);
		case Or:
			if (n.e1 instanceof BooleanLiteralExpression
					&& n.e2 instanceof BooleanLiteralExpression)
				return new BooleanLiteralExpression(l, c,
						((BooleanLiteralExpression) n.e1).value
								|| ((BooleanLiteralExpression) n.e2).value);
		case Eq:
			if (n.e1 instanceof BooleanLiteralExpression
					&& n.e2 instanceof BooleanLiteralExpression)
				return new BooleanLiteralExpression(
						l,
						c,
						((BooleanLiteralExpression) n.e1).value == ((BooleanLiteralExpression) n.e2).value);
			if (n.e1 instanceof IdentifierExpression
					&& n.e2 instanceof IdentifierExpression
					&& ((IdentifierExpression) n.e1).id
							.equals(((IdentifierExpression) n.e2).id))
				return new BooleanLiteralExpression(l, c, true);
			if (n.e1 instanceof IntegerLiteralExpression)
				if (n.e2 instanceof IntegerLiteralExpression)
					return new BooleanLiteralExpression(
							l,
							c,
							Value((IntegerLiteralExpression) n.e1) == Value((IntegerLiteralExpression) n.e2));
				else if (n.e2 instanceof LongLiteralExpression)
					return new BooleanLiteralExpression(
							l,
							c,
							Value((IntegerLiteralExpression) n.e1) == Value((LongLiteralExpression) n.e2));
			if (n.e2 instanceof LongLiteralExpression)
				if (n.e1 instanceof IntegerLiteralExpression)
					return new BooleanLiteralExpression(
							l,
							c,
							Value((IntegerLiteralExpression) n.e1) == Value((LongLiteralExpression) n.e2));
				else if (n.e1 instanceof LongLiteralExpression)
					return new BooleanLiteralExpression(
							l,
							c,
							Value((LongLiteralExpression) n.e1) == Value((LongLiteralExpression) n.e2));
		case NotEq:
			if (n.e1 instanceof BooleanLiteralExpression
					&& n.e2 instanceof BooleanLiteralExpression)
				return new BooleanLiteralExpression(
						l,
						c,
						((BooleanLiteralExpression) n.e1).value != ((BooleanLiteralExpression) n.e2).value);
			if (n.e1 instanceof IdentifierExpression
					&& n.e2 instanceof IdentifierExpression
					&& ((IdentifierExpression) n.e1).id
							.equals(((IdentifierExpression) n.e2).id))
				return new BooleanLiteralExpression(l, c, false);
			if (n.e1 instanceof IntegerLiteralExpression)
				if (n.e2 instanceof IntegerLiteralExpression)
					return new BooleanLiteralExpression(
							l,
							c,
							Value((IntegerLiteralExpression) n.e1) != Value((IntegerLiteralExpression) n.e2));
				else if (n.e2 instanceof LongLiteralExpression)
					return new BooleanLiteralExpression(
							l,
							c,
							Value((IntegerLiteralExpression) n.e1) != Value((LongLiteralExpression) n.e2));
			if (n.e2 instanceof LongLiteralExpression)
				if (n.e1 instanceof IntegerLiteralExpression)
					return new BooleanLiteralExpression(
							l,
							c,
							Value((IntegerLiteralExpression) n.e1) != Value((LongLiteralExpression) n.e2));
				else if (n.e1 instanceof LongLiteralExpression)
					return new BooleanLiteralExpression(
							l,
							c,
							Value((LongLiteralExpression) n.e1) != Value((LongLiteralExpression) n.e2));
		case Greater:
			if (n.e1 instanceof IntegerLiteralExpression)
				if (n.e2 instanceof IntegerLiteralExpression)
					return new BooleanLiteralExpression(
							l,
							c,
							Value((IntegerLiteralExpression) n.e1) > Value((IntegerLiteralExpression) n.e2));
				else if (n.e2 instanceof LongLiteralExpression)
					return new BooleanLiteralExpression(
							l,
							c,
							Value((IntegerLiteralExpression) n.e1) > Value((LongLiteralExpression) n.e2));
			if (n.e2 instanceof LongLiteralExpression)
				if (n.e1 instanceof IntegerLiteralExpression)
					return new BooleanLiteralExpression(
							l,
							c,
							Value((IntegerLiteralExpression) n.e1) > Value((LongLiteralExpression) n.e2));
				else if (n.e1 instanceof LongLiteralExpression)
					return new BooleanLiteralExpression(
							l,
							c,
							Value((LongLiteralExpression) n.e1) > Value((LongLiteralExpression) n.e2));
		case GreaterEq:
			if (n.e1 instanceof IntegerLiteralExpression)
				if (n.e2 instanceof IntegerLiteralExpression)
					return new BooleanLiteralExpression(
							l,
							c,
							Value((IntegerLiteralExpression) n.e1) >= Value((IntegerLiteralExpression) n.e2));
				else if (n.e2 instanceof LongLiteralExpression)
					return new BooleanLiteralExpression(
							l,
							c,
							Value((IntegerLiteralExpression) n.e1) >= Value((LongLiteralExpression) n.e2));
			if (n.e2 instanceof LongLiteralExpression)
				if (n.e1 instanceof IntegerLiteralExpression)
					return new BooleanLiteralExpression(
							l,
							c,
							Value((IntegerLiteralExpression) n.e1) >= Value((LongLiteralExpression) n.e2));
				else if (n.e1 instanceof LongLiteralExpression)
					return new BooleanLiteralExpression(
							l,
							c,
							Value((LongLiteralExpression) n.e1) >= Value((LongLiteralExpression) n.e2));
		case Less:
			if (n.e1 instanceof IntegerLiteralExpression)
				if (n.e2 instanceof IntegerLiteralExpression)
					return new BooleanLiteralExpression(
							l,
							c,
							Value((IntegerLiteralExpression) n.e1) < Value((IntegerLiteralExpression) n.e2));
				else if (n.e2 instanceof LongLiteralExpression)
					return new BooleanLiteralExpression(
							l,
							c,
							Value((IntegerLiteralExpression) n.e1) < Value((LongLiteralExpression) n.e2));
			if (n.e2 instanceof LongLiteralExpression)
				if (n.e1 instanceof IntegerLiteralExpression)
					return new BooleanLiteralExpression(
							l,
							c,
							Value((IntegerLiteralExpression) n.e1) < Value((LongLiteralExpression) n.e2));
				else if (n.e1 instanceof LongLiteralExpression)
					return new BooleanLiteralExpression(
							l,
							c,
							Value((LongLiteralExpression) n.e1) < Value((LongLiteralExpression) n.e2));
		case LessEq:
			if (n.e1 instanceof IntegerLiteralExpression)
				if (n.e2 instanceof IntegerLiteralExpression)
					return new BooleanLiteralExpression(
							l,
							c,
							Value((IntegerLiteralExpression) n.e1) <= Value((IntegerLiteralExpression) n.e2));
				else if (n.e2 instanceof LongLiteralExpression)
					return new BooleanLiteralExpression(
							l,
							c,
							Value((IntegerLiteralExpression) n.e1) <= Value((LongLiteralExpression) n.e2));
			if (n.e2 instanceof LongLiteralExpression)
				if (n.e1 instanceof IntegerLiteralExpression)
					return new BooleanLiteralExpression(
							l,
							c,
							Value((IntegerLiteralExpression) n.e1) <= Value((LongLiteralExpression) n.e2));
				else if (n.e1 instanceof LongLiteralExpression)
					return new BooleanLiteralExpression(
							l,
							c,
							Value((LongLiteralExpression) n.e1) <= Value((LongLiteralExpression) n.e2));
		case Minus:
			if (n.e1 instanceof IntegerLiteralExpression)
				if (n.e2 instanceof IntegerLiteralExpression)
					return new IntegerLiteralExpression(l, c,
							Value((IntegerLiteralExpression) n.e1)
									- Value((IntegerLiteralExpression) n.e2));
				else if (n.e2 instanceof LongLiteralExpression)
					return new LongLiteralExpression(l, c,
							Value((IntegerLiteralExpression) n.e1)
									- Value((LongLiteralExpression) n.e2));
			if (n.e2 instanceof LongLiteralExpression)
				if (n.e1 instanceof IntegerLiteralExpression)
					return new LongLiteralExpression(l, c,
							Value((IntegerLiteralExpression) n.e1)
									- Value((LongLiteralExpression) n.e2));
				else if (n.e1 instanceof LongLiteralExpression)
					return new LongLiteralExpression(l, c,
							Value((LongLiteralExpression) n.e1)
									- Value((LongLiteralExpression) n.e2));
		case Plus:
			if (n.e1 instanceof IntegerLiteralExpression)
				if (n.e2 instanceof IntegerLiteralExpression)
					return new IntegerLiteralExpression(l, c,
							Value((IntegerLiteralExpression) n.e1)
									+ Value((IntegerLiteralExpression) n.e2));
				else if (n.e2 instanceof LongLiteralExpression)
					return new LongLiteralExpression(l, c,
							Value((IntegerLiteralExpression) n.e1)
									+ Value((LongLiteralExpression) n.e2));
			if (n.e2 instanceof LongLiteralExpression)
				if (n.e1 instanceof IntegerLiteralExpression)
					return new LongLiteralExpression(l, c,
							Value((IntegerLiteralExpression) n.e1)
									+ Value((LongLiteralExpression) n.e2));
				else if (n.e1 instanceof LongLiteralExpression)
					return new LongLiteralExpression(l, c,
							Value((LongLiteralExpression) n.e1)
									+ Value((LongLiteralExpression) n.e2));
		case Times:
			if (n.e1 instanceof IntegerLiteralExpression)
				if (n.e2 instanceof IntegerLiteralExpression)
					return new IntegerLiteralExpression(l, c,
							Value((IntegerLiteralExpression) n.e1)
									* Value((IntegerLiteralExpression) n.e2));
				else if (n.e2 instanceof LongLiteralExpression)
					return new LongLiteralExpression(l, c,
							Value((IntegerLiteralExpression) n.e1)
									* Value((LongLiteralExpression) n.e2));
			if (n.e2 instanceof LongLiteralExpression)
				if (n.e1 instanceof IntegerLiteralExpression)
					return new LongLiteralExpression(l, c,
							Value((IntegerLiteralExpression) n.e1)
									* Value((LongLiteralExpression) n.e2));
				else if (n.e1 instanceof LongLiteralExpression)
					return new LongLiteralExpression(l, c,
							Value((LongLiteralExpression) n.e1)
									* Value((LongLiteralExpression) n.e2));
		default:
			return n;
		}
	}

	@Override
	public Expression visit(BooleanLiteralExpression n, Boolean arg) {
		return n;
	}

	@Override
	public Expression visit(Expression n, Boolean arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Expression visit(IdentifierExpression n, Boolean arg) {
		return n;
	}

	@Override
	public Expression visit(IntegerLiteralExpression n, Boolean arg) {
		return n;
	}

	@Override
	public Expression visit(LengthExpression n, Boolean arg) {
		return n;
	}

	@Override
	public Expression visit(LongLiteralExpression n, Boolean arg) {
		return n;
	}

	@Override
	public Expression visit(MemberCallExpression n, Boolean arg) {
		n.expr = n.expr.accept(this, arg);
		List<Expression> nExpList = new LinkedList<>();
		for (Expression e : n.exprlist)
			nExpList.add(e.accept(this, arg));
		n.exprlist = nExpList;
		return n;
	}

	@Override
	public Expression visit(NewArrayExpression n, Boolean arg) {
		n.size = n.size.accept(this, arg);
		return n;
	}

	@Override
	public Expression visit(NewClassExpression n, Boolean arg) {
		return n;
	}

	@Override
	public Expression visit(ThisExpression n, Boolean arg) {
		return n;
	}

	@Override
	public Expression visit(UnaryExpression n, Boolean arg) {
		n.expr = n.expr.accept(this, arg);
		switch (n.op) {
		case Not:
			if (n.expr instanceof BooleanLiteralExpression)
				return new BooleanLiteralExpression(n.getBeginLine(),
						n.getBeginColumn(),
						!((BooleanLiteralExpression) n.expr).value);
		default:
			return n;
		}
	}

	@Override
	public Expression visit(AssignmentStatement n, Boolean arg) {
		if (arg)
			n.decl.CONST = false;
		n.expr = n.expr.accept(this, arg);
		if (n.index != null)
			n.index = n.index.accept(this, arg);
		return null;
	}

	@Override
	public Expression visit(IfStatement n, Boolean arg) {
		n.cond = n.cond.accept(this, arg);
		n.thenstmt.accept(this, true);
		if (n.elsestmt != null)
			n.elsestmt.accept(this, true);
		return null;
	}

	@Override
	public Expression visit(PrintStatement n, Boolean arg) {
		n.expr = n.expr.accept(this, arg);
		return null;
	}

	@Override
	public Expression visit(Statement n, Boolean arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Expression visit(StatementBlock n, Boolean arg) {
		for (VariableDeclaration vd : n.variabledeclarations)
			vd.accept(this, arg);
		for (Statement s : n.statements) {
			s.accept(this, arg);
		}
		return null;
	}

	@Override
	public Expression visit(WhileStatement n, Boolean arg) {
		n.cond = n.cond.accept(this, arg);
		n.loop.accept(this, true);
		return null;
	}

	@Override
	public Expression visit(Type n, Boolean arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Expression visit(PrimitiveType n, Boolean arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Expression visit(ClassType n, Boolean arg) {
		// TODO Auto-generated method stub
		return null;
	}

}
