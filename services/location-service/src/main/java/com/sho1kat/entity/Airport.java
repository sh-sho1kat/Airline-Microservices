package com.sho1kat.entity;

import com.sho1kat.embeddable.Address;
import com.sho1kat.embeddable.GeoCode;
import jakarta.persistence.*;
import lombok.*;

import java.time.ZoneId;

@Entity
@Table(
        name = "airports",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_airport_iata_code",
                        columnNames = "iata_code"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Airport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "iata_code", nullable = false, unique = true, length = 3)
    private String iataCode;

    @Column(nullable = false)
    private String name;

    @Column(name = "timezone_id", nullable = false)
    private String timeZoneId;

    @Embedded
    private Address address;

    @Embedded
    private GeoCode geoCode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "city_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_airport_city")
    )
    private City city;
}