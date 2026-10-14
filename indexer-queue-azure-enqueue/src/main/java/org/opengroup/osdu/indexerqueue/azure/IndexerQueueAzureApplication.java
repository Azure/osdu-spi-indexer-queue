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

package org.opengroup.osdu.indexerqueue.azure;

import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import org.opengroup.osdu.indexerqueue.azure.queue.SubscriptionManager;
import org.opengroup.osdu.indexerqueue.azure.scope.thread.ThreadScopeBeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(value = {
        "org.opengroup.osdu"
})
public class IndexerQueueAzureApplication {
    public static void main(String[] args) throws ServiceBusException, InterruptedException {
        ApplicationContext context = SpringApplication.run(IndexerQueueAzureApplication.class, args);

        SubscriptionManager subscriptionManager = context.getBean(SubscriptionManager.class);
        subscriptionManager.subscribeRecordsTopic();
    }

    @Bean
    public static BeanFactoryPostProcessor beanFactoryPostProcessor() {
        return new ThreadScopeBeanFactoryPostProcessor();
    }
}