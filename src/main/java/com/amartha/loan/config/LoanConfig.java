package com.amartha.loan.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "loan")
public interface LoanConfig {

    Storage storage();

    interface Storage {
        @WithDefault("/tmp/amartha-loans/uploads")
        String uploadDir();
    }
}
