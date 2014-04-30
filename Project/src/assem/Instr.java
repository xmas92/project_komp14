package assem;

import actrec.Label;
import actrec.LabelList;
import actrec.Temp;
import actrec.TempList;
import actrec.TempMap;

public abstract class Instr {

	public String assem;

	public abstract TempList uses();

	public abstract TempList defines();

	public abstract Targets jumps();

	private Temp nthTemp(TempList l, int i) {
		if (i == 0)
			return l.head;
		else
			return nthTemp(l.tail, i - 1);
	}

	private Label nthLabel(LabelList l, int i) {
		if (i == 0)
			return l.head;
		else
			return nthLabel(l.tail, i - 1);
	}

	public String format(TempMap m) {
		TempList dst = defines();
		TempList src = uses();
		Targets j = jumps();
		LabelList jump = (j == null) ? null : j.labels;
		StringBuffer s = new StringBuffer();
		int len = assem.length();

		for (int i = 0; i < len; i++)
			if (assem.charAt(i) == '`')
				switch (assem.charAt(++i)) {
				case 's': {
					int n = Character.digit(assem.charAt(++i), 10);
					s.append(m.tempMap(nthTemp(src, n)));
				}
					break;
				case 'd': {
					int n = Character.digit(assem.charAt(++i), 10);
					s.append(m.tempMap(nthTemp(dst, n)));
				}
					break;
				case 'j': {
					int n = Character.digit(assem.charAt(++i), 10);
					s.append(nthLabel(jump, n).toString());
				}
					break;
				case 'k': {
					int n = Character.digit(assem.charAt(++i), 10);
					s.append(m.constMap(n));
				}
					break;
				case '`':
					s.append('`');
					break;
				default:
					throw new Error("bad Assem format");
				}
			else
				s.append(assem.charAt(i));

		if (this instanceof LABEL) {
			return s.toString() + "\n";	
		} 
		else if (s.length() == 0) {
			// Ignore empty instructions
			return "";
		}
		else {
			return "\t" + s.toString() + "\n";
		}
	}

	@Override
	public String toString() {
		return assem;
	}

	public abstract boolean ChangeUse(Temp gtemp, Temp vi);

	public abstract boolean ChangeDef(Temp gtemp, Temp vi);

	public abstract void CoalesceTemp(Temp gtemp, Temp gtemp2);

}