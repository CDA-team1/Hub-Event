-- Données minimales nécessaires à l'initialisation de la production.
-- Ce script ne modifie pas le schéma et peut être rejoué sans dupliquer
-- le compte administrateur initial.

INSERT INTO users (
    lastname,
    firstname,
    postal_address,
    email,
    phone,
    password,
    status,
    role,
    suspended,
    suspension_end_date,
    suspension_reason,
    activation_token_hash,
    activation_token_expires_at,
    pending_password,
    password_change_token_hash,
    password_change_token_expires_at
)
SELECT
    'Martin',
    'Claire',
    '12 rue de la République, 34000 Montpellier',
    'claire.martin@hub-event.fr',
    '0600000000',
    '$2a$10$anLe4nmK0n3oykk7Y49BPu9wyDgyzWzXxo.a0GjKyz0Q3UK.SR202',
    'ACTIVE',
    'ADMIN',
    b'0',
    NULL,
    NULL,
    NULL,
    NULL,
    NULL,
    NULL,
    NULL
WHERE NOT EXISTS (
    SELECT 1
    FROM users
    WHERE email = 'claire.martin@hub-event.fr'
);
