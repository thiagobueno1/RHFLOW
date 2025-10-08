CREATE TABLE IF NOT EXISTS assinaturas_mensais (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  colaborador_id BIGINT NOT NULL,
  competencia INT NOT NULL,
  status VARCHAR(20) NOT NULL,
  email_sent_at TIMESTAMP NULL,
  decidido_em TIMESTAMP NULL,
  decidido_ip VARCHAR(64),
  decidido_ua VARCHAR(255),
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  CONSTRAINT uk_ass_mensal_colab_comp UNIQUE (colaborador_id, competencia),
  CONSTRAINT fk_ass_mensal_colab FOREIGN KEY (colaborador_id) REFERENCES colaboradores(id)
);
