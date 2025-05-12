package com.czertainly.np.webhook.attribute;

import com.czertainly.api.model.common.attribute.v2.AttributeType;
import com.czertainly.api.model.common.attribute.v2.DataAttribute;
import com.czertainly.api.model.common.attribute.v2.GroupAttribute;
import com.czertainly.api.model.common.attribute.v2.InfoAttribute;
import com.czertainly.api.model.common.attribute.v2.callback.AttributeCallback;
import com.czertainly.api.model.common.attribute.v2.callback.AttributeCallbackMapping;
import com.czertainly.api.model.common.attribute.v2.callback.AttributeValueTarget;
import com.czertainly.api.model.common.attribute.v2.constraint.RegexpAttributeConstraint;
import com.czertainly.api.model.common.attribute.v2.content.*;
import com.czertainly.api.model.common.attribute.v2.content.data.CodeBlockAttributeContentData;
import com.czertainly.api.model.common.attribute.v2.properties.DataAttributeProperties;
import com.czertainly.api.model.common.attribute.v2.properties.InfoAttributeProperties;

import java.util.*;
import java.util.stream.Collectors;

public class Attributes {

    public static final String DATA_WEBHOOK_URL_UUID = "3b8a11b3-a59d-427c-9491-56c8ce27cee7";
    public static final String DATA_WEBHOOK_URL_NAME = "data_webhookUrl";
    public static final String DATA_WEBHOOK_URL_DESCRIPTION = "Webhook URL to send the event data to";
    public static final String DATA_WEBHOOK_URL_LABEL = "Webhook URL";

    public static final String DATA_CONTENT_TYPE_UUID = "b104d74d-8a54-4aa3-9e00-9c535f8bb80c";
    public static final String DATA_CONTENT_TYPE_NAME = "data_contentType";
    public static final String DATA_CONTENT_TYPE_DESCRIPTION = "Content type of the data to be sent";
    public static final String DATA_CONTENT_TYPE_LABEL = "Content type";

    public static final String GROUP_CONTENT_TEMPLATE_UUID = "f3b9ae81-279b-4886-a097-a8e08c2c356b";
    public static final String GROUP_CONTENT_TEMPLATE_NAME = "group_contentTemplate";
    public static final String GROUP_CONTENT_TEMPLATE_DESCRIPTION = "Content template for the webhook to be sent in selected type";

    public static final String DATA_CONTENT_TEMPLATE_UUID = "1b247b77-e9c0-45b9-8114-06377cbeedc7";
    public static final String DATA_CONTENT_TEMPLATE_NAME = "data_contentTemplate";
    public static final String DATA_CONTENT_TEMPLATE_DESCRIPTION = "Content template of the data to be sent";
    public static final String DATA_CONTENT_TEMPLATE_LABEL = "Content template";

    public static final String INFO_CONTENT_TYPE_UUID = "5f2ef01f-4cc3-441e-be78-5dfdf2c4c2d9";
    public static final String INFO_CONTENT_TYPE_NAME = "info_contentType";
    public static final String INFO_CONTENT_TYPE_DESCRIPTION = "Information about the content type";
    public static final String INFO_CONTENT_TYPE_LABEL = "Content type information";

    public static InfoAttribute infoContentType() {
        InfoAttribute attribute = new InfoAttribute();

        attribute.setUuid(INFO_CONTENT_TYPE_UUID);
        attribute.setName(INFO_CONTENT_TYPE_NAME);
        attribute.setDescription(INFO_CONTENT_TYPE_DESCRIPTION);
        attribute.setType(AttributeType.INFO);
        attribute.setContentType(AttributeContentType.TEXT);
        InfoAttributeProperties properties = new InfoAttributeProperties();
        properties.setLabel(INFO_CONTENT_TYPE_LABEL);
        properties.setVisible(true);
        attribute.setProperties(properties);

        String content = """
                The content type of the data to be sent to the webhook. The following content types are supported:
                - `RAW_JSON` - will send the data as raw JSON to the specified webhook URL
                - `JSON` - prepares the data in JSON format according to the specified template
                - `XML` - prepares the data in XML format according to the specified template
                
                The template support FreeMarker syntax with variables that can be used to build the content dynamically.
                """;

        attribute.setContent(List.of(new TextAttributeContent(content)));

        return attribute;
    }

    public static DataAttribute dataWebhookUrl() {
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

    public static DataAttribute dataContentType() {
        DataAttribute attribute = new DataAttribute();

        attribute.setUuid(DATA_CONTENT_TYPE_UUID);
        attribute.setName(DATA_CONTENT_TYPE_NAME);
        attribute.setDescription(DATA_CONTENT_TYPE_DESCRIPTION);
        attribute.setContentType(AttributeContentType.STRING);
        attribute.setType(AttributeType.DATA);

        DataAttributeProperties attributeProperties = new DataAttributeProperties();
        attributeProperties.setLabel(DATA_CONTENT_TYPE_LABEL);
        attributeProperties.setRequired(true);
        attributeProperties.setReadOnly(false);
        attributeProperties.setVisible(true);
        attributeProperties.setList(true);
        attributeProperties.setMultiSelect(false);
        attribute.setProperties(attributeProperties);

        List<BaseAttributeContent> contentList = Arrays.stream(ContentType.values())
                .map(type -> new StringAttributeContent(type.getContentType(), type.name()))
                .collect(Collectors.toList());
        attribute.setContent(contentList);

        return attribute;
    }

    public static GroupAttribute groupContentTemplate() {
        GroupAttribute attribute = new GroupAttribute();

        attribute.setUuid(GROUP_CONTENT_TEMPLATE_UUID);
        attribute.setName(GROUP_CONTENT_TEMPLATE_NAME);
        attribute.setDescription(GROUP_CONTENT_TEMPLATE_DESCRIPTION);
        attribute.setType(AttributeType.GROUP);

        Set<AttributeCallbackMapping> mappings = new HashSet<>();
        mappings.add(new AttributeCallbackMapping(DATA_CONTENT_TYPE_NAME + ".data", "contentType", AttributeValueTarget.PATH_VARIABLE));
        AttributeCallback attributeCallback = new AttributeCallback();
        attributeCallback.setCallbackContext("/v1/notificationProvider/callbacks/template/{contentType}/attributes");
        attributeCallback.setCallbackMethod("GET");
        attributeCallback.setMappings(mappings);
        attribute.setAttributeCallback(attributeCallback);

        return attribute;
    }

    public static DataAttribute dataContentTemplate(ContentType contentType) {
        DataAttribute attribute = new DataAttribute();

        attribute.setUuid(DATA_CONTENT_TEMPLATE_UUID);
        attribute.setName(DATA_CONTENT_TEMPLATE_NAME);
        attribute.setDescription(DATA_CONTENT_TEMPLATE_DESCRIPTION);
        attribute.setContentType(AttributeContentType.CODEBLOCK);
        attribute.setType(AttributeType.DATA);

        DataAttributeProperties attributeProperties = new DataAttributeProperties();
        attributeProperties.setLabel(DATA_CONTENT_TEMPLATE_LABEL);
        attributeProperties.setRequired(true);
        attributeProperties.setReadOnly(false);
        attributeProperties.setVisible(true);
        attributeProperties.setList(false);
        attributeProperties.setMultiSelect(false);
        attribute.setProperties(attributeProperties);

        List<BaseAttributeContent> content = new ArrayList<>();
        CodeBlockAttributeContent attributeContent = new CodeBlockAttributeContent();
        CodeBlockAttributeContentData data = new CodeBlockAttributeContentData();
        data.setLanguage(contentType == null ? null : contentType.getLanguage());
        attributeContent.setData(data);
        content.add(attributeContent);
        attribute.setContent(content);

        return attribute;
    }

}
