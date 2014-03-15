package compiler.util;

public class Report {

	public static void ExitWithError(String string, Object... args) {
		System.out.format("[ERROR]: " + string + "\n", args);
		System.exit(1);
	}

}
