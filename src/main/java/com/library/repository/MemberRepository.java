package com.library.repository;

import com.library.exception.DatabaseOperationException;
import com.library.model.Member;
import com.library.util.DatabaseConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for the {@code members} table.
 */
public class MemberRepository {

    private final DatabaseConnectionManager connectionManager;

    public MemberRepository() {
        this.connectionManager = DatabaseConnectionManager.getInstance();
    }

    public Member save(Member member) throws DatabaseOperationException {
        String sql = "INSERT INTO members (name, email, phone, address) VALUES (?, ?, ?, ?)";

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, member.getName());
            ps.setString(2, member.getEmail());
            ps.setString(3, member.getPhone());
            ps.setString(4, member.getAddress());

            ps.executeUpdate();

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    member.setMemberId(generatedKeys.getLong(1));
                }
            }
            return member;

        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to save member: " + member.getEmail(), e);
        }
    }

    public Optional<Member> findById(Long memberId) throws DatabaseOperationException {
        String sql = "SELECT * FROM members WHERE member_id = ?";

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, memberId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }

        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to find member by id: " + memberId, e);
        }
    }

    public Optional<Member> findByEmail(String email) throws DatabaseOperationException {
        String sql = "SELECT * FROM members WHERE email = ?";

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }

        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to find member by email: " + email, e);
        }
    }

    public List<Member> findAll() throws DatabaseOperationException {
        String sql = "SELECT * FROM members ORDER BY member_id";
        List<Member> members = new ArrayList<>();

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                members.add(mapRow(rs));
            }
            return members;

        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to fetch all members", e);
        }
    }

    public boolean update(Member member) throws DatabaseOperationException {
        String sql = "UPDATE members SET name = ?, email = ?, phone = ?, address = ? WHERE member_id = ?";

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, member.getName());
            ps.setString(2, member.getEmail());
            ps.setString(3, member.getPhone());
            ps.setString(4, member.getAddress());
            ps.setLong(5, member.getMemberId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to update member id: " + member.getMemberId(), e);
        }
    }

    public boolean deleteById(Long memberId) throws DatabaseOperationException {
        String sql = "DELETE FROM members WHERE member_id = ?";

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, memberId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to delete member id: " + memberId, e);
        }
    }

    private Member mapRow(ResultSet rs) throws SQLException {
        LocalDateTime registeredOn = rs.getTimestamp("registered_on") != null
                ? rs.getTimestamp("registered_on").toLocalDateTime()
                : null;

        return new Member(
                rs.getLong("member_id"),
                rs.getString("name"),
                rs.getString("email"),
                rs.getString("phone"),
                rs.getString("address"),
                registeredOn
        );
    }
}
