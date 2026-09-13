package com.quantlab.warehouse.repository;

import com.quantlab.marketdata.model.Exchange;
import com.quantlab.warehouse.entity.Instrument;
import com.quantlab.warehouse.model.InstrumentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InstrumentRepository extends JpaRepository<Instrument, Long> {

    Optional<Instrument> findByCurrentSymbolAndExchange(String currentSymbol, Exchange exchange);

    Optional<Instrument> findByIsin(String isin);

    List<Instrument> findByStatus(InstrumentStatus status);

    List<Instrument> findByIsIndexTrue();

    List<Instrument> findBySector(String sector);
}
