package org.halodoc.dc.catalog.integrationTests.tests;

import com.github.tomakehurst.wiremock.WireMockServer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BaseTest {

    public WireMockServer timorCmsMockServer = new WireMockServer(8082);

    public WireMockServer wakatobiMockServer = new WireMockServer(8083);

    public void startMocking() {
        timorCmsMockServer.start();
        wakatobiMockServer.start();
    }

    public void stopMocking() {
        timorCmsMockServer.stop();
        wakatobiMockServer.stop();
    }

    public Map<String, String> getHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("X-Transaction-Id", "Txn-" + UUID.randomUUID());
        headers.put("Referer", "automationInt");
        return headers;
    }
}
