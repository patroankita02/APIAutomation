package org.halodoc.dc.catalog.utils;

public class Constants {
    public static final String VALID_DC_APP_TOKEN="ab25be2a-90de-4754-92c5-2ea7ffd62648";
    public static final String DC_CATALOG_BASE_URL="http://digital-clinic-catalog.stage-k8s.halodoc.com";
    public static final String CREATE_DC_CATEGORY="/v1/digital_clinic/categories";

    public static final String DIGITAL_CLINIC_DB_PROPERTIES_FILE_PATH = "config/digital_clinic_db.properties" ;
    public static final String WAKATOBI_CATALOG_DB_PROPERTIES_FILE_PATH = "config/wakatobi_catalog_db.properties" ;
    public static final String TIMOR_CMS_DB_PROPERTIES_FILE_PATH = "config/timor_cms_db.properties" ;

    public static final String GET_DC_CATEGORY="/v1/digital_clinic/categories/{categoryExternalId}";
    public static final String UPDATE_DC_CATEGORY="/v1/digital_clinic/categories/{categoryExternalId}";
    public static final String ADD_DC_PACKAGE_BENEFIT_MAPPING="/v1/digital_clinic/package_benefit_mapping";
    public static final String UPDATE_DC_PACKAGE_BENEFIT_MAPPING="/v1/digital_clinic/package_benefit_mapping/{package_product_id}";
    public static final String CREATE_DC_META_ATTRIBUTES="/v1/category_meta_attributes";
    public static final String ADD_DC_META_ATTRIBUTES="/v1/digital_clinic/categories/{categoryExternalId}/attributes";
    public static final String ADD_DC_ENTITY_MAPPING="/v1/digital_clinic/categories/{categoryExternalId}/entity_mapping";
}
