package org.grpctest.core.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.grpctest.core.service.java.MavenInvoker;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class CoreService implements InitializingBean {

    private final MavenInvoker mavenInvoker;

    @Override
    public void afterPropertiesSet() throws Exception {
        mavenInvoker
                .addMvnGoal(MavenInvoker.MavenGoal.CLEAN)
                .addMvnGoal(MavenInvoker.MavenGoal.INSTALL)
                .addParam("DskipTests", "")
                .execute();
        log.info("Finished compiling .proto file for Java");
    }
}
