INSERT INTO supplier (nom, email, delai_livraison_jours) VALUES ('TechDistrib Maroc', 'commandes@techdistrib.ma', 3);
INSERT INTO supplier (nom, email, delai_livraison_jours) VALUES ('Atlas Informatique', 'achats@atlas-info.ma', 5);
INSERT INTO supplier (nom, email, delai_livraison_jours) VALUES ('Maghreb Supplies', 'contact@maghreb-supplies.ma', 7);

INSERT INTO purchase_order (supplier_id, product_id, entrepot, quantite_commandee, statut, date_commande) VALUES (1, 1, 'Rabat', 30, 'BROUILLON', '2026-11-10');
INSERT INTO purchase_order (supplier_id, product_id, entrepot, quantite_commandee, statut, date_commande) VALUES (1, 2, 'Tanger', 100, 'EMISE', '2026-11-12');
INSERT INTO purchase_order (supplier_id, product_id, entrepot, quantite_commandee, statut, date_commande) VALUES (2, 5, 'Tanger', 20, 'LIVREE', '2026-11-01');
