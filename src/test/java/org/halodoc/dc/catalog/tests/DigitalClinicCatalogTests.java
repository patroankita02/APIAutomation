package org.halodoc.dc.catalog.tests;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import io.restassured.response.Response;
import org.halodoc.dc.catalog.helpers.BaseHelper;
import org.halodoc.dc.catalog.helpers.DigitalClinicCatalogHelper;
import org.testng.Assert;
import org.testng.annotations.Test;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.javafaker.Faker;

public class DigitalClinicCatalogTests extends BaseHelper {
    DigitalClinicCatalogHelper dcHelper = new DigitalClinicCatalogHelper();

    Faker faker = new Faker();

    public DigitalClinicCatalogTests() throws Exception {
    }

    @Test (enabled = true, priority = 1)
    public void createDCCategoryWithParentIdNullTest() throws Exception {
        Thread.sleep(1000);
        Response response = dcHelper.createDCCategory("active", 1, null);
        dcHelper.verifyDCCategory(response, 201);
        dcHelper.setDCCategoryValues(response);
    }

    @Test (enabled = true, priority = 2)
    public void createDCCategoryWithParentIdTest() throws Exception {

        createDCCategoryWithParentIdNullTest();
        Thread.sleep(1000);
        Response response = dcHelper.createDCCategory("active", 2, dcHelper.categoryId);
        dcHelper.setDCCategoryValues(response);
        dcHelper.verifyDCCategory(response, 201);
    }

    @Test (enabled = true, priority = 3)
    public void createDCCategoryWithInactiveStatusTest() throws Exception {
        Thread.sleep(1000);
        Response response = dcHelper.createDCCategory("inactive", 1, null);
        dcHelper.verifyDCCategory(response, 201);
    }

    @Test (enabled = true, priority = 4)
    public void createDCCategoryWithInvalidCategoryLevelTest() throws Exception {
        Thread.sleep(1000);
        Response response = dcHelper.createDCCategory("active", 2, null);
        Assert.assertEquals(response.getStatusCode(), 400);
    }

    @Test (enabled = true, priority = 5)
    public void createDCCategoryWithInValidStatusTest() throws Exception {
        Thread.sleep(1000);
        Response response = dcHelper.createDCCategory("tive", 1, null);
        Assert.assertEquals(response.getStatusCode(), 400);
    }

    @Test (enabled = true, priority = 6)
    public void createDCCategoryWithNullStatusTest() throws Exception {
        Thread.sleep(1000);
        Response response = dcHelper.createDCCategory(null, 1, null);
        Assert.assertEquals(response.getStatusCode(), 422);
    }

    @Test (enabled = true, priority = 7)
    public void getDCCategoryForParentTest() throws Exception {
        Thread.sleep(1000);
        createDCCategoryWithParentIdNullTest();
        Response response = dcHelper.getCategoryByCategoryExternalId(dcHelper.categoryExternalId);
        dcHelper.verifyDCCategory(response, 200);
    }

    @Test (enabled = true, priority = 8)
    public void getDCCategoryForSubCategoryTest() throws Exception {
        Thread.sleep(1000);
        createDCCategoryWithParentIdTest();
        Response response = dcHelper.getCategoryByCategoryExternalId(dcHelper.categoryExternalId);
        dcHelper.verifyDCCategory(response, 200);
    }

    @Test (enabled = true, priority = 9)
    public void getDCCategoryInvalidCategoryExternalIdTest() throws Exception {
        Thread.sleep(1000);
        Response response = dcHelper.getCategoryByCategoryExternalId("categoryExternalId");
        Assert.assertEquals(response.getStatusCode(), 404);
    }

    @Test (enabled = true, priority = 10)
    public void updateDCCategoryStatusForParentTest() throws Exception {
        Thread.sleep(1000);
        createDCCategoryWithParentIdNullTest();
        Response response = dcHelper.updateDCCategory(dcHelper.categoryExternalId, "inactive", dcHelper.categoryName, dcHelper.categoryCode,
                dcHelper.categoryLevel, null);
        dcHelper.verifyDCCategory(response, 200);

    }

    @Test (enabled = true, priority = 11)
    public void updateDCCategoryStatusForSubcategoryTest() throws Exception {
        Thread.sleep(2000);
        createDCCategoryWithParentIdTest();
        Response response = dcHelper.updateDCCategory(dcHelper.categoryExternalId, "inactive", dcHelper.categoryName, dcHelper.categoryCode,
                dcHelper.categoryLevel, dcHelper.categoryParentId);
        dcHelper.verifyDCCategory(response, 200);

    }

    @Test (enabled = true, priority = 12)
    public void updateDCCategoryWithInValidCategoryExternalIdTest() throws Exception {
        Thread.sleep(1000);
        createDCCategoryWithParentIdNullTest();
        Response response = dcHelper.updateDCCategory("categoryExternalId", "inactive", dcHelper.categoryName, dcHelper.categoryCode,
                dcHelper.categoryLevel, dcHelper.categoryParentId);
        Assert.assertEquals(response.getStatusCode(), 404);
    }

    @Test (enabled = true, priority = 13)
    public void updateDCCategoryWithInValidParentIDTest() throws Exception {
        Thread.sleep(2000);
        createDCCategoryWithParentIdNullTest();
        Response response = dcHelper.updateDCCategory(dcHelper.categoryExternalId, "inactive", dcHelper.categoryName, dcHelper.categoryCode,
                dcHelper.categoryLevel, 0);
        Assert.assertEquals(response.getStatusCode(), 404);
    }

    @Test (enabled = true, priority = 14)
    public void createDCPackageBenefitMappingTest() throws Exception {
        Thread.sleep(1000);
        List<Map<String, Object>> packageBenefitMappingList = List.of(
                getAddDCPackageBenefitMappingRequest( "consultation 6 days", "active", false));

        Response response = dcHelper.createDCPackageBenefitMapping(packageBenefitMappingList);
        dcHelper.setDCPackageBenefitMappingValues(packageBenefitMappingList);
        System.out.println(response.asString());
        dcHelper.verifyDCPackageBenefitMapping(packageBenefitMappingList, response, 201);
        deleteDCPackageBenefitMappingTest();

    }

    public Map<String, Object> getAddDCPackageBenefitMappingRequest(String benefitName, String status,
            boolean isDeleted) throws IOException {
        String queryWC = "select external_id from `wakatobi_catalog`.`packages`where type='digital_clinic' limit 1;";
        String queryTC= "select p.external_id from product_packages pp, products p where p.id=pp.product_id and pp.status='active' limit 1;";
        List<Map<String, Object>> dbResponseWC = dbUtilities.getDbDataByQuery(queryWC,wakatobiCatalogDBConnection);
        List<Map<String, Object>> dbResponseTC = dbUtilities.getDbDataByQuery(queryTC,timorCMSDBConnection);
        String benefitPackageId = String.valueOf(dbResponseWC.get(0).get("external_id"));
        String packageProductId = String.valueOf(dbResponseTC.get(0).get("external_id"));
        return Map.of("package_product_id", packageProductId, "benefit_package_id", benefitPackageId, "benefit_name", benefitName, "status", status,
                "is_deleted", isDeleted);
    }

    @Test (enabled = true, priority = 15)
    public void updateDCPackageBenefitMappingTest() throws Exception {
        createDCPackageBenefitMappingTest();
        List<Map<String, Object>> packageBenefitMappingList = List.of(
                getUpdateDCPackageBenefitMappingRequest(dcHelper.packageProductId, dcHelper.benefitPackageId, "benefitName", "inactive", true));

        Response response = dcHelper.updateDCPackageBenefitMapping(packageBenefitMappingList);
        System.out.println(response.asString());

    }
    public Map<String, Object> getUpdateDCPackageBenefitMappingRequest(String packageProductId, String benefitPackageId, String benefitName, String status,
            boolean isDeleted) throws IOException {


        return Map.of("package_product_id", packageProductId, "benefit_package_id", benefitPackageId, "benefit_name", benefitName, "status", status,
                "is_deleted", isDeleted);
    }
    @Test (enabled = true, priority = 16)
    public void deleteDCPackageBenefitMappingTest() throws Exception {
        List<Map<String, Object>> packageBenefitMappingList = List.of(
                getUpdateDCPackageBenefitMappingRequest(dcHelper.packageProductId, dcHelper.benefitPackageId, "benefitName", "inactive", true));

        Response response = dcHelper.updateDCPackageBenefitMapping(packageBenefitMappingList);
        System.out.println(response.asString());

    }



    @Test (enabled = true, priority = 17)
    public void createMetaAttributesTest() throws Exception {
        Thread.sleep(1000);
        List<Map<String, Object>> metaAttributeList = List.of(
                getMetaAttributes("meta_attribute", faker.lorem().sentence(), "public_attribute", "String", false),
                getMetaAttributes("meta_attribute", faker.lorem().sentence(), "public_attribute", "String", false));
        Response response = dcHelper.createDCMetaAttributes(metaAttributeList);
        dcHelper.setDCMetaAttributeValues(metaAttributeList);

        dcHelper.verifyDCMetaAttributes(metaAttributeList, response, 201);
        System.out.println(response.asString());
    }

    private Map<String, Object> getMetaAttributes(String defaultValue, String name, String metaAttributeType, String dataType, boolean required) {
        return Map.of("default_value", defaultValue, "name", name, "meta_attribute_type", metaAttributeType, "data_type", dataType, "required",
                required);
    }

    @Test (enabled = true, priority = 18)
    public void createMetaAttributesInvalidAttributeTypeTest() throws Exception {
        Thread.sleep(1000);
        List<Map<String, Object>> packageBenefitList = List.of(
                getMetaAttributes("meta_attribute", faker.lorem().sentence(), "_attribute", "String", false));
        Response response = dcHelper.createDCMetaAttributes(packageBenefitList);
        Assert.assertEquals(response.getStatusCode(), 400);
    }

    @Test (enabled = true, priority = 19)
    public void createMetaAttributesInvalidDataTypeTest() throws Exception {
        Thread.sleep(1000);
        List<Map<String, Object>> metaAttributeList = List.of(
                getMetaAttributes("meta_attribute", faker.lorem().sentence(), "_attribute", "Boolean", false));
        Response response = dcHelper.createDCMetaAttributes(metaAttributeList);
        Assert.assertEquals(response.getStatusCode(), 400);
    }

    @Test (enabled = true, priority = 20)
    public void addMetaAttributesForCategoryTest() throws Exception {
        Thread.sleep(1000);
        createDCCategoryWithParentIdNullTest();
        createMetaAttributesTest();
        List<Map<String, Object>> metaAttributeList = List.of(getMetaAttributesForCategory(dcHelper.attributeName, "skinCare"));
        Response response = dcHelper.addDCMetaAttributesForCategory(dcHelper.categoryExternalId, metaAttributeList);
        dcHelper.verifyDCMetaAttributesForCategory(dcHelper.categoryExternalId, metaAttributeList, response, 200);
        System.out.println(response.asString());
    }

    @Test (enabled = true, priority = 21)
    public void addMetaAttributesForCategoryWithInvalidCategoryTest() throws Exception {
        Thread.sleep(1000);
        createDCCategoryWithParentIdNullTest();
        createMetaAttributesTest();
        List<Map<String, Object>> metaAttributeList = List.of(getMetaAttributesForCategory(dcHelper.attributeName, "skinCare"));
        Response response = dcHelper.addDCMetaAttributesForCategory("1234", metaAttributeList);
        Assert.assertEquals(response.getStatusCode(), 404);
    }

    @Test (enabled = true, priority = 22)
    public void addMetaAttributesForCategoryWithInvalidRequestTest() throws Exception {
        Thread.sleep(1000);
        createDCCategoryWithParentIdNullTest();
        createMetaAttributesTest();
        List<Map<String, Object>> metaAttributeList = List.of(getMetaAttributesForCategory("attribute", "skinCare"));
        Response response = dcHelper.addDCMetaAttributesForCategory(dcHelper.categoryExternalId, metaAttributeList);
        Assert.assertEquals(response.getStatusCode(), 404);
    }

    private Map<String, Object> getMetaAttributesForCategory(String attributeName, String attributeValue) {
        return Map.of("attribute_key", attributeName, "attribute_value", attributeValue, "language", "na");
    }

    @Test (enabled = true, priority = 23)
    public void updateMetaAttributesForCategoryTest() throws Exception {
        Thread.sleep(1000);
        addMetaAttributesForCategoryTest();
        List<Map<String, Object>> metaAttributeList = List.of(getMetaAttributesForCategory(dcHelper.attributeName, "hairCare"));
        Response response = dcHelper.updateDCMetaAttributesForCategory(dcHelper.categoryExternalId, metaAttributeList);
        dcHelper.verifyDCMetaAttributesForCategory(dcHelper.categoryExternalId, metaAttributeList, response, 204);
        System.out.println(response.asString());
    }
    @Test (enabled = true, priority = 23)
    public void updateInvalidMetaAttributesForCategoryTest() throws Exception {
        Thread.sleep(1000);
        addMetaAttributesForCategoryTest();
        List<Map<String, Object>> metaAttributeList = List.of(getMetaAttributesForCategory(dcHelper.attributeName, "hairCare"));
        Response response = dcHelper.updateDCMetaAttributesForCategory("1234", metaAttributeList);
        dcHelper.verifyDCMetaAttributesForCategory(dcHelper.categoryExternalId, metaAttributeList, response, 404);
        System.out.println(response.asString());
    }

    @Test (enabled = true, priority = 24)
    public void addPackageEntityMappingForCategoryTest() throws Exception {
        Thread.sleep(1000);
        createDCCategoryWithParentIdNullTest();
        List<Map<String, Object>> entityMappingList = List.of(getEntityMappingForCategory("package_product", "SkinCareKit"));
        Response response = dcHelper.addDCEntityMappingForCategory(dcHelper.categoryExternalId, entityMappingList);
        dcHelper.verifyDCEntityMappingForCategory(dcHelper.categoryExternalId, entityMappingList, response, 204);
        System.out.println(response.asString());
    }

    @Test (enabled = true, priority = 25)
    public void addDoctorEntityMappingForCategoryTest() throws Exception {
        Thread.sleep(1000);
        createDCCategoryWithParentIdNullTest();
        List<Map<String, Object>> entityMappingList = List.of(getEntityMappingForCategory("doctor_category", "SkinCare"));
        Response response = dcHelper.addDCEntityMappingForCategory(dcHelper.categoryExternalId, entityMappingList);
        dcHelper.verifyDCEntityMappingForCategory(dcHelper.categoryExternalId, entityMappingList, response, 204);
        System.out.println(response.asString());
    }

    @Test (enabled = true, priority = 26)
    public void addInvalidEntityMappingForCategoryTest() throws Exception {
        Thread.sleep(1000);
        createDCCategoryWithParentIdNullTest();
        List<Map<String, Object>> entityMappingList = List.of(getEntityMappingForCategory("doctor", "SkinCare"));
        Response response = dcHelper.addDCEntityMappingForCategory(dcHelper.categoryExternalId, entityMappingList);
        dcHelper.verifyDCEntityMappingForCategory(dcHelper.categoryExternalId, entityMappingList, response, 400);
        System.out.println(response.asString());
    }

    private Map<String, Object> getEntityMappingForCategory(String entityType, String entityValue) {
        return Map.of("entity_type", entityType, "entity_id", entityValue, "is_deleted", 0);
    }

}
