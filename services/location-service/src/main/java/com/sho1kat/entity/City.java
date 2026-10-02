package com.sho1kat.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class City {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String cityName;

    @Column(nullable = false, unique = true, length = 10)
    private String cityCode;

    @Column(nullable = false, length = 3)
    private String countryCode;

    @Column(nullable = false, length = 100)
    private String countryName;

    @Column(length = 20)
    private String regionCode;

    @Column(length = 100)
    private String timeZoneId;
}