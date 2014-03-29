package ast.visitor;

import java.util.Iterator;

import semanal.ClassTable;
import semanal.MiniJavaClass;
import semanal.SymbolTable;
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

public class SemanticsVisitor implements VoidVisitor<SymbolTable> {

	@Override
	public void visit(Node n, SymbolTable arg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(MainClass n, SymbolTable arg) {
		SymbolTable st = new SymbolTable();
		st.EnterScope();
		st.AddSymbol(n.input, null);
		n.block.accept(this, st);
		st.LeaveScope();
	}

	@Override
	public void visit(Parameter n, SymbolTable arg) {
		n.type.accept(this, arg);
		if (!arg.AddSymbol(n.id, n)) {
			Report.ExitWithError("Parameter %s already defined. (%d:%d)", 
					n.id,n.getBeginLine(), n.getBeginColumn());
		}
	}

	@Override
	public void visit(Program n, SymbolTable arg) {
		InheritanceSemanticsCheck(ClassTable.ct);
		n.mc.accept(this, null);
		for (ClassDeclaration cd : n.cds) 
			cd.accept(this, null);
	}

	private void InheritanceSemanticsCheck(ClassTable ct) {
		// Check that no cycles exist in the inheritance.
		InheritanceNameCheck(ct);
		InheritanceCycleCheck(ct);
		InheritanceMethodCheckAndSetup(ct);
	}

	private void InheritanceMethodCheckAndSetup(ClassTable ct) {
		for (MiniJavaClass c : ct.values()) {
			InheritanceCheckAndSetupClass(c,ct);
		}
	}

	private void InheritanceCheckAndSetupClass(MiniJavaClass c, ClassTable ct) {
		// If already setup return
		if (c.isSetup) return;
		// If no superclass just return
		if (c.superclass == null) {
			c.isSetup = true;
			return;
		}
		MiniJavaClass e = ct.get(c.superclass);
		// Setup the superclass (cycle checks have already been done, will return unless error)
		InheritanceCheckAndSetupClass(e, ct);
		// Check correct method overriding
		for (MethodDeclaration m : e.methodtable.values()) {
			MethodDeclaration t = c.methodtable.get(m.id);
			if (t != null) {
				// Method exists in superclass
				// Check correct return type
				if (!Type.Same(t.type, m.type)) {
					// Not same return type, might be covariant type
					if(m.type instanceof PrimitiveType || t.type instanceof PrimitiveType) {
						// Primitive return types must be same
						Report.ExitWithError("Cannot override method. %s has invalid return type. (Primitive missmatch) (%d:%d)",
								t.id, t.getBeginLine(), t.getBeginColumn());
					} else {
						// m = superclass method
						// t = class method
						// t.type must subclass of m.type
						// Check if covariant type, 
						// also must be classtype (should be only alternative)
						assert(t.type instanceof ClassType);
						assert(m.type instanceof ClassType);
						// Covariant return type check
						String cn = ((ClassType)t.type).id;
						// Special case, must see if we can resolve the type
						if (!ct.containsKey(cn)) {
							Report.ExitWithError("Class type %s cannot be resolved. (%d:%d)", 
									cn,c.cd.getBeginLine(), c.cd.getBeginColumn());
						}
						// Rest will all resolve if first resolve because we already checked all super classes
						do {
							cn = ct.get(cn).superclass;
							if (cn == null) {
								Report.ExitWithError("Cannot override method. %s has invalid return type. (Not covariant) (%d:%d)",
										t.id, t.getBeginLine(), t.getBeginColumn());
							}
						} while(!((ClassType)m.type).id.equals(cn));
					}
				}
				// Check correct parameters
				if (t.parameters.size() != m.parameters.size()) {
					Report.ExitWithError("MiniJava disallows overloading. (Override method: %s) (%d:%d)",
							t.id, t.getBeginLine(), t.getBeginColumn());
				}
				Iterator<Parameter> i1 = t.parameters.iterator();
				Iterator<Parameter> i2 = m.parameters.iterator();
				while (i1.hasNext() && i2.hasNext()) {
					if (!Type.Same(i1.next().type, i2.next().type)){
						Report.ExitWithError("MiniJava disallows overloading. (Override method: %s) (%d:%d)",
								t.id, t.getBeginLine(), t.getBeginColumn());
					}
				}
			} else {
				// Add superclass method to method table
				c.methodtable.put(m.id, m);
			}
		}
		for (VariableDeclaration vd : e.fieldtable.values()) {
			VariableDeclaration t = c.fieldtable.get(vd.id);
			if (t == null) {
				c.fieldtable.put(vd.id, vd);
			}
		}
	}

	private void InheritanceNameCheck(ClassTable ct) {
		for (MiniJavaClass c : ct.values()) {
			// If no superclass just continue
			if (c.superclass == null) continue;
			// If the superclass is not defined exit with error.
			if (ct.get(c.superclass) == null) {
				Report.ExitWithError("SuperClass %s cannot be resolved. (%d:%d)", 
						c.superclass,c.cd.getBeginLine(), c.cd.getBeginColumn());
			}
		}
	}

	private void InheritanceCycleCheck(ClassTable ct) {
		for (MiniJavaClass c : ct.values()) {
			// Tortoise and hare algorithm
			String s = c.name;
			String f1 = c.superclass;
			if (f1 == null) continue;
			String f2 = ct.get(f1).superclass;
			if (f2 == null) {
				if (s.equals(f1)) {
					Report.ExitWithError("Cyclic inharitance, class %s. (%d:%d)", 
							c.superclass,c.cd.getBeginLine(), c.cd.getBeginColumn());
				}
				continue;
			}
			do {
				if (s == f1 || s == f2) {
					Report.ExitWithError("Cyclic inharitance, class %s. (%d:%d)", 
							c.superclass,c.cd.getBeginLine(), c.cd.getBeginColumn());
				}
				s = ct.get(s).superclass;
				f1 = ct.get(f2).superclass;
				if (f1 == null) break;
				f2 = ct.get(f1).superclass;
			} while(s != null && f2 != null);
		}
	}

	@Override
	public void visit(ClassDeclaration n, SymbolTable arg) {
		arg = new SymbolTable();
		arg.current = n;
		for (VariableDeclaration vd : ClassTable.ct.get(n.id).fieldtable.values()) {
			vd.isField = true;
			if (!arg.AddField(vd.id, vd)) {
				Report.ExitWithError("Field %s already defined. (%d:%d)", 
						vd.id,vd.getBeginLine(), vd.getBeginColumn());
			}
		}
		for (MethodDeclaration md : n.methoddeclarations)
			md.accept(this, arg);
	}

	@Override
	public void visit(MethodDeclaration n, SymbolTable arg) {
		arg.EnterScope();
		n.type.accept(this, arg);
		for (Parameter p : n.parameters) 
			p.accept(this, arg);
		for (VariableDeclaration vd : n.block.variabledeclarations) 
			vd.accept(this, arg);
		for (Statement s : n.block.statements) 
			s.accept(this, arg);
		Type t = n.returnexpr.accept(new TypeVisitor(), arg);
		if (!Type.Assignable(n.type, t)) {
			Report.ExitWithError("Type Missmatch. %s = %s (ReturnStatement) (%d:%d)",
					n.type, t, n.getBeginLine(), n.getBeginColumn());
		}
		arg.LeaveScope();
	}

	@Override
	public void visit(VariableDeclaration n, SymbolTable arg) {
		n.type.accept(this, arg);
		if (!arg.AddSymbol(n.id, n)) {
			Report.ExitWithError("Variable %s already defined. (%d:%d)", 
					n.id,n.getBeginLine(), n.getBeginColumn());
		}
	}

	@Override
	public void visit(ArrayAccessExpression n, SymbolTable arg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(BinaryExpression n, SymbolTable arg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(BooleanLiteralExpression n, SymbolTable arg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Expression n, SymbolTable arg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(IdentifierExpression n, SymbolTable arg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(IntegerLiteralExpression n, SymbolTable arg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(LengthExpression n, SymbolTable arg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(LongLiteralExpression n, SymbolTable arg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(MemberCallExpression n, SymbolTable arg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(NewArrayExpression n, SymbolTable arg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(NewClassExpression n, SymbolTable arg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ThisExpression n, SymbolTable arg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(UnaryExpression n, SymbolTable arg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(AssignmentStatement n, SymbolTable arg) {
		Type t1 = arg.GetSymbolType(n.id);
		n.decl = arg.GetSymbolDecl(n.id);
		Type t2 = n.expr.accept(new TypeVisitor(), arg);
		if (n.index == null) {
			if (!Type.Assignable(t1,t2)) {
				Report.ExitWithError("Type Missmatch. %s = %s (AssignmentStatement) (%d:%d)",
						t1, t2, n.getBeginLine(), n.getBeginColumn());
			}
		} else {
			Type ti = n.index.accept(new TypeVisitor(), arg);
			if (!Type.SameIgnoreArray(t1,t2)) {
				Report.ExitWithError("Type Missmatch. %s = %s (AssignmentStatement) (%d:%d)",
						t1, t2, n.getBeginLine(), n.getBeginColumn());
			}
			if (!ti.IsType(PrimitiveType.Primitive.Int)) {
				Report.ExitWithError("Index type must be int. (AssignmentStatement) (%d:%d)",
						n.index.getBeginLine(), n.index.getBeginColumn());
			}
			if (!n.decl.type.IsType(Primitive.IntArr) && !n.decl.type.IsType(Primitive.LongArr)) {
				Report.ExitWithError("Trying to index non array. (AssignmentStatement) (%d:%d)",
						n.getBeginLine(), n.getBeginColumn());
			}
		}
	}

	@Override
	public void visit(IfStatement n, SymbolTable arg) {
		Type c = n.cond.accept(new TypeVisitor(), arg);
		if (!c.IsType(Primitive.Boolean)) {
			Report.ExitWithError("Condition must be boolean. (IfStatement) (%d:%d)",
					n.cond.getBeginLine(), n.cond.getBeginColumn());
		}
		n.thenstmt.accept(this, arg);
		if (n.elsestmt != null)
			n.elsestmt.accept(this, arg);
	}

	@Override
	public void visit(PrintStatement n, SymbolTable arg) {
		// TODO: Not sure what we can print
		// Probably everything, lets do it
		n.type = n.expr.accept(new TypeVisitor(), arg);
		if (n.type instanceof ClassType) {
			Report.ExitWithError("Apperently can't print class objects. (PrintStatement) (%d:%d)",
					n.getBeginLine(), n.getBeginColumn());
		}
	}

	@Override
	public void visit(Statement n, SymbolTable arg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(StatementBlock n, SymbolTable arg) {
		// TODO Auto-generated method stub
		arg.EnterScope();
		for (VariableDeclaration vd : n.variabledeclarations) 
			vd.accept(this, arg);
		for (Statement s : n.statements) 
			s.accept(this, arg);
		arg.LeaveScope();
	}

	@Override
	public void visit(WhileStatement n, SymbolTable arg) {
		Type c = n.cond.accept(new TypeVisitor(), arg);
		if (!c.IsType(Primitive.Boolean)) {
			Report.ExitWithError("Condition must be boolean. (WhileStatement) (%d:%d)",
					n.cond.getBeginLine(), n.cond.getBeginColumn());
		}
		n.loop.accept(this, arg);
	}

	@Override
	public void visit(Type n, SymbolTable arg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(PrimitiveType n, SymbolTable arg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ClassType n, SymbolTable arg) {
		if (!ClassTable.ct.containsKey(n.id)) {
			Report.ExitWithError("Class type %s cannot be resolved. (%d:%d)", 
					n.id, n.getBeginLine(), n.getBeginColumn());
		}
	}

}
