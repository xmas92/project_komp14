package compiler;

import java.io.File;

import semanal.ClassTable;
import ast.Program;
import ast.visitor.SemanticsVisitor;
import lexer.MiniJavaParser;
import lexer.ParseException;

public final class EntryPoint {
	public static String pathname = "test.minijava";
	public static void main(String[] args) {
		File f = new File(pathname);
		Program p = null;
		try {
			// Lex and Parser Pass
			p = MiniJavaParser.parse(f);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}
		// Semantic Analysis Pass
		ClassTable.BuildClassTable(p);
		p.accept(new SemanticsVisitor(), null);
		// Frame Layout Pass
		System.out.println(p);
	}
}
