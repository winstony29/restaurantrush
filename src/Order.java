/**
 * One customer's order for one menu item.
 *
 * Every transition checks its from-status and throws IllegalMoveException
 * before changing anything, so a rejected move leaves the order untouched.
 * The order flips itself to READY inside advancePrep(turn) once enough prep
 * units have been done, and stamps readyTurn at that moment so a Critic can
 * judge how long the dish sat before it was served.
 *
 * Stage 3: the order also records placedTurn, the turn it was taken, so the
 * Cashier can tell whether it was placed during Happy Hour. The final bill is
 * still not decided here: Cashier computes
 * happyHour.discountOn(order.getPlacedTurn(), customer.applyDiscount(order.basePriceCents()))
 * at payment.
 */
public class Order {

    private final MenuItem item;
    private final Customer customer;
    private final int placedTurn; // the turn the Waiter took the order
    private int unitsDone = 0;
    private int readyTurn = -1;   // stamped when status flips to READY
    private OrderStatus status;

    /** Called by Waiter.takeOrder(c, m, turn). */
    public Order(MenuItem item, Customer customer, int placedTurn) {
        if (item == null || customer == null) {
            throw new IllegalArgumentException("An order needs an item and a customer");
        }
        if (placedTurn < 1) {
            throw new IllegalArgumentException("An order is placed on turn 1 or later, got " + placedTurn);
        }
        this.item = item;
        this.customer = customer;
        this.placedTurn = placedTurn;
        this.status = OrderStatus.TAKEN;
    }

    /**
     * Stage 2 constructor, kept only so Waiter on main still compiles until
     * Waiter.takeOrder(c, m, turn) is merged. placedTurn is unknown (-1), so
     * Happy Hour never applies to such an order.
     * @deprecated use {@link #Order(MenuItem, Customer, int)}; removed in the Stage 3 clean-up PR.
     */
    @Deprecated
    public Order(MenuItem item, Customer customer) {
        if (item == null || customer == null) {
            throw new IllegalArgumentException("An order needs an item and a customer");
        }
        this.item = item;
        this.customer = customer;
        this.placedTurn = -1;
        this.status = OrderStatus.TAKEN;
    }

    /**
     * One unit of chef work on the given turn. Called by Chef.cook(o, turn).
     * On the last unit the order becomes READY and records readyTurn.
     */
    public void advancePrep(int turn) {
        if (turn < 1) {
            throw new IllegalMoveException("Turn must be 1 or later to cook, got " + turn);
        }
        if (status != OrderStatus.TAKEN) {
            throw new IllegalMoveException(describe() + " does not need cooking (it is " + status + ")");
        }
        unitsDone++;
        if (unitsDone >= item.getPrepUnits()) {
            status = OrderStatus.READY;
            readyTurn = turn;
        }
    }

    public boolean isReady() {
        return status == OrderStatus.READY;
    }

    /** READY -> SERVED. Called by Waiter.serve(). */
    public void markServed() {
        if (status != OrderStatus.READY) {
            throw new IllegalMoveException(describe() + " is not ready to serve (it is " + status + ")");
        }
        status = OrderStatus.SERVED;
    }

    /** SERVED -> PAID. Called by Cashier.collect(). */
    public void markPaid() {
        if (status != OrderStatus.SERVED) {
            throw new IllegalMoveException(describe() + " cannot be paid (it is " + status + ")");
        }
        status = OrderStatus.PAID;
    }

    /** TAKEN or READY -> CANCELLED, when the customer gives up before being served. */
    public void cancel() {
        if (status != OrderStatus.TAKEN && status != OrderStatus.READY) {
            throw new IllegalMoveException(describe() + " cannot be cancelled (it is " + status + ")");
        }
        status = OrderStatus.CANCELLED;
    }

    /**
     * The item's menu price, before any discount. MenuItem's price is final,
     * so this is the price at the time the order was taken.
     */
    public int basePriceCents() {
        return item.getPriceCents();
    }

    /** The turn the order was taken, or -1 if it was made with the old constructor. */
    public int getPlacedTurn() {
        return placedTurn;
    }

    /** The turn the order became READY, or -1 if it is not ready yet. */
    public int getReadyTurn() {
        return readyTurn;
    }

    public MenuItem getItem() {
        return item;
    }

    public Customer getCustomer() {
        return customer;
    }

    public OrderStatus getStatus() {
        return status;
    }

    /** Read-only, for showing cooking progress (display only). */
    public int getUnitsDone() {
        return unitsDone;
    }

    private String describe() {
        return "Order for customer " + customer.getId() + " (" + item.getName() + ")";
    }

    @Override
    public String toString() {
        return describe() + " - " + status
                + (status == OrderStatus.TAKEN ? " " + unitsDone + "/" + item.getPrepUnits() : "");
    }
}