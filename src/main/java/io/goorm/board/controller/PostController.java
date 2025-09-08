package io.goorm.board.controller;

import io.goorm.board.entity.Post;
import io.goorm.board.entity.User;
import io.goorm.board.exception.AccessDeniedException;
import io.goorm.board.service.PostService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/posts")
    public String list(Model model) {
        List<Post> posts = postService.findAll();
        model.addAttribute("posts", posts);
        return "post/list";
    }

    @GetMapping("/posts/{seq}")
    public String show(@PathVariable Long seq, Model model) {
        Post post = postService.findBySeq(seq);
        model.addAttribute("post", post);
        return "post/show";
    }

    @GetMapping("/posts/new")
    public String createForm(Model model) {
        model.addAttribute("post", new Post());
        return "post/form";
    }

    @PostMapping("/posts")
    public String create(@Valid @ModelAttribute Post post,
                         BindingResult bindingResult,
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {

        User user = (User) session.getAttribute("user");

        if (bindingResult.hasErrors()) {
            return "post/form";
        }

        post.setAuthor(user);
        postService.save(post);
        redirectAttributes.addFlashAttribute("message", "flash.post.created");
        return "redirect:/posts";
    }

    @GetMapping("/posts/{seq}/edit")
    public String editForm(@PathVariable Long seq, HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        Post post = postService.findBySeq(seq);

        if (!post.getAuthor().getId().equals(user.getId())) {
            throw new AccessDeniedException("본인이 작성한 글만 수정할 수 있습니다.");
        }

        model.addAttribute("post", post);
        return "post/form";
    }

    @PostMapping("/posts/{seq}")
    public String update(@PathVariable Long seq,
                         @Valid @ModelAttribute Post post,
                         BindingResult bindingResult,
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {

        User user = (User) session.getAttribute("user");
        Post existingPost = postService.findBySeq(seq);

        if (!existingPost.getAuthor().getId().equals(user.getId())) {
            throw new AccessDeniedException("본인이 작성한 글만 수정할 수 있습니다.");
        }

        if (bindingResult.hasErrors()) {
            post.setSeq(seq);
            return "post/form";
        }

        postService.update(seq, post);
        redirectAttributes.addFlashAttribute("message", "flash.post.updated");
        return "redirect:/posts/" + seq;
    }

    @PostMapping("/posts/{seq}/delete")
    public String delete(@PathVariable Long seq, HttpSession session, RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        Post existingPost = postService.findBySeq(seq);

        if (!existingPost.getAuthor().getId().equals(user.getId())) {
            throw new AccessDeniedException("본인이 작성한 글만 삭제할 수 있습니다.");
        }

        postService.delete(seq);
        redirectAttributes.addFlashAttribute("message", "flash.post.deleted");
        return "redirect:/posts";
    }

    @GetMapping("/posts/error-test")
    public String testError() {
        throw new RuntimeException("This is a test error for demonstration");
    }

}
