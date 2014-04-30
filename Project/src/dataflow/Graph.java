package dataflow;

import java.util.HashSet;
import java.util.LinkedList;

public class Graph {
	 int nodecount = 0;
	// TODO Change back to HashSet! :)
	LinkedList<Node> nodes = new LinkedList<>();
	
	public HashSet<Node> nodes() { 
		return new HashSet<>(nodes);
	} 

	public Node newNode() {
		Node n = new Node(this);
		nodes.add(n);
		return n;
	}

	void check(Node n) {
		if (n.parent != this)
			throw new Error("Graph.addEdge using nodes from the wrong graph");
	}

	public void addEdge(Node from, Node to) {
		check(from); 
		check(to);
		to.preds.add(from);
		from.succs.add(to);
	}

	public void rmEdge(Node from, Node to) {
		to.preds.remove(from);
		from.succs.remove(to);
	}

	/**
	 * Print a human-readable dump for debugging.
	 */
	public void show(java.io.PrintStream out) {
		for (Node n : nodes()) {
			out.print(n.toString());
			out.print(": ");
			for(Node q : n.succ()) {
				out.print(q.toString());
				out.print(" ");
			}
			out.println();
		}
	}

}