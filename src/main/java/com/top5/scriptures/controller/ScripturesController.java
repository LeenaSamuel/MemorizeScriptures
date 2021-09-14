package com.top5.scriptures.controller;

import java.util.List;
import java.util.Set;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;

import com.top5.scriptures.custom.UserAlreadyExistsException;
import com.top5.scriptures.model.BibleVersion;
import com.top5.scriptures.model.CustomUserDetail;
import com.top5.scriptures.model.Scripture;
import com.top5.scriptures.model.User;
import com.top5.scriptures.service.ScripturesService;

@Controller
public class ScripturesController {

	@Autowired
	private ScripturesService service;

	@GetMapping({ "/", "/login" })
	public String login() {
		return "/login";
	}

	@GetMapping("/signup")
	public String showRegistrationForm(WebRequest request, Model model) {
		User user = new User();
		model.addAttribute("user", user);
		return "/signup";
	}

	@GetMapping("/about")
	public String showAbout() {
		return "about";
	}
	

	@GetMapping("/dashboard")
	public String showDashboard(Model model){
		return "/dashboard";
		
	}
	
	@GetMapping("/settings")
	public String showSettings(Model model){
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetail userDetails = (CustomUserDetail)auth.getPrincipal();
        model.addAttribute("user", userDetails.getUser());
		List<BibleVersion> bibleVersions = service.getBibleVersions();
		Set<String> categories = service.loadCategories();
		if(bibleVersions != null) {
			model.addAttribute("bibleVersions", bibleVersions);
		}
		if(categories != null) {
			model.addAttribute("categories", categories);
		}
		return "/settings";
		
	}
	
	@PostMapping("/settings")
	public String saveSettings(@ModelAttribute("user") @Valid CustomUserDetail user, BindingResult result, Model model) {
		service.updateUserSettings(user.getUser());
		
		List<Scripture> scriptures = service.loadScriptures(user.getUser());
		
		if(scriptures != null) {
			model.addAttribute("scriptures", scriptures);
		}
		
		return "/dashboard";
	}
	
	@GetMapping("/history")
	public String showHistory(Model model){
		return "/history";
		
	}
	
	@PostMapping("/signup")
	public ModelAndView processRegister(@ModelAttribute("user") @Valid User user, BindingResult result) {
		User registered = null;
		ModelAndView mav = new ModelAndView();
		if (result.hasErrors()) {
			mav.setViewName("signup");
			return mav;
		}

		try {
			registered = service.registerUser(user);
		} catch (UserAlreadyExistsException uaeEx) {
			mav.addObject("message", "An account for that username/email already exists.");
			mav.setViewName("signup");
			return mav;
		}

		mav.setViewName("settings");
		return mav;
	}

	@RequestMapping("/book")
	public String Home(@RequestParam(name = "book", required = false, defaultValue = "1") String bookNumber,
			Model model) {
		model.addAttribute("bookNumber", bookNumber);

		return "categorical";
	}

}
