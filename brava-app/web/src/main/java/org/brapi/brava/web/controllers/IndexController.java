package org.brapi.brava.web.controllers;

import org.brapi.brava.core.config.CollectionFactory;
import org.brapi.brava.core.service.ValidationService;
import org.brapi.brava.core.validation.AuthorizationMethod;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

    private final CollectionFactory collectionFactory ;

    public IndexController(CollectionFactory collectionFactory) {
        this.collectionFactory = collectionFactory;
    }

    @GetMapping("/")
    public String main(Model model) {
        model.addAttribute("versions", collectionFactory.getCollectionNames());
        model.addAttribute("authorizationMethods", AuthorizationMethod.values());
        return "index";
    }
}