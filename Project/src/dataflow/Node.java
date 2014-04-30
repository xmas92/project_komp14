package dataflow;

import java.util.HashSet;

public class Node {
	Graph parent;
	public int idx;
	HashSet<Node> succs;
	HashSet<Node> preds;

	public Node(Graph g) {
		succs = new HashSet<>();
		preds = new HashSet<>();
		parent = g;
		idx = g.nodecount++;
	}

	@SuppressWarnings("unchecked")
	public HashSet<Node> succ() {
		return (HashSet<Node>) succs.clone();
	}
	public HashSet<Node> succC() {
		return succs;
	}

	@SuppressWarnings("unchecked")
	public HashSet<Node> pred() {
		return (HashSet<Node>) preds.clone();
	}

	public HashSet<Node> adj() {
		HashSet<Node> ret = new HashSet<>(succs);
		ret.addAll(preds);
		return ret;
	}
	public int inDegree() {
		return preds.size();
	}

	public int outDegree() {
		return succs.size();
	}

	public int degree() {
		return inDegree() + outDegree();
	}

	public boolean goesTo(Node n) {
		return succs.contains(n);
	}

	public boolean comesFrom(Node n) {
		return preds.contains(n);
	}

	public boolean adj(Node n) {
		return goesTo(n) || comesFrom(n);
	}


	@Override
	public int hashCode() {
		return idx;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Node)
			return idx == ((Node)o).idx;
		return false;
	}
	
	@Override
	public String toString() {
		return Integer.toString(idx);
	}
}
