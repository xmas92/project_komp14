package actrec;

public class Temp {
	private static int idx = 0;
	public final int temp;
	public Temp() {
		temp = idx++;
	}
}
