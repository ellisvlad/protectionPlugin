package com.ellisvlad.protectionPlugin;

public class Logger {
	
	private Logger() {};
	
	public static class out {
		private out() {};
		
		public static void println(String str) {
			System.out.println("["+Main.plugin.getName()+"] "+str);
		}
	}
	
	public static class err {
		private err() {};
		
		public static void println(String str) {
			System.err.println("["+Main.plugin.getName()+"] "+str);
		}
	}
	
}
