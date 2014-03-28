package semanal;

import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;

import ast.declaration.ClassDeclaration;
import ast.declaration.VariableDeclaration;
import ast.type.Type;

public class SymbolTable {
	public Deque<HashMap<String, VariableDeclaration>> table;
	public ClassDeclaration current;
	public SymbolTable() {
		table = new LinkedList<>();
		table.addFirst(new HashMap<String, VariableDeclaration>());
	}
	public void EnterScope() {
		table.addFirst(new HashMap<String, VariableDeclaration>());
	}
	public void LeaveScope() {
		table.removeFirst();
	}
	/***
	 * Adds a symbol to current scope. (Only if no other symbol with that name exists)
	 * @param sym name of symbol
	 * @param decl declaration of symbol
	 * @return true if no symbol already existed, false otherwise
	 */
	public boolean AddSymbol(String sym, VariableDeclaration decl) {
		for (HashMap<String, VariableDeclaration> scope : table) {
			if (scope.containsKey(sym)) {
				return false;
			}
		}
		// Will always be true
		return table.peekFirst().put(sym, decl) == null; 
	}
	
	public Type GetSymbolType(String sym) {
		for (HashMap<String, VariableDeclaration> scope : table) {
			if (scope.containsKey(sym)) {
				return scope.get(sym).type;
			}
		}
		return null;
	}
	
	public VariableDeclaration GetSymbolDecl(String sym) {
		for (HashMap<String, VariableDeclaration> scope : table) {
			if (scope.containsKey(sym)) {
				return scope.get(sym);
			}
		}
		return null;
	}
}
