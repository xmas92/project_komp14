package semanal;

import java.util.HashMap;

import compiler.util.Report;

import ast.MainClass;
import ast.declaration.ClassDeclaration;
import ast.declaration.MethodDeclaration;
import ast.declaration.VariableDeclaration;

public class MiniJavaClass {
	public String name;
	public String superclass;
	public HashMap<String,MethodDeclaration> methodtable;
	public HashMap<String,VariableDeclaration> fieldtable;
	public ClassDeclaration cd;
	public boolean isSetup = false;
	public MiniJavaClass(ClassDeclaration cd) {
		this.name = cd.id;
		this.superclass = cd.extendsID;
		this.cd = cd;
		methodtable = new HashMap<>();
		for (MethodDeclaration c : cd.methoddeclarations) {
			if (methodtable.put(c.id, c) != null) {
				Report.ExitWithError("Method %s redefined. (%d:%d)", 
						c.id, c.getBeginLine(),c.getBeginColumn());
			}
		}
		fieldtable = new HashMap<>();
		for (VariableDeclaration vd : cd.variabledeclatartions) {
			if (fieldtable.put(vd.id, vd) != null) {
				Report.ExitWithError("Field %s redefined. (%d:%d)", 
						vd.id, vd.getBeginLine(),vd.getBeginColumn());
			}
		}
	}

	public MiniJavaClass(MainClass mc) {
		this.name = mc.id;
		this.superclass = null;
		this.cd = null;
		this.methodtable = new HashMap<>();
		this.fieldtable = new HashMap<>();
	}
}