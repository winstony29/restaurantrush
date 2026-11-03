//Chef subclass
public class Chef extends Staff {
    //reuires order object implemented later
    public void cook(Order o, int turn) {

        // Check whether Chef has already acted this turn
        requireAvailable();

        // Check whether an order was selected
        if (o == null) {
            throw new IllegalMoveException("No order selected");
        }

        // Check whether the order can be cooked
        if (o.getStatus() != OrderStatus.TAKEN) {
            throw new IllegalMoveException("Order cannot be cooked");
        }

        // Advance cooking progress by one unit, changed to add turn for stage 2
        o.advancePrep(turn);

        // Mark Chef as having acted
        markActed();
    }
}