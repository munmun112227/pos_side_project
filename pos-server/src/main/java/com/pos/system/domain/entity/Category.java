package com.pos.system.domain.entity;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;
import java.util.List;

/**
 * 1. 品類表 Category (支援無限層級自關聯)
 */
@Entity
@Table(name = "category")
@Getter
@Setter
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    private List<Category> subCategories;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    public Category() {
    }

    public Category(Integer id, Category parent, String name) {
        this.id = id;
        this.parent = parent;
        this.name = name;
    }
}
