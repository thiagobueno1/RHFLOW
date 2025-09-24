package com.seuprojeto.rhapi.controller;

import com.seuprojeto.rhapi.domain.Departamento;
import com.seuprojeto.rhapi.dto.DepartamentoCreateDTO;
import com.seuprojeto.rhapi.dto.DepartamentoDTO;
import com.seuprojeto.rhapi.mapper.DtoMapper;
import com.seuprojeto.rhapi.repository.DepartamentoRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/departamentos")
public class DepartamentoController {

    private final DepartamentoRepository repo;

    public DepartamentoController(DepartamentoRepository repo) { this.repo = repo; }

    @PostMapping
    public ResponseEntity<?> criar(@RequestBody @Valid DepartamentoCreateDTO dto) {
        if (repo.findByNomeIgnoreCase(dto.nome()).isPresent()) {
            return ResponseEntity.unprocessableEntity().body("Departamento já existe");
        }
        Departamento d = new Departamento();
        d.setNome(dto.nome());
        d = repo.save(d);
        return ResponseEntity.created(URI.create("/departamentos/" + d.getId()))
                .body(DtoMapper.toDTO(d));
    }

    @GetMapping
    public List<DepartamentoDTO> listar() {
        return repo.findAll().stream().map(DtoMapper::toDTO).toList();
    }
}
