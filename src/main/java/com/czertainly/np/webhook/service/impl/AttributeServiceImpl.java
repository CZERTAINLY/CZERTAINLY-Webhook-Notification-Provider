package com.czertainly.np.webhook.service.impl;

import com.czertainly.api.exception.ValidationError;
import com.czertainly.api.exception.ValidationException;
import com.czertainly.api.model.client.attribute.RequestAttributeDto;
import com.czertainly.api.model.common.attribute.v2.AttributeType;
import com.czertainly.api.model.common.attribute.v2.BaseAttribute;
import com.czertainly.api.model.common.attribute.v2.DataAttribute;
import com.czertainly.api.model.common.attribute.v2.constraint.RegexpAttributeConstraint;
import com.czertainly.api.model.common.attribute.v2.content.AttributeContentType;
import com.czertainly.api.model.common.attribute.v2.content.BaseAttributeContent;
import com.czertainly.api.model.common.attribute.v2.content.StringAttributeContent;
import com.czertainly.api.model.common.attribute.v2.properties.DataAttributeProperties;
import com.czertainly.core.util.AttributeDefinitionUtils;
import com.czertainly.np.webhook.service.AttributeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AttributeServiceImpl implements AttributeService {

    private static final Logger logger = LoggerFactory.getLogger(AttributeServiceImpl.class);

    public static final String DATA_WEBHOOK_URL_UUID = "3b8a11b3-a59d-427c-9491-56c8ce27cee7";
    public static final String DATA_WEBHOOK_URL_NAME = "data_webhookUrl";
    public static final String DATA_WEBHOOK_URL_DESCRIPTION = "Webhook URL to send the event data to";
    public static final String DATA_WEBHOOK_URL_LABEL = "Webhook URL";

    @Override
    public List<BaseAttribute> getAttributes(String kind) {
        logger.debug("Getting the attributes for {}", kind);

        if (!kind.equals("WEBHOOK")) {
            throw new ValidationException(ValidationError.create("Unsupported kind {}", kind));
        }

        List<BaseAttribute> attributes = new ArrayList<>();
        attributes.add(dataWebhookUrl());

        return attributes;
    }

    @Override
    public boolean validateAttributes(String kind, List<RequestAttributeDto> attributes) {
        logger.debug("Validating the attributes for kind {} with attributes: {}", kind, attributes);

        if (!kind.equals("WEBHOOK")) {
            throw new ValidationException(ValidationError.create("Unsupported kind {}", kind));
        }
        if (attributes == null) {
            return false;
        }

        AttributeDefinitionUtils.validateAttributes(getAttributes(kind), attributes);
        return true;
    }

    private DataAttribute dataWebhookUrl() {
        DataAttribute attribute = new DataAttribute();

        attribute.setUuid(DATA_WEBHOOK_URL_UUID);
        attribute.setName(DATA_WEBHOOK_URL_NAME);
        attribute.setDescription(DATA_WEBHOOK_URL_DESCRIPTION);
        attribute.setContentType(AttributeContentType.STRING);
        attribute.setType(AttributeType.DATA);

        DataAttributeProperties attributeProperties = new DataAttributeProperties();
        attributeProperties.setLabel(DATA_WEBHOOK_URL_LABEL);
        attributeProperties.setRequired(true);
        attributeProperties.setReadOnly(false);
        attributeProperties.setVisible(true);
        attributeProperties.setList(false);
        attributeProperties.setMultiSelect(false);

        attribute.setProperties(attributeProperties);

        List<BaseAttributeContent> content = new ArrayList<>();
        StringAttributeContent attributeContent = new StringAttributeContent("https://example.com/webhook");
        content.add(attributeContent);
        attribute.setContent(content);

        // create restrictions
        RegexpAttributeConstraint regexpAttributeConstraint = new RegexpAttributeConstraint();
        regexpAttributeConstraint.setDescription("Webhook URL");
        regexpAttributeConstraint.setErrorMessage("Invalid webhook URL format");
        regexpAttributeConstraint.setData("https?://.*");
        attribute.setConstraints(List.of(regexpAttributeConstraint));

        return attribute;
    }

}
