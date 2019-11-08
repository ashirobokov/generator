package ru.ashirobokov.microservices.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.FluxSink;
import ru.ashirobokov.microservices.model.InstrumentPrice;

import java.util.List;

@Component
public class ScheduledPrices {

    public final static Logger LOG = LoggerFactory.getLogger(ScheduledPrices.class);

    public static void sent(FluxSink<List<InstrumentPrice>> sink, PricesCache pricesCache) {

        ScheduledRunnable runnable = new ScheduledRunnable(sink, pricesCache);
        Thread t = new Thread(runnable, "prices-sent-thread");
        t.start();
    }

}

