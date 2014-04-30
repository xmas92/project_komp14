package ir.tree;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class StmList implements Iterable<Stm> {
	public Stm head;
	public StmList tail;
	public StmList(Stm head, StmList tail) {
		this.head = head;
		this.tail = tail;
	}
	private StmList self() {
		return this;
	}
	@Override
	public String toString() {
		String str = "";
		for (Stm s : this) {
			str += s.toString() + "\n";
		}
		return str;
	}
	@Override
	public Iterator<Stm> iterator() {
		return new Iterator<Stm>() {
			StmList next = self();
			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
			
			@Override
			public Stm next() {
				if (next == null)
					throw new NoSuchElementException();
				Stm ret = next.head;
				next = next.tail;
				return ret;
			}
			
			@Override
			public boolean hasNext() {
				return next != null;
			}
		};
	}
}
