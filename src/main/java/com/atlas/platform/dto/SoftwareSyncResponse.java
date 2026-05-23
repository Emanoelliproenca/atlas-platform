package com.atlas.platform.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SoftwareSyncResponse {

    private int processados;
    private int criados;
    private int atualizados;
}
