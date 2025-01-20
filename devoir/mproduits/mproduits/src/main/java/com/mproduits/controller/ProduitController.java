package com.mproduits.controller;

import com.mproduits.model.Produit;
import com.mproduits.repository.ProduitRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/produits")
public class ProduitController {

    private final ProduitRepository produitRepository;

    public ProduitController(ProduitRepository produitRepository) {
        this.produitRepository = produitRepository;
    }

    @GetMapping
    public List<Produit> getAllProduits() {
        return produitRepository.findAll();
    }

    @PostMapping
    public Produit createProduit(@RequestBody Produit produit) {
        return produitRepository.save(produit);
    }
    
    @GetMapping("/{id}")
    public Produit getProduitById(@PathVariable Long id) throws InterruptedException {
        // Simulating a 5-second delay (increase if needed)
        Thread.sleep(5000);
        return new Produit(id, "Product A", 100.0);
    }
}
