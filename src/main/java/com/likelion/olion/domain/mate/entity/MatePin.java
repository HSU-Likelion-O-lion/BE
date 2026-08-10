package com.likelion.olion.domain.mate.entity;

import com.likelion.olion.domain.bookshelf.entity.UserBook;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;

@Entity
@Table(name = "mate_pins", uniqueConstraints = {
        @UniqueConstraint(name = "uk_mate_pin_user_book", columnNames = {"user_id", "user_book_id"}),
        @UniqueConstraint(name = "uk_mate_pin_user_order", columnNames = {"user_id", "pinned_order"})
})
public class MatePin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pinId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_book_id", nullable = false)
    private UserBook userBook;

    @Column(nullable = false)
    private Integer pinnedOrder;

    @Column(nullable = false)
    private Instant createdAt;

    protected MatePin() {
    }

    public MatePin(Long userId, UserBook userBook, Integer pinnedOrder) {
        this.userId = userId;
        this.userBook = userBook;
        this.pinnedOrder = pinnedOrder;
        this.createdAt = Instant.now();
    }

    public Long getPinId() { return pinId; }
    public Long getUserId() { return userId; }
    public UserBook getUserBook() { return userBook; }
    public Integer getPinnedOrder() { return pinnedOrder; }
}
