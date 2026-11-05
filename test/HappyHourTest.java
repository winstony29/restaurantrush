
public class HappyHourTest {
    public static void main(String[] args) {
        HappyHour h = new HappyHour();

        // Test activation and duration
        h.activate(3);
        check(h.isUsed(), "Activation");
        check(h.isActive(3), "Active on turn 3");
        check(h.isActive(4), "Active on turn 4");
        check(!h.isActive(5), "Inactive on turn 5");

        // Test remaining turns
        check(h.remainingTurns(3) == 2, "2 turns remaining");
        check(h.remainingTurns(4) == 1, "1 turn remaining");
        check(h.remainingTurns(5) == 0, "0 turns remaining");

        // Test discounts
        check(h.discountOn(3, 1000) == 800, "20% discount");
        check(h.discountOn(5, 1000) == 1000, "No discount after Happy Hour");
        check(h.discountOn(4, 999) == 799, "Discount rounding");

        // Test one-time activation
        try {
            h.activate(6);
            throw new AssertionError("Should reject second activation");
        } catch (IllegalMoveException e) {
            System.out.println("Second activation rejected - PASS");
        }

        // Test satisfaction recovery
        Customer c = new RegularCustomer(1);
        c.loseSatisfactionForWaiting(Reputation.NEUTRAL);
        int before = c.getSatisfaction();
        c.recoverSatisfaction(15);
        check(c.getSatisfaction() == Math.min(100, before + 15),
              "Satisfaction recovery");

        System.out.println("All Happy Hour tests passed!");
    }

    private static void check(boolean condition, String name) {
        if (!condition) {
            throw new AssertionError(name + " FAILED");
        }
        System.out.println(name + " - PASS");
    }
}
