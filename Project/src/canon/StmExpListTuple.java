package canon;

import ir.tree.ExpList;
import ir.tree.Stm;

class StmExpListTuple {
	public Stm s;
	public ExpList explist;
	public StmExpListTuple(Stm s, ExpList explist) {
		this.s = s;
		this.explist = explist;
	}
}