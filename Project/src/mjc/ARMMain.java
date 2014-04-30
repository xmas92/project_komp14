package mjc;

import ir.translate.Procedure;

import java.io.File;
import java.io.PrintStream;

import canon.Canonicalize;
import regalloc.RegAlloc;
import semanal.ClassTable;
import actrec.DefaultMap;
import actrec.arm.CodeGeneration;
import actrec.arm.Frame;
import assem.Instr;
import ast.Program;
import ast.visitor.FrameBuilderVisitor;
import ast.visitor.OptimizeExpressionPassOneVisitor;
import ast.visitor.OptimizeExpressionPassTwoVisitor;
import ast.visitor.SemanticsVisitor;
import ast.visitor.TranslateVisitor;
import lexer.MiniJavaParser;
import lexer.ParseException;

public final class ARMMain {
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
		int IX = 1;
		// Semantic Analysis Pass
		ClassTable.BuildClassTable(p);
		p.accept(new SemanticsVisitor(), null);
		p.accept(new OptimizeExpressionPassOneVisitor(), null);
		p.accept(new OptimizeExpressionPassTwoVisitor(), null);
		// Frame Layout Pass
		p.accept(new FrameBuilderVisitor(), new Frame());
		// Translate IR tree Pass
		p.accept(new TranslateVisitor(), null);
		String str = "";
		str += ".text\n";
		str += ".align 2\n";
		str += ".global main\n";
		str += "\n";
		boolean abc = false;
		for (Procedure proc : TranslateVisitor.procedures) {
			// Canoicalize and Optimize IR tree Pass
			Canonicalize.CanonicalizeProc(proc);
			// Instruction Selection Pass (codegen)
			(new CodeGeneration(proc.frame)).Codegen(proc);
			// Register allocation Pass
			proc.frame.AddPrologueEpilogue(proc);
			proc.regalloc = new RegAlloc(proc);
			proc.frame.AddDataAccess(proc);
			str += "@ Method: " + proc.frame.frameLabel.label + "\n";
			str += proc.toString();
			str += ".ltorg\t@ dump literal pool\n";
			str += "\n";
			abc |= ((Frame)proc.frame).abc;
		}
		if (abc) {
			str += "_abc:\n";
			str += "\tldr r2, [ r0 ]\n";
			str += "\tmov r0, #1\n";
			str += "\tcmp r1, #0\n";
			str += "\tbllt exit\n";
			str += "\tcmp r1, r2\n";
			str += "\tblge exit\n";
			str += "\tbx lr\n";
		}

		str += "@ data\n";
		for (Instr i : CodeGeneration.GenerateVTablesInstr()) 
			str += i.format(new DefaultMap());
		for (Instr i : CodeGeneration.GeneratePrintLnInstr()) 
			str += i.format(new DefaultMap());
		try {
			File j = new File("armasm.s");
			j.createNewFile();
			PrintStream out = new PrintStream(j);
			out.print(str);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}
	}
}
