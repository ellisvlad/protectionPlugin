package com.ellisvlad.protectionPlugin.database;

public class DatabaseConfig {
	String host;
	String username;
	String password;
	String db;
	
	public DatabaseConfig() {
		this.host="127.0.0.1";
		this.username="protectUser";
		this.password="protectPassword";
		this.db="protect";
	}

	public DatabaseConfig(String host, String db, String u, String p) {
		this.host=host;
		this.username=u;
		this.password=p;
		this.db=db;
	}
}