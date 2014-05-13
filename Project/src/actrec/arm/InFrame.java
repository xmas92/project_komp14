package actrec.arm;

import ir.tree.BINOP;
import ir.tree.CONST;
import ir.tree.Exp;
import ir.tree.MEM;
import actrec.Access;
import ast.expression.BinaryExpression.Operator;

public class InFrame extends Access {
	public int offset;
	public InFrame(int offset) {
		this.offset = offset;
	}
	@Override
	public Exp unEx(Exp sp) {
		if (offset == 0) {
			return new MEM(sp);
		}
		return new MEM(new BINOP(Operator.Plus, sp, new CONST(offset)));
	}
}
