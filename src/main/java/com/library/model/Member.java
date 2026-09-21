package com.library.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Plain Old Java Object representing a library Member entity.
 */
public class Member {

    private Long memberId;
    private String name;
    private String email;
    private String phone;
    private String address;
    private LocalDateTime registeredOn;

    public Member() {
    }

    /**
     * Constructor used when registering a brand-new member (before it has an ID).
     */
    public Member(String name, String email, String phone, String address) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
    }

    /**
     * Full constructor used when hydrating a Member from the database.
     */
    public Member(Long memberId, String name, String email, String phone, String address,
                  LocalDateTime registeredOn) {
        this.memberId = memberId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.registeredOn = registeredOn;
    }

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public LocalDateTime getRegisteredOn() {
        return registeredOn;
    }

    public void setRegisteredOn(LocalDateTime registeredOn) {
        this.registeredOn = registeredOn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Member)) return false;
        Member member = (Member) o;
        return Objects.equals(memberId, member.memberId) && Objects.equals(email, member.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(memberId, email);
    }

    @Override
    public String toString() {
        return String.format(
                "[ID: %-4s] %-25s | Email: %-30s | Phone: %-10s",
                memberId, name, email, phone);
    }
}
