/**
 * One customer's order for one menu item.
 *
 * Every transition checks its from-status and throws IllegalMoveException
 * before changing anything, so a rejected move leaves the order untouched.
 * The order flips itself to READY inside advancePrep(turn) once enough prep
 * units have been done, and stamps readyTurn at that moment so a Critic can
 * judge how long the dish sat before it was served.
 *
 * The final bill is not decided here: Cashier computes
 * customer.applyDiscount(order.basePriceCents()) at payment.
 */
public class Order {
 
    private final MenuItem item;
    private final Customer customer;
    private int unitsDone = 0;
    private int readyTurn = -1; // stamped when status flips to READY
    private OrderStatus status;
 
    public Order(MenuItem item, Customer customer) {
        if (item == null || customer == null) {
            throw new IllegalArgumentException("An order needs an item and a customer");
        }
        this.item = item;
        this.customer = customer;
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
        prepOneUnit(turn);
    }
 
    /**
     * Stage 1 version, kept only so Chef on main still compiles until
     * Chef.cook(o, turn) is merged. Leaves readyTurn unknown (-1).
     * @deprecated use {@link #advancePrep(int)}.
     */
    @Deprecated
    public void advancePrep() {
        prepOneUnit(-1);
    }
 
    private void prepOneUnit(int turn) {
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
 
    /**
     * Stage 1 name, kept only so Cashier on main still compiles until
     * Cashier uses customer.applyDiscount(order.basePriceCents()).
     * @deprecated use {@link #basePriceCents()}.
     */
    @Deprecated
    public int billCents() {
        return basePriceCents();
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
 
