package org.grpctest.core.enums;

import lombok.Getter;

@Getter
public enum CleanupMode {
    NONE(0),
    BEFORE(1),
    AFTER(2),
    BEFORE_AND_AFTER(3);

    private final int num;

    CleanupMode(int num) {
        this.num = num;
    }
}
