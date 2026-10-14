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

package org.opengroup.osdu.indexerqueue.azure.queue;

import com.microsoft.azure.servicebus.SubscriptionClient;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.azure.servicebus.ISubscriptionClientFactory;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.indexerqueue.azure.di.AzureBootstrapConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SubscriptionClientFactoryTest {
    @InjectMocks
    private SubscriptionClientFactory sut;

    @Mock
    private SubscriptionClient subscriptionClient;

    @Mock
    private ISubscriptionClientFactory subscriptionClientFactory;

    @Mock
    private AzureBootstrapConfig azureBootstrapConfig;

    private static final String sbTopic = "testTopic";
    private static final String sbSubscription = "testSubscription";
    private static final String dataPartition = "testPartition";

    @Test
    public void subscriptionClientShouldNotBeNull() throws ServiceBusException, InterruptedException {
        when(subscriptionClientFactory.getClient(dataPartition, sbTopic, sbSubscription))
                .thenReturn(subscriptionClient);

        SubscriptionClient result = sut.getSubscriptionClient(dataPartition, sbTopic, sbSubscription);
        assertNotNull(result);
        assertEquals(subscriptionClient, result);
    }

    @Test
    public void shouldThrow_when_subscriptionClientFactory_ThrowsInterruptedException() throws ServiceBusException, InterruptedException {
        doThrow(new InterruptedException()).when(subscriptionClientFactory).getClient(dataPartition, sbTopic, sbSubscription);

        try {
            SubscriptionClient result = sut.getSubscriptionClient(dataPartition, sbTopic, sbSubscription);
        } catch (Exception e) {
            assertEquals(e.getClass(), AppException.class);
            assertEquals(e.getMessage(), "Unexpected error creating Subscription Client");
        }
    }

    @Test
    public void shouldThrow_when_subscriptionClientFactory_ThrowsServiceBusException() throws ServiceBusException, InterruptedException {
        doThrow(new ServiceBusException(true)).when(subscriptionClientFactory).getClient(dataPartition, sbTopic, sbSubscription);

        try {
            SubscriptionClient result = sut.getSubscriptionClient(dataPartition, sbTopic, sbSubscription);
        } catch (Exception e) {
            assertEquals(e.getClass(), AppException.class);
            assertEquals(e.getMessage(), "Unexpected error creating Subscription Client");
        }
    }
}
