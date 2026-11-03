/** Represents a customer and tracks their order, table and satisfaction. */
public class Customer {
    private final int id;
    private int satisfaction = 100;
    private CustomerStatus status = CustomerStatus.WAITING;
    private Table table;
    private Order order;
    private int servedTurn = -1;
    private int readyToPayTurn = -1;

    public Customer(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Customer ID must be positive.");
        }
        this.id = id;
    }

    // Use the customer ID to pick the same food each time.
    public MenuItem chooseItem(Menu menu) {
        if (menu == null || menu.getItems() == null || menu.getItems().isEmpty()) {
            throw new IllegalArgumentException("Menu must contain at least one item.");
        }
        return menu.getItem((id - 1) % menu.getItems().size());
    }

    // Customers waiting for a table or food lose 8 points each turn.
    public void loseSatisfactionForWaiting() {
        if (status == CustomerStatus.WAITING || status == CustomerStatus.SEATED
                || status == CustomerStatus.ORDERED) {
            satisfaction = Math.max(0, satisfaction - 8);
        }
    }

    public boolean hasGivenUp() {
        return satisfaction == 0 && (status == CustomerStatus.WAITING
                || status == CustomerStatus.SEATED || status == CustomerStatus.ORDERED);
    }

    public void seatAt(Table t) {
        requireStatus(CustomerStatus.WAITING, "be seated");
        if (t == null) {
            throw new IllegalMoveException("Cannot seat customer: table is unavailable.");
        }
        table = t;
        status = CustomerStatus.SEATED;
        // Waiter must coordinate the corresponding Table.seat(this) call.
    }

    public void placeOrder(Order o) {
        requireStatus(CustomerStatus.SEATED, "place an order");
        if (o == null || o.getCustomer() != this) {
            throw new IllegalMoveException("Cannot place order: incorrect customer.");
        }
        order = o;
        status = CustomerStatus.ORDERED;
    }

    public void markServed(int turn) {
        requireStatus(CustomerStatus.ORDERED, "be served");
        if (turn < 1 || order == null || order.getStatus() != OrderStatus.SERVED) {
            throw new IllegalMoveException("Cannot serve customer: order is not served or turn is invalid.");
        }
        servedTurn = turn;
        status = CustomerStatus.SERVED;
    }

    public void markReadyToPay(int turn) {
        requireStatus(CustomerStatus.SERVED, "finish eating");
        if (turn <= servedTurn) {
            throw new IllegalMoveException("Customer must finish eating on a later turn.");
        }
        readyToPayTurn = turn;
        status = CustomerStatus.READY_TO_PAY;
    }

    public void markPaid() {
        requireStatus(CustomerStatus.READY_TO_PAY, "pay");
        status = CustomerStatus.PAID;
        // Cashier updates the order and clears the table.
    }

    public void leave() {
        if (!hasGivenUp()) {
            throw new IllegalMoveException("Only a customer with zero satisfaction may leave.");
        }
        status = CustomerStatus.LEFT;
        // Game removes the customer from the queue, cancels the order and clears the table.
    }

    private void requireStatus(CustomerStatus expected, String action) {
        if (status != expected) {
            throw new IllegalMoveException("Customer " + id + " cannot " + action
                    + " while " + status + "; expected " + expected + ".");
        }
    }

    public int getId() { return id; }
    public int getSatisfaction() { return satisfaction; }
    public CustomerStatus getStatus() { return status; }
    public Table getTable() { return table; }
    public Order getOrder() { return order; }
    public int getReadyToPayTurn() { return readyToPayTurn; }
    public int getServedTurn() { return servedTurn; }
}
