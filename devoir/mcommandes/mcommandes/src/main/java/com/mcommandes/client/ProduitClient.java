package com.mcommandes.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.stereotype.Component;

@FeignClient(name = "mproduits", fallback = ProduitClientFallback.class)
public interface ProduitClient {

    @CircuitBreaker(name = "produitService", fallbackMethod = "fallbackProduit")
    @GetMapping("/produits/{id}")
    Produit getProduitById(@PathVariable("id") Long id);

    default Produit fallbackProduit(Long id, Throwable throwable) {
        return new Produit(id, "Fallback Product", 0.0);
    }
}

@Component
class ProduitClientFallback implements ProduitClient {

    @Override
    public Produit getProduitById(Long id) {
        return new Produit(id, "Fallback Product", 0.0);
    }
}
