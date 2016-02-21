package com.ellisvlad.protectionPlugin.Database;

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
import com.ellisvlad.protectionPlugin.Regions.RegionPermissions;
import com.ellisvlad.protectionPlugin.Regions.RegionPermissions.RegionPermissionsBooleanValue;
import com.ellisvlad.protectionPlugin.Regions.RegionPermissions.RegionPermissionsGroupValue;
import com.ellisvlad.protectionPlugin.config.GlobalConfig;
import com.ellisvlad.protectionPlugin.config.saveToDatabase;
import com.google.gson.Gson;

public final class DatabaseConnection {
	
	private static final String configFileName = "ProtectionSqlConnection.json";

	private Connection sqlConnection = null;

	/**
	 * Connects to a database using provided database information.
	 * @return Returns false if there was an error
	 * @param host Host ip
	 * @param db Database name
	 * @param u Username
	 * @param p Password
	 */
	public DatabaseConnection(DatabaseConfig config) {
		initConnection(config);
	}
	
	/**
	 *  Loads connection info from file, making a default config file if one doesn't exist.
	 *  Then connects to the database.
	 */
	public DatabaseConnection() {
		if (isConnected()) return;
		
		File file = new File(configFileName);
		if (!file.exists()) {
			try {
				writeConfig(file, new DatabaseConfig());
			} catch (IOException e) {
				Logger.err.println("Failed to create default database config!");
				e.printStackTrace();
				return;
			}
		}
		
		DatabaseConfig config = null;
		try {
			config = readConfig(file);
		} catch (IOException e) {
			Logger.err.println("Failed to load database config!");
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
		if (isConnected()) return false;
		
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
		} catch (Exception e) {
			Logger.err.println("Mysql driver not found!");
			return false;
		}
		
		if (sqlConnection==null) {
			try {
				sqlConnection = DriverManager.getConnection("jdbc:mysql://"+config.host+":3306/"+config.db+"?characterEncoding=UTF-8&amp;autoReConnect=true", config.username, config.password);
			} catch (SQLException e) {
				Logger.err.println("Could not connect to "+config.host+"/"+config.db+" using login "+config.username);
				e.printStackTrace();
				return false;
			}
			
			// Connected OK!
			if (!initDatabaseTables()) return false;
		}
		return false;
	}
	
	/**
	 * Creates any tables not initialised in the database.
	 * @return 
	 */
	private boolean initDatabaseTables() {
		if (!isConnected()) return false;
		
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
					+ "  §6§o/p§7§orotect help§r Show this help\n"
					+ "  §6§o/p§7§orotect §6§ot§7§oool§r Aquire tool\n"
					+ "§4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥\n"
					+ "  §7Left click§r to select region\n"
					+ "  §7Shift + Left click§r to confirm a region\n"
					+ "  §7Right click§r to push a boundry\n"
					+ "  §7Shift + Right click§r for gui\n"
					+ "§4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥"
				);
				configStatement.addBatch();
				configStatement.setString(1, "tool_name");
				configStatement.setString(2, "§6Protection Tool");
				configStatement.addBatch();
				configStatement.setString(1, "tool_description");
				configStatement.setString(2,
					  "Used for selecting regions\n"
					+ "§7Left click§r to select region\n"
					+ "§7Shift + Left click§r to confirm a region\n"
					+ "§7Right click§r to push a boundry\n"
					+ "§7Shift + Right click§r for gui"
				);
				configStatement.addBatch();
				configStatement.setString(1, "first_point_selected");
				configStatement.setString(2, "Now select a second point");
				configStatement.addBatch();
				configStatement.setString(1, "second_point_selected");
				configStatement.setString(2,
						  "Cool, you have two options now:\n"
						+ "§7Shift+Left Click§r to create this region\n"
						+ "§7Left Click§r to clear the selection");
				configStatement.addBatch();
				configStatement.setString(1, "cleared_selection");
				configStatement.setString(2, "Cleared selection");
				configStatement.addBatch();
				configStatement.setString(1, "created_region");
				configStatement.setString(2, "§6Created a region from {pos1} to {pos2} of size {size} named {name}!");
				configStatement.addBatch();
				configStatement.executeBatch();
				configStatement.setString(1, "already_is_region");
				configStatement.setString(2, "§4There already is a region here!");
				configStatement.addBatch();
				configStatement.executeBatch();
				configStatement.setString(1, "regionSaveInterval");
				configStatement.setString(2, "20000");
				configStatement.addBatch();
				configStatement.executeBatch();
				configStatement.close();
				Logger.out.println("Created new config table");
			}
			if (!rows.contains("regions")) {
				Logger.out.println("Creating new regions table...");
				try {
					RegionPermissions perms=new RegionPermissions();
					String str="CREATE TABLE `regions`("
							+ "`rid` INT(10) UNSIGNED NOT NULL AUTO_INCREMENT,"
							+ "`pid` INT(10) UNSIGNED NOT NULL,"
							+ "`minX` INT(11) NOT NULL,"
							+ "`maxX` INT(11) NOT NULL,"
							+ "`minZ` INT(11) NOT NULL,"
							+ "`maxZ` INT(11) NOT NULL,"
							+ "`name` VARCHAR(16) NOT NULL,"
							+ "`greeting` VARCHAR(255) NOT NULL,"
							+ "`farewell` VARCHAR(255) NOT NULL,";
					for (Field f:perms.getClass().getDeclaredFields()) {
						if (f.get(perms) instanceof RegionPermissionsGroupValue) {
							str+="`"+f.getName()+"` ENUM('None','Owner','Members','All') NOT NULL,";
						}
						if (f.get(perms) instanceof RegionPermissionsBooleanValue) {
							str+="`"+f.getName()+"` BIT(1) NOT NULL,";
						}
					}
					str += "PRIMARY KEY (`rid`))";

					sqlConnection.prepareStatement(str).executeUpdate();
				} catch (Exception e) {
					Logger.err.println("Could not build the region table creator query!");
					throw e;
				}
			}
		} catch (Exception e) {
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
	public GlobalConfig loadDatabaseConfig() {
		if (!isConnected()) return null;
		
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
	public boolean saveDatabaseConfig(GlobalConfig config) {
		if (!isConnected()) return false;
		
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

	public PreparedStatement prepareStatement(String sql) throws Exception {
		if (!isConnected()) throw new Exception("Preparing query failed! Not connected to a database!");
		return sqlConnection.prepareStatement(sql);
	}
	
	public boolean isConnected() {
		return (sqlConnection!=null);
	}
}
