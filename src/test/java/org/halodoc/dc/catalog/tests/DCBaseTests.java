package org.halodoc.dc.catalog.tests;

import org.halodoc.dc.catalog.helpers.DigitalClinicCatalogHelper;
import org.testng.annotations.AfterSuite;

public class DCBaseTests {
    DigitalClinicCatalogHelper dcHelper = new DigitalClinicCatalogHelper();

    public DCBaseTests() throws Exception {
    }

    @AfterSuite
    public void tearDown() {
        dcHelper.closeDBConnection();
    }
}
