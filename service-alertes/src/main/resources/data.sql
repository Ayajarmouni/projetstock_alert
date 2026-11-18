INSERT INTO threshold (product_id, supplier_id, entrepot, seuil_min, quantite_reappro, canal) VALUES (1, 1, 'Rabat', 10, 30, 'EMAIL');
INSERT INTO threshold (product_id, supplier_id, entrepot, seuil_min, quantite_reappro, canal) VALUES (2, 1, 'Tanger', 20, 100, 'EMAIL');
INSERT INTO threshold (product_id, supplier_id, entrepot, seuil_min, quantite_reappro, canal) VALUES (5, 2, 'Tanger', 5, 20, 'SMS');

INSERT INTO stock_alert (threshold_id, product_id, entrepot, niveau_detecte, statut) VALUES (1, 1, 'Rabat', 8, 'OUVERTE');
INSERT INTO stock_alert (threshold_id, product_id, entrepot, niveau_detecte, statut) VALUES (3, 5, 'Tanger', 3, 'CLOTUREE');
