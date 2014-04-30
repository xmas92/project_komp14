package canon;

import ir.tree.BINOP;
import ir.tree.CALL;
import ir.tree.CJUMP;
import ir.tree.CONST;
import ir.tree.EXPS;
import ir.tree.Exp;
import ir.tree.ExpList;
import ir.tree.JUMP;
import ir.tree.LABEL;
import ir.tree.MEM;
import ir.tree.MOVE;
import ir.tree.NAME;
import ir.tree.StmList;
import ir.tree.TEMP;

public class Optimize {

	/**
	 * Removes JUMPs where the label is the next statement
	 * @param stmlist
	 */
	public static void RemoveTrivialJumps(StmList stmlist) {
		while (stmlist != null && stmlist.tail != null && stmlist.tail.tail != null)
			if (stmlist.tail.head instanceof JUMP &&
				stmlist.tail.tail.head instanceof LABEL &&
				((JUMP)stmlist.tail.head).l.equals(((LABEL)stmlist.tail.tail.head).l))
				stmlist.tail = stmlist.tail.tail;
			else
				stmlist = stmlist.tail;
	}

	public static void FixTrivialArithmetic(StmList stmlist) {
		while (stmlist != null) {
			if (stmlist.head instanceof MOVE) {
				((MOVE)stmlist.head).a = FixTrivialArithmetic(((MOVE)stmlist.head).a);
				((MOVE)stmlist.head).e = FixTrivialArithmetic(((MOVE)stmlist.head).e);
			} else if (stmlist.head instanceof EXPS) {
				((EXPS)stmlist.head).e = FixTrivialArithmetic(((EXPS)stmlist.head).e);
			} else if (stmlist.head instanceof CJUMP) {
				((CJUMP)stmlist.head).e1 = FixTrivialArithmetic(((CJUMP)stmlist.head).e1);
				((CJUMP)stmlist.head).e2 = FixTrivialArithmetic(((CJUMP)stmlist.head).e2);
			} 
			stmlist = stmlist.tail;
		}
	}

	private static Exp FixTrivialArithmetic(Exp a) {
		if (a instanceof CONST) return a;
		if (a instanceof NAME) return a;
		if (a instanceof TEMP) return a;
		if (a instanceof MEM) {
			((MEM)a).e = FixTrivialArithmetic(((MEM)a).e);
			return a;
		}
		if (a instanceof CALL) {
			((CALL)a).f = FixTrivialArithmetic(((CALL)a).f);
			ExpList e = ((CALL)a).explist;
			while (e != null) {
				e.head = FixTrivialArithmetic(e.head);
				e = e.tail;
			}
		}
		if (a instanceof BINOP) {
			BINOP e = (BINOP)a;
			Exp e1 = FixTrivialArithmetic(e.e1);
			Exp e2 = FixTrivialArithmetic(e.e2);
			if (e1 instanceof CONST && e2 instanceof CONST) {
				switch (e.op) {
				case Minus:
					return new CONST(((CONST)e1).value-((CONST)e2).value);
				case Plus:
					return new CONST(((CONST)e1).value+((CONST)e2).value);
				case Times:
					return new CONST(((CONST)e1).value*((CONST)e2).value);
				default:
					e.e1 = e1;
					e.e2 = e2;
					return e;
				}
			}
			if (e1 instanceof CONST) {
				switch (e.op) {
				case Plus:
					if (((CONST)e1).value == 0)
						return e2;
					break;
				case Times:
					if (((CONST)e1).value == 0)
						return new CONST(0);
					break;
				default:
					break;
				}
			}
			if (e2 instanceof CONST) {
				switch (e.op) {
				case Minus:
					if (((CONST)e2).value == 0)
						return e1;
					break;
				case Plus:
					if (((CONST)e2).value == 0)
						return e1;
					break;
				case Times:
					if (((CONST)e2).value == 0)
						return new CONST(0);
				case ASR:
					if (((CONST)e2).value == 0)
						return e1;
					break;
				default:
					break;
				}
			}
			e.e1 = e1;
			e.e2 = e2;
			return e;
		}
		return a;
	}

	public static void RemoveTrivialMoves(StmList stmlist) {
		if (stmlist == null) return;
		StmList prev = stmlist;
		stmlist = stmlist.tail;
		while (stmlist != null) {
			if (stmlist.head instanceof MOVE) {
				MOVE m = (MOVE)stmlist.head;
				if (IsEqual(m.a, m.e)) 
					prev.tail = stmlist.tail;
			}
			prev = stmlist;
			stmlist = stmlist.tail;
		}
	}

	private static boolean IsEqual(Exp e1, Exp e2) {
		if (e1 instanceof CONST && e2 instanceof CONST)  {
			return ((CONST)e1).value == ((CONST)e2).value;
		}
		if (e1 instanceof NAME && e2 instanceof NAME)  {
			return ((NAME)e1).label.label.equals(((NAME)e2).label.label);
		}
		if (e1 instanceof TEMP && e2 instanceof TEMP)  {
			return ((TEMP)e1).temp.temp == ((TEMP)e2).temp.temp;
		}
		if (e1 instanceof MEM && e2 instanceof MEM)  {
			return IsEqual(((MEM)e1).e, ((MEM)e2).e);
		}
		if (e1 instanceof BINOP && e2 instanceof BINOP)  {
			BINOP b1 = (BINOP)e1;
			BINOP b2 = (BINOP)e2;
			if (b1.op != b2.op)
				return false;
			switch (b1.op) {
			case Minus:
				return IsEqual(b1.e1, b2.e1) && IsEqual(b1.e2, b2.e2);
			case Plus:
				return (IsEqual(b1.e1, b2.e1) && IsEqual(b1.e2, b2.e2)) ||
						(IsEqual(b1.e1, b2.e2) && IsEqual(b1.e2, b2.e1));
			case Times:
				return (IsEqual(b1.e1, b2.e1) && IsEqual(b1.e2, b2.e2)) ||
						(IsEqual(b1.e1, b2.e2) && IsEqual(b1.e2, b2.e1));
			default:
				break;
			}
		}
		return false;
	}
}
