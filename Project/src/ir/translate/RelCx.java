package ir.translate;

import ir.tree.BINOP;
import ir.tree.CJUMP;
import ir.tree.CONST;
import ir.tree.Exp;
import ir.tree.JUMP;
import ir.tree.LABEL;
import ir.tree.MOVE;
import ir.tree.SEQ;
import ir.tree.Stm;
import ir.tree.TEMP;
import actrec.Label;
import actrec.Temp;
import ast.expression.BinaryExpression.Operator;

public class RelCx extends Cx {
	public Operator op;
	public Tr t1, t2;
	public RelCx(Operator op, Tr t1, Tr t2) {
		this.op = op;
		this.t1 = t1;
		this.t2 = t2;
	}
	
	@Override
	public Stm unCx(Label t, Label f) {
		if (!t1.HasHighValue() && !t2.HasHighValue())
			return new CJUMP(op, t1.unEx(), t2.unEx(), t, f);
		// DONE? UGH this Will be Nasty For LONG
		
		Exp e1lo = t1.unExLo();
		Exp e1hi = t1.unExHi();;
		if (!t1.HasHighValue()) { // Sign Extend
			e1hi = new BINOP(Operator.ASR, e1lo, new CONST(31));
		}
		
		Exp e2lo = t2.unExLo();
		Exp e2hi = t2.unExHi();
		if (!t2.HasHighValue()) { // Sign Extend
			e2hi = new BINOP(Operator.ASR, e2lo, new CONST(31));
		} 
		Temp e1loT = new Temp();
		Temp e1hiT = new Temp();
		Temp e2loT = new Temp();
		Temp e2hiT = new Temp();
		Stm setup = new SEQ(new MOVE(new TEMP(e1loT), e1lo),
					new SEQ(new MOVE(new TEMP(e1hiT), e1hi),
					new SEQ(new MOVE(new TEMP(e2loT), e2lo),
							new MOVE(new TEMP(e2hiT), e2hi))));
		Exp cmp;
		Temp b1 = new Temp();
		Temp b2 = new Temp();
		Label chkLoT = new Label();
		Label chkLoF = new Label();
		Label chkLoE = new Label();
		Label chkHi1T = new Label();
		Label chkHi1F = new Label();
		Label chkHi1E = new Label();
		Label chkHi2T = new Label();
		Label chkHi2E = new Label();
		switch (op) {
		case Eq:
			cmp = new BINOP(Operator.Or, 
					new BINOP(Operator.XOR, new TEMP(e1loT), new TEMP(e2loT)), 
					new BINOP(Operator.XOR, new TEMP(e1hiT), new TEMP(e2hiT)));
			return new SEQ(setup, new CJUMP(Operator.Eq, cmp, new CONST(0), t, f));
		case NotEq:
			cmp = new BINOP(Operator.Or, 
					new BINOP(Operator.XOR, new TEMP(e1loT), new TEMP(e2loT)), 
					new BINOP(Operator.XOR, new TEMP(e1hiT), new TEMP(e2hiT)));
			return new SEQ(setup, new CJUMP(Operator.NotEq, cmp, new CONST(0), t, f));
		case Greater:
		case GreaterEq:
		case Less:
		case LessEq:
			/*
			 * 	if e1lo op e2lo (unsinged)
			 *		t1 = true;
			 *	else
			 *		t1 = false;
			 *	if e1hi op e2hi (signed)
			 *		t2 = true;
			 *	else
			 *		t2 = false;
			 *	if e2hi == e1hi
			 *		t2 = t1;
			 *	if t2
			 *		return true
			 *	else
			 *		return false
			 */
			Stm chkLo = new CJUMP(Operator.Unsigned(op), new TEMP(e1loT), new TEMP(e2loT), chkLoT, chkLoF);
			Stm chkHi1 = new CJUMP(op, new TEMP(e1hiT), new TEMP(e2hiT), chkHi1T, chkHi1F);
			Stm chkHi2 = new CJUMP(Operator.Eq, new TEMP(e1hiT), new TEMP(e2hiT), chkHi2T, chkHi2E);
			TEMP tmp1 = new TEMP(b1);
			TEMP tmp2 = new TEMP(b2);
			Stm finChk = new CJUMP(Operator.NotEq, tmp2, new CONST(0), t, f);
			return new SEQ(setup,
				   new SEQ(chkLo,
				   new SEQ(new LABEL(chkLoT),
			       new SEQ(new MOVE(tmp1, new CONST(1)),
			       new SEQ(new JUMP(chkLoE),
				   new SEQ(new LABEL(chkLoF), 
				   new SEQ(new MOVE(tmp1, new CONST(0)),
				   new SEQ(new LABEL(chkLoE), 
				   new SEQ(chkHi1,
				   new SEQ(new LABEL(chkHi1T),
			       new SEQ(new MOVE(tmp2, new CONST(1)),
			       new SEQ(new JUMP(chkHi1E),
				   new SEQ(new LABEL(chkHi1F), 
				   new SEQ(new MOVE(tmp2, new CONST(0)),
				   new SEQ(new LABEL(chkHi1E),  
				   new SEQ(chkHi2,
				   new SEQ(new LABEL(chkHi2T), 
				   new SEQ(new MOVE(tmp2, tmp1),
				   new SEQ(new LABEL(chkHi2E),
				           (finChk))))))))))))))))))));
		default:
			break;
		}
		throw new Error();
	}
}
