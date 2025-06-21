package com.czertainly.np.webhook.attribute;

import com.czertainly.api.exception.ValidationError;
import com.czertainly.api.exception.ValidationException;
import com.czertainly.api.model.common.attribute.v2.content.data.ProgrammingLanguageEnum;

import java.util.Arrays;

public enum ContentType {
    RAW_JSON("raw_json", "application/json", ProgrammingLanguageEnum.JSON),
    JSON("json", "application/json", ProgrammingLanguageEnum.JSON),
    XML("xml", "application/xml", ProgrammingLanguageEnum.XML);

    private static final ContentType[] VALUES;

    static {
        VALUES = values();
    }

    private final String contentType;
    private final String contentHeader;
    private final ProgrammingLanguageEnum language;

    ContentType(String contentType, String contentHeader, ProgrammingLanguageEnum language) {
        this.contentType = contentType;
        this.contentHeader = contentHeader;
        this.language = language;
    }

    public static ContentType fromContentType(String contentType) {
        return Arrays.stream(VALUES)
                .filter(type -> type.contentType.equalsIgnoreCase(contentType))
                .findFirst()
                .orElseThrow(() ->
                        new ValidationException(ValidationError.create("Invalid content type {}", contentType)));
    }

    public String getContentType() {
        return contentType;
    }

    public String getContentHeader() {
        return contentHeader;
    }

    public ProgrammingLanguageEnum getLanguage() {
        return language;
    }
}
