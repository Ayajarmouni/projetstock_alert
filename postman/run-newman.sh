#!/usr/bin/env bash
# Exécute la collection V4 avec Newman et conserve requêtes + réponses dans postman/resultats/.
# Prérequis : Node.js ; les services doivent tourner (Eureka, Config, inventaire, alertes, approvisionnement, gateway).
set -e
cd "$(dirname "$0")"
mkdir -p resultats
COL=Stock-Alert-v4.postman_collection.json
# Tous les dossiers sauf V4-6 (qui exige service-inventaire arrêté)
npx --yes newman run "$COL" \
  --folder "service-inventaire (8081)" --folder "service-approvisionnement (8082)" --folder "service-alertes (8083)" \
  --folder "V4-0 Préparation (service-inventaire)" \
  --folder "V4-1 Création contrôlée et idempotence - succès" \
  --folder "V4-2 Idempotence - erreurs" \
  --folder "V4-3 Contrôles OpenFeign - introuvable, refus métier" \
  --folder "V4-4 Consultation enrichie" \
  --folder "V4-5 Nettoyage" \
  --verbose --reporters cli,json --reporter-json-export resultats/v4-run.json | tee resultats/v4-run.txt
echo "Pour V4-6 : arrêter service-inventaire puis lancer : npx newman run $COL --folder \"V4-6 Erreurs de communication - à lancer APRÈS avoir ARRÊTÉ service-inventaire\" --verbose | tee resultats/v4-6-run.txt"
