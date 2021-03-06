/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.syncope.core.starter;

import java.util.Comparator;
import org.apache.syncope.core.persistence.api.DomainsHolder;
import org.apache.syncope.core.persistence.api.SyncopeCoreLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

/**
 * Take care of all initializations needed by Syncope Core to run up and safe.
 */
@Component
public class SyncopeCoreInitializer implements ApplicationListener<ContextRefreshedEvent>, Ordered {

    private static final Logger LOG = LoggerFactory.getLogger(SyncopeCoreInitializer.class);

    @Autowired
    private DomainsHolder domainsHolder;

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public void onApplicationEvent(final ContextRefreshedEvent event) {
        event.getApplicationContext().getBeansOfType(SyncopeCoreLoader.class).values().stream().
                sorted(Comparator.comparing(SyncopeCoreLoader::getOrder)).
                forEach(loader -> {
                    String loaderName = AopUtils.getTargetClass(loader).getName();

                    LOG.debug("[{}] Starting initialization", loaderName);

                    loader.load();

                    domainsHolder.getDomains().forEach((domain, datasource) -> {
                        LOG.debug("[{}] Starting on domain '{}'", loaderName, domain);
                        loader.load(domain, datasource);
                        LOG.debug("[{}] Completed on domain '{}'", loaderName, domain);
                    });

                    LOG.debug("[{}] Initialization completed", loaderName);
                });
    }
}
