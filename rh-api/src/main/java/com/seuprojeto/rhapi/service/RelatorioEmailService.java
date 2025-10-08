package com.seuprojeto.rhapi.service;

import com.seuprojeto.rhapi.domain.AssinaturaMensal;
import com.seuprojeto.rhapi.domain.Colaborador;
import com.seuprojeto.rhapi.domain.enums.StatusFerias;
import com.seuprojeto.rhapi.dto.ExtratoDiaDTO;
import com.seuprojeto.rhapi.dto.ExtratoPeriodoDTO;
import com.seuprojeto.rhapi.repository.AssinaturaMensalRepository;
import com.seuprojeto.rhapi.repository.ColaboradorRepository;
import com.seuprojeto.rhapi.repository.SolicitacaoFeriasRepository;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.YearMonth;
import java.util.List;

@Service
public class RelatorioEmailService {

    private final EmailService emailService;
    private final BancoHorasService bancoHorasService;
    private final ColaboradorRepository colaboradorRepository;
    private final SolicitacaoFeriasRepository feriasRepository;
    private final AssinaturaMensalRepository assinaturaMensalRepository;

    public RelatorioEmailService(EmailService emailService,
                                 BancoHorasService bancoHorasService,
                                 ColaboradorRepository colaboradorRepository,
                                 SolicitacaoFeriasRepository feriasRepository,
                                 AssinaturaMensalRepository assinaturaMensalRepository) {
        this.emailService = emailService;
        this.bancoHorasService = bancoHorasService;
        this.colaboradorRepository = colaboradorRepository;
        this.feriasRepository = feriasRepository;
        this.assinaturaMensalRepository = assinaturaMensalRepository;
    }

    private static int ymToInt(YearMonth ym) {
        return ym.getYear() * 100 + ym.getMonthValue();
    }

    /**
     * Envia o relatório mensal (HTML + CSV) e inclui botões para decidir no sistema (sem token)
     * e um botão extra para "Acessar site".
     */
    public void enviarRelatorioMensal(Long colaboradorId, YearMonth competencia, String hostBase) {
        if (competencia == null) throw new IllegalArgumentException("competencia é obrigatória");
        LocalDate de = competencia.atDay(1);
        LocalDate ate = competencia.atEndOfMonth();

        Colaborador c = colaboradorRepository.findById(colaboradorId)
                .orElseThrow(() -> new IllegalArgumentException("Colaborador não encontrado"));

        // Extrato do período
        ExtratoPeriodoDTO extrato = bancoHorasService.extratoPeriodo(colaboradorId, de, ate);

        // Saldo de férias disponível
        LocalDate adm = c.getDataAdmissao() != null ? c.getDataAdmissao() : LocalDate.now();
        long meses = Math.max(0, Period.between(adm, LocalDate.now()).toTotalMonths());
        int diasDireito = (int) Math.min(30, Math.floor(meses * 2.5));
        Integer consumidosOuPendentes = feriasRepository.sumDiasByStatuses(
                c.getId(), List.of(StatusFerias.CRIADA, StatusFerias.APROVADA)
        );
        int saldoFeriasDias = Math.max(0, diasDireito - (consumidosOuPendentes == null ? 0 : consumidosOuPendentes));

        // Garante/obtém a assinatura mensal PENDENTE para a competência (AAAAMM)
        int compInt = ymToInt(competencia);
        AssinaturaMensal ass = assinaturaMensalRepository
                .findByColaboradorIdAndCompetencia(c.getId(), compInt)
                .orElseGet(() -> {
                    AssinaturaMensal novo = new AssinaturaMensal();
                    novo.setColaborador(c);
                    novo.setCompetencia(compInt);
                    // status default = PENDENTE na entidade
                    return assinaturaMensalRepository.save(novo);
                });

        // URLs de decisão (por enquanto meramente informativas no e-mail; a decisão real ocorre no front)
        String base = (hostBase != null && !hostBase.isBlank()) ? hostBase : "http://localhost:8080";
        String decidirSim = base + "/public/assinatura/" + ass.getId() + "/decidir?acao=sim";
        String decidirNao = base + "/public/assinatura/" + ass.getId() + "/decidir?acao=nao";

        // URL do "Acessar site"
        String frontUrl = base;

        // Montar HTML
        String linhasTabela = montarLinhasTabela(extrato.dias());
        String saldoPeriodoFmt = formatMinutos(extrato.saldoTotalMin());

        String html = """
                <!doctype html>
                <html lang="pt-br">
                <head>
                  <meta charset="UTF-8" />
                  <meta name="viewport" content="width=device-width, initial-scale=1"/>
                  <title>Extrato de Banco de Horas</title>
                </head>
                <body style="font-family:Arial, Helvetica, sans-serif; color:#111; margin:0; padding:20px; background:#f6f7fb">
                  <table role="presentation" cellspacing="0" cellpadding="0" style="width:100%%; max-width:720px; margin:0 auto; background:#fff; border-radius:12px; padding:18px; box-shadow:0 6px 18px rgba(0,0,0,.06)">
                    <tr>
                      <td>
                        <h2 style="margin:0 0 10px 0; color:#111">Extrato de Banco de Horas</h2>
                        <p style="margin:0 0 16px 0; color:#444">Competência: <b>%s</b></p>

                        <table role="presentation" cellspacing="0" cellpadding="0" style="width:100%%; border-collapse:collapse">
                          <tr>
                            <td style="padding:6px 0; color:#666">Período</td>
                            <td style="padding:6px 0; text-align:right"><b>%s</b> a <b>%s</b></td>
                          </tr>
                          <tr>
                            <td style="padding:6px 0; color:#666">Colaborador</td>
                            <td style="padding:6px 0; text-align:right"><b>%s</b></td>
                          </tr>
                          <tr>
                            <td style="padding:6px 0; color:#666">E-mail</td>
                            <td style="padding:6px 0; text-align:right">%s</td>
                          </tr>
                          <tr>
                            <td style="padding:6px 0; color:#666">Cargo</td>
                            <td style="padding:6px 0; text-align:right">%s</td>
                          </tr>
                          <tr>
                            <td style="padding:6px 0; color:#666">Departamento</td>
                            <td style="padding:6px 0; text-align:right">%s</td>
                          </tr>
                        </table>

                        <hr style="border:none; border-top:1px solid #eee; margin:18px 0" />

                        <table role="presentation" cellspacing="0" cellpadding="0" style="width:100%%; border-collapse:collapse">
                          <thead>
                            <tr>
                              <th style="text-align:left; padding:8px 6px; border-bottom:1px solid #eee">Data</th>
                              <th style="text-align:right; padding:8px 6px; border-bottom:1px solid #eee">Previsto</th>
                              <th style="text-align:right; padding:8px 6px; border-bottom:1px solid #eee">Trabalhado</th>
                              <th style="text-align:right; padding:8px 6px; border-bottom:1px solid #eee">Saldo</th>
                            </tr>
                          </thead>
                          <tbody>
                            %s
                          </tbody>
                        </table>

                        <div style="margin-top:16px; background:#f9fafb; border:1px solid #eef2f7; border-radius:10px; padding:12px">
                          <p style="margin:0; color:#111"><b>Saldo acumulado no período:</b> %s</p>
                          <p style="margin:6px 0 0 0; color:#111"><b>Saldo de férias disponível:</b> %d dia(s)</p>
                        </div>

                        <div style="margin-top:18px; text-align:center">
                          <a href="%s" target="_blank" rel="noopener noreferrer"
                             style="display:inline-block; background:#2563eb; color:#fff; text-decoration:none; padding:12px 18px; border-radius:10px; font-weight:600; margin-right:8px">
                            Estou de acordo
                          </a>
                          <a href="%s" target="_blank" rel="noopener noreferrer"
                             style="display:inline-block; background:#ef4444; color:#fff; text-decoration:none; padding:12px 18px; border-radius:10px; font-weight:600">
                            Não estou de acordo
                          </a>

                          <div style="margin-top:14px;">
                            <a href="%s" target="_blank" rel="noopener noreferrer"
                               style="display:inline-block; background:#10b981; color:#fff; text-decoration:none; padding:10px 16px; border-radius:8px; font-weight:600">
                              Acessar site
                            </a>
                          </div>

                          <p style="margin:10px 0 0 0; color:#666; font-size:12px">
                            Caso prefira, você também pode acessar o site e decidir pelo painel.
                          </p>
                        </div>

                        <p style="margin:16px 0 6px 0; color:#666">Relatório em CSV anexado a este e-mail.</p>
                        <p style="margin:0; color:#666">Este e-mail foi enviado automaticamente. Por favor, não responda.</p>
                      </td>
                    </tr>
                  </table>
                </body>
                </html>
                """.formatted(
                competencia.toString(),
                de.toString(), ate.toString(),
                nullSafe(c.getNome()),
                nullSafe(c.getEmail()),
                nullSafe(c.getCargo()),
                c.getDepartamento() != null ? nullSafe(c.getDepartamento().getNome()) : "-",
                linhasTabela,
                saldoPeriodoFmt,
                saldoFeriasDias,
                decidirSim,
                decidirNao,
                frontUrl
        );

        // CSV em anexo
        byte[] csvBytes = montarCsv(extrato).getBytes(StandardCharsets.UTF_8);

        // Assunto
        String subject = "Extrato Mensal — %s (%s)".formatted(nullSafe(c.getNome()), competencia);

        // Enviar
        emailService.sendHtmlWithAttachment(
                c.getEmail(),
                subject,
                html,
                "extrato_" + de + "_a_" + ate + ".csv",
                csvBytes,
                "text/csv"
        );

        // marca horário de envio (sem método custom no repo)
        ass.setEmailSentAt(LocalDateTime.now());
        assinaturaMensalRepository.save(ass);
    }

    /* ================== Helpers ================== */

    private static String montarLinhasTabela(List<ExtratoDiaDTO> dias) {
        StringBuilder sb = new StringBuilder();
        if (dias != null) {
            for (ExtratoDiaDTO d : dias) {
                String data = d.data().toString();
                String prev = formatMinutos(d.previstoMin());
                String trab = formatMinutos(d.trabalhadoMin());
                String saldo = formatSaldo(d.saldoMin());
                sb.append("""
                        <tr>
                          <td style="padding:8px 6px; border-bottom:1px solid #f2f2f2">%s</td>
                          <td style="padding:8px 6px; border-bottom:1px solid #f2f2f2; text-align:right">%s</td>
                          <td style="padding:8px 6px; border-bottom:1px solid #f2f2f2; text-align:right">%s</td>
                          <td style="padding:8px 6px; border-bottom:1px solid #f2f2f2; text-align:right">%s</td>
                        </tr>
                        """.formatted(data, prev, trab, saldo));
            }
        }
        return sb.toString();
    }

    private static String montarCsv(ExtratoPeriodoDTO extrato) {
        StringBuilder sb = new StringBuilder();
        sb.append("data;previsto_min;trabalhado_min;saldo_min\n");
        if (extrato != null && extrato.dias() != null) {
            for (ExtratoDiaDTO d : extrato.dias()) {
                sb.append(d.data()).append(';')
                        .append(d.previstoMin()).append(';')
                        .append(d.trabalhadoMin()).append(';')
                        .append(d.saldoMin()).append('\n');
            }
        }
        sb.append("\nTOTAL_SALDO_MIN;").append(extrato != null ? extrato.saldoTotalMin() : 0).append('\n');
        return sb.toString();
    }

    private static String formatMinutos(int totalMin) {
        int h = Math.abs(totalMin) / 60;
        int m = Math.abs(totalMin) % 60;
        String hm = "%d:%02d".formatted(h, m);
        return totalMin < 0 ? "-" + hm : hm;
    }

    private static String formatSaldo(int saldoMin) {
        String base = formatMinutos(saldoMin);
        return saldoMin < 0 ? "<span style=\"color:#b91c1c\">-" + base.substring(1) + "</span>"
                : "<span style=\"color:#065f46\">" + base + "</span>";
    }

    private static String nullSafe(String s) {
        return s == null ? "-" : s;
    }
}
