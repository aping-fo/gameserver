package kafka;


import com.google.common.collect.Lists;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.util.Properties;

/**
 * Created by lucky on 2017/7/22.
 */
public class LogConsumer implements Runnable {
    KafkaConsumer<String, String> consumer;

    private void execMsgConsume() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092,localhost:9093,localhost:9094");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "group-2");
        consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Lists.newArrayList("gamelogs"));
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        LogConsumer simpleConsumer = new LogConsumer();
        simpleConsumer.execMsgConsume();
        new Thread(simpleConsumer).start();
    }

    @Override
    public void run() {
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(200);   // 本例使用200ms作为获取超时时间
            if(records != null) {
                for (ConsumerRecord<String, String> record : records) {
                    // 这里面写处理消息的逻辑，本例中只是简单地打印消息
                    System.out.println(Thread.currentThread().getName() + " consumed " + record.partition() +
                            "th message with offset: " + record.offset());
                    System.out.println(record.value());
                }
            }
        }
    }
}
