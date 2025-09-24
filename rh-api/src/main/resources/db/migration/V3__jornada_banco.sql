CREATE TABLE IF NOT EXISTS jornadas_trabalho (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  colaborador_id BIGINT NOT NULL,
  minutos_seg INT NOT NULL DEFAULT 0,
  minutos_ter INT NOT NULL DEFAULT 0,
  minutos_qua INT NOT NULL DEFAULT 0,
  minutos_qui INT NOT NULL DEFAULT 0,
  minutos_sex INT NOT NULL DEFAULT 0,
  minutos_sab INT NOT NULL DEFAULT 0,
  minutos_dom INT NOT NULL DEFAULT 0,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_jornada_colab FOREIGN KEY (colaborador_id) REFERENCES colaboradores(id),
  UNIQUE KEY uk_jornada_colab (colaborador_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS bancos_horas (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  colaborador_id BIGINT NOT NULL,
  competencia VARCHAR(7) NOT NULL, -- formato YYYY-MM
  saldo_minutos INT NOT NULL DEFAULT 0,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_banco_colab FOREIGN KEY (colaborador_id) REFERENCES colaboradores(id),
  UNIQUE KEY uk_banco_colab_comp (colaborador_id, competencia)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;