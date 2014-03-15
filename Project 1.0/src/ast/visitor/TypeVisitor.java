package ast.visitor;

import java.util.Iterator;

import compiler.util.Report;
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
import semanal.ClassTable;
import semanal.MiniJavaClass;
import semanal.SymbolTable;

public class TypeVisitor implements GenericVisitor<Type, SymbolTable> {

	@Override
	public Type visit(Node n, SymbolTable arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Type visit(MainClass n, SymbolTable arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Type visit(Parameter n, SymbolTable arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Type visit(Program n, SymbolTable arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Type visit(ClassDeclaration n, SymbolTable arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Type visit(MethodDeclaration n, SymbolTable arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Type visit(VariableDeclaration n, SymbolTable arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Type visit(ArrayAccessExpression n, SymbolTable arg) {
		// TODO Auto-generated method stub
		Type t = n.expr.accept(this, arg);
		if (!n.index.accept(this, arg).IsType(Primitive.Int)) {
			Report.ExitWithError("Index type must be int. (ArrayAccessExpression) (%d:%d)",
					n.index.getBeginLine(), n.index.getBeginColumn());
		}
		if (t.IsType(Primitive.IntArr)) {
			return new PrimitiveType(-1, -1, Primitive.Int);
		} else if (t.IsType(Primitive.LongArr)) {
			return new PrimitiveType(-1, -1, Primitive.Long);
		} 
		return null;
	}

	@Override
	public Type visit(BinaryExpression n, SymbolTable arg) {
		Type t1 = n.e1.accept(this, arg);
		Type t2 = n.e2.accept(this, arg);
		switch (n.op) {
		// Only Int / Long
		case Greater:
		case GreaterEq:
		case Less:
		case LessEq:
			if (t1.IsType(Primitive.Int) && t2.IsType(Primitive.Int)) 
				return new PrimitiveType(-1,-1,Primitive.Boolean);
			if ((t1.IsType(Primitive.Int) || t1.IsType(Primitive.Long)) &&
					(t2.IsType(Primitive.Int) || t2.IsType(Primitive.Long)))
					return new PrimitiveType(-1,-1,Primitive.Boolean);
			Report.ExitWithError("Binary %s expression must use numeric operands. (%s,%s) (%d:%d)",
					n, t1, t2, n.getBeginLine(), n.getBeginColumn());
			break;
		case Minus:
		case Plus:
		case Times:
			if (t1.IsType(Primitive.Int) && t2.IsType(Primitive.Int)) 
				return new PrimitiveType(-1,-1,Primitive.Int);
			if ((t1.IsType(Primitive.Int) || t1.IsType(Primitive.Long)) &&
					(t2.IsType(Primitive.Int) || t2.IsType(Primitive.Long)))
					return new PrimitiveType(-1,-1,Primitive.Long);
			Report.ExitWithError("Binary %s expression must use numeric operands. (%s,%s) (%d:%d)",
					n, t1, t2, n.getBeginLine(), n.getBeginColumn());
			break;
		// Only Boolean
		case And:
		case Or:
			if (t1.IsType(Primitive.Boolean) && t2.IsType(Primitive.Boolean)) 
				return new PrimitiveType(-1,-1,Primitive.Boolean);
			Report.ExitWithError("Binary %s expression must use boolean operands. (%s,%s) (%d:%d)",
					n, t1, t2, n.getBeginLine(), n.getBeginColumn());
			break;
		// All
		case Eq:
		case NotEq:
			if (Type.Assignable(t1, t2)) {
				return new PrimitiveType(-1,-1,Primitive.Boolean);
			} else if (Type.Assignable(t2, t1)) {
				return new PrimitiveType(-1,-1,Primitive.Boolean);
			}
			Report.ExitWithError("Binary %s expression type missmatch. (%s,%s) (%d:%d)",
					n, t1, t2, n.getBeginLine(), n.getBeginColumn());
			break;

		default:
			break;
		}
		return null;
	}

	@Override
	public Type visit(BooleanLiteralExpression n, SymbolTable arg) {
		return new PrimitiveType(-1,-1,Primitive.Boolean);
	}

	@Override
	public Type visit(Expression n, SymbolTable arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Type visit(IdentifierExpression n, SymbolTable arg) {
		n.decl = arg.GetSymbolDecl(n.id);
		if (n.decl == null) {
			Report.ExitWithError("Variable %s cannot be resolved. (%d:%d)", 
					n.id, n.getBeginLine(), n.getBeginColumn());
		}
		return n.decl.type;
	}

	@Override
	public Type visit(IntegerLiteralExpression n, SymbolTable arg) {
		return new PrimitiveType(-1,-1,Primitive.Int);
	}

	@Override
	public Type visit(LengthExpression n, SymbolTable arg) {
		return new PrimitiveType(-1,-1,Primitive.Int);
	}

	@Override
	public Type visit(LongLiteralExpression n, SymbolTable arg) {
		return new PrimitiveType(-1,-1,Primitive.Long);
	}

	@Override
	public Type visit(MemberCallExpression n, SymbolTable arg) {
		Type t = n.expr.accept(this, arg);
		if (!(t instanceof ClassType)) {
			Report.ExitWithError("Expression must be of a class type. (MemberCallExpression) (%d:%d)", 
					n.getBeginLine(), n.getBeginColumn());
		}
		MiniJavaClass c = ClassTable.ct.get(((ClassType)t).id);
		MethodDeclaration m = c.methodtable.get(n.id);
		if (m == null) {
			Report.ExitWithError("The method %s cannot be resolved in class %s. (MemberCallExpression) (%d:%d)", 
					n.id, c.name, n.getBeginLine(), n.getBeginColumn());
		}
		if (n.exprlist.size() != m.parameters.size()) {
			Report.ExitWithError("Method call arguments do not match signature. (MemberCallExpression) (%d:%d)", 
					n.getBeginLine(), n.getBeginColumn());
		}
		Iterator<Expression> i1 = n.exprlist.iterator();
		Iterator<Parameter> i2 = m.parameters.iterator();
		while (i1.hasNext() && i2.hasNext()) {
			if (!Type.Assignable(i2.next().type, i1.next().accept(this, arg))) {
				Report.ExitWithError("Method call arguments do not match signature. (MemberCallExpression) (%d:%d)", 
						n.getBeginLine(), n.getBeginColumn());
			}
		}
		return m.type;
	}

	@Override
	public Type visit(NewArrayExpression n, SymbolTable arg) {
		n.size.accept(this, arg);
		return new PrimitiveType(-1,-1,n.primitive);
	}

	@Override
	public Type visit(NewClassExpression n, SymbolTable arg) {
		if (!ClassTable.ct.containsKey(n.id)) {
			Report.ExitWithError("Class %s cannot be resolved. (%d:%d)", 
					n.id, n.getBeginLine(), n.getBeginColumn());
		}
		return new ClassType(-1, -1, n.id);
	}

	@Override
	public Type visit(ThisExpression n, SymbolTable arg) {
		if (arg.current == null) {
			Report.ExitWithError("this cannot be used in static method. (%d:%d)", 
					n.getBeginLine(), n.getBeginColumn());
		}
		return new ClassType(-1, -1, arg.current.id);
	}

	@Override
	public Type visit(UnaryExpression n, SymbolTable arg) {
		if (n.expr.accept(this, arg).IsType(Primitive.Boolean)) {
			Report.ExitWithError("Unary ! operator must have boolean operand. (UnaryExpression) (%d:%d)", 
					n.getBeginLine(), n.getBeginColumn());
		}
		return new PrimitiveType(-1, -1, Primitive.Boolean);
	}

	@Override
	public Type visit(AssignmentStatement n, SymbolTable arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Type visit(IfStatement n, SymbolTable arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Type visit(PrintStatement n, SymbolTable arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Type visit(Statement n, SymbolTable arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Type visit(StatementBlock n, SymbolTable arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Type visit(WhileStatement n, SymbolTable arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Type visit(Type n, SymbolTable arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Type visit(PrimitiveType n, SymbolTable arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Type visit(ClassType n, SymbolTable arg) {
		// TODO Auto-generated method stub
		return null;
	}

}
