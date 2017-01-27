/*
 * Copyright (c) 2016 Pivotal Software Inc, All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package reactor.kafka.tools.perf;

import java.util.Map;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.junit.Before;
import org.junit.Test;

import reactor.kafka.AbstractKafkaTest;
import reactor.kafka.tools.perf.ConsumerPerformance.ConsumerPerfConfig;
import reactor.kafka.tools.perf.ConsumerPerformance.NonReactiveConsumerPerformance;
import reactor.kafka.tools.perf.ConsumerPerformance.ReactiveConsumerPerformance;
import reactor.kafka.tools.perf.ProducerPerformance.ReactiveProducerPerformance;
import reactor.kafka.tools.util.PerfTestUtils;

public class ConsumerPerformanceTest extends AbstractKafkaTest {

    private int numMessages;
    private int messageSize;
    private int maxPercentDiff;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        numMessages = PerfTestUtils.getTestConfig("reactor.kafka.test.numMessages", 5000000);
        messageSize = PerfTestUtils.getTestConfig("reactor.kafka.test.messageSize", 100);
        maxPercentDiff = PerfTestUtils.getTestConfig("reactor.kafka.test.maxPercentDiff", 20);
    }

    @Test
    public void performanceRegressionTest() throws Exception {
        ConsumerPerfConfig config = new ConsumerPerfConfig();
        Map<String, Object> consumerProps = PerfTestUtils.consumerProps(embeddedKafka);

        sendToKafka(numMessages, messageSize);

        NonReactiveConsumerPerformance nonReactive = new NonReactiveConsumerPerformance(consumerProps, topic, "non-reactive", config);
        nonReactive.runTest(numMessages);
        ReactiveConsumerPerformance reactive = new ReactiveConsumerPerformance(consumerProps, topic, "reactive", config);
        reactive.runTest(numMessages);

        PerfTestUtils.verifyReactiveThroughput(reactive.recordsPerSec(), nonReactive.recordsPerSec(), maxPercentDiff);
    }

    private void sendToKafka(int numRecords, int recordSize) throws InterruptedException {
        Map<String, Object> props = PerfTestUtils.producerProps(embeddedKafka);
        props.put(ProducerConfig.RETRIES_CONFIG, Integer.MAX_VALUE);
        new ReactiveProducerPerformance(props, topic, numRecords, recordSize, -1)
                .runTest()
                .printTotal();
    }
}
