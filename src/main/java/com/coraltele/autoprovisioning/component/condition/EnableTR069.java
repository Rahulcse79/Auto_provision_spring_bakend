package com.coraltele.autoprovisioning.component.condition;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import com.coraltele.autoprovisioning.component.helper.Constants;

public class EnableTR069 implements Condition {
    private static final Logger logger = LogManager.getLogger(EnableTR069.class);

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        logger.info("TR069 Monitoring in telemetryStatus : {} ", Boolean.TRUE.equals(Constants.ENABLE_TR069) ? "Enabled" : "Disabled");
        return Constants.ENABLE_TR069;
    }

}