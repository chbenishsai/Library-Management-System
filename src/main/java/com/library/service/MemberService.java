package com.library.service;

import com.library.exception.DatabaseOperationException;
import com.library.exception.DuplicateEntryException;
import com.library.exception.InvalidInputException;
import com.library.exception.MemberNotFoundException;
import com.library.model.Member;
import com.library.repository.MemberRepository;
import com.library.util.ValidationUtil;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Business logic layer for Member management: validation of email/phone
 * format, duplicate checking, and orchestration of repository calls.
 */
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService() {
        this.memberRepository = new MemberRepository();
    }

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public Member registerMember(String name, String email, String phone, String address)
            throws InvalidInputException, DuplicateEntryException, DatabaseOperationException {

        if (ValidationUtil.isBlank(name)) {
            throw new InvalidInputException("Name cannot be blank.");
        }
        if (!ValidationUtil.isValidEmail(email)) {
            throw new InvalidInputException("Invalid email format: " + email);
        }
        if (!ValidationUtil.isValidPhone(phone)) {
            throw new InvalidInputException("Phone number must be exactly 10 digits: " + phone);
        }

        if (memberRepository.findByEmail(email.trim()).isPresent()) {
            throw new DuplicateEntryException("A member with email " + email + " is already registered.");
        }

        Member member = new Member(name.trim(), email.trim().toLowerCase(), phone.trim(),
                address == null ? null : address.trim());
        return memberRepository.save(member);
    }

    public List<Member> getAllMembers() throws DatabaseOperationException {
        return memberRepository.findAll().stream()
                .sorted(Comparator.comparing(Member::getName, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
    }

    public Member getMemberById(Long memberId) throws MemberNotFoundException, DatabaseOperationException {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException("No member found with ID: " + memberId));
    }

    public void deleteMember(Long memberId) throws MemberNotFoundException, DatabaseOperationException {
        boolean deleted = memberRepository.deleteById(memberId);
        if (!deleted) {
            throw new MemberNotFoundException("No member found with ID: " + memberId);
        }
    }
}
