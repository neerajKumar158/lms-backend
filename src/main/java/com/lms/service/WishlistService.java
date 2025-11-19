package com.lms.service;

import com.lms.domain.*;
import com.lms.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Wishlist Service
 * Manages user wishlist/favorites for courses
 */
@Service
public class WishlistService {

    @Autowired
    private CourseWishlistRepository wishlistRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private CourseRepository courseRepository;

    /**
     * Add course to wishlist
     */
    @Transactional
    public CourseWishlist addToWishlist(Long userId, Long courseId) {
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        // Check if already in wishlist
        if (wishlistRepository.existsByUserAndCourse(user, course)) {
            throw new RuntimeException("Course is already in your wishlist");
        }

        CourseWishlist wishlist = new CourseWishlist();
        wishlist.setUser(user);
        wishlist.setCourse(course);
        return wishlistRepository.save(wishlist);
    }

    /**
     * Remove course from wishlist
     */
    @Transactional
    public void removeFromWishlist(Long userId, Long courseId) {
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        CourseWishlist wishlist = wishlistRepository.findByUserAndCourse(user, course)
                .orElseThrow(() -> new RuntimeException("Course is not in your wishlist"));
        
        wishlistRepository.delete(wishlist);
    }

    /**
     * Get user's wishlist
     */
    @Transactional(readOnly = true)
    public List<CourseWishlist> getUserWishlist(Long userId) {
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return wishlistRepository.findByUser(user);
    }

    /**
     * Check if course is in user's wishlist
     */
    @Transactional(readOnly = true)
    public boolean isInWishlist(Long userId, Long courseId) {
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        return wishlistRepository.existsByUserAndCourse(user, course);
    }
}

