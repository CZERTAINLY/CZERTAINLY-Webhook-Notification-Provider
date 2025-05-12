package com.czertainly.np.webhook.service.impl;

import com.czertainly.api.exception.AlreadyExistException;
import com.czertainly.api.exception.NotFoundException;
import com.czertainly.api.model.common.attribute.v2.content.CodeBlockAttributeContent;
import com.czertainly.api.model.common.attribute.v2.content.StringAttributeContent;
import com.czertainly.api.model.connector.notification.NotificationProviderInstanceDto;
import com.czertainly.api.model.connector.notification.NotificationProviderInstanceRequestDto;
import com.czertainly.api.model.connector.notification.NotificationProviderNotifyRequestDto;
import com.czertainly.core.util.AttributeDefinitionUtils;
import com.czertainly.np.webhook.attribute.Attributes;
import com.czertainly.np.webhook.attribute.ContentType;
import com.czertainly.np.webhook.dao.entity.NotificationInstance;
import com.czertainly.np.webhook.dao.repository.NotificationInstanceRepository;
import com.czertainly.np.webhook.exception.NotificationException;
import com.czertainly.np.webhook.service.AttributeService;
import com.czertainly.np.webhook.service.NotificationInstanceService;
import com.czertainly.np.webhook.util.TemplateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class NotificationInstanceServiceImpl implements NotificationInstanceService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationInstanceServiceImpl.class);

    private NotificationInstanceRepository notificationInstanceRepository;

    private AttributeService attributeService;

    @Autowired
    public void setNotificationInstanceRepository(NotificationInstanceRepository notificationInstanceRepository) {
        this.notificationInstanceRepository = notificationInstanceRepository;
    }

    @Autowired
    public void setAttributeService(AttributeService attributeService) {
        this.attributeService = attributeService;
    }

    @Override
    public List<NotificationProviderInstanceDto> listNotificationInstances() {
        List<NotificationInstance> instances;
        instances = notificationInstanceRepository.findAll();
        if (!instances.isEmpty()) {
            return instances
                    .stream().map(NotificationInstance::mapToDto)
                    .collect(Collectors.toList());
        }
        return List.of();
    }

    @Override
    public NotificationProviderInstanceDto createNotificationInstance(NotificationProviderInstanceRequestDto request) throws AlreadyExistException {
        if (notificationInstanceRepository.findByName(request.getName()).isPresent()) {
            throw new AlreadyExistException(NotificationInstance.class, request.getName());
        }

        final String url = AttributeDefinitionUtils.getSingleItemAttributeContentValue(
                Attributes.DATA_WEBHOOK_URL_NAME, request.getAttributes(), StringAttributeContent.class).getData();

        final ContentType contentType = ContentType.fromContentType(
                AttributeDefinitionUtils.getSingleItemAttributeContentValue(
                        Attributes.DATA_CONTENT_TYPE_NAME, request.getAttributes(), StringAttributeContent.class).getData()
        );

        String contentTemplate = null;
        if (contentType != ContentType.RAW_JSON) {
            contentTemplate = AttributeDefinitionUtils.getSingleItemAttributeContentValue(
                    Attributes.DATA_CONTENT_TEMPLATE_NAME, request.getAttributes(), CodeBlockAttributeContent.class).getData().getCode();
        }

        NotificationInstance notificationInstance = new NotificationInstance();
        notificationInstance.setUuid(UUID.randomUUID().toString());
        notificationInstance.setName(request.getName());
        notificationInstance.setUrl(url);
        notificationInstance.setContentType(contentType);
        notificationInstance.setContentTemplate(contentTemplate);
        notificationInstance.setAttributes(AttributeDefinitionUtils.mergeAttributes(attributeService.getAllDataAttributes(request.getKind()), request.getAttributes()));

        notificationInstanceRepository.save(notificationInstance);

        return notificationInstance.mapToDto();
    }

    @Override
    public NotificationProviderInstanceDto getNotificationInstance(UUID uuid) throws NotFoundException {
        return notificationInstanceRepository.findByUuid(uuid)
                .orElseThrow(() -> new NotFoundException(NotificationInstance.class, uuid))
                .mapToDto();
    }

    @Override
    public NotificationProviderInstanceDto updateNotificationInstance(UUID uuid, NotificationProviderInstanceRequestDto request) throws NotFoundException {
        NotificationInstance notificationInstance = notificationInstanceRepository
                .findByUuid(uuid)
                .orElseThrow(() -> new NotFoundException(NotificationInstance.class, uuid));

        final String url = AttributeDefinitionUtils.getSingleItemAttributeContentValue(
                Attributes.DATA_WEBHOOK_URL_NAME, request.getAttributes(), StringAttributeContent.class).getData();

        final ContentType contentType = ContentType.fromContentType(
                AttributeDefinitionUtils.getSingleItemAttributeContentValue(
                        Attributes.DATA_CONTENT_TYPE_NAME, request.getAttributes(), StringAttributeContent.class).getData()
        );

        String contentTemplate = null;
        if (contentType != ContentType.RAW_JSON) {
            contentTemplate = AttributeDefinitionUtils.getSingleItemAttributeContentValue(
                    Attributes.DATA_CONTENT_TEMPLATE_NAME, request.getAttributes(), CodeBlockAttributeContent.class).getData().getCode();
        }

        notificationInstance.setUrl(url);
        notificationInstance.setContentType(contentType);
        notificationInstance.setContentTemplate(contentTemplate);
        notificationInstance.setAttributes(AttributeDefinitionUtils.mergeAttributes(attributeService.getAllDataAttributes(request.getKind()), request.getAttributes()));

        notificationInstanceRepository.save(notificationInstance);

        return notificationInstance.mapToDto();
    }

    @Override
    public void removeNotificationInstance(UUID uuid) throws NotFoundException {
        NotificationInstance instance = notificationInstanceRepository.findByUuid(uuid)
                .orElseThrow(() -> new NotFoundException(NotificationInstance.class, uuid));

        notificationInstanceRepository.delete(instance);
    }

    @Override
    public void sendNotification(UUID uuid, NotificationProviderNotifyRequestDto request) throws NotFoundException {
        logger.info("Received request to send webhook: eventType={}, resource={}", request.getEventType(), request.getResource());
        NotificationInstance notificationInstance = notificationInstanceRepository
                .findByUuid(uuid)
                .orElseThrow(() -> new NotFoundException(NotificationInstance.class, uuid));

        logger.debug("Request to send webhook received with the content: {}", request);

        // send request data to webhook URL as POST JSON request
        // create NotificationException if the request fails
        // + add timestamp to header X-CZERTAINLY-Timestamp
        // + add random nonce to header X-CZERTAINLY-Nonce
        String url = notificationInstance.getUrl();
        String timestamp = String.valueOf(System.currentTimeMillis());
        // nonce has at least 128-bit entropy and its valus is Base64 encoded
        String nonce = Base64.getEncoder().encodeToString(UUID.randomUUID().toString().getBytes());

        Object content;
        ContentType contentType = notificationInstance.getContentType();
        if (contentType == ContentType.RAW_JSON) {
            content = request;
        } else {
            String contentTemplate = notificationInstance.getContentTemplate();
            content = TemplateUtils.processFreeMarkerTemplate(contentTemplate, request);
        }

        logger.info("Sending webhook to: {}, with timestamp {}, and nonce {}", url, timestamp, nonce);

        WebClient.builder()
                .baseUrl(url)
                .defaultHeader("Content-Type", notificationInstance.getContentType().getContentHeader())
                .defaultHeader("X-CZERTAINLY-Timestamp", timestamp)
                .defaultHeader("X-CZERTAINLY-Nonce", nonce)
                .build()
                .post()
                .bodyValue(content)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), clientResponse -> {
                    logger.error("Failed to send webhook to {}: {}", url, clientResponse.statusCode());
                    return Mono.error(new NotificationException("Failed to send webhook to " + url + ": " + clientResponse.statusCode()));
                })
                .bodyToMono(Void.class)
                .doOnError(e -> logger.error("Error sending webhook to {}: {}", url, e.getMessage()))
                .subscribe();

        logger.info("Webhook sent to: {}", url);
    }

}
