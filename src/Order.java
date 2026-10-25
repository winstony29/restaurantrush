/**
 * One customer's order for one menu item.
 *
 * Every transition checks its from-status and throws IllegalMoveException
 * before changing anything, so a rejected move leaves the order untouched.
 * The order flips itself to READY inside advancePrep() once enough prep
 * units have been done.
 */
public class Order {

    private final MenuItem item;
    private final Customer customer;
    private int unitsDone = 0;
    private OrderStatus status;

    public Order(MenuItem item, Customer customer) {
        if (item == null || customer == null) {
            throw new IllegalArgumentException("An order needs an item and a customer");
        }
        this.item = item;
        this.customer = customer;
        this.status = OrderStatus.TAKEN;
    }

    /** One unit of chef work. Called by Chef.cook(). */
    public void advancePrep() {
        if (status != OrderStatus.TAKEN) {
            throw new IllegalMoveException(describe() + " does not need cooking (it is " + status + ")");
        }
        unitsDone++;
        if (unitsDone >= item.getPrepUnits()) {
            status = OrderStatus.READY;
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

    /** SERVED -> PAID. Called during payment. */
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

    /** Amount to charge. The price is final in MenuItem, so it is the price at order time. */
    public int billCents() {
        return item.getPriceCents();
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

    /** Read-only, for showing cooking progress (not in the UML; display only). */
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
