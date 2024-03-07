package org.halodoc.dc.catalog.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.sql.*;
import java.util.*;

/**
 * @author Sudarshan Hegde
 * This class contains the required sql query execution methods.
 */

@Slf4j
public class DbUtilities {


    public Connection getDbConnection(String dbPropertiesFilePath) throws Exception {
        Properties prop =loadDbProperties(dbPropertiesFilePath);
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(prop.getProperty("db.url"), prop.getProperty("db.user"), prop.getProperty("db.pass"));
        } catch (SQLException var6) {
            System.out.println("Exception occured while connecting to db using jdbc driver management.");
            var6.printStackTrace();
        }
        return conn;
    }

    private Properties loadDbProperties(String dbPropertiesFilePath) throws Exception{
        Properties prop = new Properties();
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        InputStream resourceStream = loader.getResourceAsStream(dbPropertiesFilePath);
        prop.load(resourceStream);
        return prop;
    }

    public List<Map<String, Object>> getDbDataByQuery(String getQuery, Connection conn) {
        List<Map<String, Object>> queryResult = new ArrayList<>();
        try {
            ResultSet rs = executeQuery(getQuery, conn);
            ResultSetMetaData rsmd = rs.getMetaData();
            int columncount = rsmd.getColumnCount();
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= columncount; i++) {
                    row.put(rsmd.getColumnName(i), rs.getObject(i));
                }
                queryResult.add(row);
            }
            rs.close();
        } catch (SQLException e) {
            System.out.println("Exception caught while executing Query to fetching db data: " + e);
            e.printStackTrace();
        }
        return queryResult;
    }

    private  ResultSet executeQuery(String sqlStatement, Connection conn) {
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

    public void updateStageDbData(String query, Connection conn){
        try {
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(query);
        }catch(Exception e){
            log.error(e.getMessage());
        }
    }

    public void closeConnection(Connection conn){
        try {
            conn.close();
        } catch (SQLException e) {
            log.error("Exception occured while closing the connection: "+e.getMessage());
        }
    }

    public void deleteStageDbData(String query, Connection conn){
        try {
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(query);
        }catch(Exception e){
            log.error(e.getMessage());
        }
    }


}
