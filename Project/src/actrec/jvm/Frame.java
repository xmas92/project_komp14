package actrec.jvm;

import java.util.Deque;
import java.util.HashSet;

import ir.translate.Procedure;
import ir.tree.Exp;
import ir.tree.ExpList;
import ir.tree.NAME;
import ir.tree.Stm;
import actrec.Access;
import actrec.Label;
import actrec.Record;
import actrec.Temp;
import actrec.TempMap;
import assem.Instr;


public class Frame extends actrec.Frame {
	public int localidx = 0;
	
	public Frame(Label label) {
		super(label);
	}

	public Frame() {
		super(new Label("dummy"));
	}

	public class LocalVar extends Access {
		public final int idx;
		public LocalVar(int idx) {
			this.idx = idx;
		}
	}
	
	@Override
	public Access AllocFormal(boolean doubleword) {
		if (doubleword) {
			int t = localidx;
			localidx += 2;
			return new LocalVar(t);
		} 
		return new LocalVar(localidx++);
	}

	@Override
	public Access AllocLocal(boolean doubleword) {
		if (doubleword) {
			int t = localidx;
			localidx += 2;
			return new LocalVar(t);
		} 
		return new LocalVar(localidx++);
	}

	@Override
	public actrec.Frame newFrame(Label label) {
		return new Frame(label);
	}

	@Override
	public Stm ExitWithFailiur() {
		// Should never happen
		throw new Error();
	}

	@Override
	public Exp ExternalCall(String f, ExpList explist) {
		// Should never happen
		throw new Error();
	}

	@Override
	public Exp GetPrintStr(boolean isDoubleWord) {
		// Should never happen
		throw new Error();
	}

	@Override
	public Temp RV(int i) {
		// Should never happen
		throw new Error();
	}

	@Override
	public Temp FP() {
		// Should never happen
		throw new Error();
	}

	@Override
	public Record CreateRecord(String id, String extendsID) {
		return new dummyRecord();
	}
	private class dummyRecord extends Record {
		@Override
		public Access AllocField(boolean doubleword) {
			return null; // JUst a DUmmy
		}

		@Override
		public int Size() {
			return 0; // JUst a DUmmy
		}
		private class dummy extends Access {
			
		}
		@Override
		public Access AllocVirtual() {
			// Should never happen
			return new dummy();
		}

		@Override
		public int VtableSize() {
			// Should never happen
			throw new Error();
		}

		@Override
		public Label GetVTable() {
			// Should never happen
			throw new Error();
		}

		@Override
		public boolean HasVTable() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public int FieldSize() {
			// TODO Auto-generated method stub
			return 0;
		}
		
	}

	@Override
	public Access ArgAccess(int i) {
		// Should never happen
		throw new Error();
	}

	@Override
	public Deque<Instr> AddPrologueEpilogue(Procedure li) {
		// Should never happen
		throw new Error();
	}

	@Override
	public HashSet<Temp> registers() {
		// Should never happen
		throw new Error();
	}

	@Override
	public TempMap tempMap() {
		// Should never happen
		throw new Error();
	}

	@Override
	public int SpillOffset() {
		// Should never happen
		throw new Error();
	}

	@Override
	public void AddReturnSink(Procedure proc) {
		// Should never happen
		throw new Error();
	}

	@Override
	public Stm ArrayBoundCheck(Exp arr, Exp idx) {
		// Should never happen
		throw new Error();
	}

	@Override
	public Exp GetPrintStrBool(boolean value) {
		// Should never happen
		throw new Error();
	}

	@Override
	public Stm GetPrintStrBool(Exp value) {
		// Should never happen
		throw new Error();
	}

	@Override
	public NAME GetDataLabel(Label getVTable) {
		// Should never happen
		throw new Error();
	}

	@Override
	public Deque<Instr> AddDataAccess(Procedure li) {
		// Should never happen
		throw new Error();
	}

	@Override
	public void RewritePrologueEpilogue(Procedure proc) {
		// Should never happen
		throw new Error();
	}

	@Override
	public Temp[] PreferredRegisters() {
		// Should never happen
		throw new Error();
	}

}
