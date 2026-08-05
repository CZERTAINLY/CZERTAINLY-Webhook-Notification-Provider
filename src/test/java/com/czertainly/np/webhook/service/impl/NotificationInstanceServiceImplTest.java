package com.czertainly.np.webhook.service.impl;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.czertainly.api.model.connector.notification.NotificationProviderNotifyRequestDto;
import com.czertainly.api.model.core.auth.Resource;
import com.czertainly.api.model.core.other.ResourceEvent;
import com.czertainly.np.webhook.attribute.ContentType;
import com.czertainly.np.webhook.dao.entity.NotificationInstance;
import com.czertainly.np.webhook.dao.repository.NotificationInstanceRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationInstanceServiceImplTest {

    private static final String SENSITIVE_VALUE = "debug-visible-credential";

    @Mock
    private NotificationInstanceRepository repository;

    private NotificationInstanceServiceImpl service;

    private ListAppender<ILoggingEvent> logAppender;
    private Level originalLevel;

    @BeforeEach
    void setUp() {
        service = new NotificationInstanceServiceImpl();
        service.setNotificationInstanceRepository(repository);

        Logger serviceLogger = serviceLogger();
        originalLevel = serviceLogger.getLevel();
        logAppender = new ListAppender<>();
        logAppender.start();
        serviceLogger.addAppender(logAppender);
    }

    @AfterEach
    void tearDown() {
        Logger serviceLogger = serviceLogger();
        serviceLogger.setLevel(originalLevel);
        serviceLogger.detachAppender(logAppender);
        logAppender.stop();
    }

    private Logger serviceLogger() {
        return (Logger) LoggerFactory.getLogger(NotificationInstanceServiceImpl.class);
    }

    private NotificationProviderNotifyRequestDto request() {
        NotificationProviderNotifyRequestDto request = new NotificationProviderNotifyRequestDto();
        request.setEvent(ResourceEvent.CERTIFICATE_STATUS_CHANGED);
        request.setResource(Resource.CERTIFICATE);
        request.setNotificationData(Map.of("credential", SENSITIVE_VALUE));
        return request;
    }

    private UUID persistedInstance(ContentType contentType, String contentTemplate) {
        return persistedInstance(contentType, contentTemplate, "http://localhost:9");
    }

    private UUID persistedInstance(ContentType contentType, String contentTemplate, String url) {
        UUID uuid = UUID.randomUUID();
        NotificationInstance instance = new NotificationInstance();
        instance.setUuid(uuid);
        instance.setName("test-webhook");
        instance.setUrl(url);
        instance.setContentType(contentType);
        instance.setContentTemplate(contentTemplate);
        when(repository.findByUuid(uuid)).thenReturn(Optional.of(instance));
        return uuid;
    }

    /**
     * The delivery outcome is logged from the asynchronous callbacks, so a webhook that the
     * receiver accepts must still report the send — covered against a real endpoint because the
     * success callback is never reached when the endpoint is unreachable.
     */
    @Test
    void sendNotification_acceptedByReceiver_logsTheCompletedSend() throws Exception {
        HttpServer receiver = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        receiver.createContext("/", exchange -> {
            exchange.sendResponseHeaders(200, -1);
            exchange.close();
        });
        receiver.start();
        try {
            UUID uuid = persistedInstance(ContentType.RAW_JSON, null,
                    "http://localhost:" + receiver.getAddress().getPort());

            assertDoesNotThrow(() -> service.sendNotification(uuid, request()));

            assertTrue(awaitLog(message -> message.startsWith("Webhook sent to")),
                    "an accepted webhook must be reported as sent: " + formattedLogs());
        } finally {
            receiver.stop(0);
        }
    }

    /**
     * Payload logging is opt-in: raising the log level alone must never write the request into the
     * logs, only the payload-free summary.
     */
    @Test
    void sendNotification_debugLoggingWithoutOptIn_logsNoPayload() {
        UUID uuid = persistedInstance(ContentType.RAW_JSON, null);
        serviceLogger().setLevel(Level.DEBUG);

        assertDoesNotThrow(() -> service.sendNotification(uuid, request()));

        assertTrue(formattedLogs().stream().noneMatch(message -> message.contains(SENSITIVE_VALUE)),
                "DEBUG alone must not write the request payload: " + formattedLogs());
        assertTrue(formattedLogs().stream().anyMatch(message -> message.contains("notificationData=present")),
                "the payload-free summary must still be logged: " + formattedLogs());
    }

    /** With payload logging switched on, the full request is available for troubleshooting. */
    @Test
    void sendNotification_debugLoggingWithOptIn_retainsRequestVisibility() {
        UUID uuid = persistedInstance(ContentType.RAW_JSON, null);
        service.setLogRequestPayload(true);
        serviceLogger().setLevel(Level.DEBUG);

        assertDoesNotThrow(() -> service.sendNotification(uuid, request()));

        assertTrue(formattedLogs().stream().anyMatch(message -> message.contains(SENSITIVE_VALUE)),
                "with payload logging enabled the request content must be available");
    }

    @Test
    void describeSendFailure_transportErrorsKeepTheirMessage() {
        assertTrue(NotificationInstanceServiceImpl.describeSendFailure(
                        new com.czertainly.np.webhook.exception.NotificationException("Failed to send webhook to x: 500"))
                .contains("Failed to send webhook to x: 500"));
    }

    @Test
    void describeSendFailure_connectionErrorsKeepTheirMessage() {
        org.springframework.web.reactive.function.client.WebClientRequestException transport =
                new org.springframework.web.reactive.function.client.WebClientRequestException(
                        new java.net.ConnectException("Connection refused: localhost:9"),
                        org.springframework.http.HttpMethod.POST,
                        java.net.URI.create("http://localhost:9"),
                        new org.springframework.http.HttpHeaders());
        assertTrue(NotificationInstanceServiceImpl.describeSendFailure(transport).contains("Connection refused"));
    }

    @Test
    void describeSendFailure_otherErrorsAreReducedToTheirType() {
        String described = NotificationInstanceServiceImpl.describeSendFailure(
                new IllegalStateException("encoder failure exposing " + SENSITIVE_VALUE));
        assertTrue(described.equals("IllegalStateException"),
                "unknown failures must be reduced to the exception type: " + described);
    }

    @Test
    void sendNotification_templatedContent_rendersWithoutPayloadInDefaultLogs() throws InterruptedException {
        UUID uuid = persistedInstance(ContentType.JSON,
                Base64.getEncoder().encodeToString("{\"event\": \"${event}\"}".getBytes(StandardCharsets.UTF_8)));

        assertDoesNotThrow(() -> service.sendNotification(uuid, request()));
        // Delivery is asynchronous: wait for its outcome to be logged, otherwise the assertion
        // could run before the send path had a chance to log anything at all.
        assertTrue(awaitLog(message -> message.startsWith("Error sending webhook to") || message.startsWith("Webhook sent to")),
                "the asynchronous send outcome was never logged");

        assertTrue(formattedLogs().stream().noneMatch(message -> message.contains(SENSITIVE_VALUE)),
                "default-level logs must not carry the request payload");
    }

    private boolean awaitLog(java.util.function.Predicate<String> matcher) throws InterruptedException {
        for (int attempt = 0; attempt < 100; attempt++) {
            if (formattedLogs().stream().anyMatch(matcher)) return true;
            Thread.sleep(50);
        }
        return false;
    }

    private java.util.List<String> formattedLogs() {
        return logAppender.list.stream().map(ILoggingEvent::getFormattedMessage).toList();
    }

}
