/**
 * Lifecycle of an {@link Order}.
 *
 * TAKEN -> READY -> SERVED -> PAID
 * TAKEN or READY -> CANCELLED (the customer gave up before being served)
 */
public enum OrderStatus {
    TAKEN,
    READY,
    SERVED,
    PAID,
    CANCELLED
}
