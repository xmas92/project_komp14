package ir.tree;

import actrec.Label;
import ast.expression.BinaryExpression.Operator;

public class CJUMP extends Stm {
	public Operator op;
	public Exp e1, e2;
	public Label t, f;
	public CJUMP(Operator op, Exp e1, Exp e2, Label t, Label f) {
		this.op = op;
		this.e1 = e1;
		this.e2 = e2;
		this.t = t;
		this.f = f;
	}
	@Override
	public String toString() {
		return toString(0);
	}
	@Override
	public String toString(int i) {
		String s = PrintIndent(i);
		s += "(CJUMP " + op.toString() + "\n";
		s += e1.toString(i+1);
		s += "\n";
		s += e2.toString(i+1);
		s += "\n";
		s += PrintIndent(i+1);
		s += t.label;
		s += "\n";
		s += PrintIndent(i+1);
		s += f.label;
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
		return new CJUMP(op, el.head, el.tail.head, t, f);
	}
}
