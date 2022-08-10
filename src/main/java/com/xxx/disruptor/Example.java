package com.xxx.disruptor;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;

import java.util.concurrent.ThreadFactory;

/**
 * @author 0x822a5b87
 */
public class Example {

    private EventTranslator translator = new EventTranslator();

    private ThreadFactory newThreadFactory() {
        /*
         * 生产者的线程工厂
         */
        return new ThreadFactoryBuilder().setNameFormat("disruptor-thread-%d").build();
    }

    private EventFactory<Element<String>> newEventFactory() {
        /*
         * RingBuffer生产工厂,初始化RingBuffer的时候使用
         */
        return () -> {
            Element<String> element = new Element<>();
            element.setValue("-1");
            return element;
        };
    }

    private EventHandler<Element<String>> newEventHandler() {
        /*
         * 处理Event的handler
         */
        return (element, sequence, endOfBatch) -> System.out.println("Element: " + element.getValue());
    }

    private RingBuffer<Element<String>> startDisruptor() {
        ThreadFactory                 threadFactory = newThreadFactory();
        EventFactory<Element<String>> eventFactory  = newEventFactory();
        EventHandler<Element<String>> eventHandler  = newEventHandler();
        // 阻塞策略
        BlockingWaitStrategy strategy = new BlockingWaitStrategy();

        // 指定RingBuffer的大小
        int bufferSize = 8;

        // 创建disruptor，采用单生产者模式
        Disruptor<Element<String>> disruptor = new Disruptor<Element<String>>(eventFactory,
                                                                              bufferSize,
                                                                              threadFactory,
                                                                              ProducerType.SINGLE,
                                                                              strategy);
        // 设置EventHandler
        disruptor.handleEventsWith(eventHandler);
        // 启动disruptor的线程
        return disruptor.start();
    }

    private void startWithPublish() throws InterruptedException {
        RingBuffer<Element<String>> ringBuffer = startDisruptor();
        while (true) {
            // 获取下一个可用位置的下标
            long sequence = ringBuffer.next();
            try {
                // 返回可用位置的元素
                Element<String> event   = ringBuffer.get(sequence);
                String          current = event.getValue();
                current += "-" + sequence;
                event.setValue(current);
            } finally {
                ringBuffer.publish(sequence);
            }
            Thread.sleep(500);
        }
    }

    private void startWithPublishEvent() throws InterruptedException {
        RingBuffer<Element<String>> ringBuffer = startDisruptor();
        for (int i = 0; true; ++i) {
            long current = ringBuffer.getCursor();
            Element<String> element = ringBuffer.get(current);
            System.out.println("element: " + element.getValue() + ", current = " + current);
            ringBuffer.tryPublishEvent(translator, String.valueOf(i));
            Thread.sleep(500);
        }
    }

    public static void main(String[] args) throws Exception {
        Example example = new Example();
        example.startWithPublishEvent();
    }
}
