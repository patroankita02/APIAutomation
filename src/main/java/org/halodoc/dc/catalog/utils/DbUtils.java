package org.halodoc.dc.catalog.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.*;

/**
 * @author Sudarshan Hegde
 * This class contains the required sql query execution methods.
 */

@Slf4j
public class DbUtils {
    public static DbUtils dbUtilsInstance = null;
    private Connection conn;
    private static String DB_URL, DB_USERNAME, DB_PASSWORD;

    public static final String DB_PROPERTIES_FILE_PATH = "config/db.properties" ;

    private DbUtils(String url, String username, String password) {
        conn = getDbConnection(url, username, password);
    }

    public static DbUtils getDbUtilsInstance() {
        loadDbProperties();
        if (dbUtilsInstance == null) {
            dbUtilsInstance = new DbUtils(DB_URL, DB_USERNAME, DB_PASSWORD);
            log.info("jdbc connection establishment is success!");
        }
        return dbUtilsInstance;
    }

    private Connection getDbConnection(String url, String username, String password) {
        Connection conn = null;

        try {
            conn = DriverManager.getConnection(url, username, password);
        } catch (SQLException var6) {
            System.out.println("Exception occured while connecting to db using jdbc driver management.");
            var6.printStackTrace();
        }
        return conn;
    }

    private static void loadDbProperties() {
        Properties prop = new Properties();
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try {
            InputStream resourceStream = loader.getResourceAsStream(DB_PROPERTIES_FILE_PATH);
            prop.load(resourceStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        DB_URL = prop.getProperty("db.url");
        DB_USERNAME = prop.getProperty("db.user");
        DB_PASSWORD = prop.getProperty("db.pass");
    }

    public List<Map<String, Object>> getDbDataByQuery(String getQuery) {
        List<Map<String, Object>> queryResult = new ArrayList<>();
        try {
            ResultSet rs = executeGetQuery(getQuery);
            ResultSetMetaData rsmd = rs.getMetaData();
            int columncount = rsmd.getColumnCount();
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= columncount; i++) {
                    row.put(rsmd.getColumnName(i), rs.getObject(i));
                }
                queryResult.add(row);
            }
        } catch (SQLException e) {
            System.out.println("Exception caught while executing Query to fetching db data: " + e);
            e.printStackTrace();
        }
        return queryResult;
    }

    private  ResultSet executeGetQuery(String sqlStatement) {
        ResultSet resultSet = null;
        try {
            Statement statement = conn.createStatement();
            resultSet = statement.executeQuery(sqlStatement);
        } catch (SQLException var6) {
            System.out.println("Exception occured while executing GET Query: " + sqlStatement + "\n" + var6.getMessage());
            var6.printStackTrace();
        }
        return resultSet;
    }

    public int executeUpdateOrDeleteQuery(String sqlStatement) {
        int updatedOrDeletedRow = 0;
        try {
            Statement statement = conn.createStatement();
            updatedOrDeletedRow = statement.executeUpdate(sqlStatement);
        } catch (SQLException var6) {
            System.out.println("Exception occured while executing UPDATE/DELETE Query: " + sqlStatement + "\n" + var6.getMessage());
            var6.printStackTrace();
        }
        return updatedOrDeletedRow;
    }

    public void closeDbConnection() {
        try {
            if (conn != null) {
                conn.close();
            }
            dbUtilsInstance = null;
            System.out.println("jdbc Connection is closed");
        } catch (SQLException var3) {
            System.out.println("Connection Failed due to " + var3.getMessage());
            var3.printStackTrace();
        }

    }
}
