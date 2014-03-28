package actrec;

public abstract class Frame {
	public Label frameLabel;
	public Frame(Label label) {
		frameLabel = label;
	}
	public abstract Frame newFrame(Label label);
	public abstract Access AllocFormal(boolean doubleword);
	public abstract Access AllocLocal(boolean doubleword);
}
