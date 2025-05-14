package com.czertainly.np.webhook.service.impl;

import com.czertainly.api.exception.ValidationError;
import com.czertainly.api.exception.ValidationException;
import com.czertainly.api.model.client.attribute.RequestAttributeDto;
import com.czertainly.api.model.common.attribute.v2.BaseAttribute;
import com.czertainly.core.util.AttributeDefinitionUtils;
import com.czertainly.np.webhook.attribute.Attributes;
import com.czertainly.np.webhook.attribute.ContentType;
import com.czertainly.np.webhook.service.AttributeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AttributeServiceImpl implements AttributeService {

    private static final Logger logger = LoggerFactory.getLogger(AttributeServiceImpl.class);

    @Override
    public List<BaseAttribute> getAttributes(String kind) {
        logger.debug("Getting the attributes for {}", kind);

        if (!kind.equals("WEBHOOK")) {
            throw new ValidationException(ValidationError.create("Unsupported kind {}", kind));
        }

        List<BaseAttribute> attributes = new ArrayList<>();
        attributes.add(Attributes.dataWebhookUrl());
        attributes.add(Attributes.infoContentType());
        attributes.add(Attributes.dataContentType());
        attributes.add(Attributes.groupContentTemplate());

        return attributes;
    }

    public List<BaseAttribute> getAllDataAttributes(String kind, ContentType contentType) {
        if (!kind.equals("WEBHOOK")) {
            throw new ValidationException(ValidationError.create("Unsupported kind {}", kind));
        }

        List<BaseAttribute> attributes = new ArrayList<>();
        attributes.add(Attributes.dataWebhookUrl());
        attributes.add(Attributes.dataContentType());
        attributes.add(Attributes.dataContentTemplate(contentType));

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

}
