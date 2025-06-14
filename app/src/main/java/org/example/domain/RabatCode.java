package org.example.domain;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class RabatCode {

    private Long id;
    private String code;
    private double rabat;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

}
