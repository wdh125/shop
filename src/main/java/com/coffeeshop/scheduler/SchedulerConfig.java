package com.coffeeshop.scheduler;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SchedulerConfig {
    @Value("${scheduler.table.available.delay-minutes}")
    public int tableAvailableDelayMinutes;

    @Value("${scheduler.reservation.block-before-minutes}")
    public int reservationBlockBeforeMinutes;

    @Value("${scheduler.reservation.cancel-before-minutes}")
    public int reservationCancelBeforeMinutes;

    @Value("${scheduler.order.cancel-after-minutes}")
    public int orderCancelAfterMinutes;

    @Value("${scheduler.order.preparing-before-minutes}")
    public int orderPreparingBeforeMinutes;

    @Value("${scheduler.order.pending-to-cancelled-minutes}")
    public int orderPendingToCancelledMinutes;

    @Value("${scheduler.order.preparing-to-ready-minutes}")
    public int orderPreparingToReadyMinutes;

    @Value("${scheduler.order.served-to-completed-minutes}")
    public int orderServedToCompletedMinutes;

    @Value("${scheduler.reservation.auto-reserve-before-minutes}")
    public int reservationAutoReserveBeforeMinutes;

    @Value("${scheduler.reservation.grace-period-minutes}")
    public int reservationGracePeriodMinutes;

    @Value("${scheduler.reservation.min-gap-minutes}")
    public int reservationMinGapMinutes;

    @Value("${scheduler.reservation.min-advance-minutes}")
    public int reservationMinAdvanceMinutes;

    @Value("${scheduler.reservation.duration-minutes}")
    public int reservationDurationMinutes;

    @Value("${scheduler.reservation.buffer-after-minutes}")
    public int reservationBufferAfterMinutes;

    @Value("${scheduler.opening-time}")
    public String openingTime;

    @Value("${scheduler.closing-time}")
    public String closingTime;
} 