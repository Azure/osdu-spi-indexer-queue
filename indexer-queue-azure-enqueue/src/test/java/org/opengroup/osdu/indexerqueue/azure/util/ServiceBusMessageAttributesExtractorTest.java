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

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.servicebus.MessageBody;

@ExtendWith(MockitoExtension.class)
public class ServiceBusMessageAttributesExtractorTest {

    private static final String CORRELATION_ID = "d84f0c58-f2c6-446f-b246-d79ccdb6a4bd";
    private static final String DATA_PARTITION_ID = "opendes";
    private static final String CORRECT_MESSAGE_BODY = "{\n"
      + "    \"message\": {\n"
      + "        \"data\": [\n"
      + "            {\n"
      + "                \"id\": \"testId\",\n"
      + "                \"kind\": \"testKind\",\n"
      + "                \"op\": \"update\"\n"
      + "            }\n"
      + "        ],\n"
      + "        \"data-partition-id\": \"" + DATA_PARTITION_ID + "\",\n"
      + "        \"correlation-id\": \"" + CORRELATION_ID + "\"\n"
      + "    }\n"
      + "}";

  private static final String INCORRECT_MESSAGE_BODY = "{\n"
    + "    \"message\": \"wrong\""
    + "}";

    private final ServiceBusMessageAttributesExtractor extractor = new ServiceBusMessageAttributesExtractor();
    @Mock
    private IMessage message;
    @Mock
    private MessageBody messageBody;

    @BeforeEach
    public void setup() {
        when(message.getMessageBody()).thenReturn(messageBody);
    }

    @Test
    public void shouldExtractAttributesSuccessfully() {
        when(messageBody.getBinaryData()).thenReturn(singletonList(CORRECT_MESSAGE_BODY.getBytes(UTF_8)));

        RecordChangedAttributes recordChangedAttributes = extractor.extracRecordChangedtAttributesFromMessageBody(message);
        SchemaChangedAttributes schemaChangedAttributes = extractor.extractSchemaChangedAttributesFromMessageBody(message);

        assertEquals(CORRELATION_ID, recordChangedAttributes.getCorrelationId());
        assertEquals(DATA_PARTITION_ID, recordChangedAttributes.getDataPartitionId());
        assertEquals(CORRELATION_ID, schemaChangedAttributes.getCorrelationId());
        assertEquals(DATA_PARTITION_ID, schemaChangedAttributes.getDataPartitionId());
    }

    @Test
    public void shouldNotFailIfMessageBodyIsEmpty() {
        when(messageBody.getBinaryData()).thenReturn(singletonList(EMPTY.getBytes(UTF_8)));

        RecordChangedAttributes recordChangedAttributes = extractor.extracRecordChangedtAttributesFromMessageBody(message);
        SchemaChangedAttributes schemaChangedAttributes = extractor.extractSchemaChangedAttributesFromMessageBody(message);

        assertNull(recordChangedAttributes.getDataPartitionId(), recordChangedAttributes.getCorrelationId());
        assertNull(schemaChangedAttributes.getDataPartitionId(), schemaChangedAttributes.getCorrelationId());
    }

    @Test
    public void shouldNotFailIfMessageBodyHasIncorrectStructure() {
        when(messageBody.getBinaryData()).thenReturn(singletonList(INCORRECT_MESSAGE_BODY.getBytes(UTF_8)));

        RecordChangedAttributes recordChangedAttributes = extractor.extracRecordChangedtAttributesFromMessageBody(message);
        SchemaChangedAttributes schemaChangedAttributes = extractor.extractSchemaChangedAttributesFromMessageBody(message);

        assertNull(recordChangedAttributes.getDataPartitionId(), recordChangedAttributes.getCorrelationId());
        assertNull(schemaChangedAttributes.getDataPartitionId(), schemaChangedAttributes.getCorrelationId());
    }
}
