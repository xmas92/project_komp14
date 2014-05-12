package regalloc;

import ir.translate.Procedure;

import java.util.TreeMap;
import java.util.Set;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Stack;
import java.util.TreeSet;

import dataflow.InstrFlowGraph;
import dataflow.Node;
import actrec.Temp;
import actrec.TempList;
import actrec.TempMap;
import assem.Instr;
import assem.MOVE;
import assem.OPER;

public class RegAlloc implements TempMap {

	static public class Edge implements Comparable<Edge> {
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

		@Override
		public int compareTo(Edge o) {
			int ret = n1.compareTo(o.n1);
			if (ret == 0)
				return n2.compareTo(o.n2);
			return ret;
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
		int idx = 0;
		outer: for (Instr i : proc.instrs) {
			idx++;
			if (i instanceof MOVE) {
				MOVE m = (MOVE) i;
				if (tempMap(m.dst).equals(tempMap(m.src)))
					continue outer;
			} else if (i.assem.startsWith("ldr")) {
				Temp def = getTemp(i.defines().head);
				for (Iterator<Instr> li = proc.instrs.listIterator(idx); li.hasNext();) {
					Instr i2 = li.next();
					if (/*i2 instanceof LABEL ||*/ i2.jumps() != null || 
							(i2.uses() != null && i2.uses().contains(def, color)))
						break;
					if (i2.defines() != null && i2.defines().contains(def, color))
						continue outer;
				}
			}
			nInstr.add(i);
		}
		proc.instrs = nInstr;
	}

	private Set<Temp> registers;
	private TempMap tempMap;
	private int K;
	private int spillSize;

	private Set<Node> simplifyWorklist = new TreeSet<>();
	private Set<Node> freezeWorklist = new TreeSet<>();
	private Set<Node> spillWorklist = new TreeSet<>();

	private Set<Edge> coalescedMoves = new TreeSet<>();
	private Set<Edge> constrainedMoves = new TreeSet<>();
	private Set<Edge> frozenMoves = new TreeSet<>();
	private Set<Edge> worklistMoves = new TreeSet<>();
	private Set<Edge> activeMoves = new TreeSet<>();

	private Set<Node> spillNodes = new TreeSet<>();
	private Set<Node> coalescedNodes = new TreeSet<>();
	private Set<Node> coloredNodes = new TreeSet<>();

	private Set<Node> precolored = new TreeSet<>();
	private Set<Node> initial = new TreeSet<>();
	private Stack<Node> selectStack = new Stack<>();
	private Set<Edge> adjSet = new TreeSet<>();
	private Map<Node, Set<Node>> adjList = new TreeMap<>();
	private Map<Node, Integer> degree = new TreeMap<>();
	private Map<Node, Set<Edge>> moveList = new TreeMap<>();
	private Map<Node, Node> alias = new TreeMap<>();
	private Map<Temp, Temp> color = new TreeMap<>();
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
		Set<Node> newTemps = new TreeSet<>();
		for (Node n : coalescedNodesBeforeSpill) {
			for (Instr i : proc.instrs) {
				i.CoalesceTemp(liveness.gtemp(n), liveness.gtemp(GetAlias(n)));
			}
		}
		LinkedList<Instr> nInstrs = new LinkedList<>();
		for (Instr i : proc.instrs) {
			if (i instanceof MOVE) {
				MOVE m = (MOVE) i;
				if (m.dst.equals(m.src)) {
					ifg.remove(i);
					continue;
				}
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
							"ldr `d0, [ `s0, #-`k%d ]\t@ Reload", spillSize);
					Instr ni = new OPER(str, L(vi), L(proc.frame.FP()));
					ifg.addBefore(ni,i);
					nInstrs.add(ni);
					i.ChangeUse(liveness.gtemp(v), vi);
				}
				nInstrs.add(i);
				TempList d = i.defines();
				if (d != null && d.contains(liveness.gtemp(v))) {
					Temp vi = new Temp();
					newTemps.add(liveness.tnode(vi));
					String str = String.format(
							"str `s0, [ `s1, #-`k%d ]\t@ Spill", spillSize);
					Instr ni = new OPER(str, null, L(vi, proc.frame.FP()));
					ifg.addAfter(ni,i);
					nInstrs.add(ni);
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
			Set<Temp> okColors = new TreeSet<>(registers);
			for (Node w : adjList.get(n))
				if (precolored.contains(GetAlias(w))
						|| coloredNodes.contains(GetAlias(w)))
					okColors.remove(color.get(liveness.gtemp(GetAlias(w))));
			if (okColors.isEmpty())
				spillNodes.add(n);
			else {
				coloredNodes.add(n);
				color.put(liveness.gtemp(n), GetColor(okColors));
			}
		}
		for (Node n : coalescedNodes)
			color.put(liveness.gtemp(n), color.get(liveness.gtemp(GetAlias(n))));
	}

	private Temp GetColor(Set<Temp> okColors) {
		for (Temp t : proc.frame.PreferredRegisters())
			if (okColors.contains(t))
				return t;
		return okColors.iterator().next();
	}

	private Set<Node> coalescedNodesBeforeSpill = new TreeSet<>();

	private void SelectSpill() {
		if (selectSpillFirstTime)
			coalescedNodesBeforeSpill = new TreeSet<>(coalescedNodes);
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
		Set<Edge> mlu = moveList.get(u);
		Set<Edge> mlv = moveList.get(v);
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

	private boolean Conservative(Set<Node> nodes) {
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

	private Set<Node> Union(Set<Node> s1, Set<Node> s2) {
		Set<Node> ret = new TreeSet<>(s1);
		ret.addAll(s2);
		return ret;
	}

	private boolean AllOK(Set<Node> adjacent, Node u) {
		for (Node t : adjacent)
			if (!OK(t, u))
				return false;
		return true;
	}

	private boolean OK(Node t, Node r) {
		return Degree(t) < K || precolored.contains(t)
				|| adjSet.contains(new Edge(t, r));
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
			Set<Node> nodes = Adjacent(m);
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
		Set<Node> t = new TreeSet<>();
		t.add(v);
		EnableMoves(t);
	}

	private void EnableMoves(Set<Node> nodes) {
		for (Node n : nodes)
			for (Edge m : NodeMoves(n))
				if (activeMoves.contains(m)) {
					activeMoves.remove(m);
					worklistMoves.add(m);
				}
	}

	private Set<Node> Adjacent(Node n) {
		Set<Node> ret = adjList.get(n);
		if (ret == null)
			return new TreeSet<>();
		else
			ret = new TreeSet<>(ret);
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

	private Set<Edge> NodeMoves(Node n) {
		Set<Edge> ret = moveList.get(n);
		if (ret == null)
			return new TreeSet<>();
		else
			ret = new TreeSet<>(ret);
		Set<Edge> rem = new TreeSet<>(worklistMoves);
		rem.addAll(activeMoves);
		ret.retainAll(rem);
		return ret;
	}

	private boolean firstTimeAroundBuild = true;

	private TempList Defines(Instr i) {
		TempList ret = i.defines();
		return ret == null? new TempList() : ret;
	}

//	private TempList Uses(Instr i) {
//		TempList ret = i.uses();
//		return ret == null? new TempList() : ret;
//	}

	private void Build() {
		// Setup Precolored
		for (Temp t : registers)
			precolored.add(liveness.tnode(t));
		// Build
//		Set<Temp> live = liveness.out.get(ifg.node(proc.instrs.getLast()));
		// check if it is uses/defs or in/out :)
		for (Iterator<Instr> it = proc.instrs.descendingIterator(); it
				.hasNext();) {
			Instr I = it.next();
			if (I instanceof MOVE) {
				MOVE M = (MOVE) I;
//				live.removeAll(Uses(I));
//				Set<Temp> union = new Set<>(Defines(I));
//				union.addAll(Uses(I));
				for (Temp n : liveness.out.get(ifg.node(I))) {
					if (!moveList.containsKey(liveness.tnode(n)))
						moveList.put(liveness.tnode(n), new TreeSet<Edge>());
					moveList.get(liveness.tnode(n)).add(
							new Edge(liveness.tnode(M.dst), liveness
									.tnode(M.src)));
				}
				worklistMoves.add(new Edge(liveness.tnode(M.dst), liveness
						.tnode(M.src)));
			}
//			live.addAll(Defines(I));
			for (Temp d : Defines(I))
				for (Temp l : liveness.out.get(ifg.node(I)))
					AddEdge(liveness.tnode(l), liveness.tnode(d));
//			live.removeAll(Defines(I));
//			live.addAll(Uses(I));
		}
		// Setup Inital
		if (firstTimeAroundBuild)
			for (Node n : liveness.nodes())
				if (!precolored.contains(n))
					initial.add(n);
		firstTimeAroundBuild = false;
	}

	private void AddEdge(Node u, Node v) {
		Edge e = new Edge(u, v);
		Edge er = new Edge(v, u);
		if (!adjSet.contains(e) && !u.equals(v)) {
			adjSet.add(e);
			adjSet.add(er);
			if (!precolored.contains(u)) {
				if (!adjList.containsKey(u))
					adjList.put(u, new TreeSet<Node>());
				adjList.get(u).add(v);
				Integer d = degree.get(u);
				degree.put(u, (d == null) ? 0 : d + 1);
			}
			if (!precolored.contains(v)) {
				if (!adjList.containsKey(v))
					adjList.put(v, new TreeSet<Node>());
				adjList.get(v).add(u);
				Integer d = degree.get(v);
				degree.put(v, (d == null) ? 0 : d + 1);
			}
		}
	}

	private InstrFlowGraph ifg;
	private boolean firstTimeAroundLivenessAnalysis = true;

	private void LivenessAnalysis() {
		if (firstTimeAroundLivenessAnalysis) {
			ifg = new InstrFlowGraph(proc);
			// ifg.show(System.out);
			liveness = new Liveness(ifg, proc);
			// liveness.show(System.out);
		} else {
			liveness.iterate();
		}
	}

	@Override
	public String tempMap(Temp t) {
		return tempMap.tempMap(color.get(t));
	}
	
	public Temp getTemp(Temp t) {
		return color.get(t);
	}

	@Override
	public String constMap(int n) {
		int i = proc.frame.SpillOffset();
		return Integer.toString(i+n);
	}

	public int SpillSize() {
		return spillSize;
	}

}
