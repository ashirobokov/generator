package ru.ashirobokov.microservices.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.ashirobokov.microservices.model.InstrumentPrice;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import static java.util.concurrent.TimeUnit.SECONDS;

@Component
public class PricesCache {
    public final static Logger LOG = LoggerFactory.getLogger(PricesCache.class);
//  bool 'f' is a mark to be checked whether generation is stopped or not yet;
    private boolean f;
    private final static Long[] instruments = {1L, 2L, 3L, 4L, 5L,
            11L, 12L, 13L, 14L, 15L,
            122L, 123L, 124L, 125L, 126L};
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1,
            runnable -> {
                Thread thread = new Thread(runnable);
                thread.setName("price-cache-thread");
                return thread;
            });

//    private List<InstrumentPrice> instrumentPrices = new ArrayList<>();
//    used CopyOnWriteArrayList from java.util.concurrent so will be consumed by different threads
    private CopyOnWriteArrayList<InstrumentPrice> instrumentPrices = new CopyOnWriteArrayList<>();

    public PricesCache() {
        this.f = true;
        Arrays.asList(instruments).forEach(instrument -> {
            instrumentPrices.add(new InstrumentPrice(instrument, null));
        });
    }

    public List<InstrumentPrice> getInstrumentPrices() {
        return instrumentPrices;
    }

    public boolean isF() {
        return f;
    }

    public void setF(boolean f) {
        this.f = f;
    }

    private void consumePrices() {
        LOG.info("PricesCache...consumePrices...started");
        final Runnable consumer = new Runnable() {
            public void run() {
                Long timestamp = System.currentTimeMillis();
                instrumentPrices.forEach(instrument -> {
                    LOG.info(instrumentPrices.toString());
                });
            }
        };
        final ScheduledFuture<?> generateHandler =
                scheduler.scheduleAtFixedRate(consumer, 1, 10, SECONDS);
        scheduler.schedule(new Runnable() {
            public void run() {
                generateHandler.cancel(true);
            }
        }, 60 * 1, SECONDS); // Задержка должна быть равна времени работы сервисов
    }

}
