package assem;

import actrec.Label;
import actrec.LabelList;

public class Targets {

	public LabelList labels;

	public Targets(Label j) {
		labels = new LabelList(j);
	}
	public Targets(Label t,Label f) {
		labels = new LabelList(t, new LabelList(f));
	}
}