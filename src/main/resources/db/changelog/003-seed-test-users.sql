--liquibase formatted sql

--changeset sadamart:006-seed-test-users
INSERT INTO users (username, full_name, password_hash, role, active)
SELECT 'engineer.admin', 'Engineer Admin',
       '65536:L73sZ5lYZSROdtJoYp/EYQ==:YMuCODKqW6NhW0ICgyPrqUci+/lVNyVkxkuLL+myOV8=',
       'ADMIN', 1
FROM dual
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'engineer.admin');

INSERT INTO users (username, full_name, password_hash, role, active)
SELECT 'store.manager', 'Store Manager',
       '65536:IEhHjcJWGkNaRO6tj6cCuw==:TCGc4UNPn5BfG49GQ/6qmki32UN4+QL0G1VRtuq7lcI=',
       'MANAGER', 1
FROM dual
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'store.manager');

INSERT INTO users (username, full_name, password_hash, role, active)
SELECT 'store.cashier', 'Store Cashier',
       '65536:Yf9JhJdzadmOVNxgN3Oxqg==:3Uy9ah7UHXRPv6FkvkA6wxTXYsLmcOhCTwW7JHXvBvo=',
       'CASHIER', 1
FROM dual
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'store.cashier');
