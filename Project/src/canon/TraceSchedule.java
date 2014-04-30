package canon;

import java.util.HashMap;

import actrec.Label;
import ast.expression.BinaryExpression.Operator;
import ir.tree.CJUMP;
import ir.tree.JUMP;
import ir.tree.LABEL;
import ir.tree.Stm;
import ir.tree.StmList;

public class TraceSchedule {
	public StmList stms;
	public BasicBlocks bb;
	public HashMap<Label, StmList> map = new HashMap<>();
	
	void Trace(StmList sl) {
		while (true) {
			LABEL lab = (LABEL)sl.head;
			map.remove(lab.l); // Remove the block we are tracing
			StmList last = GetLast(sl);
			Stm s = last.tail.head;
			if (s instanceof JUMP) {
				JUMP j = (JUMP)s;
				StmList target = map.get(j.l);
				if (target != null) { // The next block still exists
					last.tail = target; // Append that block to this one
					sl = target;
				} else { // This trace ended, start another one with the free blocks
					last.tail.tail = GetNext();
					return;
				}
			} else if (s instanceof CJUMP) {
				CJUMP j = (CJUMP)s;
				StmList t = map.get(j.t);
				StmList f = map.get(j.f);
				if (f != null) { 		// If the false block is free
					last.tail.tail = f; // Append that block to the conditional jump
					sl = f;
				} else if (t != null) { // If the true block is free Append it and change to the inverse relation
					last.tail.head = new CJUMP(Operator.Reciprocal(j.op), j.e1, j.e2, j.f, j.t);
					last.tail.tail = t;
					sl = t;
				} else { // If neither block is free to be appended
					Label ff = new Label();
					last.tail.head = new CJUMP(j.op, j.e1, j.e2, j.t, ff);
					last.tail.tail = new StmList(new LABEL(ff), // Add jump to f through ff (closer)
									  new StmList(new JUMP(j.f), GetNext())); // Append rest
					return;
				}
			} else throw new Error();
		}
	}

	private StmList GetLast(StmList sl) {
		// No null ref because every block is at least two stmts (label, jump)
		while (sl.tail.tail!=null)  
			sl = sl.tail;
		return sl;
	}

	StmList GetNext() {
		if (bb.blocks.size() == 0) // No blocks left (or at all), just return done label
			return new StmList(new LABEL(bb.done), null);
		else {
			StmList s = bb.blocks.getFirst();
			LABEL lab = (LABEL)s.head;
			if (map.get(lab.l) != null) {
				Trace(s); // Start trace from the first block
				return s;
			} else { // We already traced it so remove it
				bb.blocks.removeFirst();
				return GetNext();
			}
		}
	}
	
	public TraceSchedule(BasicBlocks bb) {
		this.bb = bb;
		for(StmList sl : bb.blocks)
			map.put(((LABEL)sl.head).l, sl);
		stms = GetNext();
	}
}
