# Exécute la collection V5 avec Newman et conserve requêtes + réponses dans postman\resultats\.
# Prérequis : Node.js ; docker compose up -d ; Eureka, Config Server, inventaire, alertes, approvisionnement et notification démarrés.
# Usage : .\run-newman-v5.ps1 principal   (V5-0, V5-1, V5-2)
#         .\run-newman-v5.ps1 arret       (V5-3a : service-notification ARRÊTÉ)
#         .\run-newman-v5.ps1 reprise     (V5-3b : APRÈS redémarrage de service-notification)
param([Parameter(Mandatory = $true)][ValidateSet("principal", "arret", "reprise")][string]$etape)
Set-Location $PSScriptRoot
New-Item -ItemType Directory -Force -Path resultats | Out-Null
$col = "Stock-Alert-v5.postman_collection.json"
switch ($etape) {
  "principal" {
    npx --yes newman run $col --folder "V5-0 Préparation et santé" --folder "V5-1 Parcours complet - succès" --folder "V5-2 Cas d'échec" `
      --verbose --reporters cli,json --reporter-json-export resultats/v5-run.json | Tee-Object -FilePath resultats/v5-run.txt
  }
  "arret" {
    npx --yes newman run $col --folder "V5-3a Traitement différé - ÉTAPE 1 : à lancer service-notification ARRÊTÉ" `
      --verbose | Tee-Object -FilePath resultats/v5-3a-run.txt
  }
  "reprise" {
    npx --yes newman run $col --folder "V5-3b Traitement différé - ÉTAPE 2 : à lancer APRÈS le redémarrage de service-notification" `
      --verbose | Tee-Object -FilePath resultats/v5-3b-run.txt
  }
}
