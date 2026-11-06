# Démonstration fonctionnelle — environ 10 minutes

1. Démarrer le backend et le frontend selon le README. Ouvrir `http://localhost:5173`.
2. Montrer l’inscription et son rôle EMPLOYEE imposé. Les boutons de démonstration de la page de connexion remplissent les identifiants ; ils ne contournent pas l’authentification.
3. Se connecter avec `employe@sentinel.ma` / `Demo2026!`. Consulter le dashboard et déclarer deux incidents, l’un PHISHING/HIGH, l’autre NETWORK/MEDIUM. Noter leurs identifiants.
4. Se déconnecter. Se connecter avec `employe2@sentinel.ma`. Déclarer un troisième incident et noter son identifiant.
5. Revenir au premier employé. Ouvrir directement `/incidents/{id du troisième}` : accès refusé par le backend. Ouvrir `/admin/users` : route refusée par React. Montrer la même restriction en HTTP avec Postman.
6. Se connecter avec `admin@sentinel.ma`. Montrer les statistiques, les comptes et tous les incidents. Affecter le premier incident à Yassine Bennani.
7. Se connecter avec `technicien@sentinel.ma`. Ouvrir l’incident affecté, passer en cours, ajouter « Le poste a été isolé du réseau », puis passer à résolu.
8. Revenir à l’employé : vérifier le nouveau statut, la note et les événements datés. L’employé ne dispose d’aucun contrôle d’affectation ou de statut.
9. Avec l’administrateur, clôturer l’incident. Les commandes de traitement disparaissent et l’API refuse les changements ultérieurs.
10. Dans Postman, importer `docs/Sentinel.postman_collection.json`, ouvrir le Runner et lancer toutes les requêtes. Capturer les résultats des neuf tests obligatoires. Les tests créent leurs propres employés et incidents.
11. Montrer la modification du profil et le changement de mot de passe (utiliser un compte inscrit pour conserver les identifiants démo). L’ancien mot de passe est obligatoire ; la modification révoque la session.
12. Pour l’expiration, attendre cinq minutes ou relancer le backend avec `JWT_TTL_SECONDS=15`. La session disparaît et la page de connexion affiche un message. Le test du JWT expiré signé est également présent dans la collection et les tests Java.

Les 36 requêtes de la collection reproduisent le scénario et les contrôles d’accès indépendamment de l’interface React. Les requêtes GET sans token ou avec un token invalide/expiré doivent rendre 401 ; les refus de rôle et de propriété doivent rendre 403.
