package kafka;

import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.message.MessageAndMetadata;
import kafka.serializer.Decoder;
import kafka.serializer.StringDecoder;
import kafka.utils.VerifiableProperties;

import java.util.*;

/**
 * Created by lucky on 2017/7/22.
 */
public class HelloWorldConsumer {
    private void execMsgConsume() {
        Properties props = new Properties();
        props.put("zookeeper.connect", "localhost:2181");
        props.put("group.id", "group-1");
        props.put("serializer.class", "kafka.serializer.StringEncoder");

        ConsumerConfig config = new ConsumerConfig(props);
        ConsumerConnector consumer = Consumer.createJavaConsumerConnector(config);

        Map<String, Integer> topicCountMap = new HashMap<String, Integer>();
        topicCountMap.put("page_visits", 1);
        Decoder<String> keyDecoder = new StringDecoder(new VerifiableProperties());
        Decoder<String> valueDecoder = new StringDecoder(new VerifiableProperties());
        Map<String, List
                <KafkaStream<String, String>>> createMessageStreams = consumer.createMessageStreams(topicCountMap, keyDecoder, valueDecoder);
        for (Iterator<String> it = createMessageStreams.keySet().iterator(); it.hasNext(); ) {
            String key = it.next();
            System.out.println("The key of the createMessageStreams is " + key);
            List<KafkaStream<String, String>> values = createMessageStreams.get(key);
            for (KafkaStream<String, String> value : values) {
                ConsumerIterator<String, String> consumerIt = value.iterator();
                while (consumerIt.hasNext()) {
                    MessageAndMetadata<String, String> data = consumerIt.next();
                    System.out.println("The message got by consuer is " + data.message());
                }
            }
        }

    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        HelloWorldConsumer simpleConsumer = new HelloWorldConsumer();
        simpleConsumer.execMsgConsume();
    }
}
