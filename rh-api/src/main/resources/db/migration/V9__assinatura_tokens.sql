CREATE TABLE IF NOT EXISTS assinatura_tokens (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  token VARCHAR(80) NOT NULL UNIQUE,
  colaborador_id BIGINT NOT NULL,
  de_data DATE NOT NULL,
  ate_data DATE NOT NULL,
  expires_at TIMESTAMP NULL,
  signed_at TIMESTAMP NULL,
  signed_ip VARCHAR(64) NULL,
  signed_ua VARCHAR(255) NULL,
  status VARCHAR(20) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_ass_colab FOREIGN KEY (colaborador_id) REFERENCES colaboradores(id),
  INDEX idx_ass_token (token),
  INDEX idx_ass_colab (colaborador_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
