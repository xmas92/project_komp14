package actrec.arm;

import ir.translate.Procedure;
import ir.tree.CALL;
import ir.tree.CJUMP;
import ir.tree.CONST;
import ir.tree.EXPS;
import ir.tree.Exp;
import ir.tree.ExpList;
import ir.tree.JUMP;
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
			if (formalwords < 4) {
				a1 = new InReg(new Temp());
				formalwords++;
			} else 
				a1 = new InFrame(8 + (++formalwords - 4) * 4);
			if (formalwords < 4) {
				a2 = new InReg(new Temp());
				formalwords++;
			} else 
				a2 = new InFrame(8 + (++formalwords - 4) * 4);
			ret = new DoubleWordAccess(a1, a2);
		} else {
			if (formalwords < 4) {
				ret = new InReg(new Temp());
				formalwords++;
			} else 
				ret = new InFrame(8 + (++formalwords - 4) * 4);
		}
		formals.add(ret);
		return ret;
	}

	@Override
	public Access AllocLocal(boolean doubleword) {
		if (doubleword)
			return new DoubleWordAccess(new InReg(new Temp()), new InReg(new Temp()));
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
		return GetDataLabel(isDoubleWord?dwordStr:wordStr);
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
		maxparam = (i>maxparam)?i:maxparam;
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
			return new InFrame((i-4)*4);
		}
	}
	
	@Override
	public Deque<Instr> AddPrologueEpilogue(Procedure proc) {
		LinkedList<Instr> prologue = new LinkedList<>();
		LinkedList<Instr> epilogue = new LinkedList<>();
		if (frameLabel.label.equals("main"))
			prologue.add(new LABEL("main:", frameLabel));
		else
			prologue.add(new LABEL(frameLabel.label + ":", frameLabel));
		prologue.add(new OPER("push { r7, lr }", null, Hardware.spfp));
		prologue.add(new MOVE("mov `d0, `s0", Hardware.FP, Hardware.SP));
		prologue.add(new OPER("push { r4-r6, r8-r11 }", null, Hardware.calleeSavedList));
		prologue.add(new OPER("sub `d0, `s0, #`k0", new TempList(Hardware.SP, null), new TempList(Hardware.SP, null)));
		int i = 0;
		for (Access a : formals) {
			if (i >= 4) break;
			if (a instanceof DoubleWordAccess) {
				DoubleWordAccess da = (DoubleWordAccess)a;
				prologue.add(new MOVE("mov `d0, `s0", ((InReg)da.a1).t, Hardware.argRegs[i++]));
				if (i >= 4) break;
				prologue.add(new MOVE("mov `d0, `s0", ((InReg)da.a2).t, Hardware.argRegs[i++]));
			} else
				prologue.add(new MOVE("mov `d0, `s0", ((InReg)a).t, Hardware.argRegs[i++]));
		}
		if (frameLabel.label.equals("main"))
			epilogue.add(new OPER("ldr `d0, =0", new TempList(Hardware.r0, null), null));
		epilogue.add(new LABEL(proc.done.label + ":", proc.done));
		epilogue.add(new OPER("add `d0, `s0, #`k0", new TempList(Hardware.SP, null), new TempList(Hardware.SP, null)));
		epilogue.add(new OPER("pop { r4-r6, r8-r11 }", Hardware.calleeSavedList, null));
		epilogue.add(new MOVE("mov `d0, `s0", Hardware.SP, Hardware.FP));
		epilogue.add(new OPER("pop { r7, lr }", Hardware.spfp, null));
		epilogue.add(new OPER("bx lr", null, new TempList(Hardware.LR, null)));
		for (Iterator<Instr> it = prologue.descendingIterator(); it.hasNext(); proc.instrs.addFirst(it.next()));
		for (Iterator<Instr> it = epilogue.iterator(); it.hasNext(); proc.instrs.addLast(it.next()));
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
	public String constMap(int n) {
		// Params
		if (maxparam >= 4)
			return Integer.toString((maxparam-3)*4);
		return "0";
	}

	@Override
	public int SpillOffset() {
		// Size of 7 registers that get pushed to stack after the FP is saved
		return 28;
	}

	@Override
	public void AddReturnSink(Procedure proc) {
		proc.instrs.add(new OPER("", null, Hardware.returnSink));
	}

	@Override
	public Stm ArrayBoundCheck(Exp arr, Exp idx) {
		abc = true;
		Temp t = new Temp();
		Temp i = new Temp();
		return new SEQ(new ir.tree.MOVE(new TEMP(t), arr),
				new SEQ(new ir.tree.MOVE(new TEMP(i), idx),
				 new EXPS(new CALL(new NAME(new Label("_abc")), 
								new ExpList(new TEMP(t),
								 new ExpList(new TEMP(i), null))))));
	}

	@Override
	public Exp GetPrintStrBool(boolean value) {
		return GetDataLabel(value?trueStr:falseStr);
	}

	@Override
	public Stm GetPrintStrBool(Exp value) {
		Label t = new Label();
		Label f = new Label();
		Label e = new Label();
		Stm cond = new CJUMP(Operator.NotEq, value, new CONST(0), t, f);
		return new SEQ(cond,
				new SEQ(new ir.tree.LABEL(t), 
				new SEQ(new EXPS(ExternalCall("printf", new ExpList(GetPrintStrBool(true), null))),
				new SEQ(new JUMP(e),
				new SEQ(new ir.tree.LABEL(f), 
				new SEQ(new EXPS(ExternalCall("printf", new ExpList(GetPrintStrBool(false), null))),
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

}
