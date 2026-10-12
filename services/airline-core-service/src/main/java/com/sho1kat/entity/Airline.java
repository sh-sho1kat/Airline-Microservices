package com.sho1kat.entity;


import com.sho1kat.embeddable.Support;
import com.sho1kat.enums.AirlineStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.Instant;

@Entity
@Table(name = "airlines")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Airline {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "iata_code", nullable = false, length = 2)
    private String iataCode;

    @Column(name = "icao_code", nullable = false, length = 3)
    private String icaoCode;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 100)
    private String alias;

    @Column(nullable = false, length = 100)
    private String country;

    @Column(name = "logo_url", length = 2048)
    private String logoUrl;

    @Column(name = "website_url", length = 2048)
    private String website;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private AirlineStatus status = AirlineStatus.PENDING;

    @Column(length = 50)
    private String alliance;

    /**
     * Id of a city owned by the location-service (no cross-service foreign key).
     */
    @Column(name = "headquarters_city_id")
    private Long headquartersCityId;

    /**
     * Id of the owning user in the user-service. One airline per owner.
     */
    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Embedded
    private Support support;

    @LastModifiedBy
    @Column(name = "updated_by_id")
    private Long updatedById;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

}