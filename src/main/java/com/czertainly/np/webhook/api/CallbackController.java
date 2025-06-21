package com.czertainly.np.webhook.api;

import com.czertainly.api.model.common.attribute.v2.BaseAttribute;
import com.czertainly.np.webhook.attribute.Attributes;
import com.czertainly.np.webhook.attribute.ContentType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/notificationProvider/callbacks")
public class CallbackController {

    @RequestMapping(
            path = "/template/{contentType}/attributes",
            method = RequestMethod.GET,
            produces = "application/json"
    )
    public List<BaseAttribute> getContentTemplateAttributes(@PathVariable ContentType contentType) {
        if (contentType == ContentType.RAW_JSON) {
            return List.of();
        }

        return List.of(
                Attributes.dataContentTemplate(contentType)
        );
    }

}
