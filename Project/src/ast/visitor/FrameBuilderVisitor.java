package ast.visitor;

import actrec.Frame;
import actrec.Label;
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

public class FrameBuilderVisitor implements VoidVisitor<Frame> {

	@Override
	public void visit(Node n, Frame arg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(MainClass n, Frame arg) {
		n.frame = arg.newFrame(new Label(n.id+"$main"));
		n.frame.AllocFormal(false); // the string reference we never use
		n.block.accept(this, n.frame);
	}

	@Override
	public void visit(Parameter n, Frame arg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Program n, Frame arg) {
		n.mc.accept(this, arg);
		for (ClassDeclaration cd : n.cds) {
			cd.accept(this, arg);
		}
	}

	@Override
	public void visit(ClassDeclaration n, Frame arg) {
		for (MethodDeclaration md : n.methoddeclarations) 
			md.accept(this, arg.newFrame(new Label(n.id+"$"+md.id)));
	}

	@Override
	public void visit(MethodDeclaration n, Frame arg) {
		n.frame = arg;
		arg.AllocFormal(false); // this reference
		for (Parameter p : n.parameters) 
			p.access = arg.AllocFormal(p.type.IsDoubleWord());
		n.block.accept(this, arg);
	}

	@Override
	public void visit(VariableDeclaration n, Frame arg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ArrayAccessExpression n, Frame arg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(BinaryExpression n, Frame arg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(BooleanLiteralExpression n, Frame arg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Expression n, Frame arg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(IdentifierExpression n, Frame arg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(IntegerLiteralExpression n, Frame arg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(LengthExpression n, Frame arg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(LongLiteralExpression n, Frame arg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(MemberCallExpression n, Frame arg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(NewArrayExpression n, Frame arg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(NewClassExpression n, Frame arg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ThisExpression n, Frame arg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(UnaryExpression n, Frame arg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(AssignmentStatement n, Frame arg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(IfStatement n, Frame arg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(PrintStatement n, Frame arg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Statement n, Frame arg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(StatementBlock n, Frame arg) {
		for (VariableDeclaration vd : n.variabledeclarations)
			vd.access = arg.AllocLocal(vd.type.IsDoubleWord());
		for (Statement s: n.statements)
			s.accept(this, arg);
	}

	@Override
	public void visit(WhileStatement n, Frame arg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Type n, Frame arg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(PrimitiveType n, Frame arg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ClassType n, Frame arg) {
		// TODO Auto-generated method stub
		
	}

}
