package actrec;

import java.util.Deque;
import java.util.HashSet;

import assem.Instr;
import ir.translate.Procedure;
import ir.tree.Exp;
import ir.tree.ExpList;
import ir.tree.NAME;
import ir.tree.Stm;

public abstract class Frame {
	public Label frameLabel;
	public Access thisPtr;
	public Frame(Label label) {
		frameLabel = label;
	}
	public abstract Frame newFrame(Label label);
	public abstract Access AllocFormal(boolean doubleword);
	public abstract Access AllocLocal(boolean doubleword);
	public abstract Stm ExitWithFailiur();
	public abstract Exp ExternalCall(String f, ExpList explist) ;
	public abstract Exp GetPrintStr(boolean isDoubleWord);
	public abstract Exp GetPrintStrBool(boolean value);
	public abstract Stm GetPrintStrBool(Exp value);
	public abstract Temp RV(int i);
	public abstract Temp FP();
	public abstract Record CreateRecord(String id, String extendsID);
	public abstract Access ArgAccess(int i);
	public abstract Deque<Instr> AddPrologueEpilogue(Procedure li);
	public abstract Deque<Instr> AddDataAccess(Procedure li);
	public abstract HashSet<Temp> registers();
	public abstract TempMap tempMap();
	public abstract int SpillOffset();
	public abstract void AddReturnSink(Procedure proc);
	public abstract Stm ArrayBoundCheck(Exp arr, Exp idx);
	public abstract NAME GetDataLabel(Label getVTable) ;
}
