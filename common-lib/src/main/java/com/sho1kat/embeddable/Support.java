package com.sho1kat.embeddable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class Support {
    @Column(name = "support_email", length = 254)
    private String email;

    @Column(name = "support_phone", length = 40)
    private String phone;

    @Column(name = "support_hours", length = 200)
    private String hours;
}

