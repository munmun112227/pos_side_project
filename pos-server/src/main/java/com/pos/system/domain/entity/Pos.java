package com.pos.system.domain.entity;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;

/**
 * 7. 收銀機表 POS
 */
@Entity
@Table(name = "pos")
@Getter
@Setter
public class Pos {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "code", unique = true, nullable = false, length = 50)
    private String code;

    @Column(name = "ip", length = 50)
    private String ip;

    @Column(name = "location", length = 100)
    private String location;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    public Pos() {
    }
}
