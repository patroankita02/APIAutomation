package org.halodoc.dc.catalog.helpers;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import com.halodoc.utils.jdbc.DbUtilities;

import java.sql.Connection;
import java.util.HashMap;

import static io.dropwizard.testing.FixtureHelpers.fixture;
import static org.halodoc.dc.catalog.utils.Constants.DIGITAL_CLINIC_DB_PROPERTIES_FILE_PATH;
import static org.halodoc.dc.catalog.utils.Constants.WAKATOBI_CATALOG_DB_PROPERTIES_FILE_PATH;
import static org.halodoc.dc.catalog.utils.Constants.TIMOR_CMS_DB_PROPERTIES_FILE_PATH;

public abstract class BaseHelper {
    public static final String FILE_LOCATION = "fixtures";



    Faker faker = new Faker();
    ObjectMapper objectMapper= new ObjectMapper();
    public DbUtilities dbUtilities= new DbUtilities();
    public Connection digitalClinicDBConnection = dbUtilities.getDbConnection(DIGITAL_CLINIC_DB_PROPERTIES_FILE_PATH);
    public Connection wakatobiCatalogDBConnection = dbUtilities.getDbConnection(WAKATOBI_CATALOG_DB_PROPERTIES_FILE_PATH);
    public Connection timorCMSDBConnection = dbUtilities.getDbConnection(TIMOR_CMS_DB_PROPERTIES_FILE_PATH);

    public BaseHelper() throws Exception {
    }

    public HashMap<String, String> getAppTokenHeaders(String appToken) {
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("x-app-token", appToken);
        headers.put("Content-Type", "application/json");
        return headers;
    }

    public String getRequestFixture( String fileName) {
        return fixture(FILE_LOCATION + "/" + fileName);
    }

    public void closeDBConnection() {
        dbUtilities.closeConnection(digitalClinicDBConnection);
        dbUtilities.closeConnection(wakatobiCatalogDBConnection);
        dbUtilities.closeConnection(timorCMSDBConnection);
    }
}
