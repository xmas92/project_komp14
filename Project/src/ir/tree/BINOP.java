package ir.tree;

import ast.expression.BinaryExpression.Operator;

public class BINOP extends Exp {
	public Operator op;
	public Exp e1, e2;
	public BINOP(Operator op, Exp e1, Exp e2) {
		this.op = op;
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
		s += "(BINOP " + op.toString() + "\n";
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
	public Exp build(ExpList el) {
		return new BINOP(op, el.head, el.tail.head);
	}
}
