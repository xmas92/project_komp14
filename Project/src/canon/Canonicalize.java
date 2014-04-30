package canon;

import actrec.Temp;
import ir.translate.Procedure;
import ir.tree.CALL;
import ir.tree.CONST;
import ir.tree.ESEQ;
import ir.tree.EXPS;
import ir.tree.EXPSCALL;
import ir.tree.Exp;
import ir.tree.ExpList;
import ir.tree.MOVE;
import ir.tree.MOVECALL;
import ir.tree.NAME;
import ir.tree.SEQ;
import ir.tree.Stm;
import ir.tree.StmList;
import ir.tree.TEMP;

public class Canonicalize {
	
	public static void CanonicalizeProc(Procedure p) {
		StmList stmlist = Linearize(p.body);
		BasicBlocks bb = new BasicBlocks(stmlist);
		TraceSchedule ts = new TraceSchedule(bb);
		stmlist = ts.stms;
		Optimize.RemoveTrivialJumps(stmlist);
		Optimize.FixTrivialArithmetic(stmlist);
		Optimize.RemoveTrivialMoves(stmlist);
		p.canon = stmlist;
		p.done = bb.done;
	}
	
	static boolean IsNoOP(Stm s) {
		return s instanceof EXPS && ((EXPS)s).e instanceof CONST;
	}
	
	static boolean Commute(Stm s, Exp e) {
		return IsNoOP(s) || e instanceof NAME || e instanceof CONST;
	}

	static Stm DoStm(SEQ s) {
		return Seq(DoStm(s.s1), DoStm(s.s2));
	}
	
	static Stm DoStm(MOVE s) {
		if (!s.mem && s.e instanceof CALL)
			return Reorder(new MOVECALL((TEMP)s.a, (CALL)s.e));
		else if (s.a instanceof ESEQ)
			throw new Error();
		return Reorder(s);
	}

	static Stm DoStm(EXPS s) {
		if (s.e instanceof CALL)
			return Reorder(new EXPSCALL((CALL)s.e));
		return Reorder(s);
	}
	
	static Stm DoStm(Stm s) {
		if (s instanceof SEQ) 
			return DoStm((SEQ)s);
		else if (s instanceof MOVE) 
			return DoStm((MOVE)s);
		else if (s instanceof EXPS) 
			return DoStm((EXPS)s);
		return Reorder(s);
	}

	static ESEQ DoExp(ESEQ e) {
		Stm s = DoStm(e.s);
		ESEQ es = DoExp(e.e);
		return new ESEQ(Seq(s,es.s), es.e);
	}
	
	static ESEQ DoExp(Exp e) {
		if (e instanceof ESEQ) {
			return DoExp((ESEQ)e);
		} else {
			return Reorder(e);
		}
	}

	static ESEQ Reorder(Exp e) {
		StmExpListTuple t = Reorder(e.kids());
		return new ESEQ(t.s, e.build(t.explist));
	}

	static Stm Reorder(Stm s) {
		StmExpListTuple t = Reorder(s.kids());
		return Seq(t.s, s.build(t.explist));
	}
	
	static StmExpListTuple Reorder(ExpList el) {
		if (el == null)
			return new StmExpListTuple(Stm.NOOP, null);
		else {
			Exp e = el.head;
			if (e instanceof CALL) {
				// Move Call Result to Temp
				Temp t = new Temp();
				Exp es = new ESEQ(new MOVE(new TEMP(t), e), new TEMP(t));
				return Reorder(new ExpList(es, el.tail));
			} else {
				ESEQ es = DoExp(e);
				StmExpListTuple t = Reorder(el.tail);
				if (Commute(t.s, es.e)) {
					return new StmExpListTuple(Seq(es.s,t.s), new ExpList(es.e, t.explist));
				} else {
					Temp tmp = new Temp();
					ExpList expList = new ExpList(new TEMP(tmp), t.explist);
					Stm s = Seq(es.s, Seq(new MOVE(new TEMP(tmp),es.e), t.s));

					return new StmExpListTuple(s, expList);
				}
			}
		}
	}
	
	static Stm Seq(Stm s1, Stm s2) {
		if (IsNoOP(s1))
			return s2;
		else if (IsNoOP(s2))
			return s1;
		else return new SEQ(s1, s2);
	}
	
	static StmList Linear(SEQ s, StmList l) { 
		return Linear(s.s1, Linear(s.s2,l));
	}
	static StmList Linear(Stm s, StmList l) {
		if (s instanceof SEQ) 
			return Linear((SEQ)s,l);
		else 
			return new StmList(s,l); 
	}
	static public StmList Linearize(Stm s) { 
		return Linear(DoStm(s), null);
	
	}

	
}
