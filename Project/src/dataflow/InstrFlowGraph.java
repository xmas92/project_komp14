package dataflow;

import ir.translate.Procedure;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;

import actrec.Label;
import actrec.TempList;
import actrec.TempMap;
import assem.Instr;
import assem.LABEL;
import assem.MOVE;

public class InstrFlowGraph extends FlowGraph {
	HashMap<Node, TempList> defs = new HashMap<>();
	HashMap<Node, TempList> uses = new HashMap<>();
	HashMap<Node, Instr> instrs = new HashMap<>();
	HashMap<Instr, Node> nodes = new HashMap<>();
	HashMap<Label, LABEL> labels = new HashMap<>();

	public InstrFlowGraph(Procedure proc) {
		for (Instr instr : proc.instrs) {
			if (instr instanceof LABEL) {
				LABEL l = (LABEL) instr;
				labels.put(l.label, l);
			}
		}
		Iterator<Instr> it = proc.instrs.iterator();
		if (it.hasNext()) {
			Instr i1 = null;
			Instr i2 = it.next();
			while (it.hasNext()) {
				i1 = i2;
				i2 = it.next();
				processInstruction(i1, i2);
			}
			processInstruction(i2, null); // Last instruction
		}
	}

	private void processInstruction(Instr i1, Instr i2) {
		Node currNode = node(i1);

		defs.put(currNode, i1.defines());
		uses.put(currNode, i1.uses());

		// add edges to possible jump positions (includes following instruction
		// in conditional branch)
		if (i1.jumps() != null) {
			for (Label label : i1.jumps().labels) {
				LABEL labelInstr = labels.get(label);
				addEdge(currNode, node(labelInstr));
			}
			// add and link two instructions following each other
		} else if (i2 != null) {
			Node nextNode = node(i2);
			addEdge(currNode, nextNode);
		}
	}

	public Node node(Instr instr) {
		Node node = nodes.get(instr);
		if (node == null) {
			node = this.newNode();
			instrs.put(node, instr);
			nodes.put(instr, node);
		}

		return node;
	}

	public Instr instr(Node node) {
		return instrs.get(node);
	}

	@Override
	public TempList def(Node node) {
		return defs.get(node) == null ? new TempList() : defs.get(node);
	}

	@Override
	public TempList use(Node node) {
		return uses.get(node) == null ? new TempList() : uses.get(node);
	}

	@Override
	public boolean isMove(Node node) {
		return instrs.get(node) instanceof MOVE;
	}

	public void BuildDOTGraph(PrintStream out, TempMap tm) {
		out.println("digraph G {");
		out.println("rankdir=TB;");
		for (Entry<Instr, Node> e : nodes.entrySet()) {
			out.println(e.getValue().idx
					+ "[label=\""
					+ e.getValue().idx
					+ " "
					+ e.getKey().format(tm).replace("\"", "\\\"")
							.replace("\n", "").replace("\t", "") + "\"];");
		}
		for (Node n : nodes()) {
			for (Node n2 : n.succs) {
				out.println(n.idx + " -> " + n2.idx + "[label=\"\"];");
			}
		}
		out.println("}");
	}

	public void remove(Instr i) {
		Node n = node(i);
		for (Node np : n.preds)
			np.succs = new HashSet<>(n.succs);
	}

	public void addBefore(Instr ni, Instr i) {
		Node nn = node(ni);
		Node n = node(i);
		nn.preds = new HashSet<>(n.preds);
		nn.succs = new HashSet<>();
		nn.succs.add(n);
		n.preds = new HashSet<>();
		n.preds.add(nn);
	}

	public void addAfter(Instr ni, Instr i) {
		Node nn = node(ni);
		Node n = node(i);
		nn.preds = new HashSet<>();
		nn.preds.add(n);
		nn.succs = new HashSet<>(nn.succs);
		n.succs = new HashSet<>();
		n.succs.add(nn);
	}
}
