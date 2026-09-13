package com.quantlab.global.repository;

import com.quantlab.global.entity.GlobalInstrument;
import com.quantlab.global.model.AssetClass;
import com.quantlab.global.model.GlobalMarket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GlobalInstrumentRepository extends JpaRepository<GlobalInstrument, Long> {
    Optional<GlobalInstrument> findByCanonicalSymbol(String canonicalSymbol);
    Optional<GlobalInstrument> findByProviderSymbol(String providerSymbol);
    List<GlobalInstrument> findByAssetClass(AssetClass assetClass);
    List<GlobalInstrument> findByMarket(GlobalMarket market);
    List<GlobalInstrument> findByActiveTrue();
}
