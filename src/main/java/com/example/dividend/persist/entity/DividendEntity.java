package com.example.dividend.persist.entity;

import com.example.dividend.model.Dividend;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity(name = "DIVIDEND")
@Getter
@ToString
@NoArgsConstructor
@Table(
    uniqueConstraints = {
        @UniqueConstraint(
            columnNames = { "companyId", "date" }
        )
    }
)
public class DividendEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long companyId;

    private LocalDateTime date;

    private String dividend;

    @Builder
    public DividendEntity(Long companyId, LocalDateTime date, String dividend) {
        this.companyId = companyId;
        this.date = date;
        this.dividend = dividend;
    }

    public static DividendEntity from(Dividend dividend, Long companyId) {
        return DividendEntity.builder()
            .companyId(companyId)
            .date(dividend.getDate())
            .dividend(dividend.getDividend())
            .build();
    }
}
