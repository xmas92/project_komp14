package semanal;

import java.util.HashMap;

import compiler.util.Report;
import ast.MainClass;
import ast.Program;
import ast.declaration.ClassDeclaration;


public class ClassTable extends HashMap<String, MiniJavaClass> {
	/**
	 * Auto-generated
	 */
	private static final long serialVersionUID = 1607300543413643200L;
	public MainClass mc;
	
	public static ClassTable ct;
	public static void BuildClassTable(Program n) {
		ct = new ClassTable();
		ct.put(n.mc.id, new MiniJavaClass(n.mc));
		ct.mc = n.mc;
		for (ClassDeclaration cd : n.cds) {
			Object t = ct.put(cd.id, new MiniJavaClass(cd));
			if (t != null) {
				Report.ExitWithError("Class %s redefined. (%d:%d)", 
						cd.id, cd.getBeginLine(),cd.getBeginColumn());
			}
		}
	}
}