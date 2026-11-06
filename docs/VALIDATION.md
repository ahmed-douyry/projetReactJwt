# Vérifications réalisées — 4 novembre 2026

## Résultats

| Vérification réelle | Résultat |
| --- | --- |
| `mvn test` | **25 tests, 0 échec, 0 erreur** |
| `mvn package` | **Réussi**, JAR Spring Boot exécutable généré |
| `npm run build` | **Réussi**, compilation TypeScript et bundle Vite |
| `npm run lint` | **Réussi**, aucune erreur ESLint |
| Collection Postman exécutée avec Newman | **36 requêtes, 40 assertions, aucun échec** |
| `npm audit --omit=dev` | **Aucune vulnérabilité détectée** dans les dépendances de production au moment du contrôle |
| Navigateur Chrome, parcours EMPLOYEE → ADMIN → TECHNICIAN → EMPLOYEE | Création, affectation, changement de statut, note et consultation finale réussis |
| Route React ADMIN ouverte par EMPLOYEE | Page « Accès interdit » |
| Tailles 1440 × 1000, 768 × 1024, 390 × 844 | Affichage vérifié ; largeur du document égale au viewport à 768 et 390 pixels |
| Console navigateur après retour du backend | Aucune erreur JavaScript dans le parcours complet vérifié |
| Intercepteur Axios après un vrai 401 de JWT expiré | Session supprimée et redirection Login avec message |
| Minuteur local d’expiration React | Redirection vers Login avec message ; échéance locale raccourcie pour ce test |

Les erreurs de connexion vues pendant le redémarrage du backend ont disparu après son relancement. Le port 8080 appartient à un autre service de l’environnement : l’application a été configurée sur **8081**, sans arrêter ce service.

## Les neuf tests de sécurité imposés

| Cas | Réponse obtenue |
| --- | --- |
| Accès protégé sans JWT | 401 |
| JWT invalide | 401 |
| JWT signé mais expiré | 401 |
| EMPLOYEE sur route ADMIN | 403 |
| EMPLOYEE sur incident d’un autre employé | 403 |
| TECHNICIAN sur incident non affecté | 403 |
| Utilisateur désactivé à la connexion | 401 |
| Inscription avec email déjà utilisé | 409 |
| Création d’incident avec données invalides | 400 |

Ces cas ont été exécutés avec Spring MockMvc et avec la collection HTTP Postman/Newman. Le test d’expiration backend utilise un JWT réellement signé dont `exp` est dans le passé ; il ne s’agit pas simplement d’un token mal formé.

## Autres contrôles Java

Signature falsifiée, rôle forcé EMPLOYEE à l’inscription même avec une tentative de rôle ADMIN, mot de passe BCrypt, listes restreintes au propriétaire, affectation réservée à ADMIN et à un technicien actif, interdiction de modifier le statut ou d’ajouter une note pour EMPLOYEE, cycle complet et historique, verrouillage de CLOSED, transition invalide, incident absent, impossibilité de désactiver son propre compte administrateur, mauvais identifiants, ancien mot de passe vérifié, révocation après changement de mot de passe/email, rotation du refresh token, révocation après déconnexion et statistiques globales.

Les tests utilisent H2 en mémoire et ne modifient pas la base locale de démonstration. La collection et le parcours navigateur ajoutent leurs propres données dans la base locale persistante.

## Preuves livrées

- `output/backend-tests.txt` : résultat de la suite Java.
- `output/postman-results.json` : statuts, réponses et assertions de l’exécution réelle, sans jetons ni mots de passe.
- `output/postman-results.html` : présentation lisible des neuf tests de sécurité.
- `output/playwright/01-login.png` : connexion.
- `output/playwright/02-admin-dashboard.png` : tableau de bord administrateur.
- `output/playwright/03-admin-users.png` : utilisateurs.
- `output/playwright/04-incident-details.png` : historique et notes.
- `output/playwright/05-new-incident.png` : formulaire renseigné avant déclaration réelle.
- `output/playwright/06-tablet-dashboard.png` : rendu 768 pixels.
- `output/playwright/07-mobile-dashboard.png` : rendu 390 pixels.
- `output/playwright/08-postman-results.png` : résultats HTTP de la collection exécutée par Newman.
- `output/playwright/09-technician-treatment.png` : résolution et note dans le parcours réel du navigateur.
- `output/playwright/10-employee-followup.png` : vérification par l’employé du résultat et de l’historique.
- `output/playwright/11-session-expired.png` : message d’expiration locale et redirection.
- `output/playwright/12-api-401-expired.png` : redirection par l’intercepteur Axios après réponse 401 à un JWT signé mais expiré.

## Limites explicites

Les captures de l’interface de Postman elle-même ne sont pas fournies : les preuves livrées viennent de l’exécution de la collection avec Newman. Pour satisfaire littéralement ce livrable, importer la collection dans Postman, lancer le Runner et capturer les neuf résultats indiqués ci-dessus. La démonstration orale reste à réaliser par l’étudiant. La couverture du rapport indique Ahmed Douyry comme auteur et ENSET Mohammedia comme établissement.

L’expiration de cinq minutes a été vérifiée par ses mécanismes backend et frontend ; le navigateur a utilisé une échéance locale raccourcie, sans attendre cinq minutes réelles. La vérification des codes 401 du JWT expiré a bien utilisé une signature valide. H2 a été vérifiée ; MySQL et un déploiement public ne font pas partie des vérifications réalisées. Les polices Google Fonts ont des polices de repli si le réseau est absent.

## Vérification après restructuration du backend

Le backend a été réparti en couches `domain`, `repository`, `dto`, `mapper`, `service`, `security`, `config`, `bootstrap` et `web`. `mvn package` a compilé la nouvelle structure et exécuté **25 tests, sans échec ni erreur**. Un test supplémentaire appelle directement les services depuis un contexte EMPLOYEE et confirme le refus des actions administratives, indépendamment des routes HTTP. Le test des statistiques vérifie les anciennes clés JSON en majuscules pour garantir la compatibilité avec React.

Les contrôleurs n’ont aucune dépendance vers les repositories ou les entités ; ils ne portent aucune transaction. Les champs des entités sont privés. Les données existantes et les noms des tables/colonnes sont conservés.

La collection Postman a été relancée sur le JAR restructuré avec la base H2 existante : **36 requêtes et 40 assertions réussies**. Les résultats expurgés sont dans `output/postman-results.json`.

## Captures complémentaires du 6 novembre 2026

Neuf captures réelles ont été ajoutées dans `output/playwright/14-*.png` à `22-*.png`, en résolution 1440 × 1000 : tableau de bord, incidents, utilisateurs et affectation côté administrateur ; tableau de bord, incidents personnels, suivi résolu, déclaration et profil côté Ahmed Benali (EMPLOYEE). Elles sont intégrées au README et au rapport HTML/PDF. Les données proviennent de l’API locale ; aucune modification des comptes ou incidents n’a été soumise pendant cette session de captures. La connexion et la déconnexion administrateur, puis la connexion employé ont été exécutées dans le navigateur.
