package com.forvia.partinventoryservice.web;

import com.forvia.partinventoryservice.model.Part;
import com.forvia.partinventoryservice.repository.PartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Optional;
import java.util.UUID;

@Controller
public class PartWebController {

    @Autowired
    private PartRepository partRepository;

    @GetMapping
    public String home(Model model) {
        model.addAttribute("parts", partRepository.findAll());
        model.addAttribute("part", new Part());
        return "index";
    }

    @GetMapping("/inventory")
    public String listParts(Model model) {
        model.addAttribute("parts", partRepository.findAll());
        model.addAttribute("part", new Part());
        return "index";
    }

    @PostMapping("/parts")
    public String addPart(@ModelAttribute Part part) {
        part.setId(UUID.randomUUID().toString());
        partRepository.save(part);
        return "redirect:./";
    }

    @GetMapping("/parts/{id}/edit")
    public String showEditForm(@PathVariable String id, Model model) {
        Optional<Part> part = partRepository.findById(id);
        if (part.isPresent()) {
            model.addAttribute("part", part.get());
            return "edit-part";
        }
        return "redirect:./";
    }

    @PostMapping("/parts/{id}")
    public String updatePart(@PathVariable String id, @ModelAttribute Part part) {
        part.setId(id);
        partRepository.save(part);
        return "redirect:/";
    }

    @PostMapping("/parts/{id}/delete")
    public String deletePart(@PathVariable String id) {
        partRepository.deleteById(id);
        return "redirect:/";
    }


}