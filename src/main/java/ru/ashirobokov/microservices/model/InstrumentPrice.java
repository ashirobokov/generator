package ru.ashirobokov.microservices.model;

import java.math.BigDecimal;

public class InstrumentPrice {

    Long instrumentId;
    BigDecimal price;

    public InstrumentPrice() {

    }

    public InstrumentPrice(Long instrumentId, BigDecimal price) {
        this.instrumentId = instrumentId;
        this.price = price;
    }

    public Long getInstrumentId() {
        return instrumentId;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "InstrumentPrice{" +
                "instrumentId=" + instrumentId +
                ", price=" + price +
                '}';
    }
}
