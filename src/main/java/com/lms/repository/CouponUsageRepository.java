package com.lms.repository;

import com.lms.domain.Coupon;
import com.lms.domain.CouponUsage;
import com.lms.domain.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CouponUsageRepository extends JpaRepository<CouponUsage, Long> {
    List<CouponUsage> findByCoupon(Coupon coupon);
    List<CouponUsage> findByUser(UserAccount user);
    List<CouponUsage> findByCouponAndUser(Coupon coupon, UserAccount user);
    long countByCouponAndUser(Coupon coupon, UserAccount user);
}




