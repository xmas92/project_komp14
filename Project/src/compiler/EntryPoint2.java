package compiler;

import ir.translate.Procedure;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import canon.Canonicalize;
import dataflow.InstrFlowGraph;
import regalloc.RegAlloc;
import semanal.ClassTable;
import actrec.DefaultMap;
import actrec.arm.CodeGeneration;
import actrec.arm.Frame;
import assem.Instr;
import ast.Program;
import ast.visitor.FancyPrintVisitor;
import ast.visitor.FrameBuilderVisitor;
import ast.visitor.OptimizeExpressionPassOneVisitor;
import ast.visitor.OptimizeExpressionPassTwoVisitor;
import ast.visitor.SemanticsVisitor;
import ast.visitor.TranslateVisitor;
import lexer.MiniJavaParser;
import lexer.ParseException;

public final class EntryPoint2 {
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
//		System.out.println(p.accept(new FancyPrintVisitor(), null));
		p.accept(new OptimizeExpressionPassOneVisitor(), null);
		p.accept(new OptimizeExpressionPassTwoVisitor(), null);
		//System.exit(0);
		// Frame Layout Pass
		p.accept(new FrameBuilderVisitor(), new Frame());
		// Translate IR tree Pass
		p.accept(new TranslateVisitor(), null);
		System.out.print(".text\n");
		System.out.print(".align 2\n");
		System.out.print(".global main\n");
//		System.out.print(".extern _printf\n");
//		System.out.print(".extern _exit\n");
//		System.out.print(".extern _calloc\n");
		System.out.print("\n");
		boolean abc = false;
		for (Procedure proc : TranslateVisitor.procedures) {
			// Canoicalize and Optimize IR tree Pass
			Canonicalize.CanonicalizeProc(proc);
			// Instruction Selection Pass (codegen)
			(new CodeGeneration(proc.frame)).Codegen(proc);
			// System.out.print(proc);
			// Register allocation Pass
			proc.frame.AddPrologueEpilogue(proc);
			proc.regalloc = new RegAlloc(proc);
			proc.frame.AddDataAccess(proc);
			System.out.print("@ Method: " + proc.frame.frameLabel.label + "\n");
			System.out.print(proc);
			System.out.print(".ltorg\t@ dump literal pool\n");
			System.out.print("\n");
//			InstrFlowGraph ifg = new InstrFlowGraph(proc);
//			Liveness liveness = new Liveness(ifg, proc);
//			liveness.show(System.out);
//			try {
//				InstrFlowGraph ifg = new InstrFlowGraph(proc);
//				File DOTFILE = new File(proc.frame.frameLabel.label + "_graph.dot");
//				DOTFILE.createNewFile();
//				ifg.BuildDOTGraph(new PrintStream(DOTFILE), proc.regalloc);
//			} catch (Exception e) {}
			abc |= ((Frame)proc.frame).abc;
		}

		if (abc) {
			System.out.print("_abc:\n");
			System.out.print("\tldr r2, [ r0 ]\n");
			System.out.print("\tmov r0, #1\n");
			System.out.print("\tcmp r1, #0\n");
			System.out.print("\tbllt exit\n");
			System.out.print("\tcmp r1, r2\n");
			System.out.print("\tblge exit\n");
			System.out.print("\tbx lr\n");
		}
		
		System.out.print("@ data\n");
		for (Instr i : CodeGeneration.GenerateVTablesInstr()) 
			System.out.print(i.format(new DefaultMap()));
		for (Instr i : CodeGeneration.GeneratePrintLnInstr()) 
			System.out.print(i.format(new DefaultMap()));
		// System.out.println(p);
		//System.out.println(p.accept(new FancyPrintVisitor(), null));
	}
}
