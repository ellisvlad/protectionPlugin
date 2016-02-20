package com.ellisvlad.protectionPlugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;

import com.google.gson.Gson;

public class DatabaseConnector {
	
	private static final String configFileName = "SqlConnection.json";

	private static Connection sqlConnection = null;

	/**
	 * Connects to a database using provided database information
	 * @param host Host ip
	 * @param db Database name
	 * @param u Username
	 * @param p Password
	 */
	public DatabaseConnector(String host, String db, String u, String p) {
		initConnection(new DatabaseConfig(host, db, u, p));
	}
	
	/**
	 *  Loads connection info from file, making a default config file if one doesn't exist
	 */
	public DatabaseConnector() {
		File file = new File(configFileName);
		if (!file.exists()) {
			try {
				writeConfig(file, new DatabaseConfig());
			} catch (IOException e) {
				System.err.println("Failed to create default database config!");
				e.printStackTrace();
				return;
			}
		}
		
		DatabaseConfig config = null;
		try {
			config = readConfig(file);
		} catch (IOException e) {
			System.err.println("Failed to load database config!");
			e.printStackTrace();
			return;
		}
		initConnection(config);
	}
	
	/**
	 * Attempts to connect with the config provided
	 * @return False if the connection failed or if already connected
	 * @param config DatabaseConfig to connect with
	 */
	private boolean initConnection(DatabaseConfig config) {
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
		} catch (Exception e) {
			System.err.println("Mysql driver not found!");
			return false;
		}
		
		if (sqlConnection==null) {
			try {
				sqlConnection = DriverManager.getConnection("jdbc:mysql://"+config.host+":3306/"+config.db, config.username, config.password);
			} catch (SQLException e) {
				System.err.println("Could not connect to "+config.host+"/"+config.db+" using login "+config.username);
				e.printStackTrace();
				return false;
			}
			
			// Connected OK!
			initDatabaseTables();
			return true;
		}
		return false;
	}
	
	private void initDatabaseTables() {
	}

	/**
	 * Writes a config file using the DatabaseConfig
	 * @param file File writing to
	 * @param config Config being written
	 * @throws IOException
	 */
	private void writeConfig(File file, DatabaseConfig config) throws IOException {
		FileOutputStream fos = new FileOutputStream(file);
		fos.write(
			new Gson().toJson(config).getBytes()
		);
		fos.close();
	}
	
	/**
	 * Reads a config file and returns a DatabaseConfig object
	 * @param file File reading from
	 * @return new DatabaseConfig with fields initialised
	 * @throws IOException
	 */
	private DatabaseConfig readConfig(File file) throws IOException {
		String json=new String(Files.readAllBytes(file.toPath()));
		DatabaseConfig config=new Gson().fromJson(json, DatabaseConfig.class);
		return config;
	}

	class DatabaseConfig {
		String host;
		String username;
		String password;
		String db;
		
		private DatabaseConfig() {
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

	private class KeepAlive implements Runnable {
		@Override
		public void run() {
			while (true) {
				synchronized (sqlConnection) {
					if (sqlConnection==null) return;
					try {
						sqlConnection.prepareStatement("SELECT 1").executeQuery().close();
					} catch (SQLException e) {
						System.err.println("Database ping failed!");
					}
				}
				try {
					Thread.sleep(150000); // 150 sec
				} catch (InterruptedException e) {return;}
			}
		}
	}
}
