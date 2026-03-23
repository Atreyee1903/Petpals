package org.petpals.controller;

import org.petpals.model.SupportQuery;
import org.petpals.security.CustomUserDetails;
import org.petpals.service.SupportQueryService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class SupportController {

    private final SupportQueryService supportQueryService;

    public SupportController(SupportQueryService supportQueryService) {
        this.supportQueryService = supportQueryService;
    }

    @GetMapping("/support")
    public String supportPage(@AuthenticationPrincipal CustomUserDetails principal, Model model) {
        List<SupportQuery> queries = supportQueryService.getQueriesByUser(principal.getUserId());
        model.addAttribute("queries", queries);
        model.addAttribute("user", principal.getUser());
        return "support";
    }

    @PostMapping("/support")
    public String submitQuery(@RequestParam String queryText,
                              @AuthenticationPrincipal CustomUserDetails principal,
                              RedirectAttributes redirectAttributes) {
        if (queryText == null || queryText.isBlank()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Query cannot be empty.");
            return "redirect:/support";
        }
        supportQueryService.submitQuery(principal.getUserId(), queryText);
        redirectAttributes.addFlashAttribute("successMessage", "Query submitted successfully!");
        return "redirect:/support";
    }
}

