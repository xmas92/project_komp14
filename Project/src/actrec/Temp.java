package actrec;

public class Temp implements Comparable<Temp> {
	private static int idx = 0;
	public final int temp;
	public Temp() {
		temp = idx++;
	}
	@Override
	public String toString() {
		return "t" + temp;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Temp)
			return ((Temp)o).temp == temp;
		return false;
	}
	@Override
	public int compareTo(Temp o) {
		return Integer.compare(temp, o.temp);
	}
}
