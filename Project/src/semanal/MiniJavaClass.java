package semanal;

import java.util.HashMap;

import ast.MainClass;
import ast.declaration.ClassDeclaration;
import ast.declaration.MethodDeclaration;

public class MiniJavaClass {
	public String name;
	public String superclass;
	public HashMap<String,MethodDeclaration> methodtable;
	public ClassDeclaration cd;
	public boolean isSetup = false;
	public MiniJavaClass(ClassDeclaration cd) {
		this.name = cd.id;
		this.superclass = cd.extendsID;
		this.cd = cd;
		methodtable = new HashMap<>();
		for (MethodDeclaration c : cd.methoddeclarations) {
			methodtable.put(c.id, c);
		}
	}

	public MiniJavaClass(MainClass mc) {
		this.name = mc.id;
		this.superclass = null;
		this.cd = null;
		this.methodtable = new HashMap<>();
	}
}