CREATE TABLE IF NOT EXISTS registros_ponto (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  colaborador_id BIGINT NOT NULL,
  data DATE NOT NULL,
  hora_entrada TIME NOT NULL,
  inicio_almoco TIME NULL,
  fim_almoco TIME NULL,
  hora_saida TIME NOT NULL,
  origem VARCHAR(20) NOT NULL DEFAULT 'WEB',
  observacao VARCHAR(255) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_ponto_colab FOREIGN KEY (colaborador_id) REFERENCES colaboradores(id),
  UNIQUE KEY uk_ponto_colab_data (colaborador_id, data),
  KEY idx_ponto_colab_data (colaborador_id, data)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS solicitacoes_ferias (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  colaborador_id BIGINT NOT NULL,
  data_inicio DATE NOT NULL,
  data_fim DATE NOT NULL,
  dias INT NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'CRIADA',
  motivo VARCHAR(255) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_ferias_colab FOREIGN KEY (colaborador_id) REFERENCES colaboradores(id),
  KEY idx_ferias_colab (colaborador_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;