--liquibase formatted sql

--changeset sadamart:005-seed-store-settings
INSERT INTO store_settings (setting_key, setting_value, updated_by) VALUES ('store.name', 'SADA Mart Main Branch', 'system.bootstrap');
INSERT INTO store_settings (setting_key, setting_value, updated_by) VALUES ('store.city', 'Larkana, Sindh', 'system.bootstrap');
INSERT INTO store_settings (setting_key, setting_value, updated_by) VALUES ('store.currency', 'USD', 'system.bootstrap');
INSERT INTO store_settings (setting_key, setting_value, updated_by) VALUES ('pos.taxPercent', '5.00', 'system.bootstrap');
INSERT INTO store_settings (setting_key, setting_value, updated_by) VALUES ('receipt.widthMm', '80', 'system.bootstrap');
