package actrec.jvm;

import actrec.Access;
import actrec.Label;


public class Frame extends actrec.Frame {
	public int localidx = 0;
	
	public Frame(Label label) {
		super(label);
	}

	public Frame() {
		super(new Label("dummy"));
	}

	public class LocalVar extends Access {
		public final int idx;
		public LocalVar(int idx) {
			this.idx = idx;
		}
	}
	
	@Override
	public Access AllocFormal(boolean doubleword) {
		if (doubleword) {
			int t = localidx;
			localidx += 2;
			return new LocalVar(t);
		} 
		return new LocalVar(localidx++);
	}

	@Override
	public Access AllocLocal(boolean doubleword) {
		if (doubleword) {
			int t = localidx;
			localidx += 2;
			return new LocalVar(t);
		} 
		return new LocalVar(localidx++);
	}

	@Override
	public actrec.Frame newFrame(Label label) {
		return new Frame(label);
	}

}
