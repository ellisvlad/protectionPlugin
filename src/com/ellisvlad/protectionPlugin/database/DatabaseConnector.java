package com.ellisvlad.protectionPlugin.database;

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

import com.ellisvlad.protectionPlugin.Logger;
import com.ellisvlad.protectionPlugin.config.GlobalConfig;
import com.ellisvlad.protectionPlugin.config.saveToDatabase;
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
				sqlConnection = DriverManager.getConnection("jdbc:mysql://"+config.host+":3306/"+config.db+"?characterEncoding=UTF-8", config.username, config.password);
			} catch (SQLException e) {
				Logger.err.println("Could not connect to "+config.host+"/"+config.db+" using login "+config.username);
				e.printStackTrace();
				return false;
			}
			
			// Connected OK!
			if (!initDatabaseTables()) return false;
			new Thread(new KeepAlive(sqlConnection), "Mysql Pinger").start();
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
					+ "`tool_id` VARCHAR(50) NOT NULL COMMENT 'Tool id used for regions', "
					+ "`tool_data` SMALLINT NOT NULL COMMENT 'Tool damage value used for regions', "
					+ "PRIMARY KEY (`pid`),"
					+ "UNIQUE INDEX `uuidLo_uuidHi` (`uuidLo`, `uuidHi`)"
					+ ")"
				).executeUpdate();
				Logger.out.println("Created new players table");
			}
			if (!rows.contains("config")) {
				Logger.out.println("Creating new config table...");
				sqlConnection.prepareStatement(
					"CREATE TABLE `config` ("
					+ "`name` VARCHAR(255) NOT NULL,"
					+ "`value` VARCHAR(1024) NOT NULL COLLATE 'utf8_general_ci'"
					+ ")"
				).executeUpdate();
				PreparedStatement configStatement=sqlConnection.prepareStatement(
					"INSERT INTO `config` (`name`, `value`) VALUES (?, ?)"
				);
				configStatement.setString(1, "default_tool_name");
				configStatement.setString(2, "WOOL"); //TODO: Wool.. 271
				configStatement.addBatch();
				configStatement.setString(1, "default_tool_data");
				configStatement.setString(2, "4"); //TODO: Yellow.. -1
				configStatement.addBatch();
				configStatement.setString(1, "welcome_message");
				configStatement.setString(2, 
					  "§4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §nProtection Plugin§r §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥\n\n"
					+ "  §oWritten by Ellis for WrathPVP\n"
					+ "§4⬥ §6⬦ §4⬦ §6⬦ §4⬦ §6⬦ §4⬦ §6⬦ §4⬦ §6⬦ §4⬦ §6⬦ §4⬦ §6⬦ §4⬦ §6⬦ §4⬦ §6⬦ §4⬦ §6⬦ §4⬦ §6⬦ §4⬦ §6⬦ §4⬦ §6⬦ §4⬦ §6⬦ §4⬦ §6⬦ §4⬦ §6⬦ §4⬦ §6⬦ §4⬦ §6⬦ §4⬦ §6⬦ §4⬥\n"
					+ "  §oUse §6§o/p§7§o[rotect]§r§o to get started!\n"
					+ "§4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥\n"
				);
				configStatement.addBatch();
				configStatement.setString(1, "help_message");
				configStatement.setString(2, 
					  "§4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §nHelp§r §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥\n\n"
					+ "  §oWritten by Ellis for WrathPVP\n"
					+ "§4⬥ §6⬦ §4⬦ §6⬦ §4⬦ §6⬦ §4⬦ §6⬦ §4⬦ §6⬦ §4⬦ §6⬦ §4⬦ §6⬦ §4⬦ §6⬦ §4⬦ §6⬦ §4⬦ §6⬦ §4⬦ §6⬦ §4⬦ §6⬦ §4⬦ §6⬦ §4⬦ §6⬦ §4⬦ §6⬦ §4⬦ §6⬦ §4⬦ §6⬦ §4⬦ §6⬦ §4⬦ §6⬦ §4⬥\n"
					+ "  §oUse §6§o/p§7§o[rotect]§r§o to get started!\n"
					+ "§4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥\n"
				);
				configStatement.addBatch();
				configStatement.setString(1, "tool_name");
				configStatement.setString(2, "§6Protection Tool");
				configStatement.addBatch();
				configStatement.setString(1, "tool_description");
				configStatement.setString(2,
					  "Used for selecting regions\n"
					+ "Left click to select region\n"
					+ "Right click for gui"
				);
				configStatement.addBatch();
				configStatement.setString(1, "first_point_selected");
				configStatement.setString(2, "First point selected");
				configStatement.addBatch();
				configStatement.setString(1, "second_point_selected");
				configStatement.setString(2, "Second point selected");
				configStatement.addBatch();
				configStatement.setString(1, "cleared_selection");
				configStatement.setString(2, "Cleared selection");
				configStatement.addBatch();
				configStatement.executeBatch();
				configStatement.close();
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
				if (field.getAnnotation(saveToDatabase.class)==null) {
					throw new Exception("Field is not a value that can be loaded! ("+configFieldName+")");
				}
				switch (field.getType().getName()) {
				case "int":					field.set(config, rs.getInt("value"));		break;
				case "java.lang.String":	field.set(config, rs.getString("value"));	break;
				default:
					throw new Exception("Field type not recognised! ("+field.getType()+")");
				}
			}
		} catch (Exception e) {
			Logger.err.println("Error loading config! Field not parsed correctly: \""+configFieldName+"\"");
			e.printStackTrace();
			return null;
		}
		config.validateConfig();
		return config;
	}
	
	/**
	 * Saves database config assuming tables are valid.
	 * @return Returns false if there was a problem
	 */
	public static boolean saveDatabaseConfig(GlobalConfig config) {
		if (sqlConnection==null) return false;
		
		String configFieldName=null;
		try {
			PreparedStatement updateStatement=sqlConnection.prepareStatement("UPDATE `config` SET `value`=? WHERE `name`=? LIMIT 1");
			for (Field field:config.getClass().getFields()) {
				if (field.getAnnotation(saveToDatabase.class)==null) continue;
				updateStatement.setString(1, (String)field.get(config));
				updateStatement.setString(2, field.getName());
				updateStatement.addBatch();
			}
			updateStatement.executeBatch();
			updateStatement.close();
		} catch (Exception e) {
			Logger.err.println("Error saving config! Field not parsed correctly: \""+configFieldName+"\"");
			e.printStackTrace();
			return false;
		}
		return true;
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

	public static PreparedStatement prepareStatement(String sql) throws Exception {
		if (sqlConnection==null) throw new Exception("Preparing query failed! Not connected to a database!");
		return sqlConnection.prepareStatement(sql);
	}
}
