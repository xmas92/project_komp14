package compiler;

import java.io.File;

import semanal.ClassTable;
import actrec.jvm.Frame;
import ast.Program;
import ast.visitor.FancyPrintVisitor;
import ast.visitor.FrameBuilderVisitor;
import ast.visitor.JasminCodeGeneratorVisitor;
import ast.visitor.OptimizeExpressionPassOneVisitor;
import ast.visitor.OptimizeExpressionPassTwoVisitor;
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
		p.accept(new OptimizeExpressionPassOneVisitor(), null);
		p.accept(new OptimizeExpressionPassTwoVisitor(), null);
		// Frame Layout Pass
		/* Jasmin JVM */
		p.accept(new FrameBuilderVisitor(), new Frame());
		// Code Generation
		JasminCodeGeneratorVisitor jcgv = new JasminCodeGeneratorVisitor();
		p.accept(jcgv, null);
		for (String c : jcgv.classes.keySet()) {
			System.out.println("; " + c + ".j File");
			System.out.println(jcgv.classes.get(c));
		}
		/* Jasmin JVM */
		System.out.println(p);
		System.out.println(p.accept(new FancyPrintVisitor(), null));
	}
}
