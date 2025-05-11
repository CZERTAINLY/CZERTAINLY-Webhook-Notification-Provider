package com.czertainly.np.webhook.dao.entity;

import com.czertainly.api.model.common.attribute.v2.BaseAttribute;
import com.czertainly.api.model.connector.notification.NotificationProviderInstanceDto;
import com.czertainly.core.util.AttributeDefinitionUtils;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@Entity
@Table(name = "notification_instance")
public class NotificationInstance extends UniquelyIdentified {

    @Column(name = "name")
    private String name;

    @Column(name = "url")
    private String url;

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

    public NotificationProviderInstanceDto mapToDto() {
        NotificationProviderInstanceDto dto = new NotificationProviderInstanceDto();
        dto.setUuid(this.uuid.toString());
        dto.setName(this.name);

        dto.setAttributes(AttributeDefinitionUtils.deserialize("[]", BaseAttribute.class));

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
