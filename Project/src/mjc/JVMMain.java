package mjc;

import java.io.File;
import java.io.PrintStream;

import semanal.ClassTable;
import actrec.jvm.Frame;
import ast.Program;
import ast.visitor.FancyPrintVisitor;
import ast.visitor.FrameBuilderVisitor;
import ast.visitor.JasminCodeGeneratorVisitor;
import ast.visitor.SemanticsVisitor;
import lexer.MiniJavaParser;
import lexer.ParseException;

public final class JVMMain {
	public static String pathname = "test.minijava";
	public static void main(String[] args) {
		File f = new File(args[0]);
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
		/* Jasmin JVM */
		p.accept(new FrameBuilderVisitor(), new Frame());
		// Code Generation
		JasminCodeGeneratorVisitor jcgv = new JasminCodeGeneratorVisitor();
		p.accept(jcgv, null);
		for (String c : jcgv.classes.keySet()) {
			try {
				File j = new File(c + ".j");
				j.createNewFile();
				PrintStream out = new PrintStream(j);
				out.print(jcgv.classes.get(c));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit(1);
			}
		}
		/* Jasmin JVM */
	}
}
