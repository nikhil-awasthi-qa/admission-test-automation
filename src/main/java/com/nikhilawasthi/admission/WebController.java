package com.nikhilawasthi.admission;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class WebController {

    private final ApplicantRepository repository;

    public WebController(ApplicantRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/")
    public String form(Model model) {
        model.addAttribute("applicant", new Applicant());
        return "apply";
    }

    @PostMapping("/apply")
    public String submit(@Valid @ModelAttribute("applicant") Applicant applicant, BindingResult result) {
        if (!result.hasFieldErrors("email") && repository.existsByEmail(applicant.getEmail())) {
            result.rejectValue("email", "duplicate", "Email already registered");
        }
        if (result.hasErrors()) {
            return "apply";
        }
        applicant.setStatus("PENDING");
        Applicant saved = repository.save(applicant);
        return "redirect:/confirmation/" + saved.getId();
    }

    @GetMapping("/confirmation/{id}")
    public String confirmation(@PathVariable Long id, Model model) {
        model.addAttribute("applicant", repository.findById(id).orElseThrow());
        return "confirmation";
    }

    @GetMapping("/applicants")
    public String list(Model model) {
        model.addAttribute("applicants", repository.findAll());
        return "applicants";
    }

    @PostMapping("/applicants/{id}/approve")
    public String approve(@PathVariable Long id) {
        return changeStatus(id, "APPROVED");
    }

    @PostMapping("/applicants/{id}/reject")
    public String reject(@PathVariable Long id) {
        return changeStatus(id, "REJECTED");
    }

    private String changeStatus(Long id, String status) {
        Applicant applicant = repository.findById(id).orElseThrow();
        applicant.setStatus(status);
        repository.save(applicant);
        return "redirect:/applicants";
    }
}
