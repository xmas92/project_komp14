package actrec;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class LabelList implements Iterable<Label> {
	public Label head;
	public LabelList tail;

	public LabelList(Label h, LabelList t) {
		head = h;
		tail = t;
	}

	public LabelList(Label h) {
		head = h;
		tail = null;
	}
	
	private LabelList self() {
		return this;
	}

	@Override
	public Iterator<Label> iterator() {
		return new Iterator<Label>() {
			LabelList next = self();
			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}

			@Override
			public Label next() {
				if (next == null)
					throw new NoSuchElementException();
				Label ret = next.head;
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