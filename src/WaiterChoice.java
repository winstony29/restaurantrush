/**
 * The waiter task the manager picked this turn. Returned by
 * RestaurantCLI.readWaiterChoice() and carried out by Game.applyWaiterChoice().
 *
 *   SEAT       customer + table
 *   TAKE_ORDER customer
 *   SERVE      customer (Game serves customer.getOrder())
 *   WAIT       nothing
 *
 * Fields an action does not use are null.
 */
public record WaiterChoice(Kind kind, Customer customer, Table table) {

    public enum Kind { SEAT, TAKE_ORDER, SERVE, WAIT }

    public static WaiterChoice seat(Customer c, Table t) {
        return new WaiterChoice(Kind.SEAT, c, t);
    }

    public static WaiterChoice takeOrder(Customer c) {
        return new WaiterChoice(Kind.TAKE_ORDER, c, null);
    }

    public static WaiterChoice serve(Customer c) {
        return new WaiterChoice(Kind.SERVE, c, null);
    }

    public static WaiterChoice waitTurn() {
        return new WaiterChoice(Kind.WAIT, null, null);
    }
}
