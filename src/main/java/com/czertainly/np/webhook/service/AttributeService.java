package com.czertainly.np.webhook.service;

import com.czertainly.api.model.client.attribute.RequestAttributeDto;
import com.czertainly.api.model.common.attribute.v2.BaseAttribute;
import com.czertainly.np.webhook.attribute.ContentType;

import java.util.List;

public interface AttributeService {

    List<BaseAttribute> getAttributes(String kind);

    List<BaseAttribute> getAllDataAttributes(String kind, ContentType contentType);

    boolean validateAttributes(String kind, List<RequestAttributeDto> attributes);

}
