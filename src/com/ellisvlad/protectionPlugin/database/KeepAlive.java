package com.ellisvlad.protectionPlugin.database;

import java.sql.Connection;
import java.sql.SQLException;

import com.ellisvlad.protectionPlugin.Logger;

public class KeepAlive implements Runnable {
	
	private Connection sqlConnection;
	
	public KeepAlive(Connection sqlConnection) {
		this.sqlConnection=sqlConnection;
	}
	
	@Override
	public void run() {
		while (true) {
			if (sqlConnection==null) return;
			try {
				sqlConnection.prepareStatement("SELECT 1").executeQuery().close();
			} catch (SQLException e) {
				Logger.err.println("Database ping failed!");
			}
			try {
				Thread.sleep(150000); // 150 sec
			} catch (InterruptedException e) {return;}
		}
	}
}