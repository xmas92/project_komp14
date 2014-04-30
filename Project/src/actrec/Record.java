package actrec;

import java.util.HashMap;

public abstract class Record {
	public abstract Access AllocField(boolean doubleword);
	public abstract int Size();
	
	public static HashMap<String, Record> records = new HashMap<>();

	public abstract Access AllocVirtual();
	public abstract int VtableSize();
	public abstract Label GetVTable();
}
