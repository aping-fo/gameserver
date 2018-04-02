package kafka;


import org.apache.kafka.clients.producer.*;

import java.util.Properties;

/**
 * Created by lucky on 2017/7/21.
 */
public class LogProducer {
    public static void main(String[] args) throws Exception{
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092,localhost:9093,localhost:9094");
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 0);//a batch size of zero will disable batching entirely
        props.put(ProducerConfig.LINGER_MS_CONFIG, 0);//send message without delay
        props.put(ProducerConfig.ACKS_CONFIG, "1");//对应partition的leader写到本地后即返回成功。极端情况下，可能导致失败
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        Producer<String, String> kafkaProducer = new KafkaProducer<>(props);
        for (long nEvents = 0; nEvents < 400000; nEvents++) {
            System.out.println(nEvents);
            ProducerRecord<String,String> msg = new ProducerRecord("gamelogs","11","111");
            try {
                kafkaProducer.send(msg,new Callback() {
                    public void onCompletion(RecordMetadata metadata, Exception e) {
                        if(e != null)
                            e.printStackTrace();
                        System.out.println("The offset of the record we just sent is: " + metadata.toString());
                    }
                });
            }catch (Exception e) {
                e.printStackTrace();
            }
            Thread.sleep(2000);
        }
    }
}
