# Tests Postman V5 — requêtes et réponses

Collection : `postman/Stock-Alert-v5.postman_collection.json` (33 requêtes avec assertions, dossiers `V5-0` à `V5-3b`).

> **Statut des réponses.** Les réponses ci-dessous sont les réponses **attendues**, dérivées du code et des données de `data.sql` ; elles n'ont pas été capturées sur une exécution réelle (le build Maven et Docker n'étaient pas disponibles dans l'environnement de rédaction). Les `id`, `eventId` et horodatages sont indicatifs. Pour obtenir vos réponses réelles : `./postman/run-newman-v5.sh principal` (ou `.ps1`) — `postman/resultats/v5-run.txt` contient chaque requête et chaque réponse, `v5-run.json` le rapport complet. Remplacez alors les blocs ci-dessous par vos captures (et ajoutez une capture de l'interface Mailpit http://localhost:8025 et de la console RabbitMQ).

Variables : `approvisionnement=http://localhost:8082`, `notification=http://localhost:8084`, `mailpit=http://localhost:8025`, `rabbit=http://localhost:15672` (utilisateur `stockalert` / `stockalert`).
Données utilisées : produit 1 (Dell Latitude 5440) stock Rabat **8 / seuil 10** (bas) ; fournisseur 1 `TechDistrib Maroc` (`commandes@techdistrib.ma`). Démarrage à froid (bases H2 vides de toute commande de test).

## V5-0 Préparation et santé

| Requête | Réponse attendue |
|---|---|
| `GET {{notification}}/actuator/health` | `200` `{"status":"UP"}` |
| `DELETE {{mailpit}}/api/v1/messages` | `200` `ok` |
| `GET {{rabbit}}/api/queues/%2F/notification.purchase-order` | `200` `{"name":"notification.purchase-order","durable":true,"arguments":{"x-dead-letter-exchange":"stockalert.events.dlx","x-dead-letter-routing-key":"notification.purchase-order.dead"},"consumers":1,...}` |
| `GET {{rabbit}}/api/queues/%2F/notification.purchase-order.dlq` | `200` |
| `DELETE {{rabbit}}/api/queues/%2F/notification.purchase-order.dlq/contents` | `204` |

## V5-1 Parcours complet — succès

**1. Création (aucune notification : la création n'est pas l'opération notifiée)**

```http
POST {{approvisionnement}}/purchase-orders
Idempotency-Key: 7d1c...-guid
Content-Type: application/json

{"supplierId":1,"productId":1,"entrepot":"Rabat","quantiteCommandee":30}
```
```http
201 Created
Location: /purchase-orders/4

{"id":4,"supplierId":1,"productId":1,"entrepot":"Rabat","quantiteCommandee":30,"statut":"BROUILLON","date":"2026-11-21"}
```

`GET {{mailpit}}/api/v1/messages` → `200` `{"total":0,"messages":[],...}`

**3. Émission → publication RabbitMQ après commit** (la réponse revient en quelques dizaines de ms, sans attendre l'email)

```http
POST {{approvisionnement}}/purchase-orders/4/emettre
```
```http
200 OK

{"id":4,"supplierId":1,"productId":1,"entrepot":"Rabat","quantiteCommandee":30,"statut":"EMISE","date":"2026-11-21"}
```

Message publié (visible dans la console RabbitMQ → *Get messages* si le consommateur est arrêté) :

```json
{
  "eventId": "0b8f6c1e-5a1d-4a0e-9d9b-2f5c5f0c1a11",
  "type": "PURCHASE_ORDER_EMISE",
  "occurredAt": "2026-11-21T00:41:07.512Z",
  "purchaseOrderId": 4,
  "statut": "EMISE",
  "date": "2026-11-21",
  "supplierId": 1,
  "supplierNom": "TechDistrib Maroc",
  "supplierEmail": "commandes@techdistrib.ma",
  "delaiLivraisonJours": 3,
  "productId": 1,
  "produitNom": "Ordinateur portable Dell Latitude 5440",
  "entrepot": "Rabat",
  "quantiteCommandee": 30
}
```

**4. Email reçu** — `GET {{mailpit}}/api/v1/messages` :

```json
{"total":1,"messages":[{"Subject":"[Stock-Alert] Commande d'achat n°4 émise",
  "From":{"Address":"stock-alert@stockalert.local"},"To":[{"Address":"commandes@techdistrib.ma","Name":""}], ...}]}
```

**5. Contenu** — `GET {{mailpit}}/api/v1/message/latest` → `Text` :

```text
Bonjour TechDistrib Maroc,

Une nouvelle commande d'achat vous est transmise. Merci de la préparer pour livraison.

  Commande n°          : 4
  Statut               : EMISE
  Produit              : Ordinateur portable Dell Latitude 5440 (n°1)
  Quantité             : 30
  Entrepôt de livraison: Rabat
  Date de commande     : 2026-11-21
  Délai convenu        : 3 jour(s)
```

**6. Historique côté consommateur** — `GET {{notification}}/notifications?purchaseOrderId=4` :

```json
[{"id":1,"eventId":"0b8f6c1e-...","type":"PURCHASE_ORDER_EMISE","purchaseOrderId":4,"destinataire":"commandes@techdistrib.ma","sujet":"[Stock-Alert] Commande d'achat n°4 émise","statut":"ENVOYEE","dateEnvoi":"2026-11-21T00:41:07.9"}]
```

**7-8. Livraison** — `POST /purchase-orders/4/livrer` → `200` `"statut":"LIVREE"` ; Mailpit contient alors **2** messages, le plus récent avec le sujet `[Stock-Alert] Commande d'achat n°4 livrée`.

**9.** `GET .../api/queues/%2F/notification.purchase-order` → `"messages_ready":0` (tout est consommé).

## V5-2 Cas d'échec

| Requête | Réponse attendue | Email ? |
|---|---|---|
| `POST /purchase-orders/4/emettre` (déjà livrée) | `409` `{"status":409,"erreur":"Conflict","message":"La commande 4 ne peut pas être émise : statut actuel LIVREE, statut requis BROUILLON","chemin":"/purchase-orders/4/emettre","details":[]}` | non |
| `POST /purchase-orders/99999/emettre` | `404` `"message":"Commande d'achat introuvable avec l'id 99999"` | non |
| `POST /purchase-orders/5/livrer` (brouillon) | `409` `"message":"La commande 5 ne peut pas être livrée : statut actuel BROUILLON, statut requis EMISE"` | non |
| Mailpit après ces trois refus | `{"total":2,...}` (inchangé) | — |
| `POST {{rabbit}}/api/exchanges/%2F/stockalert.events/publish` avec `supplierEmail: null` | `200` `{"routed":true}` — le consommateur lève `EvenementInvalideException`, 4 essais (1 s, 2 s, 4 s) puis **DLQ** | non |
| Même publication avec le texte `ceci n'est pas du JSON` | `200` `{"routed":true}` — `MessageConversionException` (fatale) : **DLQ immédiate** | non |
| `GET .../api/queues/%2F/notification.purchase-order.dlq` (après ~14 s) | `"messages":2` | — |

Les logs de `service-notification` montrent les tentatives (`Retries exhausted for message ...`), puis le rejet vers la DLQ.

## V5-3 Traitement différé (consommateur arrêté puis redémarré)

**Étape 1 — `service-notification` ARRÊTÉ** (`./postman/run-newman-v5.sh arret`)

| Requête | Réponse attendue |
|---|---|
| `DELETE {{mailpit}}/api/v1/messages` | `200` |
| `POST /purchase-orders` | `201` `"statut":"BROUILLON"` |
| `POST /purchase-orders/{id}/emettre` | **`200`** `"statut":"EMISE"` — l'API ne dépend pas du consommateur |
| `GET .../api/queues/%2F/notification.purchase-order` | `"messages_ready":1,"consumers":0` — le message attend dans la file durable |
| `GET {{mailpit}}/api/v1/messages` | `"total":0` |

**Étape 2 — redémarrer `service-notification`** puis `./postman/run-newman-v5.sh reprise`

| Requête | Réponse attendue |
|---|---|
| `GET .../api/queues/%2F/notification.purchase-order` | `"messages_ready":0,"consumers":1` |
| `GET {{mailpit}}/api/v1/messages` | `"total":1`, sujet `[Stock-Alert] Commande d'achat n°N émise` : **l'email différé est arrivé** |
| `GET {{notification}}/notifications` | `[{"statut":"ENVOYEE",...}]` |

## Scénarios manuels complémentaires (non automatisables avec Postman)

| Scénario | Action | Résultat attendu |
|---|---|---|
| Mailpit arrêté | `docker compose stop mailpit`, émettre une commande | `200` côté API ; `service-notification` journalise l'échec SMTP, 4 essais, puis le message part en **DLQ** ; aucun email perdu silencieusement |
| RabbitMQ arrêté | `docker compose stop rabbitmq`, émettre une commande | `200` côté API (la commande est valide en base) ; `service-approvisionnement` journalise `Publication RabbitMQ impossible...` — limite connue, voir README |
