/*
 * Copyright (c) 2018-2023 VMware Inc. or its affiliates, All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package reactor.kafka.receiver.observation;

import java.nio.charset.StandardCharsets;

import io.micrometer.observation.transport.ReceiverContext;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;

/**
 * {@link ReceiverContext} for {@link ConsumerRecord}s.
 *
 * @author Gary Russell
 * @author Artem Bilan
 * @since 1.4
 *
 */
public class KafkaRecordReceiverContext extends ReceiverContext<ConsumerRecord<?, ?>> {

    private final String receiverId;

    public KafkaRecordReceiverContext(ConsumerRecord<?, ?> record, String receiverId, String kafkaServers) {
        super((carrier, key) -> {
            Header header = carrier.headers().lastHeader(key);
            if (header == null) {
                return null;
            }
            return new String(header.value(), StandardCharsets.UTF_8);
        });
        setCarrier(record);
        this.receiverId = receiverId;
        setRemoteServiceName("Apache Kafka: " + kafkaServers);
    }

    public String getReceiverId() {
        return this.receiverId;
    }

    /**
     * Return the source topic.
     * @return the source.
     */
    public String getSource() {
        return getCarrier().topic();
    }

}
