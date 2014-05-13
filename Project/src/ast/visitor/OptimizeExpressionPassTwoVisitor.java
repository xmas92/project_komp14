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

public class OptimizeExpressionPassTwoVisitor implements
		GenericVisitor<Node, Object> {

	@Override
	public Expression visit(Node n, Object arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Expression visit(MainClass n, Object arg) {
		n.block = (StatementBlock) FixStatement(
				(StatementBlock) n.block.accept(this, arg),
				n.block.getBeginLine(), n.block.getBeginColumn());
		return null;
	}

	@Override
	public Expression visit(Parameter n, Object arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Expression visit(Program n, Object arg) {
		n.mc.accept(this, arg);
		for (ClassDeclaration cd : n.cds)
			cd.accept(this, arg);
		return null;
	}

	@Override
	public Expression visit(ClassDeclaration n, Object arg) {
		for (MethodDeclaration md : n.methoddeclarations) {
			md.accept(this, arg);
			// We ignore fields atm (could add it but need to reset the values
			// every time a method is called on this or keep a table of changes
			// a method does
			for (VariableDeclaration vd : n.variabledeclatartions)
				vd.setData(null); // reset all fields because it is only
									// relevant
			// in method
		}
		return null;
	}

	@Override
	public Expression visit(MethodDeclaration n, Object arg) {
		n.block = (StatementBlock) FixStatement(
				(StatementBlock) n.block.accept(this, arg),
				n.block.getBeginLine(), n.block.getBeginColumn());
		n.returnexpr = (Expression) n.returnexpr.accept(this, arg);
		// won't try this because it would require multiple iterations methods
		// are unordered
		// n.setData(n.returnexpr.getData());
		return null;
	}

	@Override
	public VariableDeclaration visit(VariableDeclaration n, Object arg) {
		if (n.CONST)
			return null;
		return n;
	}

	@Override
	public Expression visit(ArrayAccessExpression n, Object arg) {
		n.expr = (Expression) n.expr.accept(this, arg);
		n.index = (Expression) n.index.accept(this, arg);
		if (n.index.getData() instanceof Integer
				&& n.expr.getData() instanceof Integer) {
			n.StaticCheck = true;
			n.ArrayBoundException = ((Integer) n.index.getData() < 0 || (Integer) n.index
					.getData() >= (Integer) n.expr.getData());
		}
		return n;
	}

	public int Value(IntegerLiteralExpression n) {
		return Integer.parseInt(n.value);
	}

	public long Value(LongLiteralExpression n) {
		return Long.parseLong(n.value.substring(0, n.value.length() - 1));
	}

	@Override
	public Expression visit(BinaryExpression n, Object arg) {
		n.e1 = (Expression) n.e1.accept(this, arg);
		n.e2 = (Expression) n.e2.accept(this, arg);
		int l = n.getBeginLine();
		int c = n.getBeginColumn();
		switch (n.op) {
		case And:
			if (n.e1 instanceof BooleanLiteralExpression
					&& n.e2 instanceof BooleanLiteralExpression)
				return new BooleanLiteralExpression(l, c,
						((BooleanLiteralExpression) n.e1).value
								&& ((BooleanLiteralExpression) n.e2).value);
			if (n.e1 instanceof BooleanLiteralExpression)
				if (((BooleanLiteralExpression) n.e1).value == false)
					return new BooleanLiteralExpression(l, c, false);
				else 
					return n.e2;
			if (n.e2 instanceof BooleanLiteralExpression)
				if (((BooleanLiteralExpression) n.e2).value == true)
					return n.e1;
			break;
		case Or:
			if (n.e1 instanceof BooleanLiteralExpression
					&& n.e2 instanceof BooleanLiteralExpression)
				return new BooleanLiteralExpression(l, c,
						((BooleanLiteralExpression) n.e1).value
								|| ((BooleanLiteralExpression) n.e2).value);
			if (n.e1 instanceof BooleanLiteralExpression)
				if (((BooleanLiteralExpression) n.e1).value == true)
					return new BooleanLiteralExpression(l, c, true);
				else 
					return n.e2;
			if (n.e2 instanceof BooleanLiteralExpression)
				if (((BooleanLiteralExpression) n.e2).value == false)
					return n.e1;
			break;
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
			if (n.e1 instanceof LongLiteralExpression)
				if (n.e2 instanceof IntegerLiteralExpression)
					return new BooleanLiteralExpression(
							l,
							c,
							Value((LongLiteralExpression) n.e1) == Value((IntegerLiteralExpression) n.e2));
				else if (n.e2 instanceof LongLiteralExpression)
					return new BooleanLiteralExpression(
							l,
							c,
							Value((LongLiteralExpression) n.e1) == Value((LongLiteralExpression) n.e2));
			break;
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
			if (n.e1 instanceof LongLiteralExpression)
				if (n.e2 instanceof IntegerLiteralExpression)
					return new BooleanLiteralExpression(
							l,
							c,
							Value((LongLiteralExpression) n.e1) != Value((IntegerLiteralExpression) n.e2));
				else if (n.e2 instanceof LongLiteralExpression)
					return new BooleanLiteralExpression(
							l,
							c,
							Value((LongLiteralExpression) n.e1) != Value((LongLiteralExpression) n.e2));
			break;
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
			if (n.e1 instanceof LongLiteralExpression)
				if (n.e2 instanceof IntegerLiteralExpression)
					return new BooleanLiteralExpression(
							l,
							c,
							Value((LongLiteralExpression) n.e1) > Value((IntegerLiteralExpression) n.e2));
				else if (n.e2 instanceof LongLiteralExpression)
					return new BooleanLiteralExpression(
							l,
							c,
							Value((LongLiteralExpression) n.e1) > Value((LongLiteralExpression) n.e2));
			break;
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
			if (n.e1 instanceof LongLiteralExpression)
				if (n.e2 instanceof IntegerLiteralExpression)
					return new BooleanLiteralExpression(
							l,
							c,
							Value((LongLiteralExpression) n.e1) >= Value((IntegerLiteralExpression) n.e2));
				else if (n.e2 instanceof LongLiteralExpression)
					return new BooleanLiteralExpression(
							l,
							c,
							Value((LongLiteralExpression) n.e1) >= Value((LongLiteralExpression) n.e2));
			break;
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
			if (n.e1 instanceof LongLiteralExpression)
				if (n.e2 instanceof IntegerLiteralExpression)
					return new BooleanLiteralExpression(
							l,
							c,
							Value((LongLiteralExpression) n.e1) < Value((IntegerLiteralExpression) n.e2));
				else if (n.e2 instanceof LongLiteralExpression)
					return new BooleanLiteralExpression(
							l,
							c,
							Value((LongLiteralExpression) n.e1) < Value((LongLiteralExpression) n.e2));
			break;
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
			if (n.e1 instanceof LongLiteralExpression)
				if (n.e2 instanceof IntegerLiteralExpression)
					return new BooleanLiteralExpression(
							l,
							c,
							Value((LongLiteralExpression) n.e1) <= Value((IntegerLiteralExpression) n.e2));
				else if (n.e2 instanceof LongLiteralExpression)
					return new BooleanLiteralExpression(
							l,
							c,
							Value((LongLiteralExpression) n.e1) <= Value((LongLiteralExpression) n.e2));
			break;
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
			if (n.e1 instanceof LongLiteralExpression)
				if (n.e2 instanceof IntegerLiteralExpression)
					return new LongLiteralExpression(l, c,
							Value((LongLiteralExpression) n.e1)
									- Value((IntegerLiteralExpression) n.e2));
				else if (n.e2 instanceof LongLiteralExpression)
					return new LongLiteralExpression(l, c,
							Value((LongLiteralExpression) n.e1)
									- Value((LongLiteralExpression) n.e2));
			break;
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
			if (n.e1 instanceof LongLiteralExpression)
				if (n.e2 instanceof IntegerLiteralExpression)
					return new LongLiteralExpression(l, c,
							Value((LongLiteralExpression) n.e1)
									+ Value((IntegerLiteralExpression) n.e2));
				else if (n.e2 instanceof LongLiteralExpression)
					return new LongLiteralExpression(l, c,
							Value((LongLiteralExpression) n.e1)
									+ Value((LongLiteralExpression) n.e2));
			break;
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
			if (n.e1 instanceof LongLiteralExpression)
				if (n.e2 instanceof IntegerLiteralExpression)
					return new LongLiteralExpression(l, c,
							Value((LongLiteralExpression) n.e1)
									* Value((IntegerLiteralExpression) n.e2));
				else if (n.e2 instanceof LongLiteralExpression)
					return new LongLiteralExpression(l, c,
							Value((LongLiteralExpression) n.e1)
									* Value((LongLiteralExpression) n.e2));
			break;
		default:
			break;
		}
		return n;
	}

	@Override
	public Expression visit(BooleanLiteralExpression n, Object arg) {
		return n;
	}

	@Override
	public Expression visit(Expression n, Object arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Expression visit(IdentifierExpression n, Object arg) {
		if (!n.decl.CONST && arg instanceof Boolean && (Boolean) arg)
			return n;
		if (!n.decl.isField && !n.decl.type.IsType(Primitive.IntArr)
				&& !n.decl.type.IsType(Primitive.LongArr)) {
			if (n.decl.getData() instanceof Integer) {
				return new IntegerLiteralExpression(n.getBeginLine(),
						n.getBeginColumn(), (Integer) n.decl.getData());
			} else if (n.decl.getData() instanceof Boolean) {
				return new BooleanLiteralExpression(n.getBeginLine(),
						n.getBeginColumn(), (Boolean) n.decl.getData());
			} else if (n.decl.getData() instanceof Long) {
				return new LongLiteralExpression(n.getBeginLine(),
						n.getBeginColumn(), (Long) n.decl.getData());
			}
		}
		if (n.decl.type.IsType(Primitive.IntArr)
			|| n.decl.type.IsType(Primitive.LongArr))
			n.setData(n.decl.getData());
		return n;
	}

	@Override
	public Expression visit(IntegerLiteralExpression n, Object arg) {
		return n;
	}

	@Override
	public Expression visit(LengthExpression n, Object arg) {
		n.expr = (Expression) n.expr.accept(this, arg);
		if (n.expr.getData() instanceof Integer) {
			return new IntegerLiteralExpression(n.getBeginLine(),
					n.getBeginColumn(), (Integer) n.expr.getData());
		}
		return n;
	}

	@Override
	public Expression visit(LongLiteralExpression n, Object arg) {
		return n;
	}

	@Override
	public Expression visit(MemberCallExpression n, Object arg) {
		n.expr = (Expression) n.expr.accept(this, arg);
		List<Expression> nExpList = new LinkedList<>();
		for (Expression e : n.exprlist)
			nExpList.add((Expression) e.accept(this, arg));
		n.exprlist = nExpList;
		return n;
	}

	@Override
	public Expression visit(NewArrayExpression n, Object arg) {
		n.size = (Expression) n.size.accept(this, arg);
		n.setData(n.size.getData());
		return n;
	}

	@Override
	public Expression visit(NewClassExpression n, Object arg) {
		return n;
	}

	@Override
	public Expression visit(ThisExpression n, Object arg) {
		return n;
	}

	@Override
	public Expression visit(UnaryExpression n, Object arg) {
		n.expr = (Expression) n.expr.accept(this, arg);
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
	public Statement visit(AssignmentStatement n, Object arg) {
		n.expr = (Expression) n.expr.accept(this, arg);
		if (n.index != null) {
			n.index = (Expression) n.index.accept(this, arg);
			if (n.index.getData() instanceof Integer
					&& n.decl.getData() instanceof Integer) {
				n.StaticCheck = true;
				n.ArrayBoundException = ((Integer) n.index.getData() < 0 || (Integer) n.index
						.getData() >= (Integer) n.decl.getData());
			}
		} else if (arg instanceof Boolean && (Boolean) arg) {
			n.decl.setData(null);
		} else {
			n.decl.setData(n.expr.getData());
			if (n.expr.getData() == null)
				n.decl.CONST = false;
			if (n.decl.type.IsType(Primitive.Long) && n.expr.getData() instanceof Integer)
				n.decl.setData(new Long((Integer)n.expr.getData()));
			if (n.decl.CONST)
				return null;
		}
		return n;
	}

	@Override
	public Statement visit(IfStatement n, Object arg) {
		n.cond = (Expression) n.cond.accept(this, arg);
		n.thenstmt = (Statement) n.thenstmt.accept(this, true);
		if (n.elsestmt != null)
			n.elsestmt = (Statement) n.elsestmt.accept(this, true);
		if (n.cond.getData() instanceof Boolean) {
			if ((Boolean) n.cond.getData() == true) {
				return n.thenstmt;
			} else {
				if (n.elsestmt != null)
					return n.elsestmt;
				else
					return null;
			}
		}
		n.thenstmt = FixStatement(n.thenstmt, n.getBeginLine(), n.getBeginColumn());
		return n;
	}

	@Override
	public Statement visit(PrintStatement n, Object arg) {
		n.expr = (Expression) n.expr.accept(this, arg);
		return n;
	}

	@Override
	public Expression visit(Statement n, Object arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Statement visit(StatementBlock n, Object arg) {
		LinkedList<Statement> t = new LinkedList<>();
		LinkedList<VariableDeclaration> tvd = new LinkedList<>();
		for (Statement s : n.statements) {
			Statement ts = (Statement) s.accept(this, arg);
			if (ts != null)
				t.add(ts);
		}
		for (VariableDeclaration vd : n.variabledeclarations) {
			VariableDeclaration tv = (VariableDeclaration) vd.accept(this, arg);
			if (tv != null)
				tvd.add(tv);
		}
		n.variabledeclarations = tvd;
		n.statements = t;
		if (n.statements.size() == 0 && n.variabledeclarations.size() == 0)
			return null;
		return n;
	}

	public Statement FixStatement(Statement s, int l, int c) {
		if (s == null)
			return new StatementBlock(l, c,
					new LinkedList<VariableDeclaration>(),
					new LinkedList<Statement>());
		return s;
	}

	@Override
	public Statement visit(WhileStatement n, Object arg) {
		n.loop = FixStatement(
				(Statement) n.loop.accept(this, new Boolean(true)),
				n.getBeginLine(), n.getBeginColumn());
		n.cond = (Expression) n.cond.accept(this, arg);
		if (n.cond.getData() instanceof Boolean
				&& (Boolean) n.cond.getData() == false)
			return null;
		return n;
	}

	@Override
	public Expression visit(Type n, Object arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Expression visit(PrimitiveType n, Object arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Expression visit(ClassType n, Object arg) {
		// TODO Auto-generated method stub
		return null;
	}

}
