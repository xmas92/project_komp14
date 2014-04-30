package actrec.arm;

import ir.tree.BINOP;
import ir.tree.CONST;
import ir.tree.Exp;
import ir.tree.MEM;
import actrec.Access;
import actrec.Label;
import ast.expression.BinaryExpression.Operator;

public class Record extends actrec.Record {
	private String extendsId;
	private int Size;
	private int vtableSize;
	private Label vtable;

	public Record(String id, String extendsID) {
		this.extendsId = extendsID;
		// if (this.extendsId == null)
		// Size = 4;
		vtable = new Label("vtable$$" + id);
		records.put(id, this);
	}

	@Override
	public Access AllocField(boolean doubleword) {
		return new InField(doubleword);
	}

	private class InField extends Access {
		int offset;

		public InField(boolean doubleword) {
			offset = Size;
			Size += doubleword ? 8 : 4;
		}

		@Override
		public Exp unEx(Exp obj) {
			if (offset + Offset() == 0)
				return new MEM(obj);
			return new MEM(new BINOP(Operator.Plus, obj, new CONST(offset
					+ Offset())));
		}

		@Override
		public Exp unExLo(Exp obj) {
			return unEx(obj);
		}

		@Override
		public Exp unExHi(Exp obj) {
			return new MEM(new BINOP(Operator.Plus, obj, new CONST(offset
					+ Offset() + 4)));
		}
	}

	private int Offset() {
		if (extendsId == null)
			return (VtableSize() > 0 ? 4 : 0);
		return records.get(extendsId).Size();
	}

	@Override
	public Label GetVTable() {
		return vtable;
	}

	@Override
	public int Size() {
		if (extendsId == null)
			return Size + (VtableSize() > 0 ? 4 : 0);
		return records.get(extendsId).Size() + Size;
	}

	@Override
	public int VtableSize() {
		if (extendsId == null)
			return vtableSize;
		return records.get(extendsId).VtableSize() + vtableSize;
	}

	private int VtableOffset() {
		if (extendsId == null)
			return 0;
		return ((Record)records.get(extendsId)).VtableSize();
	}
	@Override
	public Access AllocVirtual() {
		return new InVtable();
	}

	private class InVtable extends Access {
		int offset;

		public InVtable() {
			offset = vtableSize;
			vtableSize += 4;
		}

		@Override
		public Exp unEx(Exp obj) {
			if (GetOffset() == 0)
				return new MEM(new MEM(obj));
			return new MEM(new BINOP(Operator.Plus, new MEM(obj), new CONST(
					GetOffset())));
		}

		@Override
		public int GetOffset() {
			return offset + VtableOffset();
		}
	}

}
