package com.acm.acmwebsite.feature.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "high_board")
public class HighBoard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(nullable = false)
    private String role;

    @Column(name = "`order`")
    private Integer order;

    public HighBoard() {
    }

    public HighBoard(Long id, String name, String imageUrl, String role, Integer order) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
        this.role = role;
        this.order = order;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

}
