package ast.visitor;

import java.util.LinkedList;
import java.util.List;

import actrec.Frame;
import actrec.Label;
import actrec.Record;
import actrec.Temp;
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
import ast.expression.BinaryExpression.Operator;
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
import ir.translate.Ex;
import ir.translate.Nx;
import ir.translate.Procedure;
import ir.translate.RelCx;
import ir.translate.Tr;
import ir.tree.BINOP;
import ir.tree.CALL;
import ir.tree.CJUMP;
import ir.tree.CONST;
import ir.tree.ESEQ;
import ir.tree.EXPS;
import ir.tree.Exp;
import ir.tree.ExpList;
import ir.tree.JUMP;
import ir.tree.LABEL;
import ir.tree.MOVE;
import ir.tree.NAME;
import ir.tree.SEQ;
import ir.tree.Stm;
import ir.tree.TEMP;
import ir.tree.MEM;
import ir.tree.UMULL;

public class TranslateVisitor implements GenericVisitor<Tr, Object> {

	ClassDeclaration currentClass;
	MethodDeclaration currentMethod;
	Frame currentFrame;

	static public List<Procedure> procedures = new LinkedList<>();

	@Override
	public Tr visit(Node n, Object arg) {
		// Should never happen
		throw new Error();
	}

	@Override
	public Tr visit(MainClass n, Object arg) {
		currentClass = n;
		currentMethod = null;
		currentFrame = n.frame;
		procedures
				.add(new Procedure(n.frame, n.block.accept(this, arg).unNx()));
		return null;
	}

	@Override
	public Tr visit(Parameter n, Object arg) {
		// Should never happen
		throw new Error();
	}

	@Override
	public Tr visit(Program n, Object arg) {
		n.mc.accept(this, arg);
		for (ClassDeclaration cd : n.cds) {
			cd.accept(this, arg);
		}
		return null;
	}

	@Override
	public Tr visit(ClassDeclaration n, Object arg) {
		currentClass = n;
		for (MethodDeclaration md : n.methoddeclarations) {
			md.accept(this, arg);
		}
		return null;
	}

	@Override
	public Tr visit(MethodDeclaration n, Object arg) {
		currentMethod = n;
		currentFrame = n.frame;

		Tr methodbody = n.block.accept(this, arg);
		Tr returnexpr = n.returnexpr.accept(this, arg);
		Stm retval;
		if (!n.type.IsDoubleWord())
			retval = new MOVE(new TEMP(currentFrame.RV(0)), returnexpr.unEx());
		else {
			Exp retlo = returnexpr.unExLo();
			Exp rethi;
			if (!n.returnexpr.IsDoubleWord()) { // Sign Extend if we have too
				// Should only eval ret value once but we use it multiple times
				// move to new temp
				// retlo = new TEMP(tmp) ; after first use
				Temp tmp = new Temp();
				retlo = new ESEQ(new MOVE(new TEMP(tmp), retlo), new TEMP(tmp));
				rethi = new BINOP(Operator.ASR, new TEMP(tmp), new CONST(31));
			} else
				rethi = returnexpr.unExHi();
			retval = new SEQ(new MOVE(new TEMP(currentFrame.RV(0)), retlo),
					new MOVE(new TEMP(currentFrame.RV(1)), rethi));
		}
		Stm body = methodbody.unNx();
		if (body == Stm.NOOP)
			body = retval;
		else
			body = new SEQ(body, retval);
		procedures.add(new Procedure(n.frame, body));
		return null;
	}

	@Override
	public Tr visit(VariableDeclaration n, Object arg) {
		// Should never happen
		throw new Error();
	}

	@Override
	public Tr visit(ArrayAccessExpression n, Object arg) {
		Tr arr = n.expr.accept(this, arg);
		Tr idx = n.index.accept(this, arg);

		if (n.StaticCheck)
			if (n.ArrayBoundException)
				return new Ex(currentFrame.ExternalCall("exit", new ExpList(
						new CONST(1), null)));

		Temp idxTmp = new Temp();
		Temp arrTmp = new Temp();

		Exp elementsize = new CONST(n.IsDoubleWord() ? 8 : 4);
		Exp o = idx.unEx();
		Exp a = arr.unEx();
		if (!n.StaticCheck) {
			o = new TEMP(idxTmp);
			a = new TEMP(arrTmp);
		}
		o = new BINOP(Operator.Times, o, elementsize);
		o = new BINOP(Operator.Plus, o, new CONST(4));
		Exp addr = new BINOP(Operator.Plus, a, o);
		Temp addrT = new Temp();
		Exp access = new ESEQ(new MOVE(new TEMP(addrT), addr), new MEM(
				new TEMP(addrT)));

		if (n.StaticCheck)
			if (!n.IsDoubleWord())
				return new Ex(new MEM(addr));
			else
				return new Ex(access, new MEM(new BINOP(Operator.Plus,
						new TEMP(addrT), new CONST(4))));

		// Array Bound Check
		Stm test = currentFrame.ArrayBoundCheck(new ESEQ(new MOVE(new TEMP(
				arrTmp), arr.unEx()), new TEMP(arrTmp)), new ESEQ(new MOVE(
				new TEMP(idxTmp), idx.unEx()), new TEMP(idxTmp)));

		if (!n.IsDoubleWord())
			return new Ex(new ESEQ(test, new MEM(addr)));
		return new Ex(new ESEQ(test, access), new MEM(new BINOP(Operator.Plus,
				new TEMP(addrT), new CONST(4))));
	}

	@Override
	public Tr visit(BinaryExpression n, Object arg) {
		Tr e1 = n.e1.accept(this, arg);
		Tr e2 = n.e2.accept(this, arg);

		Exp e1lo = e1.unExLo();
		Exp e1hi = e1.unExHi();
		;
		if (n.e1promote) { // Sign Extend
			e1hi = new BINOP(Operator.ASR, e1lo, new CONST(31));
		}

		Exp e2lo = e2.unExLo();
		Exp e2hi = e2.unExHi();
		if (n.e2promote) { // Sign Extend
			e2hi = new BINOP(Operator.ASR, e2lo, new CONST(31));
		}
		Temp e1loT = new Temp();
		Temp e1hiT = new Temp();
		Temp e2loT = new Temp();
		Temp e2hiT = new Temp();
		Stm setup = new SEQ(new MOVE(new TEMP(e1loT), e1lo), new SEQ(new MOVE(
				new TEMP(e1hiT), e1hi), new SEQ(
				new MOVE(new TEMP(e2loT), e2lo),
				new MOVE(new TEMP(e2hiT), e2hi))));
		Temp rlo;
		Temp rhi;
		Temp ret;
		Label evalNext = new Label();
		Label tr = new Label();
		Label fa = new Label();
		Label ex = new Label();
		switch (n.op) {
		case And:
			ret = new Temp();
			return new Ex(new ESEQ(new SEQ(new CJUMP(Operator.NotEq, e1.unEx(),
					new CONST(0), evalNext, fa), new SEQ(new LABEL(evalNext),
					new SEQ(new CJUMP(Operator.NotEq, e2.unEx(), new CONST(0),
							tr, fa), new SEQ(new LABEL(fa), new SEQ(new MOVE(
							new TEMP(ret), new CONST(0)), new SEQ(new JUMP(ex),
							new SEQ(new LABEL(tr), new SEQ(new MOVE(new TEMP(
									ret), new CONST(1)), new LABEL(ex))))))))),
					new TEMP(ret)));
		case Or:
			ret = new Temp();
			return new Ex(new ESEQ(new SEQ(new CJUMP(Operator.NotEq, e1.unEx(),
					new CONST(0), tr, evalNext), new SEQ(new LABEL(evalNext),
					new SEQ(new CJUMP(Operator.NotEq, e2.unEx(), new CONST(0),
							tr, fa), new SEQ(new LABEL(fa), new SEQ(new MOVE(
							new TEMP(ret), new CONST(0)), new SEQ(new JUMP(ex),
							new SEQ(new LABEL(tr), new SEQ(new MOVE(new TEMP(
									ret), new CONST(1)), new LABEL(ex))))))))),
					new TEMP(ret)));
		case Plus:
			if (!n.IsDoubleWord())
				return new Ex(new BINOP(n.op, e1.unEx(), e2.unEx()));
			// adds rlo e1lo e2lo
			// adc rhi e1hi e2hi
			rlo = new Temp();
			rhi = new Temp();
			Stm add = new SEQ(setup, new SEQ(new MOVE(new TEMP(rlo), new BINOP(
					Operator.ADDS, new TEMP(e1loT), new TEMP(e2loT))),
					new MOVE(new TEMP(rhi), new BINOP(Operator.ADC, new TEMP(
							e1hiT), new TEMP(e2hiT)))));
			return new Ex(new ESEQ(add, new TEMP(rlo)), new TEMP(rhi));
		case Minus:
			if (!n.IsDoubleWord())
				return new Ex(new BINOP(n.op, e1.unEx(), e2.unEx()));
			// subs rlo e1lo e2lo
			// sbc rhi e1hi e2hi
			rlo = new Temp();
			rhi = new Temp();
			Stm sub = new SEQ(setup, new SEQ(new MOVE(new TEMP(rlo), new BINOP(
					Operator.SUBS, new TEMP(e1loT), new TEMP(e2loT))),
					new MOVE(new TEMP(rhi), new BINOP(Operator.SBC, new TEMP(
							e1hiT), new TEMP(e2hiT)))));
			return new Ex(new ESEQ(sub, new TEMP(rlo)), new TEMP(rhi));
		case Times:
			// umull rlo rhi e1lo e2lo
			// mul t e1hi e2lo
			// add rhi rhi t
			// mul t e1lo e2hi
			// add rhi rhi t
			if (!n.IsDoubleWord())
				return new Ex(new BINOP(n.op, e1.unEx(), e2.unEx()));
			rlo = new Temp();
			rhi = new Temp();
			Exp t = new BINOP(Operator.Times, new TEMP(e1hiT), new TEMP(e2loT));
			Exp hi = new BINOP(Operator.Plus, t, new TEMP(rhi));
			t = new BINOP(Operator.Times, new TEMP(e1loT), new TEMP(e2hiT));
			hi = new BINOP(Operator.Plus, t, hi);
			Stm mul = new SEQ(setup, new SEQ(new UMULL(new TEMP(rlo), new TEMP(
					rhi), new TEMP(e1loT), new TEMP(e2loT)), new MOVE(new TEMP(
					rhi), hi)));
			return new Ex(new ESEQ(mul, new TEMP(rlo)), new TEMP(rhi));
		case Eq:
		case Greater:
		case GreaterEq:
		case Less:
		case LessEq:
		case NotEq:
			return new RelCx(n.op, e1, e2);
		default:
			return null;
		}
	}

	@Override
	public Tr visit(BooleanLiteralExpression n, Object arg) {
		return new Ex(new CONST(n.value ? 1 : 0));
	}

	@Override
	public Tr visit(Expression n, Object arg) {
		// Should never happen
		throw new Error();
	}

	@Override
	public Tr visit(IdentifierExpression n, Object arg) {
		Exp fp = new TEMP(currentFrame.FP());
		if (n.decl.isField) {
			fp = currentFrame.thisPtr.unEx(fp);
		}
		if (n.IsDoubleWord())
			return new Ex(n.decl.access.unExLo(fp), n.decl.access.unExHi(fp));
		return new Ex(n.decl.access.unEx(fp));
	}

	@Override
	public Tr visit(IntegerLiteralExpression n, Object arg) {
		return new Ex(new CONST(n.value));
	}

	@Override
	public Tr visit(LengthExpression n, Object arg) {
		Tr arr = n.expr.accept(this, arg);
		return new Ex(new MEM(arr.unEx()));
	}

	@Override
	public Tr visit(LongLiteralExpression n, Object arg) {
		long l = Long.parseLong(n.value.substring(0, n.value.length() - 1));
		int rlo, rhi;
		rlo = (int) l;
		rhi = (int) (l >> 32);
		return new Ex(new CONST(rlo), new CONST(rhi));
	}

	@Override
	public Tr visit(MemberCallExpression n, Object arg) {
		Tr e = n.expr.accept(this, arg);
		Exp ptr = e.unEx();
		Exp virtPtr = null;
		ExpList explist = null;
		Temp tmp = new Temp();
		if (n.expr instanceof NewClassExpression
				&& Type.IsPrimative(n.decl.type)) {
			ptr = new ESEQ(new MOVE(new TEMP(tmp), ptr), new TEMP(tmp));
			Stm free = new EXPS(currentFrame.ExternalCall("free", new ExpList(new TEMP(tmp), null)));
			if (!n.decl.virtual)
				explist = new ExpList(ptr, null);
			else
		if (n.decl.virtual) { // ptr is used twice, so store it in a temp
			virtPtr = new ESEQ(new MOVE(new TEMP(tmp), ptr), new TEMP(tmp));
			ptr = new TEMP(tmp); // Swap place as virtPtr is evaluated first and
									// creates ptr
		}
		explist = new ExpList(ptr, null);
		ExpList t = explist;
		for (Expression ex : n.exprlist) {
			if (ex.IsDoubleWord()) {
				Tr exp = ex.accept(this, arg);
				t = t.tail = new ExpList(exp.unExLo(), null);
				t = t.tail = new ExpList(exp.unExHi(), null);
			} else {
				t = t.tail = new ExpList(ex.accept(this, arg).unEx(), null);
			}
		}
		CALL call;
		if (n.decl.virtual)
			call = new CALL(n.decl.access.unEx(virtPtr), explist);
		else
			call = new CALL(new NAME(n.decl.frame.frameLabel), explist);
		if (!n.IsDoubleWord())
			return new Ex(call);
		tmp = new Temp();
		Temp tmphi = new Temp();
		return new Ex(
				new ESEQ(new SEQ(new MOVE(new TEMP(tmp), call), new MOVE(
						new TEMP(tmphi), new TEMP(currentFrame.RV(1)))),
						new TEMP(tmp)), new TEMP(tmphi));

	}

	@Override
	public Tr visit(NewArrayExpression n, Object arg) {
		Tr size = n.size.accept(this, arg);
		ExpList explist = null;
		Exp num = size.unEx();
		Temp t = new Temp();
		Temp ret = new Temp();
		// Obviously fails if array size is over half max int
		num = new ESEQ(new MOVE(new TEMP(t), num), new TEMP(t));
		if (n.IsDoubleInnerWord())
			num = new BINOP(Operator.Times, num, new CONST(2));
		explist = new ExpList(new BINOP(Operator.Plus, num, new CONST(1)),
				explist);
		explist = new ExpList(new CONST(4), explist);
		Exp call = currentFrame.ExternalCall("calloc", explist);
		return new Ex(new ESEQ(new SEQ(new MOVE(new TEMP(ret), call), new MOVE(
				new MEM(new TEMP(ret)), new TEMP(t))), new TEMP(ret)));
	}

	@Override
	public Tr visit(NewClassExpression n, Object arg) {
		ExpList explist = null;
		// Size of the object in bytes
		if (Record.records.get(n.id).Size() == 0)
			explist = new ExpList(new CONST(1), explist);
		else
			explist = new ExpList(new CONST(Record.records.get(n.id).Size()),
					explist);
		explist = new ExpList(new CONST(1), explist);
		Temp t = new Temp();
		if (Record.records.get(n.id).VtableSize() == 0)
			return new Ex(new ESEQ(new MOVE(new TEMP(t),
					currentFrame.ExternalCall("calloc", explist)), new TEMP(t)));
		return new Ex(new ESEQ(new SEQ(new MOVE(new TEMP(t),
				currentFrame.ExternalCall("calloc", explist)), new MOVE(
				new MEM(new TEMP(t)), currentFrame.GetDataLabel(Record.records
						.get(n.id).GetVTable()))), new TEMP(t)));
	}

	@Override
	public Tr visit(ThisExpression n, Object arg) {
		return new Ex(currentFrame.thisPtr.unEx(new TEMP(currentFrame.FP())));
	}

	@Override
	public Tr visit(UnaryExpression n, Object arg) {
		Tr e = n.expr.accept(this, arg);
		return new Ex(new BINOP(Operator.XOR, new CONST(1), e.unEx()));
	}

	@Override
	public Tr visit(AssignmentStatement n, Object arg) {
		Exp fp = new TEMP(currentFrame.FP());
		if (n.decl.isField)
			fp = currentFrame.thisPtr.unEx(fp);
		Tr rhs = n.expr.accept(this, arg);
		Temp rhslo = new Temp();
		Temp rhshi = new Temp();
		Stm setup = null;
		if (n.decl.type.IsDoubleWord() || n.decl.type.IsDoubleWordInnerType()) {
			setup = new MOVE(new TEMP(rhslo), rhs.unExLo());
			if (!n.expr.IsDoubleWord()) { // Sign Extend 
				setup = new SEQ(setup, 
						new MOVE(new TEMP(rhshi), 
								 new BINOP(Operator.ASR, new TEMP(rhslo), new CONST(31))));
			} else
				setup = new SEQ(setup, 
						new MOVE(new TEMP(rhshi), rhs.unExHi()));
		}
		if (n.index == null) {
			if (n.decl.type.IsDoubleWord())
				return new Nx(new SEQ(setup,
							  new SEQ(new MOVE(n.decl.access.unExLo(fp), new TEMP(rhslo)), 
									  new MOVE(n.decl.access.unExHi(fp), new TEMP(rhshi)))));
			else
				return new Nx(new MOVE(n.decl.access.unEx(fp), rhs.unEx()));
		}
		if (n.StaticCheck)
			if (n.ArrayBoundException)
				return new Nx(currentFrame.ExitWithFailiur());
		Tr idx = n.index.accept(this, arg);
		Exp arr = n.decl.access.unEx(fp);

		Temp idxTmp = new Temp();

		Exp elementsize = new CONST(n.decl.type.IsDoubleWordInnerType() ? 8 : 4);
		Exp o = idx.unEx();
		if (!n.StaticCheck)
			o = new TEMP(idxTmp);
		o = new BINOP(Operator.Times, o, elementsize);
		o = new BINOP(Operator.Plus, o, new CONST(4));
		Exp addr = new BINOP(Operator.Plus, arr, o);
		Temp addrT = new Temp();
		Stm access = new MOVE(new TEMP(addrT), addr);

		if (n.StaticCheck)
			if (!n.decl.type.IsDoubleWordInnerType())
				return new Nx(new MOVE(new MEM(addr), rhs.unEx()));
			else
				return new Nx(new SEQ(access, 
							  new SEQ(setup, 
						      new SEQ(new MOVE(new MEM(new TEMP(addrT)), new TEMP(rhslo)), 
						    		  new MOVE(new MEM(new BINOP(Operator.Plus, new TEMP(addrT),new CONST(4))), 
						    				   new TEMP(rhshi))))));

		// Array Bound Check
		Stm test = currentFrame.ArrayBoundCheck(arr, new ESEQ(new MOVE(
				new TEMP(idxTmp), idx.unEx()), new TEMP(idxTmp)));

		if (!n.decl.type.IsDoubleWordInnerType())
			return new Nx(new SEQ(test, new MOVE(new MEM(addr), rhs.unEx())));
		return new Nx(new SEQ(test, 
				  	  new SEQ(access,
					  new SEQ(setup,
					  new SEQ(new MOVE(new MEM(new TEMP(addrT)), new TEMP(rhslo)), 
							  new MOVE(new MEM(new BINOP(Operator.Plus, new TEMP(addrT), new CONST(4))), 
									   new TEMP(rhshi)))))));
	}

	@Override
	public Tr visit(IfStatement n, Object arg) {
		Tr co = n.cond.accept(this, arg);
		Tr th = n.thenstmt.accept(this, arg);
		Label t = new Label();
		Label e = new Label();
		if (n.elsestmt != null) {
			Label f = new Label();
			Tr el = n.elsestmt.accept(this, arg);
			return new Nx(new SEQ(co.unCx(t, f), new SEQ(new LABEL(t), new SEQ(
					th.unNx(), new SEQ(new JUMP(e), new SEQ(new LABEL(f),
							new SEQ(el.unNx(), new LABEL(e))))))));
		} else {
			return new Nx(new SEQ(co.unCx(t, e), new SEQ(new LABEL(t), new SEQ(
					th.unNx(), new LABEL(e)))));
		}
	}

	@Override
	public Tr visit(PrintStatement n, Object arg) {
		Exp str;
		Tr val = n.expr.accept(this, arg);
		if (n.expr instanceof BooleanLiteralExpression) {
			str = currentFrame
					.GetPrintStrBool(((BooleanLiteralExpression) n.expr).value);
			return new Nx(new EXPS(currentFrame.ExternalCall("printf",
					new ExpList(str, null))));
		} else if (n.type.IsType(Primitive.Boolean)) {
			return new Nx(currentFrame.GetPrintStrBool(val.unEx()));
		}
		str = currentFrame.GetPrintStr(n.type.IsDoubleWord());
		ExpList explist = null;
		explist = new ExpList(str, explist);
		ExpList t = explist;
		if (!n.type.IsDoubleWord())
			t = t.tail = new ExpList(val.unEx(), null);
		else {
			// t = t.tail = new ExpList(new CONST(0), null); // padding
			t = t.tail = new ExpList(val.unExLo(), null);
			t = t.tail = new ExpList(val.unExHi(), null);
		}
		return new Nx(new EXPS(currentFrame.ExternalCall("printf", explist)));
	}

	@Override
	public Tr visit(Statement n, Object arg) {
		// Should never happen
		throw new Error();
	}

	@Override
	public Tr visit(StatementBlock n, Object arg) {
		Stm s = null;
		for (Statement st : n.statements)
			if (s == null)
				s = st.accept(this, arg).unNx();
			else
				s = new SEQ(s, st.accept(this, arg).unNx());
		if (s == null)
			s = Stm.NOOP;
		return new Nx(s);
	}

	@Override
	public Tr visit(WhileStatement n, Object arg) {
		Tr co = n.cond.accept(this, arg);
		Tr lo = n.loop.accept(this, arg);
		Label cond = new Label();
		Label loop = new Label();
		Label exit = new Label();
		return new Nx(new SEQ(new LABEL(cond), new SEQ(co.unCx(loop, exit),
				new SEQ(new LABEL(loop), new SEQ(lo.unNx(), new SEQ(new JUMP(
						cond), new LABEL(exit)))))));
	}

	@Override
	public Tr visit(Type n, Object arg) {
		// Should never happen
		throw new Error();
	}

	@Override
	public Tr visit(PrimitiveType n, Object arg) {
		// Should never happen
		throw new Error();
	}

	@Override
	public Tr visit(ClassType n, Object arg) {
		// Should never happen
		throw new Error();
	}

}
