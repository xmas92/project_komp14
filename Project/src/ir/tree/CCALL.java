package ir.tree;

import ast.expression.BinaryExpression.Operator;

public class CCALL extends Stm {
	public CALL c;
	public Operator op;
	public Exp e1, e2;
	public CCALL(Operator op, Exp e1, Exp e2, CALL c) {
		this.c = c;
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
		s += "(CCALL" + op.toString() + "\n";
		s += e1.toString(i+1);
		s += "\n";
		s += e2.toString(i+1);
		s += "\n";
		s += c.toString(i+1);
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
		return new CCALL(op, el.head, el.tail.head, c);
	}

}
