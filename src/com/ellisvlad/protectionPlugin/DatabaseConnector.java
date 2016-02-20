package com.ellisvlad.protectionPlugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;

import com.google.gson.Gson;

public final class DatabaseConnector {
	
	private static final String configFileName = "ProtectionSqlConnection.json";

	private static Connection sqlConnection = null;

	private DatabaseConnector() {}

	/**
	 * Connects to a database using provided database information.
	 * @return Returns false if there was an error
	 * @param host Host ip
	 * @param db Database name
	 * @param u Username
	 * @param p Password
	 */
	public static boolean init(DatabaseConfig config) {
		return initConnection(config);
	}
	
	/**
	 *  Loads connection info from file, making a default config file if one doesn't exist.
	 *  Then connects to the database.
	 * @return Returns false if there was an error
	 */
	public static boolean init() {
		File file = new File(configFileName);
		if (!file.exists()) {
			try {
				writeConfig(file, new DatabaseConfig());
			} catch (IOException e) {
				Logger.err.println("Failed to create default database config!");
				e.printStackTrace();
				return false;
			}
		}
		
		DatabaseConfig config = null;
		try {
			config = readConfig(file);
		} catch (IOException e) {
			Logger.err.println("Failed to load database config!");
			e.printStackTrace();
			return false;
		}
		return initConnection(config);
	}
	
	/**
	 * Attempts to connect with the config provided
	 * @return False if the connection failed or if already connected
	 * @param config DatabaseConfig to connect with
	 */
	private static boolean initConnection(DatabaseConfig config) {
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
		} catch (Exception e) {
			Logger.err.println("Mysql driver not found!");
			return false;
		}
		
		if (sqlConnection==null) {
			try {
				sqlConnection = DriverManager.getConnection("jdbc:mysql://"+config.host+":3306/"+config.db, config.username, config.password);
			} catch (SQLException e) {
				Logger.err.println("Could not connect to "+config.host+"/"+config.db+" using login "+config.username);
				e.printStackTrace();
				return false;
			}
			
			// Connected OK!
			return initDatabaseTables();
		}
		return false;
	}
	
	/**
	 * Creates any tables not initialised in the database.
	 * @return 
	 */
	private static boolean initDatabaseTables() {
		try {
			HashSet<String> rows=new HashSet<>();
			ResultSet rs=sqlConnection.prepareStatement("SHOW TABLE STATUS").executeQuery();
			while (rs.next()) rows.add(rs.getString("Name"));
			rs.close();

			if (!rows.contains("players")) {
				Logger.out.println("Creating new players table...");
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
				Logger.out.println("Created new players table");
			}
			if (!rows.contains("config")) {
				Logger.out.println("Creating new config table...");
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
				Logger.out.println("Created new config table");
			}
		} catch (SQLException e) {
			Logger.err.println("Database init failed!");
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/**
	 * Loads database config into returned object, assuming tables are valid.
	 * @return Returns null if there was a problem
	 */
	public static GlobalConfig loadDatabaseConfig() {
		if (sqlConnection==null) return null;
		
		GlobalConfig config=new GlobalConfig();
		String configFieldName=null;
		try {
			ResultSet rs=sqlConnection.prepareStatement("SELECT * FROM `config`").executeQuery();
			while (rs.next()) {
				configFieldName=rs.getString("name");
				Field field=config.getClass().getField(configFieldName);
				switch (field.getType().getName()) {
				case "int": field.set(config, rs.getInt("value")); break;
				default:
					throw new Exception("Field type not recognised! ("+field.getType()+")");
				//case "java.lang.String": break;
				}
			}
		} catch (Exception e) {
			Logger.err.println("Error loading config! Field not parsed correctly: \""+configFieldName+"\"");
			e.printStackTrace();
			return null;
		}
		return config;
	}

	/**
	 * Writes a config file using the DatabaseConfig
	 * @param file File writing to
	 * @param config Config being written
	 * @throws IOException
	 */
	private static void writeConfig(File file, DatabaseConfig config) throws IOException {
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
	private static DatabaseConfig readConfig(File file) throws IOException {
		String json=new String(Files.readAllBytes(file.toPath()));
		DatabaseConfig config=new Gson().fromJson(json, DatabaseConfig.class);
		return config;
	}

	public static class DatabaseConfig {
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
						Logger.err.println("Database ping failed!");
					}
				}
				try {
					Thread.sleep(150000); // 150 sec
				} catch (InterruptedException e) {return;}
			}
		}
	}
}
