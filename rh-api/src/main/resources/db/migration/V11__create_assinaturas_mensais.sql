-- Tabela de aceite mensal (sem token)
-- Guarda o aceite do colaborador por competência (AAAAMM)

CREATE TABLE IF NOT EXISTS assinaturas_mensais (
  id BIGINT NOT NULL AUTO_INCREMENT,
  colaborador_id BIGINT NOT NULL,
  competencia INT NOT NULL,
  status VARCHAR(20) NOT NULL,
  email_sent_at DATETIME(6) NULL,
  decidido_em DATETIME(6) NULL,
  decidido_ip VARCHAR(64) NULL,
  decidido_ua VARCHAR(255) NULL,
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

  PRIMARY KEY (id),

  -- um registro por colaborador por competência
  UNIQUE KEY uk_ass_colab_comp (colaborador_id, competencia),

  -- índice auxiliar por competência
  KEY idx_ass_competencia (competencia),

  -- FK para colaboradores (ajuste o nome da tabela/coluna se no seu banco for diferente)
  CONSTRAINT fk_ass_colab FOREIGN KEY (colaborador_id) REFERENCES colaboradores(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
