#!/usr/bin/env bash
# Exécute la collection V5 avec Newman et conserve requêtes + réponses dans postman/resultats/.
# Prérequis : Node.js ; docker compose up -d ; Eureka, Config Server, inventaire, alertes, approvisionnement et notification démarrés.
#
# Usage : ./run-newman-v5.sh principal   -> V5-0, V5-1, V5-2 (succès + échecs)
#         ./run-newman-v5.sh arret       -> V5-3a (À LANCER service-notification ARRÊTÉ)
#         ./run-newman-v5.sh reprise     -> V5-3b (À LANCER APRÈS redémarrage de service-notification)
set -e
cd "$(dirname "$0")"
mkdir -p resultats
COL=Stock-Alert-v5.postman_collection.json
case "$1" in
  principal)
    npx --yes newman run "$COL" \
      --folder "V5-0 Préparation et santé" --folder "V5-1 Parcours complet - succès" --folder "V5-2 Cas d'échec" \
      --verbose --reporters cli,json --reporter-json-export resultats/v5-run.json | tee resultats/v5-run.txt ;;
  arret)
    npx --yes newman run "$COL" \
      --folder "V5-3a Traitement différé - ÉTAPE 1 : à lancer service-notification ARRÊTÉ" \
      --verbose | tee resultats/v5-3a-run.txt ;;
  reprise)
    npx --yes newman run "$COL" \
      --folder "V5-3b Traitement différé - ÉTAPE 2 : à lancer APRÈS le redémarrage de service-notification" \
      --verbose | tee resultats/v5-3b-run.txt ;;
  *) echo "Usage : $0 principal|arret|reprise"; exit 1 ;;
esac
