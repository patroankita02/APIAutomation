package org.halodoc.dc.catalog.helpers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.halodoc.utils.http.RestClient;
import com.halodoc.utils.json.JsonUtils;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.halodoc.dc.catalog.utils.DbUtilities;
import org.testng.Assert;

import java.io.IOException;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.halodoc.dc.catalog.utils.Constants.*;


@Slf4j
public class DigitalClinicCatalogHelper extends BaseHelper {

    private HashMap<String, String> headers = getAppTokenHeaders(VALID_DC_APP_TOKEN);
    private DbUtilities dbUtilities= new DbUtilities();
    Connection digitalClinicDBConnection = dbUtilities.getDbConnection(DIGITAL_CLINIC_DB_PROPERTIES_FILE_PATH);
    Map<String, Object> tempMap = new HashMap<>();

    public DigitalClinicCatalogHelper() throws Exception {
    }

    public Response createDCCategory(String status, int categoryLevel,Integer parentId) throws Exception {
        RestClient client = new RestClient(DC_CATALOG_BASE_URL+CREATE_DC_CATEGORY, headers);
        String request = getCreateDCCategoryRequest(status,categoryLevel,parentId);
        log.info("create category Request: "+request);
        tempMap.put("crateCategoryRequest",request);
        return client.executePost(request);

    }

    public String getCreateDCCategoryRequest(String status, int categoryLevel, Integer parentId) throws IOException {
        String jsonBody = getRequestFixture("dc_catalog/createDCCategory.json");
        JsonNode jsonNode =JsonUtils.convertJsonToString(jsonBody);
        ( (ObjectNode) jsonNode).put("name",faker.ancient().god());
        ( (ObjectNode) jsonNode).put("code",faker.random().hex());
        ( (ObjectNode) jsonNode).put("status",status);
        if(parentId!=null){
            ( (ObjectNode) jsonNode).put("parent_id",parentId);
        }
        ( (ObjectNode) jsonNode).put("category_level",categoryLevel);
        return jsonNode.toString();
    }

    public void verifyCreateDCCategory(Response response, int expectedResponseCode) throws IOException {
        verifyCreateDCCategoryResponse(response, expectedResponseCode);
        validateDCCategoryInDB(JsonUtils.convertJsonToString(response.asString()));
    }


    private void verifyCreateDCCategoryResponse(Response response, int expectedResponseCode) throws IOException {
        Assert.assertEquals(response.getStatusCode(), expectedResponseCode);
        if(expectedResponseCode!=201 || expectedResponseCode!=200){
            return;
        }
        JsonNode expectedJsonNode = JsonUtils.convertJsonToString(tempMap.get("crateCategoryRequest").toString());
        JsonNode actualJsonNode = JsonUtils.convertJsonToString(response.asString());

        Assert.assertEquals(actualJsonNode.get("name").asText(),expectedJsonNode.get("name").asText());
        Assert.assertEquals(actualJsonNode.get("code").asText(),expectedJsonNode.get("code").asText());
        Assert.assertEquals(actualJsonNode.get("status").asText(),expectedJsonNode.get("status").asText());
        Assert.assertEquals(actualJsonNode.get("category_level").asInt(),expectedJsonNode.get("category_level").asInt());
        if( actualJsonNode.has("parent_id")){
            Assert.assertEquals(actualJsonNode.get("parent_id").asInt(),expectedJsonNode.get("parent_id").asInt());
        }

    }

    private void validateDCCategoryInDB(JsonNode actualJsonNode) {
        String query = "select * from categories where id = "+actualJsonNode.get("id").asInt();

        List<Map<String, Object>> dbResponse = dbUtilities.getDbDataByQuery(query,digitalClinicDBConnection);
        Assert.assertEquals(dbResponse.size(),1);
        Assert.assertEquals(dbResponse.get(0).get("name"),actualJsonNode.get("name").asText());
        Assert.assertEquals(dbResponse.get(0).get("code"),actualJsonNode.get("code").asText());
        Assert.assertEquals(dbResponse.get(0).get("status"),actualJsonNode.get("status").asText());
        Assert.assertEquals(dbResponse.get(0).get("category_level"),actualJsonNode.get("category_level").asInt());
        if(actualJsonNode.has("parent_id") && actualJsonNode.get("parent_id").asText()!="null"){
            Assert.assertEquals(dbResponse.get(0).get("parent_id"),actualJsonNode.get("parent_id").asInt());
        }
    }

    public void closeDBConnection(){
        dbUtilities.closeConnection(digitalClinicDBConnection);
    }
}
