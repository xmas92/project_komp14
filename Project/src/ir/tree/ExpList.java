package ir.tree;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class ExpList implements Iterable<Exp> {
	public Exp head;
	public ExpList tail;
	public ExpList(Exp head, ExpList tail) {
		this.head = head;
		this.tail = tail;
	}
	private ExpList self() {
		return this;
	}
	@Override
	public String toString() {
		String str = "";
		for (Exp e : this)
			str += e.toString() + "\n";
		return str;
	}
	@Override
	public Iterator<Exp> iterator() {
		return new Iterator<Exp>() {
			ExpList next = self();
			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
			
			@Override
			public Exp next() {
				if (next == null)
					throw new NoSuchElementException();
				Exp ret = next.head;
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
