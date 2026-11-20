# Tests Postman V4 — requêtes et réponses

Collection : `postman/Stock-Alert-v4.postman_collection.json` (dossiers `V4-0` à `V4-6`).

> **Statut des réponses.** Les réponses ci-dessous sont les réponses **attendues**, dérivées du code et des données de `data.sql` ; elles n'ont pas été capturées sur une exécution réelle (le build Maven n'était pas possible dans l'environnement de rédaction). Les `id` sont indicatifs (démarrage à froid, V1 non exécutée avant). Pour obtenir vos réponses réelles, lancez `postman/run-newman.ps1` (ou `.sh`) : le fichier `postman/resultats/v4-run.txt` contient chaque requête et chaque réponse, et `postman/resultats/v4-run.json` le rapport complet. Remplacez alors ce tableau par vos captures.

Variables : `inventaire=http://localhost:8081`, `approvisionnement=http://localhost:8082`, `alertes=http://localhost:8083`, `gateway=http://localhost:8080`.
Données initiales utilisées : produit 1 (Dell Latitude) stock Rabat **8 / seuil 10** (bas) et Casablanca **25 / 10** (suffisant) ; fournisseur 1 `TechDistrib Maroc` ; alerte 1 `OUVERTE` (produit 1, Rabat).

## V4-0 Préparation

| Requête | Réponse attendue |
|---|---|
| `POST {{inventaire}}/products` `{"nom":"Chargeur USB-C 65W","reference":"CHG-65W-<timestamp>","categorie":"Accessoires"}` | `201` `{"id":6,"nom":"Chargeur USB-C 65W",...}` |
| `POST {{inventaire}}/stock-levels` `{"productId":6,"entrepot":"Fès","quantite":4,"seuilAlerte":10}` | `201` stock **bas** |
| `POST {{inventaire}}/stock-levels` `{"productId":6,"entrepot":"Marrakech","quantite":100,"seuilAlerte":10}` | `201` stock **suffisant** |

## V4-1 Création contrôlée et idempotence — succès

**1. Création (stock bas)**

```http
POST {{approvisionnement}}/purchase-orders
Idempotency-Key: 7d1c...-guid
Content-Type: application/json

{"supplierId":1,"productId":6,"entrepot":"Fès","quantiteCommandee":50}
```
```http
201 Created
Location: /purchase-orders/4

{"id":4,"supplierId":1,"productId":6,"entrepot":"Fès","quantiteCommandee":50,"statut":"BROUILLON","date":"2026-11-20"}
```

**2. Rejeu : même clé, même corps**

```http
200 OK
Idempotent-Replayed: true

{"id":4,"supplierId":1,"productId":6,"entrepot":"Fès","quantiteCommandee":50,"statut":"BROUILLON","date":"2026-11-20"}
```

**3. Vérification d'absence de doublon** — `GET /purchase-orders?supplierId=1` → `200`, une seule ligne `productId=6, entrepot="Fès"`.

**4. Nouvelle clé, même contenu** → `201`, nouvel `id` (5) : l'idempotence porte sur la clé, pas sur le contenu.

**5. `PUT /purchase-orders/5`** (quantité 60, stock toujours bas) → `200`, `"quantiteCommandee":60`.

## V4-2 Idempotence — erreurs

| Requête | Réponse attendue |
|---|---|
| Même clé que l'étape 1, `quantiteCommandee: 99` | `409` `{"status":409,"erreur":"Conflict","message":"La clé d'idempotence '7d1c...' a déjà été utilisée avec un contenu différent","chemin":"/purchase-orders","details":[]}` |
| Sans en-tête `Idempotency-Key` | `400` `"message":"L'en-tête Idempotency-Key est obligatoire pour créer une commande"` |
| Clé vide (`" "`) | `400` (même message) |
| Clé de 101 caractères | `400` `"message":"L'en-tête Idempotency-Key ne doit pas dépasser 100 caractères"` |

## V4-3 Contrôles OpenFeign — introuvable et refus métier

| Requête (corps) | Réponse attendue |
|---|---|
| `productId: 9999` | `404` `"message":"Produit introuvable avec l'id 9999 (vérifié auprès de service-inventaire)"` |
| produit 6, `entrepot: "Agadir"` | `404` `"message":"Aucun niveau de stock pour le produit 6 dans l'entrepôt 'Agadir'"` |
| produit 6, `entrepot: "Marrakech"` (100 > 10) | `409` `"message":"Réapprovisionnement refusé : le stock du produit 6 dans l'entrepôt 'Marrakech' est suffisant (quantité 100 > seuil d'alerte 10)"` |
| `supplierId: 9999` | `404` `"message":"Fournisseur introuvable avec l'id 9999"` (aucun appel Feign) |
| `entrepot: ""`, `quantiteCommandee: 0` | `400` `"message":"Données de la requête invalides"`, `details` : `entrepot : L'entrepôt est obligatoire`, `quantiteCommandee : La quantité commandée doit être strictement positive` |
| Refus 409 (Marrakech) avec la clé `K`, puis création valide (Fès) avec la **même** clé `K` | `409` puis `201` : un refus ne « consomme » pas la clé |
| `PUT` d'un brouillon vers `productId: 9999` | `404` `Produit introuvable...` |
| `PUT` d'un brouillon vers Marrakech | `409` `...suffisant...` |

## V4-4 Consultation enrichie

`GET {{approvisionnement}}/purchase-orders/1` →

```json
200 OK
{
  "id": 1, "supplierId": 1, "productId": 1, "entrepot": "Rabat",
  "quantiteCommandee": 30, "statut": "BROUILLON", "date": "2026-11-10",
  "fournisseur":   {"id":1,"nom":"TechDistrib Maroc","email":"commandes@techdistrib.ma","delaiLivraisonJours":3},
  "produit":       {"id":1,"nom":"Ordinateur portable Dell Latitude 5440","reference":"DELL-LAT-5440","categorie":"Informatique"},
  "stock":         {"id":2,"productId":1,"entrepot":"Rabat","quantite":8,"seuilAlerte":10},
  "alerteOuverte": {"id":1,"thresholdId":1,"productId":1,"entrepot":"Rabat","niveauDetecte":8,"statut":"OUVERTE"},
  "avertissements": []
}
```

| Requête | Réponse attendue |
|---|---|
| `GET /purchase-orders/4` (commande créée en V4-1) | `200`, fournisseur `TechDistrib Maroc`, produit `Chargeur USB-C 65W`, stock `4 / 10`, `alerteOuverte: null`, `avertissements: []` |
| `GET {{gateway}}/service-approvisionnement/purchase-orders/1` | `200`, même réponse enrichie via la Gateway |
| `GET /purchase-orders/9999` | `404` `"message":"Commande d'achat introuvable avec l'id 9999"` |

## V4-5 Nettoyage

`DELETE /purchase-orders/{brouillon}` → `204`, puis nouvelle création avec la clé libérée → `201` (nouvel `id`).

## V4-6 Erreurs de communication (service-inventaire arrêté)

**Création** → `503` :

```json
{"timestamp":"2026-11-20T22:10:31.512","status":503,"erreur":"Service Unavailable",
 "message":"Le service 'service-inventaire' est indisponible : injoignable ou délai dépassé",
 "chemin":"/purchase-orders","details":[]}
```
(si l'instance a déjà quitté Eureka : `"... est indisponible : répond avec l'erreur 503 (...)"`). Rien n'est enregistré.

**Consultation** `GET /purchase-orders/1` → `200` dégradé :

```json
{"id":1,"supplierId":1,"productId":1,"entrepot":"Rabat","quantiteCommandee":30,"statut":"BROUILLON","date":"2026-11-10",
 "fournisseur":{"id":1,"nom":"TechDistrib Maroc",...},"produit":null,"stock":null,
 "alerteOuverte":{"id":1,...,"statut":"OUVERTE"},
 "avertissements":["Produit non enrichi : Le service 'service-inventaire' est indisponible : ...",
                   "Stock non enrichi : Le service 'service-inventaire' est indisponible : ..."]}
```
