package com.raffleease.raffleease.Domains.Images.Model;

import com.raffleease.raffleease.Domains.Associations.Model.Association;
import com.raffleease.raffleease.Domains.Images.Services.Impls.ImageEntityListener;
import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "Images")
@EntityListeners(ImageEntityListener.class)
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String fileName;
    private String filePath;
    private String contentType;

    @Column(unique = true)
    private String url;
    private Integer imageOrder;

    @ManyToOne
    @JoinColumn(name = "raffle_id")
    private Raffle raffle;

    @ManyToOne
    @JoinColumn(name = "association_id")
    private Association association;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
