package com.nikhilawasthi.admission;

import jakarta.validation.Valid;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/applicants")
public class ApplicantApiController {

    private static final List<String> FINAL_STATUSES = List.of("APPROVED", "REJECTED");

    private final ApplicantRepository repository;

    public ApplicantApiController(ApplicantRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody Applicant applicant, BindingResult result) {
        if (result.hasErrors()) {
            Map<String, String> errors = new LinkedHashMap<>();
            result.getFieldErrors().forEach(e -> errors.putIfAbsent(e.getField(), e.getDefaultMessage()));
            return ResponseEntity.badRequest().body(Map.of("errors", errors));
        }
        if (repository.existsByEmail(applicant.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Email already registered"));
        }
        applicant.setId(null);
        applicant.setStatus("PENDING");
        return ResponseEntity.status(HttpStatus.CREATED).body(repository.save(applicant));
    }

    @GetMapping
    public List<Applicant> all() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> one(@PathVariable Long id) {
        Optional<Applicant> found = repository.findById(id);
        if (found.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Applicant not found"));
        }
        return ResponseEntity.ok(found.get());
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestParam String value) {
        if (!FINAL_STATUSES.contains(value)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Status must be APPROVED or REJECTED"));
        }
        Optional<Applicant> found = repository.findById(id);
        if (found.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Applicant not found"));
        }
        Applicant applicant = found.get();
        applicant.setStatus(value);
        return ResponseEntity.ok(repository.save(applicant));
    }
}
