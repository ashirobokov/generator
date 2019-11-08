package ru.ashirobokov.microservices.generator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.util.function.Tuple2;
import ru.ashirobokov.microservices.model.InstrumentPrice;
import ru.ashirobokov.microservices.service.PricesCache;
import ru.ashirobokov.microservices.service.ScheduledPrices;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Stream;

import static java.util.concurrent.TimeUnit.SECONDS;

@RestController
public class GeneratorController {

    public final static Logger LOG = LoggerFactory.getLogger(GeneratorController.class);
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1,
            runnable -> {
                Thread thread = new Thread(runnable);
                thread.setName("price-generator-thread");
                return thread;
            });

    @Autowired
    private PricesCache pricesCache;

    @PostConstruct
    public void initIt() throws Exception {
        generatePrices();
    }

    @RequestMapping("/")
    public String test() {
        LOG.info("GeneratorController...test");
        return " GeneratorController...test...test...test ";
    }

    @GetMapping(value = "/1prices", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<List<InstrumentPrice>> generateInstrumentListValues() {
        return Flux
                .<List<InstrumentPrice>>generate(sink -> sink.next(new ArrayList<InstrumentPrice>(pricesCache.getInstrumentPrices())))
                .delayElements(Duration.ofSeconds(10));
//                .onBackpressureDrop();
    }

    @GetMapping(value = "/2prices", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<List<InstrumentPrice>> generateInstrumentsWithPrices() {
        Flux<List<InstrumentPrice>> pricesFlux = Flux.fromStream(
                Stream.generate(() -> new ArrayList<InstrumentPrice>(pricesCache.getInstrumentPrices()))
        );

        Flux<Long> durationFlux = Flux.interval(Duration.ofSeconds(10));
        return Flux.zip(pricesFlux, durationFlux).map(Tuple2::getT1);
    }

    @GetMapping(value = "/allprices", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<List<InstrumentPrice>> generateAllInstrumentListValuesBySchedule() {
        Flux<List<InstrumentPrice>> pricesFlux = Flux.create(sink -> {
            ScheduledPrices.sent(sink, pricesCache);
        });
        return pricesFlux;
    }

    private void generatePrices() {
        LOG.info("GeneratorController...generatePrices into cache...started");
        final Runnable generator = new Runnable() {
            public void run() {
                Long timestamp = System.currentTimeMillis();
                pricesCache.getInstrumentPrices().forEach(instrument -> {
//  Генерирует случайное BigDecimal в диапазоне 100.00 -- 200.00
                    BigDecimal price = new BigDecimal(100.00 + Math.random() * 100);
                    instrument.setPrice(price.setScale(2, BigDecimal.ROUND_CEILING));
                });
            }
        };
        final ScheduledFuture<?> generateHandler =
                scheduler.scheduleAtFixedRate(generator, 1, 10, SECONDS);
        scheduler.schedule(new Runnable() {
            public void run() {
                pricesCache.setF(false);
                LOG.info("Cache status = {}", pricesCache.isF());
                generateHandler.cancel(true);
            }
        }, 60 * 1, SECONDS); // Задержка должна быть равна времени работы сервисов
    }

}
