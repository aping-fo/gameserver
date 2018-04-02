package algorithm;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by aping.foo
 * RingBuffer 是 Distruptor 中的一个用来替代 ArrayBlockingQueue 的队列,
 * 它的思想在于长度可控, 且无锁, 只有在 blocking 的时候(没有数据的时候出队, 数据满的时候入队)会自旋.
 * 实现原理是使用一个环形array, 生产者作为 tail, 消费者作为 head, 每生产一次 tail atomic++,
 * 每消费一次 head atomic++, tail 不能超过 head 一圈(array size, 即队列满时 blocking),
 * tail 不能超过自己tail一圈(即不能覆盖未被消费的值), head 不能超过 tail (即无可消费任务时 blocking),
 * head 不能取到空值(取到空值时 blocking). blocking 使用一个 while 自旋来完成,
 * 那么只要生产者消费者的速度相当时, 即可通过 atomicInteger(cas)
 * 保证无锁, 而如果你需要在 blocking 的时候立即返回,
 * 则 while 自旋都可以不需要. 相比于 ArrayBlockingQueue,
 * 它可以绝大部分时间无锁, blocking 自旋, 相比于 concurrentLinkedQueue, 他又能做到长度限制.
 * 模拟实现下 这个东东
 */
public class Exercise19 {

    public static void main(String[] args) {

    }


    public static class RingBuffer<T> implements Serializable {
        private static final long serialVersionUID = 6976960108708949038L;

        private volatile AtomicInteger head;

        private volatile AtomicInteger tail;

        private int length;

        final T EMPTY = null;

        private volatile T[] queue;

        public RingBuffer(Class<T> type, int length) {
            this.head = new AtomicInteger(0);
            this.tail = new AtomicInteger(0);
            this.length = length == 0 ? 2 << 16 : length; // 默认2^16
            this.queue = (T[]) Array.newInstance(type, this.length);
        }

        public void enQueue(T t) {
            if (t == null) t = (T) new Object();
            // 阻塞 -- 避免多生成者循环生产同一个节点
            while (this.getTail() - this.getHead() >= this.length) ;
            int ctail = this.tail.getAndIncrement();
            while (this.queue[this.getTail(ctail)] != EMPTY) ; // 自旋
            this.queue[this.getTail(ctail)] = t;
        }

        public T deQueue() {
            T t = null;
            // 阻塞 -- 避免多消费者循环消费同一个节点
            while (this.head.get() >= this.tail.get()) ;
            int chead = this.head.getAndIncrement();
            while (this.queue[this.getHead(chead)] == EMPTY) ; // 自旋
            t = this.queue[this.getHead(chead)];
            this.queue[this.getHead(chead)] = EMPTY;
            return t;
        }

        public int getHead(int index) {
            return index & (this.length - 1);
        }

        public int getTail(int index) {
            return index & (this.length - 1);
        }

        public int getHead() {
            return head.get() & (this.length - 1);
        }

        public int getTail() {
            return tail.get() & (this.length - 1);
        }

        public T[] getQueue() {
            return queue;
        }

        public int getLength() {
            return length;
        }

        public void setLength(int length) {
            this.length = length;
        }
    }
}
