package com.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpSession;

/**
 * @author Clrvn
 */
@Controller
public class PageController {

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @GetMapping("loginOut")
    public String loginOut(HttpSession session) {
        session.removeAttribute("USER");
        session.invalidate();
        session.getServletContext().removeAttribute(session.getId());
        return "login";
    }

    @GetMapping("/index")
    public String index(HttpSession session, Model model) {
        return "index";
    }

    @GetMapping("/welcome")
    public String welcome() {
        return "welcome";
    }

    @GetMapping("/editMine")
    public String editMine() {
        return "editMine";
    }

    @GetMapping("/editPassword")
    public String editPassword() {
        return "editPassword";
    }

    /**
     * 就业分析
     */
    @GetMapping("/jobAnalysis")
    public String jobAnalysis() {
        return "jobAnalysis";
    }

    @GetMapping("/jobAdd")
    public String jobAdd() {
        return "jobAdd";
    }

    @GetMapping("/jobEdit")
    public String jobEdit() {
        return "jobEdit";
    }

    @GetMapping("/jobManage")
    public String jobManage() {
        return "jobManage";
    }


}
