package ir.tree;

public class UMULL extends Stm {
	public TEMP rlo;
	public TEMP rhi;
	public Exp e1, e2;
	public UMULL(TEMP rlo, TEMP rhi, Exp e1, Exp e2) {
		this.rlo = rlo;
		this.rhi = rhi;
		this.e1 = e1;
		this.e2 = e2;
	}
	@Override
	public String toString() {
		return toString(0);
	}
	@Override
	public String toString(int i) {
		String s = PrintIndent(i);
		s += "(UMULL\n";
		s += rlo.toString(i+1);
		s += "\n";
		s += rhi.toString(i+1);
		s += "\n";
		s += e1.toString(i+1);
		s += "\n";
		s += e2.toString(i+1);
		s += "\n";
		s += PrintIndent(i);
		s += ")";
		return s;
	}
	@Override
	public ExpList kids() {
		return new ExpList(e1, new ExpList(e2, null));
	}
	@Override
	public Stm build(ExpList el) {
		return new UMULL(rlo, rhi, el.head, el.tail.head);
	}
}
