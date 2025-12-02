package com.lms.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Handles course payment transactions. This entity manages payment processing,
 * Razorpay integration, payment status tracking, coupon and offer application,
 * and discount calculation for course purchases.
 *
 * @author VisionWaves
 * @version 1.0
 */
@Entity
public class CoursePayment {
    /**
     * Unique identifier for the payment
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The student who made the payment
     */
    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private UserAccount student;

    /**
     * The course being purchased
     */
    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    /**
     * Final amount paid after discounts
     */
    @Column(nullable = false)
    private BigDecimal amount;

    /**
     * Razorpay order ID for the payment
     */
    @Column
    private String razorpayOrderId;

    /**
     * Razorpay payment ID for the transaction
     */
    @Column
    private String razorpayPaymentId;

    /**
     * Razorpay signature for payment verification
     */
    @Column
    private String razorpaySignature;

    /**
     * Payment status: PENDING, SUCCESS, FAILED, or REFUNDED
     */
    @Column
    private String status;

    /**
     * Timestamp when the payment was created
     */
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Timestamp when the payment was completed
     */
    @Column
    private LocalDateTime paidAt;

    /**
     * Reason for payment failure (if applicable)
     */
    @Column(length = 1000)
    private String failureReason;

    /**
     * Coupon applied to this payment (if any)
     */
    @ManyToOne
    @JoinColumn(name = "coupon_id")
    private Coupon coupon;

    /**
     * Offer applied to this payment (if any)
     */
    @ManyToOne
    @JoinColumn(name = "offer_id")
    private CourseOffer offer;

    /**
     * Original course price before any discounts
     */
    @Column
    private BigDecimal originalAmount;

    /**
     * Total discount amount applied (from coupon and/or offer)
     */
    @Column
    private BigDecimal discountAmount;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public UserAccount getStudent() { return student; }
    public void setStudent(UserAccount student) { this.student = student; }
    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getRazorpayOrderId() { return razorpayOrderId; }
    public void setRazorpayOrderId(String razorpayOrderId) { this.razorpayOrderId = razorpayOrderId; }
    public String getRazorpayPaymentId() { return razorpayPaymentId; }
    public void setRazorpayPaymentId(String razorpayPaymentId) { this.razorpayPaymentId = razorpayPaymentId; }
    public String getRazorpaySignature() { return razorpaySignature; }
    public void setRazorpaySignature(String razorpaySignature) { this.razorpaySignature = razorpaySignature; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getPaidAt() { return paidAt; }
    public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }
    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
    public Coupon getCoupon() { return coupon; }
    public void setCoupon(Coupon coupon) { this.coupon = coupon; }
    public CourseOffer getOffer() { return offer; }
    public void setOffer(CourseOffer offer) { this.offer = offer; }
    public BigDecimal getOriginalAmount() { return originalAmount; }
    public void setOriginalAmount(BigDecimal originalAmount) { this.originalAmount = originalAmount; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }
}

