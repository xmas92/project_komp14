package assem;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class InstrList implements Iterable<Instr> {

	public Instr		head;
	public InstrList	tail;

	public InstrList(Instr h, InstrList t) {
		head = h;
		tail = t;
	}
	
	private InstrList self() {
		return this;
	}
	
	@Override
	public Iterator<Instr> iterator() {
		return new Iterator<Instr>() {
			InstrList next = self();
			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}

			@Override
			public Instr next() {
				if (next == null)
					throw new NoSuchElementException();
				Instr ret = next.head;
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
