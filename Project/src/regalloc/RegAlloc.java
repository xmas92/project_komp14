package regalloc;

import ir.translate.Procedure;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Stack;

import dataflow.InstrFlowGraph;
import dataflow.Node;
import actrec.Temp;
import actrec.TempList;
import actrec.TempMap;
import assem.Instr;
import assem.MOVE;
import assem.OPER;

public class RegAlloc implements TempMap {

	static public class Edge {
		Node n1, n2;

		public Edge(Node n1, Node n2) {
			this.n1 = n1;
			this.n2 = n2;
		}

		@Override
		public String toString() {
			return "(" + n1 + ", " + n2 + ")";
		}

		@Override
		public boolean equals(Object o) {
			if (o instanceof Edge)
				return n1.equals(((Edge) o).n1) && n2.equals(((Edge) o).n2);
			return false;
		}

		@Override
		public int hashCode() {
			int seed = 0;
			seed ^= n1.hashCode() + 0x9e3779b9 + (seed << 6) + (seed >> 2);
			seed ^= n2.hashCode() + 0x9e3779b9 + (seed << 6) + (seed >> 2);
			return seed;
		}
	}

	public RegAlloc(Procedure proc) {
		this.proc = proc;
		this.registers = proc.frame.registers();
		this.tempMap = proc.frame.tempMap();
		this.K = this.registers.size();
		for (Temp t : registers) {
			color.put(t, t); // Registers are colored with their own color (ofc)
		}
		Main();
		LinkedList<Instr> nInstr = new LinkedList<>();
		outer: for (Instr i : proc.instrs) {
			if (i instanceof MOVE) {
				MOVE m = (MOVE) i;
				if (tempMap(m.dst).equals(tempMap(m.src)))
					continue outer;
			}
			nInstr.add(i);
		}
		proc.instrs = nInstr;
	}

	private HashSet<Temp> registers;
	private TempMap tempMap;
	private int K;
	private int spillSize;

	private HashSet<Node> simplifyWorklist = new HashSet<>();
	private HashSet<Node> freezeWorklist = new HashSet<>();
	private HashSet<Node> spillWorklist = new HashSet<>();

	private HashSet<Edge> coalescedMoves = new HashSet<>();
	private HashSet<Edge> constrainedMoves = new HashSet<>();
	private HashSet<Edge> frozenMoves = new HashSet<>();
	private HashSet<Edge> worklistMoves = new HashSet<>();
	private HashSet<Edge> activeMoves = new HashSet<>();

	private HashSet<Node> spillNodes = new HashSet<>();
	private HashSet<Node> coalescedNodes = new HashSet<>();
	private HashSet<Node> coloredNodes = new HashSet<>();

	private HashSet<Node> precolored = new HashSet<>();
	private HashSet<Node> initial = new HashSet<>();
	private Stack<Node> selectStack = new Stack<>();
	private HashSet<Edge> adjSet = new HashSet<>();
	private HashMap<Node, HashSet<Node>> adjList = new HashMap<>();
	private HashMap<Node, Integer> degree = new HashMap<>();
	private HashMap<Node, HashSet<Edge>> moveList = new HashMap<>();
	private HashMap<Node, Node> alias = new HashMap<>();
	private HashMap<Temp, Temp> color = new HashMap<>();
	private Liveness liveness;
	private Procedure proc;
	private boolean selectSpillFirstTime;

	private void Main() {
		selectSpillFirstTime = true;
		LivenessAnalysis();
		Build();
		MakeWorklist();
		do {
			if (!simplifyWorklist.isEmpty())
				Simplify();
			else if (!worklistMoves.isEmpty())
				Coalesce();
			else if (!freezeWorklist.isEmpty())
				Freeze();
			else if (!spillWorklist.isEmpty())
				SelectSpill();
		} while (!simplifyWorklist.isEmpty() || !worklistMoves.isEmpty()
				|| !freezeWorklist.isEmpty() || !spillWorklist.isEmpty());
		AssignColors();
		if (!spillNodes.isEmpty()) {
			RewriteProgram();
			Main();
		}
	}

	private TempList L(Temp h) {
		return new TempList(h, null);
	}

	private TempList L(Temp t1, Temp t2) {
		return new TempList(t1, new TempList(t2, null));
	}

	private void RewriteProgram() {
		HashSet<Node> newTemps = new HashSet<>();
		for (Instr i : proc.instrs) {
			for (Node n : coalescedNodesBeforeSpill) {
				i.CoalesceTemp(liveness.gtemp(n), liveness.gtemp(GetAlias(n)));
			}
		}
		LinkedList<Instr> nInstrs = new LinkedList<>();
		for (Instr i : proc.instrs) {
			if (i instanceof MOVE) {
				MOVE m = (MOVE) i;
				if (m.dst.equals(m.src))
					continue;
			}
			nInstrs.add(i);
		}
		proc.instrs = nInstrs;
		for (Node v : spillNodes) {
			nInstrs = new LinkedList<>();
			spillSize += 4;
			for (Instr i : proc.instrs) {
				TempList u = i.uses();
				if (u != null && u.contains(liveness.gtemp(v))) {
					Temp vi = new Temp();
					newTemps.add(liveness.tnode(vi));
					String str = String.format(
							"ldr `d0, [ `s0, #-%d ]\t@ Reload",
							proc.frame.SpillOffset() + spillSize);
					nInstrs.add(new OPER(str, L(vi), L(proc.frame.FP())));
					i.ChangeUse(liveness.gtemp(v), vi);
				}
				nInstrs.add(i);
				TempList d = i.defines();
				if (d != null && d.contains(liveness.gtemp(v))) {
					Temp vi = new Temp();
					newTemps.add(liveness.tnode(vi));
					String str = String.format(
							"str `s0, [ `s1, #-%d ]\t@ Spill",
							proc.frame.SpillOffset() + spillSize);
					nInstrs.add(new OPER(str, null, L(vi, proc.frame.FP())));
					i.ChangeDef(liveness.gtemp(v), vi);
				}
			}
			proc.instrs = nInstrs;
		}
		spillNodes.clear();
		// add stuff to initial
		initial.clear();
		initial.addAll(newTemps);
		initial.addAll(coloredNodes);
		initial.addAll(coalescedNodes);
		coloredNodes.clear();
		coalescedNodes.clear();
	}

	private void AssignColors() {
		while (!selectStack.empty()) {
			Node n = selectStack.pop();
			HashSet<Temp> okColors = new HashSet<>(registers);
			for (Node w : adjList.get(n))
				if (precolored.contains(GetAlias(w))
						|| coloredNodes.contains(GetAlias(w)))
					okColors.remove(color.get(liveness.gtemp(GetAlias(w))));
			if (okColors.isEmpty())
				spillNodes.add(n);
			else {
				coloredNodes.add(n);
				color.put(liveness.gtemp(n), okColors.iterator().next());
			}
		}
		for (Node n : coalescedNodes)
			color.put(liveness.gtemp(n), color.get(liveness.gtemp(GetAlias(n))));
	}
	
	private HashSet<Node> coalescedNodesBeforeSpill = new HashSet<>();

	private void SelectSpill() {
		if (selectSpillFirstTime)
			coalescedNodesBeforeSpill = new HashSet<>(coalescedNodes);
		selectSpillFirstTime = false;
		Node m = null;
		int p = -1;
		for (Node n : spillWorklist)
			if (degree.get(n) > p) {
				p = degree.get(n);
				m = n;
			}
		spillWorklist.remove(m);
		simplifyWorklist.add(m);
		FreezeMoves(m);
	}

	private void Freeze() {
		Node u = freezeWorklist.iterator().next();
		freezeWorklist.remove(u);
		simplifyWorklist.add(u);
		FreezeMoves(u);
	}

	private void FreezeMoves(Node u) {
		for (Edge m : NodeMoves(u)) {
			Node x = m.n1;
			Node y = m.n2;
			Node v;
			if (GetAlias(y).equals(GetAlias(u)))
				v = GetAlias(x);
			else
				v = GetAlias(y);
			activeMoves.remove(m);
			frozenMoves.add(m);
			if (freezeWorklist.contains(v) && NodeMoves(v).isEmpty()) {
				freezeWorklist.remove(v);
				simplifyWorklist.add(v);
			}
		}
	}

	private void Coalesce() {
		Edge m = worklistMoves.iterator().next();
		Node x = GetAlias(m.n1);
		Node y = GetAlias(m.n2);
		Node u, v;
		if (precolored.contains(y)) {
			u = y;
			v = x;
		} else {
			u = x;
			v = y;
		}
		worklistMoves.remove(m);
		if (u.equals(v)) {
			coalescedMoves.add(m);
			AddWorkList(u);
		} else if (precolored.contains(v) || adjSet.contains(new Edge(u, v))) {
			constrainedMoves.add(m);
			AddWorkList(u);
			AddWorkList(v);
		} else if (precolored.contains(u) && AllOK(Adjacent(v), u)
				|| !precolored.contains(u)
				&& Conservative(Union(Adjacent(u), Adjacent(v)))) {
			coalescedMoves.add(m);
			Combine(u, v);
			AddWorkList(u);
		} else {
			activeMoves.add(m);
		}
	}

	private void Combine(Node u, Node v) {
		if (freezeWorklist.contains(v))
			freezeWorklist.remove(v);
		else
			spillWorklist.remove(v);
		coalescedNodes.add(v);
		alias.put(v, u);
		HashSet<Edge> mlu = moveList.get(u);
		HashSet<Edge> mlv = moveList.get(v);
		if (mlu != null && mlv != null) {
			mlu.addAll(mlv);
		} else if (mlv != null) {
			moveList.put(u, mlv);
		}
		EnableMoves(v);
		for (Node t : Adjacent(v)) {
			AddEdge(t, u);
			DecrementDegree(t);
		}
		if (Degree(u) >= K && freezeWorklist.contains(u)) {
			freezeWorklist.remove(u);
			spillWorklist.add(u);
		}
	}

	private boolean Conservative(HashSet<Node> nodes) {
		int k = 0;
		for (Node n : nodes)
			if (Degree(n) >= K)
				k++;
		return k < K;
	}

	private void AddWorkList(Node u) {
		if (!precolored.contains(u) && !MoveReleted(u) && degree.get(u) < K) {
			freezeWorklist.remove(u);
			simplifyWorklist.add(u);
		}
	}

	private Node GetAlias(Node n) {
		if (coalescedNodes.contains(n))
			return GetAlias(alias.get(n));
		return n;
	}

	private HashSet<Node> Union(HashSet<Node> s1, HashSet<Node> s2) {
		HashSet<Node> ret = new HashSet<>(s1);
		ret.addAll(s2);
		return ret;
	}

	private boolean AllOK(HashSet<Node> adjacent, Node u) {
		for (Node t : adjacent)
			if (!OK(t, u))
				return false;
		return true;
	}

	private boolean OK(Node t, Node r) {
		return Degree(t) < K || precolored.contains(t) || adjSet.contains(new Edge(t, r));
	}

	private int Degree(Node t) {
		if (degree.containsKey(t))
			return degree.get(t);
		return 0;
	}

	private void Simplify() {
		Node n = simplifyWorklist.iterator().next();
		simplifyWorklist.remove(n);
		selectStack.push(n);
		for (Node m : Adjacent(n)) {
			DecrementDegree(m);
		}
	}

	private void DecrementDegree(Node m) {
		int d = Degree(m);
		degree.put(m, d - 1);
		if (d == K) {
			HashSet<Node> nodes = Adjacent(m);
			nodes.add(m);
			EnableMoves(nodes);
			spillWorklist.remove(m);
			if (MoveReleted(m))
				freezeWorklist.add(m);
			else
				simplifyWorklist.add(m);

		}
	}

	private void EnableMoves(Node v) {
		HashSet<Node> t = new HashSet<>();
		t.add(v);
		EnableMoves(t);
	}

	private void EnableMoves(HashSet<Node> nodes) {
		for (Node n : nodes)
			for (Edge m : NodeMoves(n))
				if (activeMoves.contains(m)) {
					activeMoves.remove(m);
					worklistMoves.add(m);
				}
	}

	private HashSet<Node> Adjacent(Node n) {
		HashSet<Node> ret = adjList.get(n);
		if (ret == null)
			return new HashSet<>();
		else
			ret = new HashSet<>(ret);
		ret.removeAll(selectStack);
		ret.removeAll(coalescedNodes);
		return ret;
	}

	private void MakeWorklist() {
		for (Node n : initial) {
			if (degree.get(n) >= K)
				spillWorklist.add(n);
			else if (MoveReleted(n))
				freezeWorklist.add(n);
			else
				simplifyWorklist.add(n);
		}
		initial.clear();
	}

	private boolean MoveReleted(Node n) {
		return !NodeMoves(n).isEmpty();
	}

	private HashSet<Edge> NodeMoves(Node n) {
		HashSet<Edge> ret = moveList.get(n);
		if (ret == null)
			return new HashSet<>();
		else
			ret = new HashSet<>(ret);
		HashSet<Edge> rem = new HashSet<>(worklistMoves);
		rem.addAll(activeMoves);
		ret.retainAll(rem);
		return ret;
	}
	private boolean firstTimeAround = true;
	private void Build() {
		// Setup Precolored
		for (Temp t : registers) 
			precolored.add(liveness.tnode(t));
		// Build
		HashSet<Temp> live = liveness.out.get(ifg.node(proc.instrs.getLast()));
		// check if it is uses/defs or in/out :)
		for (Iterator<Instr> it = proc.instrs.descendingIterator(); it
				.hasNext();) {
			Instr I = it.next();
			if (I instanceof MOVE) {
				MOVE M = (MOVE) I;
				live.removeAll(ifg.use(ifg.node(I)));
				HashSet<Temp> union = new HashSet<>(ifg.def(ifg.node(I)));
				union.addAll(ifg.use(ifg.node(I)));
				for (Temp n : union) {
					if (!moveList.containsKey(liveness.tnode(n)))
						moveList.put(liveness.tnode(n), new HashSet<Edge>());
					moveList.get(liveness.tnode(n)).add(
							new Edge(liveness.tnode(M.dst), liveness
									.tnode(M.src)));
				}
				worklistMoves.add(new Edge(liveness.tnode(M.dst), liveness
						.tnode(M.src)));
			}
			live.addAll(ifg.def(ifg.node(I)));
			for (Temp d : ifg.def(ifg.node(I)))
				for (Temp l : live)
					AddEdge(liveness.tnode(l),liveness.tnode(d));
			live.removeAll(ifg.def(ifg.node(I)));
			live.addAll(ifg.use(ifg.node(I)));
		}
		// Setup Inital
		if (firstTimeAround)
			for (Node n : liveness.nodes())
				if (!precolored.contains(n))
					initial.add(n);
		firstTimeAround = false;
	}

	private void AddEdge(Node u, Node v) {
		Edge e = new Edge(u, v);
		Edge er = new Edge(v, u);
		if (!adjSet.contains(e) && !u.equals(v)) {
			adjSet.add(e);
			adjSet.add(er);
			if (!precolored.contains(u)) {
				if (!adjList.containsKey(u))
					adjList.put(u, new HashSet<Node>());
				adjList.get(u).add(v);
				Integer d = degree.get(u);
				degree.put(u, (d == null) ? 0 : d + 1);
			}
			if (!precolored.contains(v)) {
				if (!adjList.containsKey(v))
					adjList.put(v, new HashSet<Node>());
				adjList.get(v).add(u);
				Integer d = degree.get(v);
				degree.put(v, (d == null) ? 0 : d + 1);
			}
		}
	}

	private InstrFlowGraph ifg;

	private void LivenessAnalysis() {
		ifg = new InstrFlowGraph(proc);
		// ifg.show(System.out);
		liveness = new Liveness(ifg, proc);
		// liveness.show(System.out);
	}

	@Override
	public String tempMap(Temp t) {
		return tempMap.tempMap(color.get(t));
	}

	@Override
	public String constMap(int n) {
		return Integer.toString(spillSize
				+ Integer.parseInt(tempMap.constMap(n)));
	}

}
