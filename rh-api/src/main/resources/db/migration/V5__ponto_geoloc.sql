-- Geolocalização por batida (opcional). Mantém 1 registro por dia.
ALTER TABLE registros_ponto
  ADD COLUMN entrada_lat DECIMAL(10,7) NULL,
  ADD COLUMN entrada_lng DECIMAL(10,7) NULL,
  ADD COLUMN almoco_ini_lat DECIMAL(10,7) NULL,
  ADD COLUMN almoco_ini_lng DECIMAL(10,7) NULL,
  ADD COLUMN almoco_fim_lat DECIMAL(10,7) NULL,
  ADD COLUMN almoco_fim_lng DECIMAL(10,7) NULL,
  ADD COLUMN saida_lat DECIMAL(10,7) NULL,
  ADD COLUMN saida_lng DECIMAL(10,7) NULL;

