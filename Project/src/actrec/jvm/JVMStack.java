package actrec.jvm;

public class JVMStack {
	int max;
	int stack;
	public void Push(int i) {
		stack += i;
		max = stack>max?stack:max;
	}
	public void Pop(int i) {
		stack -= i;
		assert(stack >= 0);
	}
	public int Max() {
		return max;
	}
}
