package org.halodoc.dc.catalog.helpers;

import com.github.javafaker.Faker;
import com.halodoc.utils.http.RestClient;
import com.halodoc.utils.json.JsonUtils;

import java.util.HashMap;

import static io.dropwizard.testing.FixtureHelpers.fixture;

public class BaseHelper {
    public static final String FILE_LOCATION = "fixtures";
    Faker faker = new Faker();

    public HashMap<String, String> getAppTokenHeaders(String appToken) {
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("x-app-token", appToken);
        headers.put("Content-Type", "application/json");
        return headers;
    }

    public String getRequestFixture( String fileName) {
        return fixture(FILE_LOCATION + "/" + fileName);
    }
}
