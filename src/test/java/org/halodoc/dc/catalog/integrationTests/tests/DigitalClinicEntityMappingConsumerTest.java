package org.halodoc.dc.catalog.integrationTests.tests;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.MessageOrBuilder;
import com.halodoc.timor.cms.v1.productPackage.ProductPackage;
import com.halodoc.timor.cms.v1.productPackage.ProductPackageDcCategoryMappingUpdated;
import com.halodoc.timor.oms.v1.Order;
import com.halodoc.utils.kafka.KafkaClientConsumerUtility;
import com.halodoc.utils.kafka.KafkaClientProducersUtility;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.producer.Producer;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Optional;
import java.util.Properties;

public class DigitalClinicEntityMappingConsumerTest extends BaseTest{
    Producer<String, GeneratedMessageV3> producer;
    KafkaClientProducersUtility producersUtility = new KafkaClientProducersUtility();

    Consumer<String, MessageOrBuilder> orderConsumer;

    KafkaClientConsumerUtility kafkaClientConsumerUtility = new KafkaClientConsumerUtility();

    @BeforeClass
    public void beforeClass() throws Exception {
        startMocking();
        Properties propertiesOverride = new Properties();
        propertiesOverride.put("security.protocol", "PLAINTEXT");
        propertiesOverride.put("sasl.mechanism", "PLAIN");
        producer = producersUtility.getProducer("localhost:9092", Optional.of(propertiesOverride));

    }

    @Test
    public void testDigitalClinicEntityMappingConsumer() throws Exception {
        // Given

        String dcCategoryId="6511ce50-4d61-4e91-9743-1a5623390cf9";
        String productExternalId="123-123";
        producersUtility.sendMessage("com.halodoc.timor.productPacakge.v1.product_package", null, getProductPackageDCCategoryEventData(dcCategoryId, productExternalId), getHeaders(), producer);

        // Then
        // verifyAutoRepairEvent();
    }

    @Test
    public void testDigitalClinicEntityMappingConsumer1() throws Exception {
        // Given

        String dcCategoryId="6511ce50-4d61-4e91-9743-1a5623390cf9NoExist";
        String productExternalId="123-123";
        producersUtility.sendMessage("com.halodoc.timor.productPacakge.v1.product_package", null, getProductPackageDCCategoryEventData(dcCategoryId, productExternalId), getHeaders(), producer);

        // Then
        // verifyAutoRepairEvent();
    }

    private com.halodoc.timor.cms.v1.productPackage.ProductPackage getProductPackageDCCategoryEventData( String dcCategoryId, String productExternalId) {
        ProductPackageDcCategoryMappingUpdated.Builder productPackageDcCategoryMapping = ProductPackageDcCategoryMappingUpdated.newBuilder();
        productPackageDcCategoryMapping.setCategoryId(dcCategoryId);
        productPackageDcCategoryMapping.setProductPackageId(productExternalId);
        productPackageDcCategoryMapping.setLinkingType("linked");
        if (dcCategoryId.isEmpty() || "null".equalsIgnoreCase(dcCategoryId)) {
            productPackageDcCategoryMapping.setLinkingType("unlinked");
        }
        return com.halodoc.timor.cms.v1.productPackage.ProductPackage.newBuilder().setEntityId(productExternalId)
                .setEntityType("product_package").setEventType("product_package_dc_category_mapping_updated")
                .setEventSource("TIMOR_CMS")
                .setProductPackageDcCategoryMappingUpdated(productPackageDcCategoryMapping)
                .build();
    }

    @AfterClass
    public void afterClass() throws Exception {
        stopMocking();
    }

}
