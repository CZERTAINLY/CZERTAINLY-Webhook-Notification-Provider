package com.czertainly.np.webhook.api;

import com.czertainly.api.interfaces.connector.InfoController;
import com.czertainly.api.model.client.connector.InfoResponse;
import com.czertainly.api.model.core.connector.FunctionGroupCode;
import com.czertainly.np.webhook.EndpointsListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class InfoControllerImpl implements InfoController {
    private static final Logger logger = LoggerFactory.getLogger(InfoControllerImpl.class);

    @Autowired
    public void setEndpointsListener(EndpointsListener endpointsListener) {
        this.endpointsListener = endpointsListener;
    }

    private EndpointsListener endpointsListener;

    @Override
    public List<InfoResponse> listSupportedFunctions() {
        logger.debug("Listing the end points for Webhook Notification Provider");
        List<String> kinds = List.of("WEBHOOK");
        List<InfoResponse> functions = new ArrayList<>();
        functions.add(new InfoResponse(
                kinds,
                FunctionGroupCode.NOTIFICATION_PROVIDER,
                endpointsListener.getEndpoints(FunctionGroupCode.NOTIFICATION_PROVIDER))
        );

        return functions;
    }
}
