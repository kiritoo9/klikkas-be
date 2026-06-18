CREATE EXTENSION IF NOT EXISTS pgcrypto;
INSERT INTO accounts (
    id,
    code,
    name,
    type,
    parent_id,
    is_active,
    created_at
)
VALUES

-- ASSET (1xxx)
(gen_random_uuid(), '1000', 'Kas', 'ASSET', NULL, TRUE, NOW()),
(gen_random_uuid(), '1100', 'Bank', 'ASSET', NULL, TRUE, NOW()),
(gen_random_uuid(), '1200', 'Piutang Usaha', 'ASSET', NULL, TRUE, NOW()),
(gen_random_uuid(), '1300', 'Persediaan', 'ASSET', NULL, TRUE, NOW()),
(gen_random_uuid(), '1400', 'Peralatan', 'ASSET', NULL, TRUE, NOW()),

-- LIABILITY (2xxx)
(gen_random_uuid(), '2000', 'Hutang Usaha', 'LIABILITY', NULL, TRUE, NOW()),
(gen_random_uuid(), '2100', 'Hutang Pajak', 'LIABILITY', NULL, TRUE, NOW()),

-- EQUITY (3xxx)
(gen_random_uuid(), '3000', 'Modal Pemilik', 'EQUITY', NULL, TRUE, NOW()),
(gen_random_uuid(), '3100', 'Prive', 'EQUITY', NULL, TRUE, NOW()),

-- REVENUE (4xxx)
(gen_random_uuid(), '4000', 'Pendapatan Penjualan', 'REVENUE', NULL, TRUE, NOW()),
(gen_random_uuid(), '4100', 'Pendapatan Jasa', 'REVENUE', NULL, TRUE, NOW()),
(gen_random_uuid(), '4200', 'Pendapatan Lain-lain', 'REVENUE', NULL, TRUE, NOW()),

-- EXPENSE (5xxx)
(gen_random_uuid(), '5000', 'Harga Pokok Penjualan', 'EXPENSE', NULL, TRUE, NOW()),
(gen_random_uuid(), '5100', 'Beban Gaji', 'EXPENSE', NULL, TRUE, NOW()),
(gen_random_uuid(), '5200', 'Beban Listrik', 'EXPENSE', NULL, TRUE, NOW()),
(gen_random_uuid(), '5300', 'Beban Internet', 'EXPENSE', NULL, TRUE, NOW()),
(gen_random_uuid(), '5400', 'Beban Transportasi', 'EXPENSE', NULL, TRUE, NOW()),
(gen_random_uuid(), '5500', 'Beban Operasional', 'EXPENSE', NULL, TRUE, NOW()),
(gen_random_uuid(), '5600', 'Beban Administrasi', 'EXPENSE', NULL, TRUE, NOW()),
(gen_random_uuid(), '5700', 'Beban Lain-lain', 'EXPENSE', NULL, TRUE, NOW());