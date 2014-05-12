package actrec.arm;

import java.util.HashMap;
import java.util.HashSet;

import actrec.Temp;
import actrec.TempList;

public class Hardware {
	public static Temp r0 = new Temp();
	public static Temp r1 = new Temp();
	public static Temp r2 = new Temp();
	public static Temp r3 = new Temp();
	public static Temp r4 = new Temp();
	public static Temp r5 = new Temp();
	public static Temp r6 = new Temp();
	public static Temp r7 = new Temp();
	public static Temp r8 = new Temp();
	public static Temp r9 = new Temp();
	public static Temp r10 = new Temp();
	public static Temp r11 = new Temp();
	public static Temp r12 = new Temp();
	public static Temp r13 = new Temp();
	public static Temp r14 = new Temp();
	public static Temp r15 = new Temp();

	public static Temp[] retRegs = { r0, r1 };
	public static Temp[] argRegs = { r0, r1, r2, r3 };
	public static Temp[] popRegs = { r4, r5, r6, r8, r9, r10, r11 };
	public static Temp[] varRegs = { r4, r5, r6, r7, r8, r9, r10, r11, r14 };
	public static Temp[] calleeSaved = {  r4, r5, r6, r7, r8, r9, r10, r11, r14 };
	public static Temp[] callerSaved = { r0, r1, r2, r3, r12, r14 };

	//public static Temp RV = new Temp(); // Abstract register for return values (retRegs physical)
	
	public static Temp FP = r7; //r7; might use r7 but FP is imaginary and will be transformed into the sp
	public static Temp SP = r13;
	public static Temp LR = r14;
	public static Temp PC = r15;
	
	public static TempList calldefs;
	public static TempList returnSink = new TempList();

	public static HashMap<Temp, String> tempMap = new HashMap<>();
	public static HashSet<Temp> regiseters = new HashSet<>();
	static {
		TempList tl = null;
		for (Temp t : callerSaved) {
			if (tl == null) {
				tl = new TempList(t, null);
			} else {
				tl = new TempList(t, tl);
			}
		}
		calldefs = tl;
		for (Temp t : calleeSaved) {
			returnSink.add(t);
		}
		returnSink.add(r0);
		returnSink.add(r1);
		returnSink.add(SP);

		tempMap.put(r0, "r0");
		regiseters.add(r0);
		tempMap.put(r1, "r1");
		regiseters.add(r1);
		tempMap.put(r2, "r2");
		regiseters.add(r2);
		tempMap.put(r3, "r3");
		regiseters.add(r3);
	
		tempMap.put(r4, "r4");
		regiseters.add(r4);
		tempMap.put(r5, "r5");
		regiseters.add(r5);
		tempMap.put(r6, "r6");
		regiseters.add(r6);
		tempMap.put(r7, "r7");
		regiseters.add(r7);
	
		tempMap.put(r8, "r8");
		regiseters.add(r8);
		tempMap.put(r9, "r9");
		regiseters.add(r9);
		tempMap.put(r10, "r10");
		regiseters.add(r10);
		tempMap.put(r11, "r11");
		regiseters.add(r11);
	
		tempMap.put(r12, "r12");
		regiseters.add(r12);
		tempMap.put(r13, "sp");
		regiseters.add(r13);
		tempMap.put(r14, "lr");
		regiseters.add(r14);
		// tempMap.put(r15, "pc");
		// regiseters.add(r15);
	}
	public static TempList spfp = 	new TempList(r7,
									new TempList(r14, null));
	public static TempList calleeSavedList = 	new TempList(r4,
												new TempList(r5,
												new TempList(r6,
												new TempList(r8,
												new TempList(r9,
												new TempList(r10,
												new TempList(r11, null)))))));
}
