package com.pos.system.domain.entity;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;

/**
 * 8. 會員表 Member
 */
@Entity
@Table(name = "member")
@Getter
@Setter
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "code", unique = true, nullable = false, length = 50)
    private String code;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "phone", length = 50)
    private String phone;

    public Member() {
    }
}
