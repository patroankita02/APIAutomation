package org.halodoc.dc.catalog.helpers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
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
import java.util.concurrent.atomic.AtomicInteger;
import static org.halodoc.dc.catalog.utils.Constants.*;

@Slf4j
public class DigitalClinicCatalogHelper extends BaseHelper {
    private HashMap<String, String> headers = getAppTokenHeaders(VALID_DC_APP_TOKEN);

    Map<String, Object> tempMap = new HashMap<>();

    public String categoryExternalId;

    public String categoryName;

    public String categoryCode;

    public int categoryLevel;

    public Integer categoryParentId;

    public Integer categoryId;

    public String attributeName;

    public String attributeType;

    public String packageProductId;

    public String benefitPackageId;

    public DigitalClinicCatalogHelper() throws Exception {
    }

    public Response createDCCategory(String status, int categoryLevel, Integer parentId) throws Exception {
        RestClient client = new RestClient(DC_CATALOG_BASE_URL + CREATE_DC_CATEGORY, headers);
        String request = getCreateDCCategoryRequest(status, categoryLevel, parentId);
        log.info("create category Request: " + request);
        tempMap.put("crateCategoryRequest", request);
        return client.executePost(request);

    }

    public String getCreateDCCategoryRequest(String status, int categoryLevel, Integer parentId) throws IOException {
        String jsonBody = getRequestFixture("dc_catalog/createDCCategory.json");
        JsonNode jsonNode = JsonUtils.convertJsonToString(jsonBody);
        ((ObjectNode) jsonNode).put("name", faker.ancient().god());
        ((ObjectNode) jsonNode).put("code", faker.random().hex());
        ((ObjectNode) jsonNode).put("status", status);
        if (parentId != null) {
            ((ObjectNode) jsonNode).put("parent_id", parentId);
        }
        ((ObjectNode) jsonNode).put("category_level", categoryLevel);
        return jsonNode.toString();
    }

    public void verifyDCCategory(Response response, int expectedResponseCode) throws IOException {
        verifyCreateDCCategoryResponse(response, expectedResponseCode);
        validateDCCategoryInDB(JsonUtils.convertJsonToString(response.asString()));
    }

    private void verifyCreateDCCategoryResponse(Response response, int expectedResponseCode) throws IOException {
        Assert.assertEquals(response.getStatusCode(), expectedResponseCode);
        if (expectedResponseCode != 201 || expectedResponseCode != 200) {
            return;
        }
        JsonNode expectedJsonNode = JsonUtils.convertJsonToString(tempMap.get("crateCategoryRequest").toString());
        JsonNode actualJsonNode = JsonUtils.convertJsonToString(response.asString());

        Assert.assertEquals(actualJsonNode.get("name").asText(), expectedJsonNode.get("name").asText());
        Assert.assertEquals(actualJsonNode.get("code").asText(), expectedJsonNode.get("code").asText());
        Assert.assertEquals(actualJsonNode.get("status").asText(), expectedJsonNode.get("status").asText());
        Assert.assertEquals(actualJsonNode.get("category_level").asInt(), expectedJsonNode.get("category_level").asInt());
        if (actualJsonNode.has("parent_id")) {
            Assert.assertEquals(actualJsonNode.get("parent_id").asInt(), expectedJsonNode.get("parent_id").asInt());
        }

    }

    private void validateDCCategoryInDB(JsonNode actualJsonNode) {
        String query = "select * from categories where id = " + actualJsonNode.get("id").asInt();

        List<Map<String, Object>> dbResponse = dbUtilities.getDbDataByQuery(query, digitalClinicDBConnection);
        Assert.assertEquals(dbResponse.size(), 1);
        Assert.assertEquals(dbResponse.get(0).get("name"), actualJsonNode.get("name").asText(), "Name is not matching");
        Assert.assertEquals(dbResponse.get(0).get("code"), actualJsonNode.get("code").asText(), "Code is not matching");
        Assert.assertEquals(dbResponse.get(0).get("status"), actualJsonNode.get("status").asText(), "Status is not matching");
        Assert.assertEquals(dbResponse.get(0).get("category_level"), actualJsonNode.get("category_level").asInt(), "Category level is not matching");

        if (actualJsonNode.get("parent_id").asText() != "null") {

            Assert.assertEquals(Integer.valueOf(dbResponse.get(0).get("parent_id").toString()), actualJsonNode.get("parent_id").asInt(),
                    "Parent id is not matching");
        }
    }

    public Response getCategoryByCategoryExternalId(String categoryExternalId) {
        RestClient client = new RestClient(DC_CATALOG_BASE_URL + GET_DC_CATEGORY.replace("{categoryExternalId}", categoryExternalId), headers);
        Response response = client.executeGet();
        return response;
    }

    public Response updateDCCategory(String categoryExternalId, String status, String categoryName, String categoryCode, int categoryLevel,
            Integer categoryParentId) throws JsonProcessingException {
        String jsonBody = getRequestFixture("dc_catalog/updateDCCategory.json");

        JsonNode jsonNode = objectMapper.readTree(jsonBody);
        jsonNode = ((ObjectNode) jsonNode).put("external_id", categoryExternalId);
        ((ObjectNode) jsonNode).put("status", status);
        ((ObjectNode) jsonNode).put("name", categoryName);
        ((ObjectNode) jsonNode).put("description", faker.lorem().sentence());
        ((ObjectNode) jsonNode).put("code", categoryCode);
        ((ObjectNode) jsonNode).put("category_level", categoryLevel);
        ((ObjectNode) jsonNode).put("parent_id", categoryParentId);
        jsonBody = jsonNode.toString();

        String url = DC_CATALOG_BASE_URL + UPDATE_DC_CATEGORY.replace("{categoryExternalId}", categoryExternalId);
        RestClient client = new RestClient(url, headers);
        Response response = client.executePut(jsonBody);
        return response;
    }

    public void setDCCategoryValues(Response response) {
        categoryExternalId = response.jsonPath().get("external_id");
        categoryName = response.jsonPath().get("name");
        categoryCode = response.jsonPath().get("code");
        categoryLevel = response.jsonPath().get("category_level");
        categoryId = response.jsonPath().get("id");
        categoryParentId = response.jsonPath().get("parent_id");

    }

    public Response createDCPackageBenefitMapping(List<Map<String, Object>> packageBenefitMappingList) throws Exception {
        JsonNode jsonNode = objectMapper.valueToTree(packageBenefitMappingList);

        RestClient client = new RestClient(DC_CATALOG_BASE_URL + ADD_DC_PACKAGE_BENEFIT_MAPPING, headers);
        return client.executePost(jsonNode.toString());

    }

    public void verifyDCPackageBenefitMapping(List<Map<String, Object>> expectedPackageBenefitResponse, Response response, int expectedResponseCode)
            throws Exception {

        Assert.assertEquals(response.getStatusCode(), expectedResponseCode);
        if (expectedResponseCode != 201) {
            return;
        }
        AtomicInteger count = new AtomicInteger(0);
        ArrayNode actualPackageBenefitResponse = (ArrayNode) objectMapper.readTree(response.asString());
        expectedPackageBenefitResponse.stream().forEach(expectedResponse -> {
            actualPackageBenefitResponse.forEach(actualResponse -> {

                if (actualResponse.get("package_product_id").toString().replace("\"", "")
                                  .equals(expectedResponse.get("package_product_id").toString())) {
                    count.getAndIncrement();
                    Assert.assertEquals(actualResponse.get("benefit_package_id").toString().replace("\"", ""),
                            expectedResponse.get("benefit_package_id"));
                    Assert.assertEquals(actualResponse.get("status").toString().replace("\"", ""), expectedResponse.get("status"));
                    Assert.assertEquals(actualResponse.get("is_deleted").toString().replace("\"", ""), expectedResponse.get("is_deleted").toString());

                }
            });
        });
        assert count.get() == expectedPackageBenefitResponse.size();
        verifyDCPackageBenefitMappingInDB(expectedPackageBenefitResponse);
    }

    public void verifyDCPackageBenefitMappingInDB(List<Map<String, Object>> expectedPackageBenefitResponse) throws Exception {

        List<Map<String, Object>> packageBenefitMappingInDB = getDCPackageBenefitMappingInDB(packageProductId, benefitPackageId);

        AtomicInteger count = new AtomicInteger(0);

        expectedPackageBenefitResponse.stream().forEach(expectedPackageBenefit -> {
            packageBenefitMappingInDB.forEach(packageBenefitInDB -> {

                if (expectedPackageBenefit.get("package_product_id").equals(packageBenefitInDB.get("package_product_id"))) {
                    count.getAndIncrement();

                    Assert.assertEquals(packageBenefitInDB.get("status"), expectedPackageBenefit.get("status"));

                }
            });
        });

        Assert.assertEquals(count.get(), expectedPackageBenefitResponse.size());

    }

    private List<Map<String, Object>> getDCPackageBenefitMappingInDB(String packageProductId, String benefitPackageId) {
        List<Map<String, Object>> dbResponse = dbUtilities.getDbDataByQuery(
                "select * from package_benefit_mapping where package_product_id = '" + packageProductId + "' and benefit_package_id = '" + benefitPackageId + "'and status='active' and is_deleted=0",
                digitalClinicDBConnection);

        return dbResponse;
    }

    public Response updateDCPackageBenefitMapping(List<Map<String, Object>> packageBenefitMappingList) throws Exception {
        JsonNode jsonNode = objectMapper.valueToTree(packageBenefitMappingList);
        RestClient client = new RestClient(DC_CATALOG_BASE_URL + UPDATE_DC_PACKAGE_BENEFIT_MAPPING.replace("{package_product_id}", packageProductId),
                headers);

        tempMap.put("PackageBenefitRequest", packageBenefitMappingList);

        return client.executePut(jsonNode.toString());

    }

    public Response createDCMetaAttributes(List<Map<String, Object>> metaAttributes) throws Exception {
        JsonNode jsonNode = objectMapper.valueToTree(metaAttributes);
        RestClient client = new RestClient(DC_CATALOG_BASE_URL + CREATE_DC_META_ATTRIBUTES, headers);
        return client.executePost(jsonNode.toString());

    }

    public void verifyDCMetaAttributes(List<Map<String, Object>> expectedMetaAttributes, Response createMetaAttributesResponse,
            int expectedResponseCode) throws Exception {

        Assert.assertEquals(createMetaAttributesResponse.getStatusCode(), expectedResponseCode);
        if (expectedResponseCode != 200) {
            return;
        }
        AtomicInteger count = new AtomicInteger(0);
        ArrayNode actualMetaAttributes = (ArrayNode) objectMapper.readTree(createMetaAttributesResponse.asString());
        expectedMetaAttributes.stream().forEach(expectedMetaAttribute -> {
            actualMetaAttributes.forEach(actualMetaAttribute -> {
                if (expectedMetaAttribute.get("name").equals(actualMetaAttribute.get("name"))) {
                    count.getAndIncrement();

                    Assert.assertEquals(actualMetaAttribute.get("required"), expectedMetaAttribute.get("required"));
                    Assert.assertEquals(actualMetaAttribute.get("meta_attribute_type").asText(), expectedMetaAttribute.get("meta_attribute_type"));
                    Assert.assertEquals(actualMetaAttribute.get("data_type"), expectedMetaAttribute.get("data_type"));
                }
            });
        });
        Assert.assertEquals(count.get(), expectedMetaAttributes.size());
        verifyDCMetaAttributesInDB(expectedMetaAttributes);
    }

    public void verifyDCMetaAttributesInDB(List<Map<String, Object>> expectedMetaAttributes) throws Exception {

        List<Map<String, Object>> metaAttributesInDB = getDCMetaAttributesInDB(expectedMetaAttributes.get(0).get("name").toString(),
                expectedMetaAttributes.get(1).get("name").toString());

        AtomicInteger count = new AtomicInteger(0);
        expectedMetaAttributes.stream().forEach(expectedMetaAttribute -> {
            metaAttributesInDB.forEach(metaAttributeInDB -> {
                if (expectedMetaAttribute.get("name").equals(metaAttributeInDB.get("name"))) {
                    count.getAndIncrement();

                    Assert.assertEquals(metaAttributeInDB.get("required"), expectedMetaAttribute.get("required"));
                    Assert.assertEquals(metaAttributeInDB.get("meta_attribute_type"), expectedMetaAttribute.get("meta_attribute_type"));
                    Assert.assertEquals(metaAttributeInDB.get("data_type"), expectedMetaAttribute.get("data_type"));
                }
            });
        });

        Assert.assertEquals(count.get(), expectedMetaAttributes.size());
    }

    private List<Map<String, Object>> getDCMetaAttributesInDB(String metaAttribute1, String metaAttribute2) {
        List<Map<String, Object>> dbResponse = dbUtilities.getDbDataByQuery(
                "select * from category_meta_attributes where name in ( '" + metaAttribute1 + "','" + metaAttribute2 + "')",
                digitalClinicDBConnection);

        return dbResponse;
    }

    public void setDCMetaAttributeValues(List<Map<String, Object>> expectedMetaAttributes) {

        attributeName = expectedMetaAttributes.get(0).get("name").toString();
        attributeType = expectedMetaAttributes.get(0).get("data_type").toString();

    }

    public void setDCPackageBenefitMappingValues(List<Map<String, Object>> expectedPackageBenefitResponse) {

        packageProductId = expectedPackageBenefitResponse.get(0).get("package_product_id").toString();
        benefitPackageId = expectedPackageBenefitResponse.get(0).get("benefit_package_id").toString();

    }

    public Response addDCMetaAttributesForCategory(String categoryExternalId, List<Map<String, Object>> metaAttributes) throws Exception {
        JsonNode jsonNode = objectMapper.valueToTree(metaAttributes);
        String url = DC_CATALOG_BASE_URL + ADD_DC_META_ATTRIBUTES.replace("{categoryExternalId}", categoryExternalId);
        RestClient client = new RestClient(url, headers);
        return client.executePost(jsonNode.toString());

    }

    public Response updateDCMetaAttributesForCategory(String categoryExternalId, List<Map<String, Object>> metaAttributes) throws Exception {
        JsonNode jsonNode = objectMapper.valueToTree(metaAttributes);
        String url = DC_CATALOG_BASE_URL + ADD_DC_META_ATTRIBUTES.replace("{categoryExternalId}", categoryExternalId);
        RestClient client = new RestClient(url, headers);
        return client.executePut(jsonNode.toString());

    }

    public Response addDCEntityMappingForCategory(String categoryExternalId, List<Map<String, Object>> metaAttributes) throws Exception {
        JsonNode jsonNode = objectMapper.valueToTree(metaAttributes);
        String url = DC_CATALOG_BASE_URL + ADD_DC_ENTITY_MAPPING.replace("{categoryExternalId}", categoryExternalId);
        RestClient client = new RestClient(url, headers);
        return client.executePut(jsonNode.toString());

    }

    public void verifyDCMetaAttributesForCategory(String categoryExternalId, List<Map<String, Object>> expectedMetaAttributes,
            Response addMetaAttributesResponse, int expectedResponseCode) throws JsonProcessingException, Exception {

        Assert.assertEquals(addMetaAttributesResponse.getStatusCode(), expectedResponseCode);
        if (expectedResponseCode != 200) {
            return;
        }
        AtomicInteger count = new AtomicInteger(0);
        JsonNode actualMetaAttributes = JsonUtils.convertJsonToString(addMetaAttributesResponse.asString());
        String actualAttributes = actualMetaAttributes.get("attributes").toString();
        JsonNode actualJsonNode = null;
        try {
            actualJsonNode = objectMapper.readTree(actualAttributes);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        JsonNode finalActualJsonNode = actualJsonNode;
        expectedMetaAttributes.stream().forEach(expectedMetaAttribute -> {
            finalActualJsonNode.forEach(attribute -> {

                if (expectedMetaAttribute.get("attribute_key").equals(attribute.get("attribute_key").toString().replace("\"", ""))) {
                    count.getAndIncrement();
                    Assert.assertEquals(expectedMetaAttribute.get("attribute_value"), attribute.get("attribute_value").toString().replace("\"", ""));
                    Assert.assertEquals(expectedMetaAttribute.get("language"), attribute.get("language").toString().replace("\"", ""));

                }

            });
        });
        verifyDCMetaAttributesForCategoryInDB(expectedMetaAttributes, categoryExternalId);
        Assert.assertEquals(count.get(), expectedMetaAttributes.size());

    }

    public void verifyDCEntityMappingForCategory(String categoryExternalId, List<Map<String, Object>> expectedEntityMapping,
            Response addEntityMappingResponse, int expectedResponseCode) throws JsonProcessingException, Exception {

        Assert.assertEquals(addEntityMappingResponse.getStatusCode(), expectedResponseCode);
        if (expectedResponseCode != 200) {
            return;
        }
        List<Map<String, Object>> entityMappingInDB = getDCEntityMappingForCategoryInDB(categoryExternalId);

        AtomicInteger count = new AtomicInteger(0);
        expectedEntityMapping.stream().forEach(expectedEntity -> {
            entityMappingInDB.forEach(entityInDB -> {
                if (expectedEntity.get("entity_type").equals(entityInDB.get("entity_type"))) {
                    count.getAndIncrement();

                    Assert.assertEquals(entityInDB.get("entity_id"), expectedEntity.get("entity_id"));
                    Assert.assertEquals(entityInDB.get("is_deleted"), expectedEntity.get("is_deleted"));

                }
            });
        });

        Assert.assertEquals(count.get(), expectedEntityMapping.size());

    }

    private List<Map<String, Object>> getDCEntityMappingForCategoryInDB(String categoryExternalId) {
        List<Map<String, Object>> dbResponse = dbUtilities.getDbDataByQuery(
                "select cem.* from categories c,`category_entity_mapping` cem where c.`id`=cem.`category_id` and c.`external_id`='" + categoryExternalId + "'",
                digitalClinicDBConnection);

        return dbResponse;
    }

    public void verifyDCMetaAttributesForCategoryInDB(List<Map<String, Object>> expectedMetaAttributes, String categoryExternalId) throws Exception {

        List<Map<String, Object>> metaAttributesInDB = getDCMetaAttributesForCategoryInDB(categoryExternalId);

        AtomicInteger count = new AtomicInteger(0);
        expectedMetaAttributes.stream().forEach(expectedMetaAttribute -> {
            metaAttributesInDB.forEach(metaAttributeInDB -> {
                if (expectedMetaAttribute.get("attribute_key").equals(metaAttributeInDB.get("attribute_key"))) {
                    count.getAndIncrement();

                    Assert.assertEquals(metaAttributeInDB.get("attribute_value"), expectedMetaAttribute.get("attribute_value"));
                    Assert.assertEquals(metaAttributeInDB.get("language"), expectedMetaAttribute.get("language"));

                }
            });
        });

        Assert.assertEquals(count.get(), expectedMetaAttributes.size());
    }

    private List<Map<String, Object>> getDCMetaAttributesForCategoryInDB(String categoryExternalId) {
        List<Map<String, Object>> dbResponse = dbUtilities.getDbDataByQuery(
                "select ca.* from categories c,`category_attributes` ca where c.`id`=ca.`category_id` and c.`external_id`='" + categoryExternalId + "'",
                digitalClinicDBConnection);

        return dbResponse;
    }

    public void closeDBConnection() {
        dbUtilities.closeConnection(digitalClinicDBConnection);
    }
}
