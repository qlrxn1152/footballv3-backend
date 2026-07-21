package io.github.qlrxn1152.footballv3.member.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Table(name = "members")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    private static final int MEMBER_INIT_RATING = 1500;

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @Column(nullable = false, unique = true, length = 12)
    private String username;

    @Column(nullable = false, length = 255)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "member_role", nullable = false)
    private MemberRole role;

    @Column(name = "member_rating")
    private int rating;

    @Column(name = "member_total_goal_count")
    private int totalGoalCount;

    @Column(name = "joined_at")
    private LocalDateTime createdAt;

    private Member(String username, String password) {
        this.username = username;
        this.password = password;

        this.role = MemberRole.USER;
        this.rating = MEMBER_INIT_RATING;
        this.totalGoalCount = 0;
        this.createdAt = LocalDateTime.now();
    }

    public static Member signup(String username, String encodedPassword) {
        return new Member(username, encodedPassword);
    }

}
