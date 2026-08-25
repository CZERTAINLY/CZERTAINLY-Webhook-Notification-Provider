package com.otilm.np.webhook.service;

import com.otilm.api.model.client.attribute.RequestAttribute;
import com.otilm.api.model.common.attribute.common.BaseAttribute;
import com.otilm.np.webhook.attribute.ContentType;

import java.util.List;

public interface AttributeService {

    List<BaseAttribute> getAttributes(String kind);

    List<BaseAttribute> getAllDataAttributes(String kind, ContentType contentType);

    boolean validateAttributes(String kind, List<RequestAttribute> attributes);

}
