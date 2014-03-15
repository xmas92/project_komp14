package ast.type;

import ast.Node;

public abstract class Type extends Node {

	public Type(int line, int column) {
		super(line, column);
		// TODO Auto-generated constructor stub
	}
	public static boolean Same(Type t1, Type t2) {
		if (t1 instanceof PrimitiveType) {
			if (t2 instanceof PrimitiveType) {
				return ((PrimitiveType)t1).primitive == ((PrimitiveType)t2).primitive;
			}
		} else if (t1 instanceof ClassType) {
			if (t2 instanceof ClassType) {
				return ((ClassType)t1).id.equals(((ClassType)t2).id);
			}
		}
		return false;
	}

	public static boolean SameIgnoreArray(Type t1, Type t2) {
		if (t1 instanceof PrimitiveType) {
			if (t2 instanceof PrimitiveType) {
				PrimitiveType p1 = (PrimitiveType)t1;
				PrimitiveType p2 = (PrimitiveType)t2;
				switch (p1.primitive) {
				case Int:
					return p2.primitive == Primitive.Int || p2.primitive == Primitive.IntArr;
				case IntArr:
					return p2.primitive == Primitive.Int || p2.primitive == Primitive.IntArr;

				case Long:
					return p2.primitive == Primitive.Long || p2.primitive == Primitive.LongArr;
				case LongArr:
					return p2.primitive == Primitive.Long || p2.primitive == Primitive.LongArr;
				default:
					return Same(t1,t2);
				}
			}
		}
		return Same(t1,t2);
	}

	public boolean IsType(Primitive i) {
		return false;
	}
	
	public boolean IsType(String id) {
		return false;
	}

	public static boolean Assignable(Type t1, Type t2) {
		if (t1 instanceof PrimitiveType || 
				t2 instanceof PrimitiveType) return Same(t1,t2);
		String s1 = ((ClassType)t1).id;
		String s2 = ((ClassType)t2).id;
		while (!s1.equals(s2)) {
			s2 = ClassTable.ct.get(s2).superclass;
			if (s2 == null) return false;
		}
		return true;
	}
	

}
