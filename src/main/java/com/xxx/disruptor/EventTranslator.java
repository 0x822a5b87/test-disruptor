package com.xxx.disruptor;

import com.lmax.disruptor.EventTranslatorOneArg;

/**
 * @author 0x822a5b87
 */
public class EventTranslator implements EventTranslatorOneArg<Element<String>, String> {
    @Override
    public void translateTo(Element<String> event, long sequence, String arg0) {
        event.setValue(arg0);
    }
}
