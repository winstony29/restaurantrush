/**
 * A customer's verdict on one serving, returned by Customer.rateService().
 *
 * NONE means "no opinion" (Regular and VIP customers). Only the Critic
 * returns GOOD, NEUTRAL or BAD, which Game turns into the restaurant's
 * Reputation (the names map 1:1).
 */
public enum Rating {
    NONE,
    GOOD,
    NEUTRAL,
    BAD
}