package canon;

import ir.tree.CJUMP;
import ir.tree.JUMP;
import ir.tree.LABEL;
import ir.tree.Stm;
import ir.tree.StmList;

import java.util.Deque;
import java.util.LinkedList;

import actrec.Label;

public class BasicBlocks {
	public Deque<StmList> blocks;
	public Label done;

	private StmList listStart;
	private StmList listEnd;
	
	private void AddStm(Stm s) {
		listEnd.tail = new StmList(s, null);
		listEnd = listEnd.tail;
	}

	private void DoStms(StmList sl) {
		if (sl == null)  // Reached the end? Append a jump to done (epilogue)
			DoStms(new StmList(new JUMP(done),null));
		else if (sl.head instanceof JUMP || sl.head instanceof CJUMP) {
			AddStm(sl.head); // Add the branch to the block
			MakeBlocks(sl.tail); // Make a new block
		} 
		else if (sl.head instanceof LABEL) // Create psudo jump to have blocks w/o Labels and Jumps in them
			DoStms(new StmList(new JUMP(((LABEL)sl.head).l), sl));
		else {
			AddStm(sl.head); // Add statement
			DoStms(sl.tail); // Continue creating block
		}
	}

	void MakeBlocks(StmList sl) {
		if (sl==null) return; // We are done!
		else if (sl.head instanceof LABEL) { // We have a label at the start of the block? GREAT
			listStart = listEnd = new StmList(sl.head,null);
			if (listStart != null)
				blocks.addLast(listStart);
			DoStms(sl.tail);
		} // Create psudo Label at start of the block
		else MakeBlocks(new StmList(new LABEL(new Label()), sl));
	}


	public BasicBlocks(StmList stms) {
		done = new Label();
		blocks = new LinkedList<>();
		MakeBlocks(stms);
	}
}