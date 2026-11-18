INSERT INTO product (nom, reference, categorie) VALUES ('Ordinateur portable Dell Latitude 5440', 'DELL-LAT-5440', 'Informatique');
INSERT INTO product (nom, reference, categorie) VALUES ('Souris sans fil Logitech M185', 'LOG-M185', 'Accessoires');
INSERT INTO product (nom, reference, categorie) VALUES ('Clavier mécanique Keychron K2', 'KEY-K2', 'Accessoires');
INSERT INTO product (nom, reference, categorie) VALUES ('Écran 24 pouces HP P24', 'HP-P24', 'Écrans');
INSERT INTO product (nom, reference, categorie) VALUES ('Imprimante laser Brother HL-L2350DW', 'BRO-HLL2350', 'Impression');

INSERT INTO stock_level (product_id, entrepot, quantite, seuil_alerte) VALUES (1, 'Casablanca', 25, 10);
INSERT INTO stock_level (product_id, entrepot, quantite, seuil_alerte) VALUES (1, 'Rabat', 8, 10);
INSERT INTO stock_level (product_id, entrepot, quantite, seuil_alerte) VALUES (2, 'Casablanca', 120, 30);
INSERT INTO stock_level (product_id, entrepot, quantite, seuil_alerte) VALUES (2, 'Tanger', 15, 20);
INSERT INTO stock_level (product_id, entrepot, quantite, seuil_alerte) VALUES (3, 'Casablanca', 40, 15);
INSERT INTO stock_level (product_id, entrepot, quantite, seuil_alerte) VALUES (4, 'Rabat', 12, 5);
INSERT INTO stock_level (product_id, entrepot, quantite, seuil_alerte) VALUES (5, 'Tanger', 3, 5);
