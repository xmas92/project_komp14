package ir.translate;

import java.util.LinkedList;

import regalloc.RegAlloc;
import ir.tree.Stm;
import ir.tree.StmList;
import actrec.DefaultMap;
import actrec.Frame;
import actrec.Label;
import assem.Instr;

public class Procedure {
	public Frame frame;
	public Stm body;
	public StmList canon;
	public LinkedList<Instr> instrs;
	public Label done;
	public RegAlloc regalloc;
	public Procedure(Frame frame, Stm body) {
		this.frame = frame;
		this.body = body;
	}
	@Override
	public String toString() {
		if (canon == null && instrs == null)
			return "Procedure: " + frame.frameLabel.label + "\n" + body.toString();
		if (instrs == null) {
			String s = "Procedure: " + frame.frameLabel.label + "\n";
			for (Stm st : canon) 
				s += st.toString() + "\n";
			return s;
		}
		String s = "";
		for (Instr i : instrs) 
			if (regalloc == null)
				s += i.format(new DefaultMap());
			else 
				s += i.format(regalloc);
		return s;
	}
}
