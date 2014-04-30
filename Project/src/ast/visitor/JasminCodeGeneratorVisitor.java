package ast.visitor;

import java.util.HashMap;

import actrec.Label;
import actrec.jvm.Frame.LocalVar;
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

public class JasminCodeGeneratorVisitor implements
		GenericVisitor<String, Object> {
	public HashMap<String, String> classes = new HashMap<String, String>();
	private String currentClass;
	private String NewLine(String s) {
		return s + "\n";
	}
	@Override
	public String visit(Node n, Object arg) {
		// Should never happen
		throw new Error();
	}

	@Override
	public String visit(MainClass n, Object arg) {
		String s = "";
		s += NewLine(".class public '" + n.id + "'");
		s += NewLine(".super java/lang/Object");
		s += DefaultConstructor();
		s += NewLine(".method public static main([Ljava/lang/String;)V");
		s += NewLine(".limit stack " + n.block.accept(new JVMStackVisitor(), new JVMStack()));
		s += NewLine(".limit locals " + ((actrec.jvm.Frame)n.frame).localidx);
		s += n.block.accept(this, arg);
		s += NewLine("return");
		s += NewLine(".end method");
		classes.put(n.id, s);
		return null;
	}

	private String DefaultConstructor() {
		return DefaultConstructor("java/lang/Object");
	}
	private String DefaultConstructor(String sup) {
		String s = "";
		s += NewLine(".method public <init>()V");
		s += NewLine(".limit stack 1");
		s += NewLine(".limit locals 1");
		s += NewLine("aload_0");
		s += NewLine("invokespecial " + sup + "/<init>()V");
		s += NewLine("return");
		s += NewLine(".end method");
		return s;
	}
	@Override
	public String visit(Parameter n, Object arg) {
		// Should never happen
		throw new Error();
	}

	@Override
	public String visit(Program n, Object arg) {
		n.mc.accept(this, arg);
		for (ClassDeclaration cd : n.cds)
			cd.accept(this, arg);
		return null;
	}

	@Override
	public String visit(ClassDeclaration n, Object arg) {
		String s = "";
		currentClass = n.id;
		s += NewLine(".class public '" + n.id + "'");
		if (n.extendsID == null)
			s += NewLine(".super java/lang/Object");
		else
			s += NewLine(".super '" + n.extendsID + "'");
		for (VariableDeclaration vd : n.variabledeclatartions)
			s += NewLine(".field public '" + vd.id + "' " + GetType(vd.type));
		if (n.extendsID == null)
			s += DefaultConstructor();
		else
			s += DefaultConstructor(n.extendsID);
		for (MethodDeclaration md : n.methoddeclarations) 
			s += md.accept(this, arg);
		classes.put(n.id, s);
		return null;
	}

	@Override
	public String visit(MethodDeclaration n, Object arg) {
		String s = "";
		s += NewLine(".method public " + GetDesc(n));
		s += NewLine(".limit stack " + n.accept(new JVMStackVisitor(), new JVMStack()));
		s += NewLine(".limit locals " + ((actrec.jvm.Frame)n.frame).localidx);
		s += n.block.accept(this, arg);
		s += n.returnexpr.accept(this, arg);
		if (n.type.IsType(Primitive.Boolean) || n.type.IsType(Primitive.Int))
			s += NewLine("ireturn");
		else if (n.type.IsType(Primitive.Long)) {
			if (!n.returnexpr.IsDoubleWord())
				s += NewLine("i2l");
			s += NewLine("lreturn");
		} else
			s += NewLine("areturn");
		s += NewLine(".end method");
		return s;
	}

	@Override
	public String visit(VariableDeclaration n, Object arg) {
		// Should never happen
		throw new Error();
	}

	@Override
	public String visit(ArrayAccessExpression n, Object arg) {
		String s = "";
		s += n.expr.accept(this, arg);
		s += n.index.accept(this, arg);
		if (n.type == Primitive.IntArr) {
			s += NewLine("iaload");
		} else if (n.type == Primitive.LongArr) {
			s += NewLine("laload");
		}
		return s;
	}

	@Override
	public String visit(BinaryExpression n, Object arg) {
		String s = "";
		s += n.e1.accept(this, arg);
		Label l1 = new Label();
		Label l2 = new Label();
		if (n.op == BinaryExpression.Operator.Or) {
			s += NewLine("dup");
			s += NewLine("ifne " + l1.label);
		}
		if (n.op == BinaryExpression.Operator.And) {
			s += NewLine("dup");
			s += NewLine("ifeq " + l1.label);
		}
		if (n.e1promote)
			s += NewLine("i2l");
		s += n.e2.accept(this, arg);
		if (n.e2promote)
			s += NewLine("i2l");
		switch (n.op) {
		case Greater:
			if (n.itype == Primitive.Int) {
				s += NewLine("if_icmpgt " + l1.label);
			} else {
				s += NewLine("lcmp");
				s += NewLine("ifgt " + l1.label);
			}
			s += NewLine("iconst_0");
			s += NewLine("goto " + l2.label);
			s += NewLine(l1.label + ":");
			s += NewLine("iconst_1");
			s += NewLine(l2.label + ":");
			break;
		case GreaterEq:
			if (n.itype == Primitive.Int) {
				s += NewLine("if_icmpge " + l1.label);
			} else {
				s += NewLine("lcmp");
				s += NewLine("ifge " + l1.label);
			}
			s += NewLine("iconst_0");
			s += NewLine("goto " + l2.label);
			s += NewLine(l1.label + ":");
			s += NewLine("iconst_1");
			s += NewLine(l2.label + ":");
			break;
		case Less:
			if (n.itype == Primitive.Int) {
				s += NewLine("if_icmplt " + l1.label);
			} else {
				s += NewLine("lcmp");
				s += NewLine("iflt " + l1.label);
			}
			s += NewLine("iconst_0");
			s += NewLine("goto " + l2.label);
			s += NewLine(l1.label + ":");
			s += NewLine("iconst_1");
			s += NewLine(l2.label + ":");
			break;
		case LessEq:
			if (n.itype == Primitive.Int) {
				s += NewLine("if_icmple " + l1.label);
			} else {
				s += NewLine("lcmp");
				s += NewLine("ifle " + l1.label);
			}
			s += NewLine("iconst_0");
			s += NewLine("goto " + l2.label);
			s += NewLine(l1.label + ":");
			s += NewLine("iconst_1");
			s += NewLine(l2.label + ":");
			break;
		case NotEq:
			if (n.itype == Primitive.Int || n.itype == Primitive.Boolean) {
				s += NewLine("if_icmpne " + l1.label);
			} else if (n.itype == Primitive.Long) {
				s += NewLine("lcmp");
				s += NewLine("ifne " + l1.label);
			} else {
				s += NewLine("if_acmpne " + l1.label);
			}
			s += NewLine("iconst_0");
			s += NewLine("goto " + l2.label);
			s += NewLine(l1.label + ":");
			s += NewLine("iconst_1");
			s += NewLine(l2.label + ":");
			break;
		case Eq:
			if (n.itype == Primitive.Int || n.itype == Primitive.Boolean) {
				s += NewLine("if_icmpeq " + l1.label);
			} else if (n.itype == Primitive.Long) {
				s += NewLine("lcmp");
				s += NewLine("ifeq " + l1.label);
			} else {
				s += NewLine("if_acmpeq " + l1.label);
			}
			s += NewLine("iconst_0");
			s += NewLine("goto " + l2.label);
			s += NewLine(l1.label + ":");
			s += NewLine("iconst_1");
			s += NewLine(l2.label + ":");
			break;
		case Or:
			s += NewLine("ior");
			s += NewLine(l1.label + ":");
			break;
		case And:
			s += NewLine("iand");
			s += NewLine(l1.label + ":");
			break;
		case Plus:
			if (n.type == Primitive.Int) {
				s += NewLine("iadd");
			} else {
				s += NewLine("ladd");
			}
			break;
		case Minus:
			if (n.type == Primitive.Int) {
				s += NewLine("isub");
			} else {
				s += NewLine("lsub");
			}
			break;
		case Times:
			if (n.type == Primitive.Int) {
				s += NewLine("imul");
			} else {
				s += NewLine("lmul");
			}
			break;

		default:
			break;
		}
		return s;
	}

	@Override
	public String visit(BooleanLiteralExpression n, Object arg) {
		if (n.value) 
			return NewLine("iconst_1");
		return NewLine("iconst_0");
	}

	@Override
	public String visit(Expression n, Object arg) {
		// Should never happen
		throw new Error();
	}

	@Override
	public String visit(IdentifierExpression n, Object arg) {
		Type t = n.decl.type;
		if (n.decl.isField) {
			return NewLine("aload_0") + 
					NewLine("getfield " + currentClass + "/" + n.id + " " + GetType(n.decl.type));
		}
		LocalVar l = (LocalVar)n.decl.access;
		if (t.IsType(Primitive.Int) || t.IsType(Primitive.Boolean))
			return NewLine("iload " + l.idx);
		else if (t.IsType(Primitive.Long))
			return NewLine("lload " + l.idx);
		return NewLine("aload " + l.idx);
	}

	@Override
	public String visit(IntegerLiteralExpression n, Object arg) {
		return NewLine("ldc " + n.value);
	}

	@Override
	public String visit(LengthExpression n, Object arg) {
		String s = "";
		s += n.expr.accept(this, arg);
		s += NewLine("arraylength");
		return s;
	}

	@Override
	public String visit(LongLiteralExpression n, Object arg) {
		return NewLine("ldc2_w " + n.value.substring(0,n.value.length()-1));
	}

	@Override
	public String visit(MemberCallExpression n, Object arg) {
		String s = "";
		s += n.expr.accept(this, arg);
		for (Expression e : n.exprlist) 
			s += e.accept(this, arg);
		s += NewLine("invokevirtual " + n.typeid + "/" + GetDesc(n.decl));
		return s;
	}

	private String GetDesc(MethodDeclaration decl) {
		String s = "";
		s += decl.id + "(";
		for (Parameter p : decl.parameters)
			s += GetType(p.type);
		s += ")" + GetType(decl.type);
		return s;
	}
	private String GetType(Type type) {
		if (type.IsType(Primitive.Boolean))
			return "Z";
		if (type.IsType(Primitive.Int))
			return "I";
		if (type.IsType(Primitive.IntArr))
			return "[I";
		if (type.IsType(Primitive.Long))
			return "J";
		if (type.IsType(Primitive.LongArr))
			return "[J";
		return "L" + ((ClassType)type).id + ";";
	}
	@Override
	public String visit(NewArrayExpression n, Object arg) {
		String s = "";
		s += n.size.accept(this, arg);
		if (n.primitive == Primitive.IntArr)
			s += NewLine("newarray int");
		else 
			s += NewLine("newarray long");
		return s;
	}

	@Override
	public String visit(NewClassExpression n, Object arg) {
		String s = "";
		s += NewLine("new '" + n.id + "'");
		s += NewLine("dup");
		s += NewLine("invokespecial " + n.id + "/<init>()V");
		return s;
	}

	@Override
	public String visit(ThisExpression n, Object arg) {
		return NewLine("aload_0");
	}

	@Override
	public String visit(UnaryExpression n, Object arg) {
		String s = "";
		Label l1 = new Label();
		Label l2 = new Label();
		s += n.expr.accept(this, arg);
		s += NewLine("ifeq " + l1.label);
		s += NewLine("iconst_0");
		s += NewLine("goto " + l2.label);
		s += NewLine(l1.label + ":");
		s += NewLine("iconst_1");
		s += NewLine(l2.label + ":");
		return s;
	}

	@Override
	public String visit(AssignmentStatement n, Object arg) {
		String s = "";
		LocalVar l = (LocalVar)n.decl.access;
		if (n.index == null) {
			if (n.decl.isField) {
				s += NewLine("aload_0");
				s += n.expr.accept(this, arg);
				if (!n.expr.IsDoubleWord() && n.decl.type.IsDoubleWord()) 
					s += NewLine("i2l");
				s += NewLine("putfield " + currentClass + "/" + n.id + " " + GetType(n.decl.type));
			} else {
				s += n.expr.accept(this, arg);
				if (n.decl.type.IsType(Primitive.Boolean) || n.decl.type.IsType(Primitive.Int))
					s += NewLine("istore " + l.idx);
				else if (n.decl.type.IsType(Primitive.Long)) {
					if (!n.expr.IsDoubleWord()) 
						s += NewLine("i2l");
					s += NewLine("lstore " + l.idx);
				} else
					s += NewLine("astore " + l.idx);
			}
		} else {
			if (n.decl.isField) {
				s += NewLine("aload_0");
				s += NewLine("getfield " + currentClass + "/" + n.id + " " + GetType(n.decl.type));
			} else {
				s += NewLine("aload " + l.idx);
			}
			s += n.index.accept(this, arg);
			s += n.expr.accept(this, arg);
			if (n.decl.type.IsType(Primitive.IntArr))
				s += NewLine("iastore");
			else {
				if (!n.expr.IsDoubleWord()) 
					s += NewLine("i2l");
				s += NewLine("lastore");
			}
		}
		return s;
	}

	@Override
	public String visit(IfStatement n, Object arg) {
		String s = "";
		Label el = new Label();
		Label en = new Label();
		s += n.cond.accept(this, arg);
		if (n.elsestmt == null)
			s += NewLine("ifeq " + en.label);
		else
			s += NewLine("ifeq " + el.label);
		s += n.thenstmt.accept(this, arg);
		if (n.elsestmt != null) {
			s += NewLine("goto " + en.label);
			s += NewLine(el.label + ":");
			s += n.elsestmt.accept(this, arg);
		}
		s += NewLine(en.label + ":");
		return s;
	}

	@Override
	public String visit(PrintStatement n, Object arg) {
		String s = "";
		s += NewLine("getstatic java/lang/System/out Ljava/io/PrintStream;");
		s += n.expr.accept(this, arg);
		if (n.type.IsType(Primitive.Boolean)) 
			s += NewLine("invokevirtual java/io/PrintStream/println(Z)V");
		else if (n.type.IsType(Primitive.Int)) 
			s += NewLine("invokevirtual java/io/PrintStream/println(I)V");
		else if (n.type.IsType(Primitive.Long)) 
			s += NewLine("invokevirtual java/io/PrintStream/println(J)V");
		else
			s += NewLine("invokevirtual java/io/PrintStream/println(Ljava/lang/Object;)V");
		return s;
	}

	@Override
	public String visit(Statement n, Object arg) {
		// Should never happen
		throw new Error();
	}

	@Override
	public String visit(StatementBlock n, Object arg) {
		String s = "";
		for (Statement st : n.statements) 
			s += st.accept(this, arg);
		return s;
	}

	@Override
	public String visit(WhileStatement n, Object arg) {
		String s = "";
		Label st = new Label();
		Label en = new Label();
		s += NewLine("goto " + en.label);
		s += NewLine(st.label + ":");
		s += n.loop.accept(this, arg);
		s += NewLine(en.label + ":");
		s += n.cond.accept(this, arg);
		s += NewLine("ifne " + st.label);
		return s;
	}

	@Override
	public String visit(Type n, Object arg) {
		// Should never happen
		throw new Error();
	}

	@Override
	public String visit(PrimitiveType n, Object arg) {
		// Should never happen
		throw new Error();
	}

	@Override
	public String visit(ClassType n, Object arg) {
		// Should never happen
		throw new Error();
	}

}
