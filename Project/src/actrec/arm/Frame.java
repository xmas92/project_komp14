package actrec.arm;

import ir.translate.Procedure;
import ir.tree.CALL;
import ir.tree.CCALL;
import ir.tree.CJUMP;
import ir.tree.CONST;
import ir.tree.EXPS;
import ir.tree.Exp;
import ir.tree.ExpList;
import ir.tree.JUMP;
import ir.tree.MEM;
import ir.tree.NAME;
import ir.tree.SEQ;
import ir.tree.Stm;
import ir.tree.TEMP;

import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import regalloc.RegAlloc;
import actrec.Access;
import actrec.Label;
import actrec.Temp;
import actrec.TempList;
import actrec.TempMap;
import assem.Instr;
import assem.LABEL;
import assem.MOVE;
import assem.OPER;
import assem.VALUE;
import ast.expression.BinaryExpression.Operator;

public class Frame extends actrec.Frame implements TempMap {
	static Label wordStr = new Label();
	static Label dwordStr = new Label();
	static Label trueStr = new Label();
	static Label falseStr = new Label();
	int offset = 0;
	List<Access> formals = new LinkedList<>();
	int formalwords;
	int maxparam;
	HashMap<Label, Label> labelmap = new HashMap<>();
	public boolean abc;

	public Frame(Label label) {
		super(label);

	}

	public Frame() {
		super(new Label("dummy"));
	}

	@Override
	public actrec.Frame newFrame(Label label) {
		return new Frame(label);
	}

	@Override
	public Access AllocFormal(boolean doubleword) {
		Access ret;
		if (doubleword) {
			Access a1, a2;
			if (formalwords % 2 == 1)
				formalwords++; // 8 byte align formals
			if (formalwords < 4) {
				a1 = new InReg(new Temp());
				formalwords++;
			} else
				a1 = new InFrameC((formalwords++ - 4) * 4);
			if (formalwords < 4) {
				a2 = new InReg(new Temp());
				formalwords++;
			} else
				a2 = new InFrameC((formalwords++ - 4) * 4);
			ret = new DoubleWordAccess(a1, a2);
		} else {
			if (formalwords < 4) {
				ret = new InReg(new Temp());
				formalwords++;
			} else
				ret = new InFrameC((formalwords++ - 4) * 4);
		}
		formals.add(ret);
		return ret;
	}

	@Override
	public Access AllocLocal(boolean doubleword) {
		if (doubleword)
			return new DoubleWordAccess(new InReg(new Temp()), new InReg(
					new Temp()));
		return new InReg(new Temp());
	}

	@Override
	public Exp ExternalCall(String f, ExpList explist) {
		return new CALL(new NAME(new Label(f)), explist);
	}

	@Override
	public Stm ExitWithFailiur() {
		Exp exit = ExternalCall("exit", new ExpList(new CONST(1), null));
		return new EXPS(exit);
	}

	@Override
	public Exp GetPrintStr(boolean isDoubleWord) {
		return GetDataLabel(isDoubleWord ? dwordStr : wordStr);
	}

	@Override
	public Temp RV(int i) {
		return Hardware.retRegs[i];
	}

	@Override
	public Temp FP() {
		return Hardware.FP;
	}

	@Override
	public actrec.Record CreateRecord(String id, String extendsID) {
		return new Record(id, extendsID);
	}

	@Override
	public Access ArgAccess(int i) {
		maxparam = (i > maxparam) ? i : maxparam;
		switch (i) {
		case 0:
			return new InReg(Hardware.r0);
		case 1:
			return new InReg(Hardware.r1);
		case 2:
			return new InReg(Hardware.r2);
		case 3:
			return new InReg(Hardware.r3);
		default:
			return new InFrame((i - 4) * 4);
		}
	}

	private int popregcount = 0;
	
	@Override
	public void RewritePrologueEpilogue(Procedure proc) {
		LinkedList<Instr> prologue = new LinkedList<>();
		LinkedList<Instr> epilogue = new LinkedList<>();
		HashSet<Temp> defs = new HashSet<>();
		for (Instr i : proc.instrs) {
			if (i.defines() != null)
				for (Temp t : i.defines())
					defs.add(proc.regalloc.getTemp(t));
		}
		String sPush = "{ ";
		String sPop = null;
		for (Temp t : Hardware.popRegs) 
			if (defs.contains(t)) {
				sPush += Hardware.tempMap.get(t) + ", ";
				popregcount++;
			}
		if (leafFrame && !defs.contains(Hardware.LR)) {
			epilogue.add(new MOVE("mov pc, lr", null, null));
			sPush = sPush.substring(0, sPush.length()-2);
			sPush += " }";
			sPop = sPush;
		} else {
			popregcount++;
			sPop = sPush + "pc }";
			sPush += "lr }";
		}
		if (sPush.length() > 3) {
			prologue.addFirst(new OPER("push " + sPush, null, null));
			epilogue.addFirst(new OPER("pop " + sPop, null, null));
		}
		int i = proc.regalloc.SpillSize() + ParamSize() + AlignFrameSize();
		if (i != 0) {
			prologue.add(new OPER("sub `d0, `s0, #" + i, new TempList(Hardware.SP,
					null), new TempList(Hardware.SP, null)));
			epilogue.addFirst(new OPER("add `d0, `s0, #" + i, new TempList(Hardware.SP,
					null), new TempList(Hardware.SP, null)));
		}
		if (frameLabel.label.equals("main"))
			prologue.addFirst(new LABEL("main:", frameLabel));
		else
			prologue.addFirst(new LABEL(frameLabel.label + ":", frameLabel));
		

		for (Iterator<Instr> it = prologue.descendingIterator(); it.hasNext(); proc.instrs
				.addFirst(it.next()))
			;
		for (Iterator<Instr> it = epilogue.iterator(); it.hasNext(); proc.instrs
				.addLast(it.next()))
			;
	}
	
	@Override
	public Deque<Instr> AddPrologueEpilogue(Procedure proc) {
		LinkedList<Instr> prologue = new LinkedList<>();
		LinkedList<Instr> epilogue = new LinkedList<>();
		prologue.add(new OPER("", new TempList(Hardware.SP, null), null));
		int i = 0;
		for (Access a : formals) {
			if (a instanceof DoubleWordAccess) {
				DoubleWordAccess da = (DoubleWordAccess) a;
				if (da.a1 instanceof InReg) {
					if (i%2 == 1) i++; // Align 8 byte formal
					prologue.add(new MOVE("mov `d0, `s0", ((InReg) da.a1).t,
							Hardware.argRegs[i++]));
				} else if (da.a1 instanceof InFrameC) {
					prologue.add(((InFrameC) da.a1).init(Hardware.SP));
				}
				if (da.a2 instanceof InReg)
					prologue.add(new MOVE("mov `d0, `s0", ((InReg) da.a2).t,
							Hardware.argRegs[i++]));
				else if (da.a2 instanceof InFrameC) 
								prologue.add(((InFrameC) da.a2).init(Hardware.SP));
			} else if (a instanceof InReg)
				prologue.add(new MOVE("mov `d0, `s0", ((InReg) a).t,
						Hardware.argRegs[i++]));
			else if (a instanceof InFrameC) 
				prologue.add(((InFrameC) a).init(Hardware.SP));
		}
		epilogue.add(new LABEL(proc.done.label + ":", proc.done));
		if (frameLabel.label.equals("main"))
			epilogue.add(new OPER("ldr `d0, =0",
					new TempList(Hardware.r0, null), null));
		epilogue.add(new OPER("",null, Hardware.returnSink));
		for (Iterator<Instr> it = prologue.descendingIterator(); it.hasNext(); proc.instrs
				.addFirst(it.next()))
			;
		for (Iterator<Instr> it = epilogue.iterator(); it.hasNext(); proc.instrs
				.addLast(it.next()))
			;
		return proc.instrs;
	}

	@Override
	public Deque<Instr> AddDataAccess(Procedure proc) {
		LinkedList<Instr> data = new LinkedList<>();
		for (Entry<Label, Label> e : labelmap.entrySet()) {
			data.add(new LABEL(e.getValue().label + ":", e.getValue()));
			data.add(new VALUE(".word " + e.getKey().label, e.getKey().label));
		}
		proc.instrs.addAll(data);
		return proc.instrs;
	}

	@Override
	public HashSet<Temp> registers() {
		return Hardware.regiseters;
	}

	@Override
	public TempMap tempMap() {
		return this;
	}

	@Override
	public String tempMap(Temp t) {
		return Hardware.tempMap.get(t);
	}

	@Override
	public String constMap(int n, char c) {
		// Params
		if (maxparam >= 4)
			return Integer.toString((maxparam - 3) * 4);
		return "0";
	}

	@Override
	public int SpillOffset() {
		// Size of 7 registers that get pushed to stack after the FP is saved
		return popregcount*4;
	}

	@Override
	public void AddReturnSink(Procedure proc) {
		proc.instrs.add(new OPER("", null, Hardware.returnSink));
	}

	@Override
	public Stm ArrayBoundCheck(Exp arr, Exp idx) {
		abc = true;
		Temp a = new Temp();
		Temp i = new Temp();
		CALL exit = (CALL)ExternalCall("exit", new ExpList(new CONST(1), null));
		Temp s = new Temp();
		Stm test = new SEQ(new ir.tree.MOVE(new TEMP(s), new MEM(new TEMP(a))),
				   new SEQ(new CCALL(Operator.Less, new TEMP(i), new CONST(0), exit),
						   new CCALL(Operator.GreaterEq, new TEMP(i), new TEMP(s), exit)));
		return new SEQ(new ir.tree.MOVE(new TEMP(a), arr), new SEQ(
				new ir.tree.MOVE(new TEMP(i), idx), test));
	}

	@Override
	public Exp GetPrintStrBool(boolean value) {
		return GetDataLabel(value ? trueStr : falseStr);
	}

	@Override
	public Stm GetPrintStrBool(Exp value) {
		Label t = new Label();
		Label f = new Label();
		Label e = new Label();
		Stm cond = new CJUMP(Operator.NotEq, value, new CONST(0), t, f);
		return new SEQ(cond, new SEQ(new ir.tree.LABEL(t), new SEQ(
				new EXPS(ExternalCall("printf", new ExpList(
						GetPrintStrBool(true), null))), new SEQ(new JUMP(e),
						new SEQ(new ir.tree.LABEL(f), new SEQ(new EXPS(
								ExternalCall("printf", new ExpList(
										GetPrintStrBool(false), null))),
								new ir.tree.LABEL(e)))))));

	}

	@Override
	public NAME GetDataLabel(Label getVTable) {
		Label l = labelmap.get(getVTable);
		if (l == null) {
			l = new Label();
			labelmap.put(getVTable, l);
		}
		return new NAME(l);
	}

	@Override
	public Temp[] PreferredRegisters() {
		return Hardware.callerSaved;
	}

	@Override
	public Temp SP() {
		return Hardware.SP;
	}

	@Override
	public int ParamSize() {
		return ((maxparam<4)?0:((maxparam - 3) * 4));
	}
	
	private int AlignFrameSize() {
		return (popregcount%2==1?4:0);
	}
	
	private int PushRegSize() {
		return popregcount*4;
	}

	@Override
	public int FrameSize(RegAlloc regalloc) {
		return PushRegSize() + AlignFrameSize() + regalloc.SpillSize() + ParamSize();
	}


}
