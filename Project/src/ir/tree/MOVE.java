package ir.tree;

public class MOVE extends Stm {
	public boolean mem;
	public Exp e;
	public Exp a;
	public MOVE(MEM a, Exp e) {
		this.mem = true;
		this.a = a;
		this.e = e;
		if(e == null)
			return;
	}
	public MOVE(TEMP a, Exp e) {
		this.mem = false;
		this.a = a;
		this.e = e;
		if(e == null)
			return;
	}
	public MOVE(Exp a, Exp e) {
		assert(a instanceof MEM || a instanceof TEMP);
		this.mem = a instanceof MEM;
		this.a = a;
		this.e = e;
		if(e == null)
			return;
	}
	@Override
	public String toString() {
		return toString(0);
	}
	@Override
	public String toString(int i) {
		String s = PrintIndent(i);
		s += "(MOVE\n";
		s += a.toString(i+1);
		s += "\n";
		s += e.toString(i+1);
		s += "\n";
		s += PrintIndent(i);
		s += ")";
		return s;
	}
	@Override
	public ExpList kids() {
		if (mem)
			return new ExpList(((MEM)a).e, new ExpList(e, null));
		return new ExpList(e, null);
	}
	@Override
	public Stm build(ExpList el) {
		if (mem)
			return new MOVE(new MEM(el.head), el.tail.head);
		return new MOVE(a, el.head);
	}
}
