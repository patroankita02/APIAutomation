package org.halodoc.dc.catalog.tests;

import io.restassured.response.Response;
import org.halodoc.dc.catalog.helpers.DigitalClinicCatalogHelper;
import org.testng.annotations.Test;

public class DigitalClinicCatalogTests extends DCBaseTests {

    DigitalClinicCatalogHelper dcHelper = new DigitalClinicCatalogHelper();

    @Test
    public void createDigitalClinicCategoryTest() throws Exception {
        Response response = dcHelper.createDCCategory("active", 1, null);
        dcHelper.verifyCreateDCCategoryResponse(response, 201);
    }

}
