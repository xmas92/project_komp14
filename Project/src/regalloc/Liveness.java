package regalloc;

import ir.translate.Procedure;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import regalloc.RegAlloc.Edge;
import actrec.Temp;
import actrec.TempList;
import assem.Instr;
//import assem.Instr;
//import assem.MOVE;
import dataflow.InstrFlowGraph;
import dataflow.Node;

public class Liveness extends InterferenceGraph {
	HashMap<Node, HashSet<Edge>> moves = new HashMap<>();

	HashMap<Node, HashSet<Temp>> in = new HashMap<>();
	HashMap<Node, HashSet<Temp>> out = new HashMap<>();
	
	private TempList Defines(Instr i) {
		TempList ret = i.defines();
		return ret == null? new TempList() : ret;
	}
	public void iterate() {
		boolean c = true;
		while (c) {
			c = false;
			for (Iterator<Instr> it = proc.instrs.descendingIterator(); it.hasNext();) {
				Instr i = it.next();
				Node n = ifg.node(i);
				for (Temp t : out.get(n))
					if (!Defines(i).contains(t))
						c |= in.get(n).add(t); // update in
				for (Node np : n.succC()) 
					for (Temp t : in.get(np))
						c |= out.get(n).add(t); // update out
			}
		}
	}
	private InstrFlowGraph ifg;
	private Procedure proc;
	public Liveness(InstrFlowGraph ifg, Procedure proc) {
		// init every node to 0
		this.ifg = ifg;
		this.proc = proc;
		for(Node n : ifg.nodes()) {
			in.put(n, new HashSet<Temp>(ifg.use(n)));
			out.put(n, new HashSet<Temp>());
		}
		iterate();
		/*
		ifg.show(System.out);
		System.out.println();
		for (Node n : ifg.nodes()) {
			System.out.print(n + ": (");
			out:
			for (Temp t : out.get(n)) {
				for (Temp t2 : in.get(n))
					if (t.equals(t2))
						continue out;
				System.out.print(" " + t);
			}
			System.out.print(" <-");
			out:
			for (Temp t : in.get(n)) {
				for (Temp t2 : out.get(n))
					if (t.equals(t2))
						continue out;
				System.out.print(" " + t);
			}
			System.out.print(")");
			for (Temp t : out.get(n)) 
				System.out.print(" " + t);
			System.out.print(" <-");
			for (Temp t : in.get(n)) 
				System.out.print(" " + t);
			System.out.println();
		}*/
//		for (Node n : ifg.nodes()) {
//			Instr i = ifg.instr(n);
//			if (i instanceof MOVE) {
//				MOVE m = (MOVE)i;
//				Node sn = tnode(m.src);
//				Node dn = tnode(m.dst);
//				HashSet<Edge> l = moves.get(sn);
//				if (l == null) {
//					l = new HashSet<>();
//					l.add(new Edge(sn, dn));
//					moves.put(sn, l);
//				} else {
//					l.add(new Edge(sn, dn));
//				}
//				for(Temp b : out.get(n))
//					if (!b.equals(m.src))
//						addEdge(dn, tnode(b));
//			} else {
//				for (Temp a : ifg.def(n)) 
//					for (Temp b : out.get(n)) 
//						if (!b.equals(a))
//							addEdge(tnode(a), tnode(b));
//			}
//		}
	}
	int size = 0;
	public int size() {
		return size;
	}
	public Node newNode(Temp t) {
		size++;
		Node n = super.newNode();
		n.idx = t.temp;
		nodes.put(t, n);
		temps.put(n, t);
		return n;
	}
	
	@Override
	public void addEdge(Node a, Node b) {
		super.addEdge(a, b);
		super.addEdge(b, a);
	}
	@Override
	public void rmEdge(Node a, Node b) {
		super.rmEdge(a, b);
		super.rmEdge(b, a);
	}
	
	HashMap<Temp, Node> nodes = new HashMap<>();
	@Override
	public Node tnode(Temp temp) {
		Node node = nodes.get(temp);
		if (node == null) {
			node = newNode(temp);
		}
		return node;
	}

	HashMap<Node, Temp> temps = new HashMap<>();
	@Override
	public Temp gtemp(Node node) {
		return temps.get(node);
	}

	@Override
	public HashMap<Node, HashSet<Edge>> moves() {
		return moves;
	}

	@Override
	public int spillCost(Node node) {
		// TODO Setup spillCost if you feel like a boss ;) (aka have time)
		return 100 / (node.degree()==0?1:node.degree());
	}
}
