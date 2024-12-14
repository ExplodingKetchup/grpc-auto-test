package org.grpctest.core.util;

import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

public class TimeUtil {

    /**
     * Poll {@code condition} every {@code pollInterval} milliseconds. Returns when {@code condition} returns
     * true, throws {@link TimeoutException} if {@code condition} always evaluated to {@literal false} after
     * {@code timeout} milliseconds.
     *
     * @param condition
     * @param pollInterval
     * @param timeout
     * @throws TimeoutException
     * @throws InterruptedException
     */
    public static void pollForCondition(Supplier<Boolean> condition, long pollInterval, long timeout) throws TimeoutException, InterruptedException {
        long currentTime = System.currentTimeMillis();
        long expiredTime = currentTime + timeout;
        while (currentTime < expiredTime) {
            try {
                if (condition.get()) {
                    return;
                }
            } catch (Throwable ignored) {}
            Thread.sleep(pollInterval);
            currentTime = System.currentTimeMillis();
        }
        throw new TimeoutException("[pollForCondition] Polling timed out for condition " + condition);
    }
}
