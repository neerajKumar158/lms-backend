package com.lms.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
public class CoursePayment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private UserAccount student;

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column
    private String razorpayOrderId;

    @Column
    private String razorpayPaymentId;

    @Column
    private String razorpaySignature;

    @Column
    private String status; // PENDING, SUCCESS, FAILED, REFUNDED

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column
    private LocalDateTime paidAt;

    @Column(length = 1000)
    private String failureReason;

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
}

