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
import ast.expression.BinaryExpression.Operator;
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

public class FancyPrintVisitor implements GenericVisitor<String, Object> {
	int indent = 0;
	
	private String Indents() {
		String ret = "";
		for (int i = 0; i < indent; i++) 
			ret += "  ";
		return ret;
	}
	private String NewLine(String s) {
		return Indents() + s + "\n";
	}
	@Override
	public String visit(Node n, Object arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String visit(MainClass n, Object arg) {
		String ret = "";
		ret += NewLine("class " + n.id + " {");
		indent++;
		ret += NewLine("public static void main (String[] " + n.input + ")");
		ret += n.block.accept(this, arg);
		indent--;
		ret += NewLine("}");
		return ret;
	}

	@Override
	public String visit(Parameter n, Object arg) {
		return n.type.accept(this, arg) + " " + n.id;
	}

	@Override
	public String visit(Program n, Object arg) {
		String ret = "";
		ret += NewLine("/**********************************");
		ret += NewLine(" * Auto-generated with FancyPrint *");
		ret += NewLine(" **********************************/");
		ret += NewLine("");
		ret += n.mc.accept(this, arg);
		for (ClassDeclaration cd : n.cds)
			ret += cd.accept(this, arg);
		return ret;
	}

	@Override
	public String visit(ClassDeclaration n, Object arg) {
		String ret = "";
		if (n.extendsID == null)
			ret += NewLine("class " + n.id + " {");
		else 
			ret += NewLine("class " + n.id + " extends " + n.extendsID + " {");
		indent++;
		for (VariableDeclaration vd : n.variabledeclatartions) 
			ret += vd.accept(this, arg);
		for (MethodDeclaration md : n.methoddeclarations) 
			ret += md.accept(this, arg);
		indent--;
		ret += NewLine("}");
		return ret;
	}

	@Override
	public String visit(MethodDeclaration n, Object arg) {
		String ret = "public " + n.type.accept(this, arg) + " " + n.id + "(";
		for (Parameter p : n.parameters) {
			ret += p.accept(this, arg) + ", ";
		}
		if (n.parameters.size() > 0)
			ret = ret.substring(0, ret.length()-2);
		ret += ")";
		ret = NewLine(ret);
		ret += n.block.accept(this, n.returnexpr);
		return ret;
	}

	@Override
	public String visit(VariableDeclaration n, Object arg) {
		return NewLine(n.type.accept(this, arg) + " " + n.id + ";");
	}

	@Override
	public String visit(ArrayAccessExpression n, Object arg) {
		return n.expr.accept(this, arg) + "[" + n.index.accept(this, arg) + "]";
	}

	@Override
	public String visit(BinaryExpression n, Object arg) {
		String ret = "";
		if (n.e1.Precedence() < n.Precedence()) 
			ret += "(" + n.e1.accept(this, arg) + ")";
		else
			ret += n.e1.accept(this, arg);
		ret += " " + n.toString() + " ";
		if (n.e2.Precedence() < n.Precedence()) 
			ret += "(" + n.e2.accept(this, arg) + ")";
		else if (n.op == Operator.Minus && n.e2.Precedence() == 11)
			ret += "(" + n.e2.accept(this, arg) + ")";
		else
			ret += n.e2.accept(this, arg);
		return ret;
	}

	@Override
	public String visit(BooleanLiteralExpression n, Object arg) {
		return String.valueOf(n.value);
	}

	@Override
	public String visit(Expression n, Object arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String visit(IdentifierExpression n, Object arg) {
		return n.id;
	}

	@Override
	public String visit(IntegerLiteralExpression n, Object arg) {
		return String.valueOf(n.value);
	}

	@Override
	public String visit(LengthExpression n, Object arg) {
		if (n.expr.Precedence() < n.Precedence())
			return "(" + n.expr.accept(this, arg) + ").length";
		return n.expr.accept(this, arg) + ".length";
	}

	@Override
	public String visit(LongLiteralExpression n, Object arg) {
		return String.valueOf(n.value);
	}

	@Override
	public String visit(MemberCallExpression n, Object arg) {
		String ret = "";
		if (n.expr.Precedence() < n.Precedence())
			ret +=  "(" + n.expr.accept(this, arg) + ")." + n.id + "(";
		else
			ret += n.expr.accept(this, arg) + "." + n.id + "(";
		for (Expression e : n.exprlist) 
			ret += e.accept(this, arg) + ", ";
		if (n.exprlist.size() > 0)
			ret = ret.substring(0, ret.length()-2);
		ret += ")";
		return ret;
	}

	@Override
	public String visit(NewArrayExpression n, Object arg) {
		if (n.primitive == Primitive.IntArr)
			return "new int[" + n.size.accept(this, arg) + "]";
		return "new long[" + n.size.accept(this, arg) + "]";
	}

	@Override
	public String visit(NewClassExpression n, Object arg) {
		return "new " + n.id + "()";
	}

	@Override
	public String visit(ThisExpression n, Object arg) {
		return "this";
	}

	@Override
	public String visit(UnaryExpression n, Object arg) {
		if (n.expr.Precedence() < n.Precedence())
			return "!(" + n.expr.accept(this, arg)	+ ")";
		return "!" + n.expr.accept(this, arg);
	}

	@Override
	public String visit(AssignmentStatement n, Object arg) {
		if (n.index == null) 
			return NewLine(n.id + " = " + n.expr.accept(this, arg)+ ";");
		return NewLine(n.id + "[" + n.index.accept(this, arg) + "] = " + n.expr.accept(this, arg) + ";");
	}

	@Override
	public String visit(IfStatement n, Object arg) {
		String ret = "";
		ret += NewLine("if (" + n.cond.accept(this, arg) + ")");
		if (!(n.thenstmt instanceof StatementBlock))
			indent++;
		ret += n.thenstmt.accept(this, arg);
		if (!(n.thenstmt instanceof StatementBlock))
			indent--;
		if (n.elsestmt != null) {
			ret += NewLine("else");
			if (!(n.elsestmt instanceof StatementBlock))
				indent++;
			ret += n.elsestmt.accept(this, arg);
			if (!(n.elsestmt instanceof StatementBlock))
				indent--;
		}
		return ret;
	}

	@Override
	public String visit(PrintStatement n, Object arg) {
		return NewLine("System.out.println(" + n.expr.accept(this, arg) + ");");
	}

	@Override
	public String visit(Statement n, Object arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String visit(StatementBlock n, Object arg) {
		String ret = "";
		ret += NewLine("{");
		indent++;
		for (VariableDeclaration vd : n.variabledeclarations) 
			ret += vd.accept(this, null);
		for (Statement s : n.statements) 
			ret += s.accept(this, null);
		if (arg != null) 
			ret += NewLine("return " + ((Expression)arg).accept(this, arg) + ";");
		indent--;
		ret += NewLine("}");
		return ret;
	}

	@Override
	public String visit(WhileStatement n, Object arg) {
		String ret = "";
		ret += NewLine("while (" + n.cond.accept(this, arg) + ")");
		if (!(n.loop instanceof StatementBlock))
			indent++;
		ret += n.loop.accept(this, arg);
		if (!(n.loop instanceof StatementBlock))
			indent--;
		return ret;
	}

	@Override
	public String visit(Type n, Object arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String visit(PrimitiveType n, Object arg) {
		switch (n.primitive) {
		case Boolean:
			return "boolean";
		case Int:
			return "int";
		case IntArr:
			return "int[]";
		case Long:
			return "long";
		case LongArr:
			return "long[]";

		default:
			return "ERROR";
		}
	}

	@Override
	public String visit(ClassType n, Object arg) {
		return n.id;
	}

}
