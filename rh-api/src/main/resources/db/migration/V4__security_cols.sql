-- V4: segurança - colunas senha_hash e papel + índice único em email

ALTER TABLE colaboradores
  ADD COLUMN senha_hash VARCHAR(100) NULL;

ALTER TABLE colaboradores
  ADD COLUMN papel VARCHAR(20) NOT NULL DEFAULT 'COLABORADOR';

-- garanta email único (ajuste se já existir)
ALTER TABLE colaboradores
  ADD CONSTRAINT uk_colab_email UNIQUE (email);
