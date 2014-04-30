package actrec;

public class DefaultMap implements TempMap {
	@Override
	public String tempMap(Temp t) {
	   return t.toString();
	}

	public DefaultMap() {}

	@Override
	public String constMap(int n) {
		return "CompilerConst" + n;
	}
}