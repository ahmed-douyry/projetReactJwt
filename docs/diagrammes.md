# Architecture et modèle de données

Les versions SVG consultables se trouvent dans `architecture.svg` et `entites.svg`.

```mermaid
flowchart TB
  R[React 19 + TypeScript\nLogin / Register / Dashboard / Incidents / Users / Profile]
  A[Axios\nIntercepteurs Bearer et 401]
  F[Spring Security\nCORS / STATELESS / JwtFilter]
  C[Controllers REST\nValidation / DTO / ControllerAdvice]
  S[Services\nPreAuthorize / Propriétaire / Affectation / Transitions / Historique]
  J[Repositories Spring Data JPA\nTransactions / Verrou optimiste]
  D[(H2 persistante)]
  R --> A
  A -->|HTTP JSON + Authorization Bearer JWT| F
  F --> C --> S --> J --> D
```

```mermaid
erDiagram
  USER ||--o{ INCIDENT : "createdBy"
  USER o|--o{ INCIDENT : "assignedTo"
  INCIDENT ||--o{ INCIDENT_HISTORY : conserve
  INCIDENT ||--o{ INCIDENT_COMMENT : contient
  USER ||--o{ INCIDENT_HISTORY : auteur
  USER ||--o{ INCIDENT_COMMENT : auteur
  USER ||--o{ REFRESH_TOKEN : possede
  USER {
    Long id PK
    String fullName
    String email UK
    String password "BCrypt"
    Role role
    boolean enabled
    Instant createdAt
    long authVersion
  }
  INCIDENT {
    Long id PK
    String title
    String description
    Category category
    Priority priority
    Status status
    Instant createdAt
    Instant updatedAt
    Long createdBy_id FK
    Long assignedTo_id FK "nullable"
    long version
  }
  INCIDENT_HISTORY {
    Long id PK
    Long incident_id FK
    Long author_id FK
    String message
    Instant createdAt
  }
  INCIDENT_COMMENT {
    Long id PK
    Long incident_id FK
    Long author_id FK
    String content
    Instant createdAt
  }
  REFRESH_TOKEN {
    Long id PK
    String tokenHash UK
    Long user_id FK
    Instant expiresAt
    boolean revoked
    long authVersion
    long version
  }
```

Les relations `ManyToOne` sont déclarées explicitement `LAZY`. Les collections `OneToMany` d’un incident sont aussi chargées à la demande. Elles sont mappées par `incident`, ordonnées par date et identifiant, avec cascade et suppression des orphelins. Aucune collection inverse n’est nécessaire sur `User` : les requêtes de repository sélectionnent les incidents par propriétaire ou technicien. Les mappers construisent les DTO dans la transaction du service. Les accesseurs des entités initialisent les proxys Hibernate nécessaires. Les listes chargent createdBy et assignedTo avec @EntityGraph, sans charger les historiques ni les notes. Ce choix évite les graphes JSON cycliques, l’exposition du hash du mot de passe et le chargement systématique des historiques dans les listes. Pour des volumes importants, ajouter pagination et projections pour réduire les requêtes N+1.
