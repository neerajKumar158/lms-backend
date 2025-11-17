package com.lms.web;

import com.lms.domain.ForumPost;
import com.lms.domain.ForumThread;
import com.lms.repository.UserAccountRepository;
import com.lms.service.ForumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/lms/forum")
public class ForumController {

    @Autowired
    private ForumService forumService;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @GetMapping("/course/{courseId}/threads")
    public ResponseEntity<?> getThreadsByCourse(@PathVariable("courseId") Long courseId) {
        try {
            List<ForumThread> threads = forumService.getThreadsByCourse(courseId);
            return ResponseEntity.ok(threads);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Failed to load threads"));
        }
    }

    @GetMapping("/threads/{threadId}")
    public ResponseEntity<?> getThreadById(@PathVariable("threadId") Long threadId) {
        try {
            Optional<ForumThread> thread = forumService.getThreadById(threadId);
            if (thread.isPresent()) {
                forumService.incrementViewCount(threadId);
                return ResponseEntity.ok(thread.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Failed to load thread"));
        }
    }

    @PostMapping("/course/{courseId}/threads")
    public ResponseEntity<?> createThread(
            @AuthenticationPrincipal User principal,
            @PathVariable("courseId") Long courseId,
            @RequestBody CreateThreadRequest request) {
        try {
            if (principal == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
            }

            var user = userAccountRepository.findByEmail(principal.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            ForumThread thread = new ForumThread();
            thread.setTitle(request.title());
            thread.setContent(request.content());

            ForumThread created = forumService.createThread(courseId, user.getId(), thread);
            return ResponseEntity.ok(Map.of("id", created.getId(), "message", "Thread created successfully"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Failed to create thread"));
        }
    }

    @PutMapping("/threads/{threadId}")
    public ResponseEntity<?> updateThread(
            @AuthenticationPrincipal User principal,
            @PathVariable("threadId") Long threadId,
            @RequestBody UpdateThreadRequest request) {
        try {
            if (principal == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
            }

            var user = userAccountRepository.findByEmail(principal.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            ForumThread thread = new ForumThread();
            thread.setTitle(request.title());
            thread.setContent(request.content());

            forumService.updateThread(threadId, user.getId(), thread);
            return ResponseEntity.ok(Map.of("message", "Thread updated successfully"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Failed to update thread"));
        }
    }

    @DeleteMapping("/threads/{threadId}")
    public ResponseEntity<?> deleteThread(
            @AuthenticationPrincipal User principal,
            @PathVariable("threadId") Long threadId) {
        try {
            if (principal == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
            }

            var user = userAccountRepository.findByEmail(principal.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            forumService.deleteThread(threadId, user.getId());
            return ResponseEntity.ok(Map.of("message", "Thread deleted successfully"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Failed to delete thread"));
        }
    }

    @PostMapping("/threads/{threadId}/pin")
    public ResponseEntity<?> pinThread(
            @AuthenticationPrincipal User principal,
            @PathVariable("threadId") Long threadId) {
        try {
            if (principal == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
            }

            var user = userAccountRepository.findByEmail(principal.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            forumService.pinThread(threadId, user.getId());
            return ResponseEntity.ok(Map.of("message", "Thread pinned successfully"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Failed to pin thread"));
        }
    }

    @PostMapping("/threads/{threadId}/unpin")
    public ResponseEntity<?> unpinThread(
            @AuthenticationPrincipal User principal,
            @PathVariable("threadId") Long threadId) {
        try {
            if (principal == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
            }

            var user = userAccountRepository.findByEmail(principal.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            forumService.unpinThread(threadId, user.getId());
            return ResponseEntity.ok(Map.of("message", "Thread unpinned successfully"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Failed to unpin thread"));
        }
    }

    @PostMapping("/threads/{threadId}/lock")
    public ResponseEntity<?> lockThread(
            @AuthenticationPrincipal User principal,
            @PathVariable("threadId") Long threadId) {
        try {
            if (principal == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
            }

            var user = userAccountRepository.findByEmail(principal.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            forumService.lockThread(threadId, user.getId());
            return ResponseEntity.ok(Map.of("message", "Thread locked successfully"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Failed to lock thread"));
        }
    }

    @PostMapping("/threads/{threadId}/unlock")
    public ResponseEntity<?> unlockThread(
            @AuthenticationPrincipal User principal,
            @PathVariable("threadId") Long threadId) {
        try {
            if (principal == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
            }

            var user = userAccountRepository.findByEmail(principal.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            forumService.unlockThread(threadId, user.getId());
            return ResponseEntity.ok(Map.of("message", "Thread unlocked successfully"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Failed to unlock thread"));
        }
    }

    @GetMapping("/threads/{threadId}/posts")
    public ResponseEntity<?> getPostsByThread(@PathVariable("threadId") Long threadId) {
        try {
            List<ForumPost> posts = forumService.getPostsByThread(threadId);
            return ResponseEntity.ok(posts);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Failed to load posts"));
        }
    }

    @PostMapping("/threads/{threadId}/posts")
    public ResponseEntity<?> createPost(
            @AuthenticationPrincipal User principal,
            @PathVariable("threadId") Long threadId,
            @RequestBody CreatePostRequest request) {
        try {
            if (principal == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
            }

            var user = userAccountRepository.findByEmail(principal.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            ForumPost post = new ForumPost();
            post.setContent(request.content());

            ForumPost created = forumService.createPost(threadId, user.getId(), request.parentPostId(), post);
            return ResponseEntity.ok(Map.of("id", created.getId(), "message", "Post created successfully"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Failed to create post"));
        }
    }

    @PutMapping("/posts/{postId}")
    public ResponseEntity<?> updatePost(
            @AuthenticationPrincipal User principal,
            @PathVariable("postId") Long postId,
            @RequestBody UpdatePostRequest request) {
        try {
            if (principal == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
            }

            var user = userAccountRepository.findByEmail(principal.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            ForumPost post = new ForumPost();
            post.setContent(request.content());

            forumService.updatePost(postId, user.getId(), post);
            return ResponseEntity.ok(Map.of("message", "Post updated successfully"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Failed to update post"));
        }
    }

    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<?> deletePost(
            @AuthenticationPrincipal User principal,
            @PathVariable("postId") Long postId) {
        try {
            if (principal == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
            }

            var user = userAccountRepository.findByEmail(principal.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            forumService.deletePost(postId, user.getId());
            return ResponseEntity.ok(Map.of("message", "Post deleted successfully"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Failed to delete post"));
        }
    }

    @GetMapping("/posts/{postId}/replies")
    public ResponseEntity<?> getRepliesByPost(@PathVariable("postId") Long postId) {
        try {
            List<ForumPost> replies = forumService.getRepliesByPost(postId);
            return ResponseEntity.ok(replies);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Failed to load replies"));
        }
    }

    record CreateThreadRequest(String title, String content) {}
    record UpdateThreadRequest(String title, String content) {}
    record CreatePostRequest(String content, Long parentPostId) {}
    record UpdatePostRequest(String content) {}
}

