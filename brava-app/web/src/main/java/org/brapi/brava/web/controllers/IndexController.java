package org.brapi.brava.web.controllers;

import org.brapi.brava.core.validation.AuthorizationMethod;
import org.brapi.brava.web.ValidationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

    private final ValidationService validationService ;

    public IndexController(ValidationService validationService) {
        this.validationService = validationService;
    }

    @GetMapping("/")
    public String main(Model model) {
        model.addAttribute("versions", validationService.getCollectionNames());
        model.addAttribute("authorizationMethods", AuthorizationMethod.values());
        return "index";
    }
}