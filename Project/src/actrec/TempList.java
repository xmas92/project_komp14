package actrec;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class TempList implements Collection<Temp>{
	public Temp head;
	public TempList tail;
	public TempList parent;
	public TempList(Temp h, TempList t) {
		head = h;
		tail = t;
		if (tail != null) tail.parent = this;
	}
	public TempList() {}
	@Override
	public int size() {
		if (head == null)
			return 0;
		if (tail == null)
			return 1;
		return tail.size() + 1;
	}
	@Override
	public boolean isEmpty() {
		return head == null;
	}
	@Override
	public boolean contains(Object o) {
		if (o != null)
			if (o.equals(head))
				return true;
			else if (tail != null)
				return tail.contains(o);
		return false;
	}
	public Iterator<Temp> descendingIterator() {
		return new  Iterator<Temp>() {
			TempList next = selfLast();
			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
			
			@Override
			public Temp next() {
				if (next == null || next.head == null)
					throw new NoSuchElementException();
				Temp ret = next.head;
				next = next.parent;
				return ret;
			}
			
			@Override
			public boolean hasNext() {
				return next != null && next.head != null;
			}
		};
	}
	protected TempList selfLast() {
		if (tail == null) return this;
		return tail.selfLast();
	}
	@Override
	public Iterator<Temp> iterator() {
		return new Iterator<Temp>() {
			TempList next = self();
			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}

			@Override
			public Temp next() {
				if (next == null || next.head == null)
					throw new NoSuchElementException();
				Temp ret = next.head;
				next = next.tail;
				return ret;
			}
			
			@Override
			public boolean hasNext() {
				return next != null && next.head != null;
			}
		};
	}
	protected TempList self() {
		return this;
	}
	@Override
	public Object[] toArray() {
		return toArray(new Object[0]);
	}
	@SuppressWarnings("unchecked")
	@Override
	public <T> T[] toArray(T[] a) {
		if (a.length < size()) 
			a = (T[])java.lang.reflect.Array.newInstance( a.getClass().getComponentType(), size());
		toArray(a, 0);
		return a;
	}
	private void toArray(Object[] a, int n) {
		a[n] = head;
		if (tail != null) tail.toArray(a,n++);
	}
	@Override
	public boolean add(Temp e) {
		if (head == null) head = e;
		else if (tail == null) tail = new TempList(e, null);
		else tail.add(e);
		return true;
	}
	@Override
	public boolean remove(Object o) {
		boolean r = _remove(o);
		CleanUp();
		return r;
	}
	protected void CleanUp() {
		if (head == null) {
			tail = null;
			return;
		} 
		if (tail != null)
			tail.CleanUp();
	}
	protected boolean _remove(Object o) {
		if (o == null) return false;
		if (o.equals(head)) {
			if (tail != null) {
				head = tail.head;
				tail = tail.tail;
			} else {
				head = null;
			}
			return true;
		} else if (tail != null) {
			return tail.remove(o);
		}
		return false;
	}
	@Override
	public boolean containsAll(Collection<?> c) {
		for (Object o : c) {
			if (!contains(o))
				return false;
		}
		return true;
	}
	@Override
	public boolean addAll(Collection<? extends Temp> c) {
		boolean b = false;
		for (Temp t : c) {
			add(t);
			b = true;
		}
		return b;
	}
	@Override
	public boolean removeAll(Collection<?> c) {
		boolean b = false;
		for (Object o : c) {
			remove(o);
			b = true;
		}
		return b;
	}
	@Override
	public boolean retainAll(Collection<?> c) {
		return _retainAll(c);
	}
	private boolean _retainAll(Collection<?> c) {
		for (Object o : c) {
			if (o == null) continue;
			if (o.equals(head)) {
				if (tail != null) {
					return tail.retainAll(c);
				} 
				return false;
			}
		}
		head = null;
		if (tail != null)
			tail = tail.tail;
		if (tail != null)
			tail.retainAll(c);
		return true;
	}
	@Override
	public void clear() {
		head = null;
		tail = null;
	}
	public boolean swap(Temp gtemp, Temp vi) {
		if (head == null) return false;
		if (head.equals(gtemp)) {
			head = vi;
			return true;
		}
		if (tail == null) return false;
		return tail.swap(gtemp, vi);
	}

	public boolean swapAll(Temp gtemp, Temp vi) {
		if (head == null) return false;
		boolean b = false;
		if (head.equals(gtemp)) {
			head = vi;
			b = true;
		}
		if (tail == null) return b || false;
		return  b || tail.swap(gtemp, vi);
	}
}