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
import ir.tree.UMULL;

public class Optimize {

	public static void PropagateConstants(StmList stmlist) {
		FixTrivialArithmetic(stmlist);
		while (stmlist != null) {
			if (stmlist.head instanceof MOVE) {
				MOVE M = (MOVE) stmlist.head;
				if (M.a instanceof TEMP && M.e instanceof CONST) {
					PropagateConstants(stmlist.tail, (TEMP) M.a, (CONST) M.e);
					FixTrivialArithmetic(stmlist);
				}
			}
			stmlist = stmlist.tail;
		}
	}

	private static void PropagateConstants(StmList stmlist, TEMP a, CONST c) {
		while (stmlist != null) {
			if (stmlist.head instanceof JUMP) {
				// TODO Maybe Propogate Over JUMP
				return;
			} else if (stmlist.head instanceof CJUMP) {
				// TODO Maybe Propogate Over JUMP
				((CJUMP) stmlist.head).e1 = PropagateConstants(
						((CJUMP) stmlist.head).e1, a, c);
				((CJUMP) stmlist.head).e2 = PropagateConstants(
						((CJUMP) stmlist.head).e2, a, c);
				return;
			} else if (stmlist.head instanceof MOVE) {
				((MOVE) stmlist.head).e = PropagateConstants(
						((MOVE) stmlist.head).e, a, c);
				if (((MOVE) stmlist.head).a instanceof TEMP)
					if (Same((TEMP) ((MOVE) stmlist.head).a, a))
						return;
			} else if (stmlist.head instanceof EXPS) {
				((EXPS) stmlist.head).e = PropagateConstants(
						((EXPS) stmlist.head).e, a, c);
			} else if (stmlist.head instanceof UMULL) {
				((UMULL) stmlist.head).e1 = PropagateConstants(
						((UMULL) stmlist.head).e1, a, c);
				((UMULL) stmlist.head).e2 = PropagateConstants(
						((UMULL) stmlist.head).e2, a, c);
			} else if (stmlist.head instanceof LABEL) {
				// Here we must stop :)
				return;
			}
			stmlist = stmlist.tail;
		}
	}

	private static boolean Same(TEMP t1, TEMP t2) {
		return t1.temp.equals(t2.temp);
	}

	private static Exp PropagateConstants(Exp e, TEMP a, CONST c) {
		if (e instanceof TEMP) {
			if (Same((TEMP) e, (a)))
				return c;
		} else if (e instanceof MEM) {
			((MEM) e).e = PropagateConstants(((MEM) e).e, a, c);
		} else if (e instanceof CALL) {
			((CALL) e).f = PropagateConstants(((CALL) e).f, a, c);
			ExpList el = ((CALL) e).explist;
			while (el != null) {
				el.head = PropagateConstants(el.head, a, c);
				el = el.tail;
			}
		} else if (e instanceof BINOP) {
			((BINOP) e).e1 = PropagateConstants(((BINOP) e).e1, a, c);
			((BINOP) e).e2 = PropagateConstants(((BINOP) e).e2, a, c);
		}
		return e;
	}

	public static void RemoveTrivialJumps(StmList stmlist) {
		while (stmlist != null && stmlist.tail != null
				&& stmlist.tail.tail != null)
			if (stmlist.tail.head instanceof JUMP
					&& stmlist.tail.tail.head instanceof LABEL
					&& ((JUMP) stmlist.tail.head).l
							.equals(((LABEL) stmlist.tail.tail.head).l))
				stmlist.tail = stmlist.tail.tail;
			else
				stmlist = stmlist.tail;
	}

	public static void FixTrivialArithmetic(StmList stmlist) {
		while (stmlist != null) {
			if (stmlist.head instanceof MOVE) {
				((MOVE) stmlist.head).a = FixTrivialArithmetic(((MOVE) stmlist.head).a);
				((MOVE) stmlist.head).e = FixTrivialArithmetic(((MOVE) stmlist.head).e);
			} else if (stmlist.head instanceof EXPS) {
				((EXPS) stmlist.head).e = FixTrivialArithmetic(((EXPS) stmlist.head).e);
			} else if (stmlist.head instanceof CJUMP) {
				((CJUMP) stmlist.head).e1 = FixTrivialArithmetic(((CJUMP) stmlist.head).e1);
				((CJUMP) stmlist.head).e2 = FixTrivialArithmetic(((CJUMP) stmlist.head).e2);
			} else if (stmlist.head instanceof UMULL) {
				((UMULL) stmlist.head).e1 = FixTrivialArithmetic(((UMULL) stmlist.head).e1);
				((UMULL) stmlist.head).e2 = FixTrivialArithmetic(((UMULL) stmlist.head).e2);
			}
			stmlist = stmlist.tail;
		}
	}

	private static Exp FixTrivialArithmetic(Exp a) {
		if (a instanceof CONST)
			return a;
		if (a instanceof NAME)
			return a;
		if (a instanceof TEMP)
			return a;
		if (a instanceof MEM) {
			((MEM) a).e = FixTrivialArithmetic(((MEM) a).e);
			return a;
		}
		if (a instanceof CALL) {
			((CALL) a).f = FixTrivialArithmetic(((CALL) a).f);
			ExpList e = ((CALL) a).explist;
			while (e != null) {
				e.head = FixTrivialArithmetic(e.head);
				e = e.tail;
			}
		}
		if (a instanceof BINOP) {
			BINOP e = (BINOP) a;
			Exp e1 = FixTrivialArithmetic(e.e1);
			Exp e2 = FixTrivialArithmetic(e.e2);
			if (e1 instanceof CONST && e2 instanceof CONST) {
				switch (e.op) {
				case Minus:
					return new CONST(((CONST) e1).value - ((CONST) e2).value);
				case Plus:
					return new CONST(((CONST) e1).value + ((CONST) e2).value);
				case Times:
					return new CONST(((CONST) e1).value * ((CONST) e2).value);
				case ASR:
					return new CONST(((CONST) e1).value >> ((CONST) e2).value);
				case And:
					return new CONST(((CONST) e1).value & ((CONST) e2).value);
				case Or:
					return new CONST(((CONST) e1).value | ((CONST) e2).value);
				case XOR:
					return new CONST(((CONST) e1).value ^ ((CONST) e2).value);
				default:
					e.e1 = e1;
					e.e2 = e2;
					return e;
				}
			}
			if (e1 instanceof CONST) {
				switch (e.op) {
				case Plus:
					if (((CONST) e1).value == 0)
						return e2;
					break;
				case Times:
					if (((CONST) e1).value == 0)
						return new CONST(0);
					if (((CONST) e1).value == 1)
						return e2;
					break;
				default:
					break;
				}
			}
			if (e2 instanceof CONST) {
				switch (e.op) {
				case Minus:
					if (((CONST) e2).value == 0)
						return e1;
					break;
				case Plus:
					if (((CONST) e2).value == 0)
						return e1;
					break;
				case Times:
					if (((CONST) e2).value == 0)
						return new CONST(0);
					if (((CONST) e2).value == 1)
						return e1;
					break;
				case ASR:
					if (((CONST) e2).value == 0)
						return e1;
					break;
				default:
					break;
				}
			}
			if (e1 instanceof CONST) {
				switch (e.op) {
				case ASR: // Makes No Sense
				case Minus:
				case SBC:
				case SUBS:
					break;
				case Times: // Doesn't matter in ARM
					break;
				case And: // NOT SURE, NOT USED :)
				case Or:
					break;
				case Greater:
				case GreaterEq:
				case GreaterEqU:
				case GreaterU:
				case Less:
				case LessEq:
				case LessEqU:
				case LessU:
				case Eq:
				case NotEq: // should be able to turn
					break;
				case ADDS:
				case Plus:
				case XOR:
					e.e1 = e2;
					e.e2 = e1;
					return e;
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
		if (stmlist == null)
			return;
		StmList prev = stmlist;
		stmlist = stmlist.tail;
		while (stmlist != null) {
			if (stmlist.head instanceof MOVE) {
				MOVE m = (MOVE) stmlist.head;
				if (IsEqual(m.a, m.e))
					prev.tail = stmlist.tail;
			}
			prev = stmlist;
			stmlist = stmlist.tail;
		}
	}

	private static boolean IsEqual(Exp e1, Exp e2) {
		if (e1 instanceof CONST && e2 instanceof CONST) {
			return ((CONST) e1).value == ((CONST) e2).value;
		}
		if (e1 instanceof NAME && e2 instanceof NAME) {
			return ((NAME) e1).label.label.equals(((NAME) e2).label.label);
		}
		if (e1 instanceof TEMP && e2 instanceof TEMP) {
			return ((TEMP) e1).temp.temp == ((TEMP) e2).temp.temp;
		}
		if (e1 instanceof MEM && e2 instanceof MEM) {
			return IsEqual(((MEM) e1).e, ((MEM) e2).e);
		}
		if (e1 instanceof BINOP && e2 instanceof BINOP) {
			BINOP b1 = (BINOP) e1;
			BINOP b2 = (BINOP) e2;
			if (b1.op != b2.op)
				return false;
			switch (b1.op) {
			case Minus:
				return IsEqual(b1.e1, b2.e1) && IsEqual(b1.e2, b2.e2);
			case Plus:
				return (IsEqual(b1.e1, b2.e1) && IsEqual(b1.e2, b2.e2))
						|| (IsEqual(b1.e1, b2.e2) && IsEqual(b1.e2, b2.e1));
			case Times:
				return (IsEqual(b1.e1, b2.e1) && IsEqual(b1.e2, b2.e2))
						|| (IsEqual(b1.e1, b2.e2) && IsEqual(b1.e2, b2.e1));
			default:
				break;
			}
		}
		return false;
	}
}
