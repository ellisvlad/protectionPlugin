package com.ellisvlad.protectionPlugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;

import com.google.gson.Gson;

public class DatabaseConnector {
	
	private static final String configFileName = "ProtectionSqlConnection.json";

	private static Connection sqlConnection = null;

	/**
	 * Connects to a database using provided database information.
	 * @param host Host ip
	 * @param db Database name
	 * @param u Username
	 * @param p Password
	 */
	public DatabaseConnector(String host, String db, String u, String p) {
		initConnection(new DatabaseConfig(host, db, u, p));
	}
	
	/**
	 *  Loads connection info from file, making a default config file if one doesn't exist.
	 *  Then connects to the database.
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
	
	/**
	 * Creates any tables not initialised in the database.
	 */
	private void initDatabaseTables() {
		try {
			HashSet<String> rows=new HashSet<>();
			ResultSet rs=sqlConnection.prepareStatement("SHOW TABLE STATUS").executeQuery();
			while (rs.next()) rows.add(rs.getString("Name"));
			rs.close();

			if (!rows.contains("players")) {
				System.out.println("Creating new players table...");
				sqlConnection.prepareStatement(
					"CREATE TABLE `players`("
					+ "`pid` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Unique ID', "
					+ "`uuidLo` BIGINT NOT NULL COMMENT 'Ingame UUID-Lo', "
					+ "`uuidHi` BIGINT NOT NULL COMMENT 'Ingame UUID-Hi', "
					+ "`tool_id` SMALLINT UNSIGNED NOT NULL COMMENT 'Tool id used for regions', "
					+ "`tool_data` SMALLINT NOT NULL COMMENT 'Tool damage value used for regions', "
					+ "PRIMARY KEY (`pid`)"
					+ ")"
				).executeUpdate();
				System.out.println("Created new players table");
			}
			if (!rows.contains("config")) {
				System.out.println("Creating new config table...");
				sqlConnection.prepareStatement(
					"CREATE TABLE `config` ("
					+ "`name` VARCHAR(255) NOT NULL,"
					+ "`value` VARCHAR(255) NOT NULL"
					+ ")"
				).executeUpdate();
				PreparedStatement configStatement=sqlConnection.prepareStatement(
					"INSERT INTO `config` (`name`, `value`) VALUES (?, ?)"
				);
				configStatement.setString(1, "default_tool_id");
				configStatement.setString(2, "35"); //TODO: Wool.. 271
				configStatement.addBatch();
				configStatement.setString(1, "default_tool_data");
				configStatement.setString(2, "4"); //TODO: Yellow.. -1
				configStatement.addBatch();
				configStatement.executeBatch();
				System.out.println("Created new config table");
			}
		} catch (SQLException e) {
			System.err.println("Database init!");
			e.printStackTrace();
		}
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
