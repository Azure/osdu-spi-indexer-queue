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

package org.opengroup.osdu.indexerqueue.azure.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class MdcContextMapTest {

    @InjectMocks
    MdcContextMap mdcContextMap;

    private static final String correlationId = "test-correlation-id";
    private static final String dataPartitionId = "test-data-partition-id";

    @Test
    public void shouldReturnContextMapWithProvidedIds() {
        Map<String, String> contextMap = mdcContextMap.getContextMap(correlationId, dataPartitionId);

        assertEquals(2, contextMap.size());
        assertEquals(correlationId, contextMap.get("correlation-id"));
        assertEquals(dataPartitionId, contextMap.get("data-partition-id"));
    }

}
