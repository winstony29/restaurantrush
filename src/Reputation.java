/**
 * The restaurant's reputation, set by the last Critic rating.
 * Starts NEUTRAL and stays until the next Critic rates a serving.
 *
 * It owns the rule for how fast every waiting customer loses satisfaction:
 * GOOD makes waiting 2 points cheaper per turn, BAD makes it 2 points dearer,
 * NEUTRAL leaves it unchanged. The adjusted loss is never below 1.
 */
public enum Reputation {
    GOOD(-2),
    NEUTRAL(0),
    BAD(2);
 
    private static final int MIN_LOSS = 1;
 
    private final int lossChange;
 
    Reputation(int lossChange) {
        this.lossChange = lossChange;
    }
 
    /** Applies this reputation to a customer type's base loss (8 / 5 / 12). */
    public int adjustLoss(int base) {
        return Math.max(MIN_LOSS, base + lossChange);
    }
}