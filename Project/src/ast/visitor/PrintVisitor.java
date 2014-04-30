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
import ast.type.Type;

public class PrintVisitor implements GenericVisitor<String, Object> {
	int indent = 0;
	
	private String Indents() {
		String ret = "";
		for (int i = 0; i < indent; i++) 
			ret += "  ";
		return ret;
	}
	
	@Override
	public String visit(Node n, Object arg) {
		// Should never happen
		throw new Error();
	}

	@Override
	public String visit(MainClass n, Object arg) {
		indent++;
		String ret = Indents();
		ret += "MainClass: (id: " + n.id + " param: " + n.input + "\n  " + 
				n.block.accept(this, arg) + " )";
		indent--;
		return ret;
	}

	@Override
	public String visit(Parameter n, Object arg) {
		return "Parameter: (id: " + n.id + " type: " + n.type.accept(this, arg) + " )";
	}

	@Override
	public String visit(Program n, Object arg) {
		String ret;
		ret = "Program: ( \n" + n.mc.accept(this, arg);
		for (ClassDeclaration cd : n.cds) 
			ret += "\n" + cd.accept(this, arg) + "\n)";
		return  ret;
	}

	@Override
	public String visit(ClassDeclaration n, Object arg) {
		indent++;
		String ret = Indents();
		ret += "Class: (id: " + n.id;
		if (n.extendsID != null) {
			ret += " extends: " + n.extendsID;
		}
		for (VariableDeclaration vd : n.variabledeclatartions)
			ret += "\n" + vd.accept(this, arg);
		for (MethodDeclaration md : n.methoddeclarations)
			ret += "\n" + md.accept(this, arg);
		ret += "\n)";
		indent--;
		return ret;
	}

	@Override
	public String visit(MethodDeclaration n, Object arg) {
		indent++;
		String ret = Indents();
		ret += "Method: (id: " + n.id + " type: " + n.type.accept(this, arg) + "\n";
		if (n.parameters.size() > 0) {
			ret += Indents() + "Arguments: (";
			indent++;
			for (Parameter p : n.parameters) 
				ret += "\n" + Indents() + p.accept(this, arg);
			indent--;
			ret += Indents() + ")\n";
		}
		ret += Indents() + n.block.accept(this, arg) + "\n";
		ret += Indents() + "Return: (" + n.returnexpr.accept(this, arg) + ")\n"
				+ Indents() + ")";
		indent--;
		return ret;
	}

	@Override
	public String visit(VariableDeclaration n, Object arg) {
		indent++;
		String ret = Indents();
		ret += "Variable: (id: " + n.id + " type: " + n.type.accept(this, arg) + ")";
		indent--;
		return ret;
	}

	@Override
	public String visit(ArrayAccessExpression n, Object arg) {
		return "ArrayAccessExp: (obj: " + n.expr.accept(this, arg)
				+ " index: " + n.index.accept(this, arg) + ")";
	}

	@Override
	public String visit(BinaryExpression n, Object arg) {
		return "BinaryExp: (" + n.e1.accept(this, arg)
				+ " op: " + n.toString() + n.e2.accept(this, arg) + ")";
	}

	@Override
	public String visit(BooleanLiteralExpression n, Object arg) {
		return "boolean: (" + n.value + ")";
	}

	@Override
	public String visit(Expression n, Object arg) {
		// Should never happen
		throw new Error();
	}

	@Override
	public String visit(IdentifierExpression n, Object arg) {
		return "Id: (" + n.id + ")";
	}

	@Override
	public String visit(IntegerLiteralExpression n, Object arg) {
		return "int: (" + n.value + ")";
	}

	@Override
	public String visit(LengthExpression n, Object arg) {
		return "LengthExp: (" + n.expr.accept(this, arg) + ")";
	}

	@Override
	public String visit(LongLiteralExpression n, Object arg) {
		return "long: (" + n.value + ")";
	}

	@Override
	public String visit(MemberCallExpression n, Object arg) {
		String ret = "MemberCallExp: (" + n.expr.accept(this, arg) + " methodId: " + n.id;
		if (n.exprlist.size() > 0) {
			ret += Indents() + "Arguments: (";
			indent++;
			for (Expression p : n.exprlist) 
				ret += "\n" + Indents() + p.accept(this, arg);
			indent--;
			ret += Indents() + ")\n";
			ret += Indents();
		}
		ret += ")";
		return ret;
	}

	@Override
	public String visit(NewArrayExpression n, Object arg) {
		return "NewArrayExp: (type: " + n.primitive.toString() + " size: " + n.size.accept(this, arg) + ")";
	}

	@Override
	public String visit(NewClassExpression n, Object arg) {
		return "NewClassExp: (class: " + n.id + ")";
		
	}

	@Override
	public String visit(ThisExpression n, Object arg) {
		return "ThisExp: ()";
	}

	@Override
	public String visit(UnaryExpression n, Object arg) {
		return "UnaryExp: ( op: Not " + n.expr + ")";
	}

	@Override
	public String visit(AssignmentStatement n, Object arg) {
		return "AssignmentStatement: (id: " + n.id + " " + n.expr.accept(this, arg) + ")";
	}

	@Override
	public String visit(IfStatement n, Object arg) {
		String ret = "IfStatement: (cond: " + n.cond.accept(this, arg) + "\n" + 
				Indents() + "then: " + n.thenstmt.accept(this, arg);
		if (n.elsestmt != null) {
			ret += "\n" + Indents() + "else: " + n.elsestmt.accept(this, arg) + 
					"\n" + Indents();
		}
		ret += ")";
		return ret;
	}

	@Override
	public String visit(PrintStatement n, Object arg) {
		return "PrintStatement: (" + n.expr.accept(this, arg) + ")";
	}

	@Override
	public String visit(Statement n, Object arg) {
		// Should never happen
		throw new Error();
	}

	@Override
	public String visit(StatementBlock n, Object arg) {
		indent++;
		String ret = Indents();
		ret += "Block: (";
		for (VariableDeclaration vd : n.variabledeclarations) {
			ret += "\n" + vd.accept(this, arg);
		}
		for (Statement s : n.statements) {
			ret += "\n" + Indents() + s.accept(this, arg);
		}
		indent--;
		return ret;
	}

	@Override
	public String visit(WhileStatement n, Object arg) {
		String ret = "IfStatement: (cond: " + n.cond.accept(this, arg) + "\n" + 
				Indents() + "loop: " + n.loop.accept(this, arg) + "\n" +
				Indents() + ")";
		
		return ret;
	}

	@Override
	public String visit(Type n, Object arg) {
		// Should never happen
		throw new Error();
	}

	@Override
	public String visit(PrimitiveType n, Object arg) {
		return "Primitive: (" + n.primitive.toString() + ")";
	}

	@Override
	public String visit(ClassType n, Object arg) {
		return "Class: (" + n.id + ")";
	}

}
