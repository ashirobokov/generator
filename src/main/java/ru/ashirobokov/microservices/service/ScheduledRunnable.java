package ru.ashirobokov.microservices.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.FluxSink;
import ru.ashirobokov.microservices.model.InstrumentPrice;

import java.util.ArrayList;
import java.util.List;

public class ScheduledRunnable implements Runnable {

    FluxSink<List<InstrumentPrice>> sink;
    PricesCache pricesCache;

    public final static Logger LOG = LoggerFactory.getLogger(ScheduledRunnable.class);

    public ScheduledRunnable(FluxSink<List<InstrumentPrice>> sink, PricesCache pricesCache) {
        this.sink = sink;
        this.pricesCache = pricesCache;
    }

    public void run() {
        while (this.pricesCache.isF()) {
            try {Thread.sleep(10000);} catch (InterruptedException e) {e.printStackTrace();}
            sink.next(new ArrayList<InstrumentPrice>(this.pricesCache.getInstrumentPrices()));
        }
        sink.complete();
    }

}
