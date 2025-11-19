package com.lms.web;

import com.lms.domain.CourseWishlist;
import com.lms.repository.UserAccountRepository;
import com.lms.service.WishlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/lms/wishlist")
public class WishlistController {

    @Autowired
    private WishlistService wishlistService;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @PostMapping("/course/{courseId}")
    public ResponseEntity<Map<String, Object>> addToWishlist(
            @AuthenticationPrincipal User principal,
            @PathVariable Long courseId) {
        try {
            var user = userAccountRepository.findByEmail(principal.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            CourseWishlist wishlist = wishlistService.addToWishlist(user.getId(), courseId);
            return ResponseEntity.ok(Map.of(
                    "message", "Course added to wishlist",
                    "wishlistId", wishlist.getId()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/course/{courseId}")
    public ResponseEntity<Map<String, Object>> removeFromWishlist(
            @AuthenticationPrincipal User principal,
            @PathVariable Long courseId) {
        try {
            var user = userAccountRepository.findByEmail(principal.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            wishlistService.removeFromWishlist(user.getId(), courseId);
            return ResponseEntity.ok(Map.of("message", "Course removed from wishlist"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<CourseWishlist>> getWishlist(@AuthenticationPrincipal User principal) {
        try {
            var user = userAccountRepository.findByEmail(principal.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            List<CourseWishlist> wishlist = wishlistService.getUserWishlist(user.getId());
            return ResponseEntity.ok(wishlist);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/course/{courseId}/check")
    public ResponseEntity<Map<String, Object>> checkWishlist(
            @AuthenticationPrincipal User principal,
            @PathVariable Long courseId) {
        try {
            var user = userAccountRepository.findByEmail(principal.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            boolean inWishlist = wishlistService.isInWishlist(user.getId(), courseId);
            return ResponseEntity.ok(Map.of("inWishlist", inWishlist));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}

