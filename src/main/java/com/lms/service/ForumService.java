package com.lms.service;

import com.lms.domain.Course;
import com.lms.domain.CourseEnrollment;
import com.lms.domain.ForumPost;
import com.lms.domain.ForumThread;
import com.lms.domain.UserAccount;
import com.lms.repository.CourseEnrollmentRepository;
import com.lms.repository.CourseRepository;
import com.lms.repository.ForumPostRepository;
import com.lms.repository.ForumThreadRepository;
import com.lms.repository.UserAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ForumService {

    @Autowired
    private ForumThreadRepository threadRepository;

    @Autowired
    private ForumPostRepository postRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private CourseEnrollmentRepository enrollmentRepository;

    @Transactional(readOnly = true)
    public List<ForumThread> getThreadsByCourse(Long courseId) {
        return threadRepository.findByCourseIdOrderByIsPinnedDescLastActivityAtDesc(courseId);
    }

    @Transactional(readOnly = true)
    public Optional<ForumThread> getThreadById(Long threadId) {
        return threadRepository.findById(threadId);
    }

    @Transactional
    public ForumThread createThread(Long courseId, Long authorId, ForumThread thread) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        UserAccount author = userAccountRepository.findById(authorId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Verify user is enrolled in the course
        boolean isEnrolled = enrollmentRepository.findByStudentAndCourse(author, course).isPresent();
        boolean isInstructor = course.getInstructor().getId().equals(authorId);
        
        if (!isEnrolled && !isInstructor) {
            throw new RuntimeException("You must be enrolled in the course to create forum threads");
        }

        thread.setCourse(course);
        thread.setAuthor(author);
        thread.setCreatedAt(LocalDateTime.now());
        thread.setLastActivityAt(LocalDateTime.now());

        return threadRepository.save(thread);
    }

    @Transactional
    public ForumThread updateThread(Long threadId, Long userId, ForumThread updatedThread) {
        ForumThread thread = threadRepository.findById(threadId)
                .orElseThrow(() -> new RuntimeException("Thread not found"));

        // Only author or instructor can update
        boolean isAuthor = thread.getAuthor().getId().equals(userId);
        boolean isInstructor = thread.getCourse().getInstructor().getId().equals(userId);

        if (!isAuthor && !isInstructor) {
            throw new RuntimeException("You don't have permission to update this thread");
        }

        if (updatedThread.getTitle() != null) {
            thread.setTitle(updatedThread.getTitle());
        }
        if (updatedThread.getContent() != null) {
            thread.setContent(updatedThread.getContent());
        }
        thread.setUpdatedAt(LocalDateTime.now());

        return threadRepository.save(thread);
    }

    @Transactional
    public void deleteThread(Long threadId, Long userId) {
        ForumThread thread = threadRepository.findById(threadId)
                .orElseThrow(() -> new RuntimeException("Thread not found"));

        // Only author or instructor can delete
        boolean isAuthor = thread.getAuthor().getId().equals(userId);
        boolean isInstructor = thread.getCourse().getInstructor().getId().equals(userId);

        if (!isAuthor && !isInstructor) {
            throw new RuntimeException("You don't have permission to delete this thread");
        }

        threadRepository.delete(thread);
    }

    @Transactional
    public void pinThread(Long threadId, Long instructorId) {
        ForumThread thread = threadRepository.findById(threadId)
                .orElseThrow(() -> new RuntimeException("Thread not found"));

        if (!thread.getCourse().getInstructor().getId().equals(instructorId)) {
            throw new RuntimeException("Only the course instructor can pin threads");
        }

        thread.setIsPinned(true);
        threadRepository.save(thread);
    }

    @Transactional
    public void unpinThread(Long threadId, Long instructorId) {
        ForumThread thread = threadRepository.findById(threadId)
                .orElseThrow(() -> new RuntimeException("Thread not found"));

        if (!thread.getCourse().getInstructor().getId().equals(instructorId)) {
            throw new RuntimeException("Only the course instructor can unpin threads");
        }

        thread.setIsPinned(false);
        threadRepository.save(thread);
    }

    @Transactional
    public void lockThread(Long threadId, Long instructorId) {
        ForumThread thread = threadRepository.findById(threadId)
                .orElseThrow(() -> new RuntimeException("Thread not found"));

        if (!thread.getCourse().getInstructor().getId().equals(instructorId)) {
            throw new RuntimeException("Only the course instructor can lock threads");
        }

        thread.setIsLocked(true);
        threadRepository.save(thread);
    }

    @Transactional
    public void unlockThread(Long threadId, Long instructorId) {
        ForumThread thread = threadRepository.findById(threadId)
                .orElseThrow(() -> new RuntimeException("Thread not found"));

        if (!thread.getCourse().getInstructor().getId().equals(instructorId)) {
            throw new RuntimeException("Only the course instructor can unlock threads");
        }

        thread.setIsLocked(false);
        threadRepository.save(thread);
    }

    @Transactional
    public void incrementViewCount(Long threadId) {
        ForumThread thread = threadRepository.findById(threadId)
                .orElse(null);
        if (thread != null) {
            thread.setViewCount(thread.getViewCount() + 1);
            threadRepository.save(thread);
        }
    }

    @Transactional(readOnly = true)
    public List<ForumPost> getPostsByThread(Long threadId) {
        return postRepository.findByThreadIdAndParentPostIsNullOrderByCreatedAtAsc(threadId);
    }

    @Transactional
    public ForumPost createPost(Long threadId, Long authorId, Long parentPostId, ForumPost post) {
        ForumThread thread = threadRepository.findById(threadId)
                .orElseThrow(() -> new RuntimeException("Thread not found"));

        if (thread.getIsLocked()) {
            throw new RuntimeException("This thread is locked");
        }

        UserAccount author = userAccountRepository.findById(authorId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Verify user is enrolled in the course
        boolean isEnrolled = enrollmentRepository.findByStudentAndCourse(author, thread.getCourse()).isPresent();
        boolean isInstructor = thread.getCourse().getInstructor().getId().equals(authorId);
        
        if (!isEnrolled && !isInstructor) {
            throw new RuntimeException("You must be enrolled in the course to post in forums");
        }

        post.setThread(thread);
        post.setAuthor(author);
        post.setCreatedAt(LocalDateTime.now());

        if (parentPostId != null) {
            ForumPost parentPost = postRepository.findById(parentPostId)
                    .orElseThrow(() -> new RuntimeException("Parent post not found"));
            post.setParentPost(parentPost);
        }

        ForumPost saved = postRepository.save(post);

        // Update thread's last activity
        thread.setLastActivityAt(LocalDateTime.now());
        threadRepository.save(thread);

        return saved;
    }

    @Transactional
    public ForumPost updatePost(Long postId, Long userId, ForumPost updatedPost) {
        ForumPost post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        // Only author or instructor can update
        boolean isAuthor = post.getAuthor().getId().equals(userId);
        boolean isInstructor = post.getThread().getCourse().getInstructor().getId().equals(userId);

        if (!isAuthor && !isInstructor) {
            throw new RuntimeException("You don't have permission to update this post");
        }

        if (updatedPost.getContent() != null) {
            post.setContent(updatedPost.getContent());
        }
        post.setIsEdited(true);
        post.setUpdatedAt(LocalDateTime.now());

        return postRepository.save(post);
    }

    @Transactional
    public void deletePost(Long postId, Long userId) {
        ForumPost post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        // Only author or instructor can delete
        boolean isAuthor = post.getAuthor().getId().equals(userId);
        boolean isInstructor = post.getThread().getCourse().getInstructor().getId().equals(userId);

        if (!isAuthor && !isInstructor) {
            throw new RuntimeException("You don't have permission to delete this post");
        }

        postRepository.delete(post);
    }

    @Transactional(readOnly = true)
    public List<ForumPost> getRepliesByPost(Long postId) {
        return postRepository.findByParentPostIdOrderByCreatedAtAsc(postId);
    }
}

