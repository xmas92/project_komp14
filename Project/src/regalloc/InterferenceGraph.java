package regalloc;

import java.util.Map;
import java.util.Set;

import regalloc.RegAlloc.Edge;
import actrec.Temp;
import dataflow.Graph;
import dataflow.Node;

abstract public class InterferenceGraph extends Graph {
	abstract public Node tnode(Temp temp);

	abstract public Temp gtemp(Node node);

	abstract public Map<Node, Set<Edge>> moves();

	public int spillCost(Node node) {
		return 1000/(1+node.inDegree());
	}
}