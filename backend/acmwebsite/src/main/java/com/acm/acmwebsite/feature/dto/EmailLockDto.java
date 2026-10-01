package com.acm.acmwebsite.feature.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailLockDto {
    private boolean emailsEnabled;
}
