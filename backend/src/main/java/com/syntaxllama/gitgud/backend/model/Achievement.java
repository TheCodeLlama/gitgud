package com.syntaxllama.gitgud.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Achievement entity defining available achievements in the system.
 */
@Entity
@Table(name = "achievements")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Achievement extends BaseEntity {

    @Column(unique = true, nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "icon_url", length = 500)
    private String iconUrl;

    @Column(name = "criteria_json", columnDefinition = "TEXT")
    private String criteriaJson;

    @Column(name = "xp_reward", nullable = false)
    private Integer xpReward = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Rarity rarity;

    public enum Rarity {
        COMMON,
        RARE,
        EPIC,
        LEGENDARY
    }
}
