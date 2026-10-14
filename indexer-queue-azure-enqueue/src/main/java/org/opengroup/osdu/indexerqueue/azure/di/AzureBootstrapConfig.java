// Copyright © Microsoft Corporation
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//      http://www.apache.org/licenses/LICENSE-2.0
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.opengroup.osdu.indexerqueue.azure.di;

import lombok.Getter;
import org.opengroup.osdu.indexerqueue.azure.util.MdcContextMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.inject.Named;

@Configuration
@Getter
public class AzureBootstrapConfig {

    private final static Logger LOGGER = LoggerFactory.getLogger(AzureBootstrapConfig.class);

    @Value("${azure.keyvault.url}")
    private String keyVaultURL;

    @Value("${azure.app.resource.id}")
    private String appResourceId;

    @Value("${executor-n-threads}")
    private String nThreads;

    @Value("${max-concurrent-calls}")
    private String maxConcurrentCalls;

    @Value("${max-lock-renew-duration-seconds}")
    private String maxLockRenewDurationInSeconds;

    @Value("${max-delivery-count}")
    private String maxDeliveryCount;

    @Value("${azure.servicebus.topic-name}")
    private String serviceBusTopic;

    @Value("${azure.servicebus.topic-subscription}")
    private String serviceBusTopicSubscription;

    @Value("${azure.reindex.topic-name}")
    private String reindexTopic;

    @Value("${azure.reindex.topic-subscription}")
    private String reindexTopicSubscription;

    @Value("${azure.schemachanged.topic-name}")
    private String schemachangedTopic;

    @Value("${azure.schemachanged.topic-subscription}")
    private String schemachangedSubscription;

    @Value("${indexer.worker.url}")
    private String indexerWorkerURL;

    @Value("${schema.worker.url}")
    private String schemaWorkerURL;

    @Value("#{new Integer('${sleep.duration.main.thread.seconds}')}")
    private Integer sleepDurationForMainThreadInSeconds;

    @Value("${spring.application.name}")
    private String appName;

    @Bean
    @Named("KEY_VAULT_URL")
    public String keyVaultURL() {
        return keyVaultURL;
    }

    @Bean
    public MdcContextMap mdcContextMap() {
        return new MdcContextMap();
    }
}
