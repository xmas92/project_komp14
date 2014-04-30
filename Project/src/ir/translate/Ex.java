package ir.translate;

import ir.tree.CJUMP;
import ir.tree.CONST;
import ir.tree.Exp;
import ir.tree.LABEL;
import ir.tree.SEQ;
import ir.tree.Stm;
import ir.tree.EXPS;
import actrec.Label;
import ast.expression.BinaryExpression.Operator;

public class Ex extends Tr {
	public Exp e;
	public Exp ehi;
	public Ex(Exp e) {
		this.e = e;
	}
	public Ex(Exp e, Exp ehi) {
		this.e = e;
		this.ehi = ehi;
	}

	@Override
	public Exp unEx() {
		return e;
	}

	@Override
	public Stm unNx() {
		return new EXPS(e);
	}

	@Override
	public Stm unCx(Label t, Label f) {
		if (!HasHighValue()) 
			return new CJUMP(Operator.Eq, e, new CONST(0), f, t);
		Label m = new Label();
		return new SEQ(new CJUMP(Operator.NotEq, e, new CONST(0), t, m),
				 new SEQ(new LABEL(m), new CJUMP(Operator.Eq, ehi, new CONST(0), f, t)));
	}
	@Override
	public Exp unExLo() {
		return unEx();
	}
	@Override
	public Exp unExHi() {
		return ehi;
	}

}
