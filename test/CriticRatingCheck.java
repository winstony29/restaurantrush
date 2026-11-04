
public class CriticRatingCheck {

    public static void main(String[] args) {
        checkRating(1, Rating.GOOD);
        checkRating(2, Rating.NEUTRAL);
        checkRating(3, Rating.BAD);

        System.out.println("All 3 Critic rating tests passed!");
    }

    private static void checkRating(int delay, Rating expected) {
        CriticCustomer critic = new CriticCustomer(1);
        MenuItem pasta = new MenuItem("Pasta", 1800, 2);
        Order order = new Order(pasta, critic);

        // Two units of preparation, ready on turn 5
        order.advancePrep(4);
        order.advancePrep(5);

        int servedTurn = 5 + delay;
        Rating actual = critic.rateService(order, servedTurn);

        if (actual != expected) {
            throw new AssertionError(
                "Delay " + delay + ": expected " + expected
                + ", got " + actual
            );
        }

        System.out.println("Delay " + delay + ": " + actual + " - PASS");
    }
}
