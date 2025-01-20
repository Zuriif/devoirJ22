package com.mcommandes.controller;

import com.mcommandes.client.Produit;
import com.mcommandes.client.ProduitClient;
import com.mcommandes.model.Commande;
import com.mcommandes.repository.CommandeRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/commandes")
public class CommandeController {
    @Autowired
    private final CommandeRepository commandeRepository;

    @Autowired
    private ProduitClient produitClient;

    @Value("${mes-config-ms.commandes-last}")
    private int commandesLast;

    public CommandeController(CommandeRepository commandeRepository) {
        this.commandeRepository = commandeRepository;
    }

    @GetMapping("/recent")
    public List<Commande> getRecentCommandes() {
        LocalDate startDate = LocalDate.now().minusDays(commandesLast);
        return commandeRepository.findRecentCommandes(startDate);
    }

    @GetMapping
    public List<CommandeWithProduits> getAllCommandes() {
        return commandeRepository.findAll().stream().map(commande -> {
            List<Produit> produits = commande.getProduitIds().stream()
                    .map(this::getProduitWithCircuitBreaker)
                    .collect(Collectors.toList());
            return new CommandeWithProduits(commande, produits);
        }).collect(Collectors.toList());
    }

    @PostMapping
    public Commande createCommande(@RequestBody Commande commande) {
        return commandeRepository.save(commande);
    }

    @GetMapping("/{id}")
    public Commande getCommandeById(@PathVariable Long id) {
        return commandeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Commande not found"));
    }

    @PutMapping("/{id}")
    public Commande updateCommande(@PathVariable Long id, @RequestBody Commande updatedCommande) {
        Commande existingCommande = commandeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Commande not found"));
        existingCommande.setDescription(updatedCommande.getDescription());
        existingCommande.setQuantite(updatedCommande.getQuantite());
        existingCommande.setDate(updatedCommande.getDate());
        existingCommande.setMontant(updatedCommande.getMontant());
        return commandeRepository.save(existingCommande);
    }

    @DeleteMapping("/{id}")
    public void deleteCommande(@PathVariable Long id) {
        commandeRepository.deleteById(id);
    }

    @CircuitBreaker(name = "produitService", fallbackMethod = "fallbackProduit")
    private Produit getProduitWithCircuitBreaker(Long produitId) {
        return produitClient.getProduitById(produitId);
    }

    public Produit fallbackProduit(Long produitId, Throwable throwable) {
        return new Produit(produitId, "Fallback Product", 0.0);
    }

    static class CommandeWithProduits {
        private Commande commande;
        private List<Produit> produits;

        public CommandeWithProduits(Commande commande, List<Produit> produits) {
            this.commande = commande;
            this.produits = produits;
        }

        public Commande getCommande() { return commande; }
        public void setCommande(Commande commande) { this.commande = commande; }

        public List<Produit> getProduits() { return produits; }
        public void setProduits(List<Produit> produits) { this.produits = produits; }
    }
}
