package tech.yakov.AtemProxy.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import tech.yakov.AtemProxy.service.TallyConstellationService;

@Controller
public class WebController {

    private final TallyConstellationService tallyConstellationService;

    public WebController(TallyConstellationService tallyConstellationService){
        this.tallyConstellationService = tallyConstellationService;
    }

    @GetMapping("/")
    public String index(Model model){
        model.addAttribute("signals", tallyConstellationService.getAtem().getSignals());
        return "index";
    }

}
