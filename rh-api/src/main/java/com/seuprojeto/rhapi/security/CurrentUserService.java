package com.seuprojeto.rhapi.security;

import com.seuprojeto.rhapi.domain.Colaborador;
import com.seuprojeto.rhapi.repository.ColaboradorRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Resolve o colaborador logado a partir do SecurityContext.
 * Suposição: o "username" do Authentication é o e-mail do colaborador.
 * Ajuste caso sua autenticação guarde o ID diretamente em um claim.
 */
@Service
public class CurrentUserService {

    private final ColaboradorRepository colaboradorRepository;

    public CurrentUserService(ColaboradorRepository colaboradorRepository) {
        this.colaboradorRepository = colaboradorRepository;
    }

    public Long getColaboradorIdOrThrow() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new IllegalStateException("Usuário não autenticado.");
        }
        String email = auth.getName();
        Colaborador c = colaboradorRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalStateException("Colaborador não encontrado para o usuário autenticado."));
        return c.getId();
    }
}
