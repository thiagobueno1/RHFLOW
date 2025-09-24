-- Torna hora_saida opcional para permitir primeira batida sem saída
ALTER TABLE registros_ponto
  MODIFY COLUMN hora_saida TIME NULL;
