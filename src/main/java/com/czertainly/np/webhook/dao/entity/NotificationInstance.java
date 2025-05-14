package com.czertainly.np.webhook.dao.entity;

import com.czertainly.api.model.common.attribute.v2.BaseAttribute;
import com.czertainly.api.model.common.attribute.v2.DataAttribute;
import com.czertainly.api.model.connector.notification.NotificationProviderInstanceDto;
import com.czertainly.core.util.AttributeDefinitionUtils;
import com.czertainly.np.webhook.attribute.ContentType;
import jakarta.persistence.*;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Base64;
import java.util.List;

@Entity
@Table(name = "notification_instance")
public class NotificationInstance extends UniquelyIdentified {

    @Column(name = "name")
    private String name;

    @Column(name = "url")
    private String url;

    @Column(name = "content_type")
    @Enumerated(EnumType.STRING)
    private ContentType contentType;

    @Column(name = "content_template", length = Integer.MAX_VALUE)
    private String contentTemplate;

    @Column(name = "attributes", length = Integer.MAX_VALUE)
    private String attributes;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public ContentType getContentType() {
        return contentType;
    }

    public void setContentType(ContentType contentType) {
        this.contentType = contentType;
    }

    public String getContentTemplate() {
        return new String(Base64.getDecoder().decode(contentTemplate));
    }

    /**
     * @param contentTemplate - base64 encoded content template
     */
    public void setContentTemplate(String contentTemplate) {
        this.contentTemplate = contentTemplate;
    }

    public List<BaseAttribute> getAttributes() {
        return AttributeDefinitionUtils.deserialize(attributes, BaseAttribute.class);
    }

    public void setAttributes(List<DataAttribute> attributes) {
        this.attributes = AttributeDefinitionUtils.serialize(attributes);
    }

    public NotificationProviderInstanceDto mapToDto() {
        NotificationProviderInstanceDto dto = new NotificationProviderInstanceDto();
        dto.setUuid(this.uuid.toString());
        dto.setName(this.name);

        if (attributes != null) {
            dto.setAttributes(getAttributes());
        } else { // empty list of attributes
            dto.setAttributes(AttributeDefinitionUtils.deserialize("[]", BaseAttribute.class));
        }

        return dto;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NotificationInstance that = (NotificationInstance) o;
        return new EqualsBuilder().append(uuid, that.uuid).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(uuid).toHashCode();
    }

}
