package gov.epa.mims.analysisengine.table.db;

import java.sql.*;
import java.util.HashMap;

public class DBManager {

	private static HashMap connections = new HashMap(10);

	public static Connection openConnection(String dbName) throws Exception {
		Connection connection = (Connection) connections.get(dbName);
		if (connection != null && !connection.isClosed())
			return connection;
		// Load the DB driver.
		Class.forName("com.mysql.jdbc.Driver");

		if (dbName == null)
			dbName = "";
		connection = DriverManager.getConnection("jdbc:mysql://localhost/" + dbName, "root", "");

		if (connection != null) {
			connections.put(dbName, connection);
		}
		return connection;
	}

	public static void closeConnection(String dbName) throws Exception {
		Connection connection = openConnection(dbName);
		if (connection != null)
			connection.close();
	}

	public static void printResultSet(ResultSet rs) throws Exception {
		try {
			rs.beforeFirst();
			int columns = rs.getMetaData().getColumnCount();
			while (rs.next()) {
				for (int i = 1; i < columns; i++)
					System.out.print(rs.getString(i) + "          ");
				System.out.println();
			}// while(rs.next())
		}// try
		catch (Exception e) {
			throw new Exception("Error in printing the data sets: " + e.getMessage());
		}
	}

	public static String[] getDatabaseNames() throws Exception {
		// open the connection to the database .. an error might be thrown here
		// AME: previously, if the selected database didn't exist, this didn't work
		// instead, open without the database

		Connection connection = openConnection("");
		if (connection == null || connection.isClosed()) {
			// Load the DB driver.
			Class.forName("com.mysql.jdbc.Driver");
			connection = DriverManager.getConnection("jdbc:mysql://localhost/", "root", "");
		}
		// execute the "show databases" query .. this could throw an error too
		ResultSet rs = connection.createStatement().executeQuery("show databases");
		// to get the number of rows in the resultset.. move the pointer to the
		// last row and then get the row number.. this way we can allocate the
		// size of the dbNames string array
		rs.last();
		String[] dbNames = new String[rs.getRow()];
		rs.beforeFirst();
		int i = 0;
		while (rs.next()) {
			dbNames[i] = rs.getString(1);
			i++;
		}
		// closeConnection();
		rs.close();
		return dbNames;
	}

	/**
	 * To retrive the table names in a particular Database
	 */
	public static String[] getTableNames(String databaseName) throws Exception {
		Connection connection = openConnection(databaseName);
		if (connection == null || connection.isClosed()) {
			connection = openConnection(databaseName);
		}
		if (connection == null) {
			throw new Exception("Could not open database " + databaseName);
		}
		// execute the "show databases" query .. this could throw an error too
		ResultSet rs = connection.createStatement().executeQuery("SHOW tables");
		// to get the number of rows in the resultset.. move the pointer to the
		// last row and then get the row number.. this way we can allocate the
		// size of the dbNames string array
		rs.last();
		String[] tableNames = new String[rs.getRow()];
		rs.beforeFirst();
		int i = 0;
		while (rs.next()) {
			tableNames[i] = rs.getString(1);
			i++;
		}
		// set the previous database again
		// AME: should not be needed anymore openConnection(oldDatabaseName);
		// closeConnection();
		rs.close();
		return tableNames;
	}

	/**
	 * Helper method to get distinct values for a particular column in a table
	 * 
	 * @param tableColumn
	 *            the combination of table and column where to get the results from
	 * @return a list of distinct values
	 * @throws Exception
	 */
	public static String[] getDistinctValues(String dbName, String tableColumn) throws Exception {
		Connection connection = openConnection(dbName);
		try {
			// if a connection does not exist or is closed, open a new one
			if (connection == null || connection.isClosed()) {
				connection = openConnection(dbName);
			}// if (connection == null || connection.isClosed())
			ResultSet rs = connection.createStatement().executeQuery(
					"SELECT DISTINCT " + tableColumn + " FROM " + tableColumn.substring(0, tableColumn.indexOf('.')));
			rs.last();
			String[] distinctValues = new String[rs.getRow()];
			rs.beforeFirst();
			int i = 0;
			while (rs.next()) {
				distinctValues[i] = rs.getString(1);
				i++;
			}// while(rs.next())
			rs.close();
			return distinctValues;
		} catch (SQLException sqle) {
			// if an exception occurs while executing the query or creating a
			// connection
			throw new Exception("Error occurred while getting distinct values from " + tableColumn + "\n"
					+ sqle.getMessage());
		}
	}

	/**
	 * check whether tables of the dbName data base contains data
	 * 
	 * @param dbTables
	 *            String [] array of required table not all the table in the database
	 * @see DaveContants DB_TABLE...
	 * @param dbName
	 *            name of the database
	 */
	public static boolean doTablesContainData(String[] dbTables, String dbName) throws Exception {
		Connection connection = (Connection) connections.get(dbName);
		if (connection == null || connection.isClosed()) {
			connection = openConnection(dbName);
		}

		for (int i = 0; i < dbTables.length; i++) {
			try {
				doesTableContainData(dbName, dbTables[i]);
			} catch (Exception e) {
				throw new Exception(e.getMessage() + " in the database " + dbName);
			}
		}// for i
		return true;
	}// doTablesContainData

	/**
	 * a helper method to check a table contain a data
	 * 
	 * @param tableName
	 */
	private static void doesTableContainData(String dbName, String tableName) throws Exception {
		Connection connection = (Connection) connections.get(dbName);

		// HACK to handle case of empty races table - proper fix is not to check
		// for races if it's not needed
		if (tableName.equalsIgnoreCase("races"))
			return;

		ResultSet rs = connection.createStatement().executeQuery("Select count(*) from " + tableName);
		rs.beforeFirst();
		rs.next();
		// just a note that instead of the below line you can use
		// if (rs.getInt(1) > 0)
		if (!(Integer.valueOf(rs.getString(1)).intValue() > 0)) {
			rs.close();
			throw new Exception("No values were found in table " + tableName);
		}// if
		rs.close();
	}// doesTableContainData(String tableName)

	// /**
	// * @param dbName
	// * @return String The database type for the specified dbName, or null if
	// * no match is found
	// */
	// public static String findDatabaseType(String dbName) throws Exception
	// {
	// String [] dbNameTables;
	// try
	// {
	// dbNameTables = DBManager.getTableNames(dbName);
	// }
	// catch (Exception exc)
	// {
	// throw new Exception ("No tables are available for database "+dbName);
	// }
	//
	// //if a db has tables check whether it's a TRIM database and store it in
	// // two vectors
	// if(dbNameTables != null)
	// {
	// for(int j=0; j< DaveConstants.AVAIL_DBS_TYPES.length; j++)
	// {
	// if(DaveConstants.AVAIL_DBS_TYPES[j] != DaveConstants.ALL_TRIM)
	// {
	// String [] dbTypeTables = DaveConstants.getTables(
	// DaveConstants.AVAIL_DBS_TYPES[j]);
	//
	// if(checkTables(dbTypeTables, dbNameTables))
	// {
	// return DaveConstants.AVAIL_DBS_TYPES[j];
	// }//if(checkTables(dbTypeTables, dbNameTables))
	// }//if(DaveConstants.AVAIL_DBS_TYPES[j] != DaveConstants.ALL_TRIM)
	// }//for int j
	// }//if(dbNameTables != null)
	// return null;
	// }

	/**
	 * Create the table using the header
	 * 
	 * @param colNames
	 *            the column names
	 * @param colTypes
	 *            the column types
	 * @param primaryCol
	 *            the primary Col
	 * @param auto
	 *            whether the primary Col should be auto increment
	 * @throws Exception
	 *             when there are different numbers of colNames and colTypes
	 */
	public static void createTable(String dbName, String table, String[] colNames, String[] colTypes,
			String primaryCol, boolean auto) throws Exception {
		// if(!dbName.equals(System.getProperty("DB_NAME")))
		// {
		// DefaultUserInteractor.get().notify("Error", "The user selected "+ dbName
		// + " database, but it's not selected in the back end.",UserInteractor.ERROR);
		// }
		Connection conection = openConnection(dbName);
		// check to see if there are the same number of column names and column types
		if (colNames.length != colTypes.length)
			throw new Exception("There are different numbers of column names and types");

		String queryString = "CREATE TABLE IF NOT EXISTS " + dbName + "." + table + " (";
		for (int i = 0; i < colNames.length; i++) {
			queryString = queryString + (colNames[i]) + " " + colTypes[i]
					+ (colNames[i].equals(primaryCol) ? " PRIMARY KEY " + (auto ? " AUTO_INCREMENT, " : ", ") : ", ");
		}// for i
		// to get rid last two charaters ', "
		queryString = queryString.substring(0, queryString.length() - 2) + ")";
		System.out.println("create table : " + queryString);
		conection.createStatement().execute(queryString);
	}// createTable()

	/**
	 * check whether a table exist with in a database ASSUMPTION tableName string should be exact name of the file
	 * should not have any NO WILD CARD characters
	 * 
	 * @param database
	 *            name
	 * @param table
	 *            name
	 * @return true if exist and false if not
	 */
	public static boolean isTableExist(String dbName, String tableName) throws Exception {
		Connection connection = openConnection(dbName);
		System.out.println("tableName@isTableExist=" + tableName);
		String query = "SHOW TABLES LIKE \"" + tableName + "\"";
		ResultSet rs = connection.createStatement().executeQuery(query);
		rs.beforeFirst();
		if (!rs.next()) {
			return false;
		}
		return true;
	}

	/**
	 * delete a table
	 * 
	 * @param dbName
	 *            data base name
	 * @param tableName
	 *            table to be deleted
	 */
	public static void deleteTable(String dbName, String tableName) throws Exception {
		if (isTableExist(dbName, tableName)) {
			Connection connection = openConnection(dbName);
			String query = "DROP TABLE " + tableName;
			connection.createStatement().execute(query);
		}
	}

	/**
	 * copy one table to another table. table 2 is empty
	 * 
	 * @param dbName
	 *            where table will be created
	 * @param origTableName
	 *            where data will be copied from
	 * @param newTableName
	 *            table will be created data will be copied to
	 */
	public static void copyTable(String dbName, String origTableName, String newTableName) throws Exception {
		Connection conection = openConnection(dbName);
		String query = "CREATE TABLE " + newTableName + " SELECT * FROM " + origTableName;
		System.out.println("query=" + query);
		conection.createStatement().execute(query);
	}// copyTable

	/**
	 * A helper method to retreive the info from metrics table of each type WARNING: This method is written with the
	 * knowlege of the metrics table schema desCol=description, metricsDescCol='metric_desc' metricsName='acr_risk' OR
	 * 'sci'.... WARNING:If used for other table, won't give any useful results.
	 * 
	 * @param desTable
	 *            a table from description will be fetched
	 * @param desCol
	 *            a column in which description is exist
	 * @param optionCol
	 *            to narrow down resultset or a column is used to specify a condition
	 * @param optionString
	 *            a String to match a string in optionCol
	 * @return String db info string
	 */
	public static String getDbInfo(String dbName, String desTable, String desCol, String metricsDescCol,
			String metricsName) throws Exception {
		ResultSet rs = null;
		String info = null;
		try {
			Statement statement = DBManager.openConnection(dbName).createStatement();
			rs = statement.executeQuery("SELECT " + desCol + " FROM " + desTable + " WHERE " + metricsDescCol + " = '"
					+ metricsName + "'");
			if (rs.next()) {
				info = rs.getString(desCol);
			}
		} catch (Exception e) {
			throw new Exception("Error in getting DB info for db=" + dbName + " and table=" + desTable);
		}
		return info;

	}

}
