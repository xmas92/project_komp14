package ir.translate;

import ir.tree.CONST;
import ir.tree.ESEQ;
import ir.tree.Exp;
import ir.tree.JUMP;
import ir.tree.LABEL;
import ir.tree.MOVE;
import ir.tree.SEQ;
import ir.tree.Stm;
import ir.tree.TEMP;
import actrec.Label;
import actrec.Temp;

public abstract class Cx extends Tr {

	@Override
	public Exp unEx() {
		Label t = new Label();
		Label f = new Label();
		Label e = new Label();
		Temp tmp = new Temp();

		MOVE moveconsttrue = new MOVE(new TEMP(tmp), new CONST(1));
		MOVE moveconstfalse = new MOVE(new TEMP(tmp), new CONST(0));
		Stm sideeffect= new SEQ(this.unCx(t, f), 
						new SEQ(new LABEL(f),
						new SEQ(moveconstfalse,
						new SEQ(new JUMP(e),
						new SEQ(new LABEL(t),
						new SEQ(moveconsttrue, 
								new LABEL(e)))))));
		return new ESEQ(sideeffect, new TEMP(tmp));
	}

	@Override
	public Stm unNx() {
		// Should never happen
		throw new Error();
	}

	@Override
	public Exp unExLo() {
		return unEx();
	}

	@Override
	public Exp unExHi() {
		// Should never happen
		// TODO happens some times (ex. binop) but not used, fix for cleaner code
		return null;
	}

	@Override
	public abstract Stm unCx(Label t, Label f);

}
