package actrec.arm;

import ir.translate.Procedure;
import ir.tree.BINOP;
import ir.tree.CALL;
import ir.tree.CJUMP;
import ir.tree.CONST;
import ir.tree.EXPS;
import ir.tree.Exp;
import ir.tree.ExpList;
import ir.tree.JUMP;
import ir.tree.LABEL;
import ir.tree.MEM;
import ir.tree.MOVE;
import ir.tree.NAME;
import ir.tree.Stm;
import ir.tree.StmList;
import ir.tree.TEMP;
import ir.tree.UMULL;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import semanal.ClassTable;
import semanal.MiniJavaClass;
import actrec.Access;
import actrec.Label;
import actrec.Temp;
import actrec.TempList;
import assem.Instr;
import assem.OPER;
import assem.VALUE;
import ast.declaration.MethodDeclaration;
import ast.expression.BinaryExpression.Operator;

public class CodeGeneration {
	public static List<Instr> GenerateVTablesInstr() {
		List<Instr> al = new ArrayList<>();
		for (MiniJavaClass mjc : ClassTable.ct.values()) {
			if (mjc.cd == null)
				continue;
			Label l = mjc.cd.record.GetVTable();
			if (mjc.cd.record.VtableSize() == 0)
				continue;
			al.add(new assem.LABEL(l.label + ":", l));
			Instr[] vtable = new Instr[mjc.cd.record.VtableSize() / 4];
			for (MethodDeclaration md : mjc.methodtable.values()) {
				if (md.virtual || md.override) {
					vtable[md.access.GetOffset() / 4] = new VALUE(".long "
							+ md.frame.frameLabel.label,
							md.frame.frameLabel.label);
				}
			}
			al.addAll(Arrays.asList(vtable));
		}
		return al;
	}

	public static List<Instr> GeneratePrintLnInstr() {
		List<Instr> al = new ArrayList<>();
		al.add(new assem.LABEL(Frame.wordStr.label + ":", Frame.wordStr));
		al.add(new assem.VALUE(".asciz \"%li\\n\"", "\"%lli\""));
		al.add(new assem.VALUE(".align 2", "2"));
		al.add(new assem.LABEL(Frame.dwordStr.label + ":", Frame.dwordStr));
		al.add(new assem.VALUE(".asciz \"%lli\\n\"", "\"%lli\\n\""));
		al.add(new assem.VALUE(".align 2", "2"));
		al.add(new assem.LABEL(Frame.trueStr.label + ":", Frame.trueStr));
		al.add(new assem.VALUE(".asciz \"true\\n\"", "\"true\\n\""));
		al.add(new assem.VALUE(".align 2", "2"));
		al.add(new assem.LABEL(Frame.falseStr.label + ":", Frame.falseStr));
		al.add(new assem.VALUE(".asciz \"false\\n\"", "\"false\\n\""));
		al.add(new assem.VALUE(".align 2", "2"));
		return al;
	}

	public LinkedList<Instr> instructions;
	public actrec.Frame frame;

	public CodeGeneration(actrec.Frame frame) {
		this.frame = frame;
		instructions = new LinkedList<>();
	}

	public LinkedList<Instr> Codegen(Procedure proc) {
		StmList sl = proc.canon;
		while (sl.tail != null) {
			munchStm(sl.head);
			sl = sl.tail;
		}
		return proc.instrs = instructions;
	}

	private TempList L(Temp h) {
		return new TempList(h, null);
	}

	private TempList L(Temp t1, Temp t2) {
		return new TempList(t1, new TempList(t2, null));
	}

	public void Emit(Instr i) {
		instructions.add(i);
	}

	private void munchStm(Stm s) {
		if (s instanceof MOVE) {
			MOVE m = (MOVE) s;
			munchMOVE(m.a, m.e);
		} else if (s instanceof EXPS) {
			EXPS e = (EXPS) s;
			munchEXPS(e.e);
		} else if (s instanceof JUMP) {
			JUMP j = (JUMP) s;
			munchJUMP(j.l);
		} else if (s instanceof CJUMP) {
			CJUMP cj = (CJUMP) s;
			munchCJUMP(cj.op, cj.e1, cj.e2, cj.t, cj.f);
		} else if (s instanceof LABEL) {
			LABEL l = (LABEL) s;
			munchLABEL(l.l);
		} else if (s instanceof UMULL) {
			UMULL l = (UMULL) s;
			munchUMULL(l.rhi, l.rlo, l.e1, l.e2);
		}
	}

	/*
	 * munchMOVE
	 */
	private void munchMOVE(Exp a, Exp e) {
		if (a instanceof MEM && e instanceof TEMP) {
			MEM d = (MEM) a;
			TEMP s = (TEMP) e;
			if (d.e instanceof BINOP) {
				BINOP b = (BINOP) d.e;
				if (b.e2 instanceof CONST && ((CONST) b.e2).value < 4096) {
					TEMP t = new TEMP(munchExp(b.e1));
					CONST c = (CONST) b.e2;
					if (b.op == Operator.Plus) {
						String str = String.format("str `s0, [ `s1, #%d ]",
								c.value);
						Emit(new OPER(str, null, L(s.temp, t.temp)));
					} else if (b.op == Operator.Minus) {
						String str = String.format("str `s0, [ `s1, #%d ]",
								-c.value);
						Emit(new OPER(str, null, L(s.temp, t.temp)));
					}
				} else {
					Temp addr = munchExp(b);
					String str = String.format("str `s0, [ `s1 ]");
					Emit(new OPER(str, null, L(s.temp, addr)));
				} 
			} else if (d.e instanceof TEMP) {
				Temp addr = ((TEMP) d.e).temp;
				String str = String.format("str `s0, [ `s1 ]");
				Emit(new OPER(str, null, L(s.temp, addr)));
			} else {
				throw new Error("CG Tile:\n" + d.e);
			}
		} else if (a instanceof MEM && e instanceof NAME) {
			MEM d = (MEM) a;
			NAME n = (NAME) e;
			Temp t = munchNAME(n.label);
			if (d.e instanceof TEMP) {
				Temp addr = ((TEMP) d.e).temp;
				String str = String.format("str `s0, [ `s1 ]");
				Emit(new OPER(str, null, L(t, addr)));
			} else {
				throw new Error("CG Tile:\n" + d.e);
			}
		} else if (a instanceof MEM) {
			MEM d = (MEM) a;
			Temp s = munchExp(e);
			if (d.e instanceof BINOP) {
				BINOP b = (BINOP) d.e;
				if (b.e1 instanceof TEMP && b.e2 instanceof CONST
						&& ((CONST) b.e2).value < 4096) {
					TEMP t = (TEMP) b.e1;
					CONST c = (CONST) b.e2;
					if (b.op == Operator.Plus) {
						String str = String.format("str `s0, [ `s1, #%d ]",
								c.value);
						Emit(new OPER(str, null, L(s, t.temp)));
					} else if (b.op == Operator.Minus) {
						String str = String.format("str `s0, [ `s1, #%d ]",
								-c.value);
						Emit(new OPER(str, null, L(s, t.temp)));
					}
				} else {
					Temp addr = munchExp(b);
					String str = String.format("str `s0, [ `s1 ]");
					Emit(new OPER(str, null, L(s, addr)));
				}
			} else if (d.e instanceof TEMP) {
				Temp addr = ((TEMP) d.e).temp;
				String str = String.format("str `s0, [ `s1 ]");
				Emit(new OPER(str, null, L(s, addr)));
			} else {
				throw new Error("CG Tile:\n" + d.e);
			}
		} else if (a instanceof TEMP && e instanceof CALL) {
			TEMP t = (TEMP) a;
			CALL c = (CALL) e;
			TempList args = munchArgs(c.explist);
			if (c.f instanceof NAME) {
				NAME n = (NAME) c.f;
				Emit(new OPER("bl " + n.label.label, Hardware.calldefs, args));
				Emit(new assem.MOVE("mov `d0, `s0", t.temp, frame.RV(0)));
			} else if (c.f instanceof MEM) {
				MEM m = (MEM) c.f;
				// TODO figure out order here? calculate function ptr first or
				// args?
				Temp mt = munchMEM(m.e);
				Emit(new OPER("blx `s0", Hardware.calldefs, new TempList(mt,
						args)));
				Emit(new assem.MOVE("mov `d0, `s0", t.temp, frame.RV(0)));
			} else if (c.f instanceof TEMP) {
				TEMP tf = (TEMP) c.f;
				Temp mt = tf.temp;
				Emit(new OPER("blx `s0", Hardware.calldefs, new TempList(mt,
						args)));
				Emit(new assem.MOVE("mov `d0, `s0", t.temp, frame.RV(0)));
			} else {
				throw new Error("CG Tile:\n" + a + "\n" + e);
			}
		} else if (a instanceof TEMP && e instanceof CONST) {
			TEMP t = (TEMP) a;
			CONST c = (CONST) e;
			// TODO Pseudo instruction, check that asm actually allows it
			String str = String.format("ldr `d0, =%d", c.value);
			Emit(new OPER(str, L(t.temp), null));
		} else if (a instanceof MEM) {
			throw new Error("CG Tile:\n" + a + "\n" + e);
		} else {
			Temp at = munchExp(a);
			Temp et = munchExp(e);
			Emit(new assem.MOVE("mov `d0, `s0", at, et));
		}
	}

	/*
	 * munchEXP
	 */
	private void munchEXPS(Exp exp) {
		if (exp instanceof CALL) {
			CALL c = (CALL) exp;
			if (c.f instanceof NAME) {
				NAME n = (NAME) c.f;
				TempList args = munchArgs(c.explist);
				Emit(new OPER("bl " + n.label.label, Hardware.calldefs, args));
			} else if (c.f instanceof MEM) {
				MEM m = (MEM) c.f;
				// TODO figure out order here? calculate function ptr first or
				// args?
				TempList args = munchArgs(c.explist);
				Temp mt = munchMEM(m);
				Emit(new OPER("blx `s0", Hardware.calldefs, new TempList(mt,
						args)));
			}
		} else if (exp instanceof CONST) { // No Op
			return;
		} else {
			munchExp(exp);
		}
	}

	/*
	 * munchJUMP
	 */
	private void munchJUMP(Label l) {
		Emit(new OPER("b " + l.label, null, null, l));
	}

	/*
	 * munchCJUMP
	 */
	private void munchCJUMP(Operator op, Exp e1, Exp e2, Label t, Label f) {
		String condc;
		switch (op) {
		case Eq:
			condc = "eq";
			break;
		case Greater:
			condc = "gt";
			break;
		case GreaterEq:
			condc = "ge";
			break;
		case GreaterEqU:
			condc = "hs";
			break;
		case GreaterU:
			condc = "hi";
			break;
		case Less:
			condc = "lt";
			break;
		case LessEq:
			condc = "le";
			break;
		case LessEqU:
			condc = "ls";
			break;
		case LessU:
			condc = "lo";
			break;
		case NotEq:
			condc = "ne";
			break;
		default:
			throw new Error("Invalid RelOp");
		}

		// False label is always the next instruction
		Temp e1t;
		if (e1 instanceof TEMP) {
			e1t = ((TEMP) e1).temp;
		} else {
			e1t = munchExp(e1);
		}
		if (e2 instanceof BINOP && ((BINOP) e2).e1 instanceof TEMP
				&& ((BINOP) e2).op == Operator.ASR
				&& ((BINOP) e2).e2 instanceof CONST) {
			CONST c = (CONST) ((BINOP) e2).e2;
			Temp e2t = ((TEMP) ((BINOP) e2).e1).temp;
			String str = String.format("cmp `s0,`s1, asr #%d", c.value);
			Emit(new OPER(str, null, L(e1t, e2t)));
			str = String.format("b%s %s", condc, t.label);
			Emit(new OPER(str, null, null, t, f));
		} else if (e2 instanceof TEMP) {
			Temp e2t = ((TEMP) e2).temp;
			Emit(new OPER("cmp `s0,`s1", null, L(e1t, e2t)));
			String str = String.format("b%s %s", condc, t.label);
			Emit(new OPER(str, null, null, t, f));
		} else if (e2 instanceof CONST) {
			CONST c = (CONST) e2;
			String str;
			if (!IsFlexOp2(c.value)) {
				Temp ct = munchCONST(c.value);
				str = String.format("cmp `s0, `s1");
				Emit(new OPER(str, null, L(e1t, ct)));
			} else {
				str = String.format("cmp `s0, #%d", c.value);
				Emit(new OPER(str, null, L(e1t)));
			}
			str = String.format("b%s %s", condc, t.label);
			Emit(new OPER(str, null, null, t, f));
		} else {
			Temp e2t = munchExp(e2);
			Emit(new OPER("cmp `s0,`s1", null, L(e1t, e2t)));
			String str = String.format("b%s %s", condc, t.label, f.label);
			Emit(new OPER(str, null, null, t, f));
		}
	}

	private boolean IsFlexOp2(int value) {
		// TODO Could add rotations
		return (value < 128);
	}

	/*
	 * munchLABEL
	 */
	private void munchLABEL(Label l) {
		Emit(new assem.LABEL(l.label + ":", l));
	}

	/*
	 * munchUMULL
	 */
	private void munchUMULL(TEMP rlo, TEMP rhi, Exp e1, Exp e2) {
		Temp e1t = munchExp(e1);
		Temp e2t = munchExp(e2);
		Emit(new OPER("umull `d0, `d1, `s0, `s1", L(rhi.temp, rlo.temp), L(e1t,
				e2t)));
	}

	private Temp munchExp(Exp e) {
		if (e instanceof CALL) {
			CALL c = (CALL) e;
			return munchCALL(c.f, c.explist);
		} else if (e instanceof TEMP) {
			TEMP t = (TEMP) e;
			return munchTEMP(t.temp);
		} else if (e instanceof CONST) {
			CONST c = (CONST) e;
			return munchCONST(c.value);
		} else if (e instanceof BINOP) {
			BINOP b = (BINOP) e;
			return munchBINOP(b.op, b.e1, b.e2);
		} else if (e instanceof MEM) {
			MEM m = (MEM) e;
			return munchMEM(m.e);
		} else if (e instanceof NAME) {
			NAME n = (NAME) e;
			return munchNAME(n.label);
		}
		throw new Error("CG Tile:\n" + e);
	}

	/*
	 * munchCALL
	 */
	private Temp munchCALL(Exp f, ExpList a) {
		Temp t = new Temp();
		if (f instanceof NAME) {
			NAME n = (NAME) f;
			TempList args = munchArgs(a);
			Emit(new OPER("bl " + n.label.label, Hardware.calldefs, args));
			Emit(new assem.MOVE("mov `d0, `s0", t, frame.RV(0)));
		} else if (f instanceof MEM) {
			MEM m = (MEM) f;
			// TODO figure out order here? calculate function ptr first or args?
			TempList args = munchArgs(a);
			Temp mt = munchMEM(m.e);
			Emit(new OPER("blx `s0", Hardware.calldefs, new TempList(mt, args)));
			Emit(new assem.MOVE("mov `d0, `s0", t, frame.RV(0)));
		} else {
			throw new Error("CG Tile:\n" + f);
		}
		return t;
	}

	/*
	 * munchTEMP
	 */
	private Temp munchTEMP(Temp t) {
		return t;
	}

	/*
	 * munchCONST
	 */
	private Temp munchCONST(int value) {
		// move the value in const to a temp
		Temp t = new Temp();
		String str = String.format("ldr `d0, =%d", value);
		Emit(new OPER(str, L(t), null));
		return t;
	}

	/*
	 * munchBINOP
	 */
	private Temp munchBINOP(Operator op, Exp e1, Exp e2) {
		String instr;
		switch (op) {
		case ADC:
			instr = "adc";
			break;
		case ADDS:
			instr = "adds";
			break;
		case ASR:
			instr = "asr";
			break;
		case And:
			instr = "and";
			break;
		case Minus:
			instr = "sub";
			break;
		case Or:
			instr = "orr";
			break;
		case Plus:
			instr = "add";
			break;
		case SBC:
			instr = "sbc";
			break;
		case SUBS:
			instr = "subs";
			break;
		case Times:
			instr = "mul";
			break;
		case XOR:
			instr = "eor";
			break;
		default:
			throw new Error("Invalid BinOp");
		}
		Temp t = new Temp();
		if (e1 instanceof TEMP && e2 instanceof CONST
				&& IsFlexOp2(((CONST) e2).value) && op != Operator.Times) {
			Temp e1t = ((TEMP) e1).temp;
			int v = ((CONST) e2).value;
			// NOTE should fix ASR value but I know I only ever use 31
			String str = String.format("%s `d0, `s0, #%d", instr, v);
			Emit(new OPER(str, L(t), L(e1t)));
		} else if (e1 instanceof TEMP && e2 instanceof TEMP) {
			Temp e1t = ((TEMP) e1).temp;
			Temp e2t = ((TEMP) e2).temp;
			String str = String.format("%s `d0, `s0, `s1", instr);
			Emit(new OPER(str, L(t), L(e1t, e2t)));
		} else if (!(e1 instanceof TEMP)) {
			Temp e1t = munchExp(e1);
			t = munchBINOP(op, new TEMP(e1t), e2);
		} else if (e1 instanceof TEMP) {
			Temp e1t = ((TEMP) e1).temp;
			Temp e2t = munchExp(e2);
			String str = String.format("%s `d0, `s0, `s1", instr);
			Emit(new OPER(str, L(t), L(e1t, e2t)));
		} else {
			throw new Error("CG Tile:\n" + e1 + "\n" + e2);
		}
		return t;
	}

	/*
	 * munchMEM
	 */
	private Temp munchMEM(Exp e) {
		// ldr
		Temp t = new Temp();
		if (e instanceof BINOP
				&& ((BINOP) e).e2 instanceof CONST
				&& ((CONST) ((BINOP) e).e2).value < 4096
				&& (((BINOP) e).op == Operator.Plus || ((BINOP) e).op == Operator.Minus)) {
			Temp e1t = munchExp(((BINOP) e).e1);
			int c = ((CONST) ((BINOP) e).e2).value;
			String str;
			if (((BINOP) e).op == Operator.Plus)
				str = String.format("ldr `d0, [ `s0 , #%d ]", c);
			else
				str = String.format("ldr `d0, [ `s0 , #%d ]", -c);
			Emit(new OPER(str, L(t), L(e1t)));
		} else if (e instanceof TEMP) {
			Temp et = ((TEMP) e).temp;
			Emit(new OPER("ldr `d0, [ `s0 ]", L(t), L(et)));
		} else if (e instanceof NAME) {
			Label l = ((NAME) e).label;
			Emit(new OPER("ldr `d0, " + l.label, L(t), null));
		} else {
			Temp et = munchExp(e);
			Emit(new OPER("ldr `d0, [ `s0 ]", L(t), L(et)));
		}
		return t;
	}

	/*
	 * munchNAME
	 */
	private Temp munchNAME(Label l) {
		Temp t = new Temp();
		// TODO Pseudo instruction, need to check if it works
		Emit(new OPER("ldr `d0, " + l.label, L(t), null));
		return t;
	}

	/*
	 * helpers
	 */
	private TempList munchArgs(ExpList list) {
		TempList ret = null;
		TempList t = ret;
		int n = 0;
//		LinkedList<Temp> exps = new LinkedList<>();
		while (list != null) {
			Temp et = munchExp(list.head);
//			exps.addLast(et);
			list = list.tail;
			Access argN = frame.ArgAccess(n++);
			Exp a = argN.unEx(new TEMP(Hardware.SP));
			munchMOVE(a, new TEMP(et)); // Move expression into arg slot
			if (a instanceof TEMP) {
				Temp at = ((TEMP)a).temp;
				if (t == null)
					ret = t = new TempList(at, null);
				else
					t = t.tail = new TempList(at, null);
			}
		}
//		n = exps.size();
//		for (Temp et : exps) {
//			Access argN = frame.ArgAccess(--n);
//			Exp a = argN.unEx(new TEMP(Hardware.SP));
//			munchMOVE(a, new TEMP(et)); // Move expression into arg slot
//			if (a instanceof TEMP) {
//				Temp at = ((TEMP)a).temp;
//				if (t == null)
//					ret = t = new TempList(at, null);
//				else
//					t = t.tail = new TempList(at, null);
//			}
//		}
		return ret;
	}
}
