/**
 * A table holds at most one customer (multiplicity 0..1).
 *
 * Table only manages its own side of the Customer-Table link. Waiter.seat()
 * is expected to call both table.seat(c) and c.seatAt(table), and Game calls
 * table.clear() when the customer pays or leaves.
 */
public class Table {

    private final int tableId;
    private Customer customer; // null when the table is free

    public Table(int tableId) {
        this.tableId = tableId;
    }

    public boolean isFree() {
        return customer == null;
    }

    /** Guard: the table must be free. Nothing changes if the guard fails. */
    public void seat(Customer c) {
        if (c == null) {
            throw new IllegalArgumentException("Cannot seat a null customer");
        }
        if (!isFree()) {
            throw new IllegalMoveException("Table " + tableId
                    + " is already taken by customer " + customer.getId());
        }
        this.customer = c;
    }

    /** Frees the table. Safe to call on a table that is already free. */
    public void clear() {
        this.customer = null;
    }

    public int getTableId() {
        return tableId;
    }

    /** The seated customer, or null if the table is free. */
    public Customer getCustomer() {
        return customer;
    }

    @Override
    public String toString() {
        return "Table " + tableId + (isFree() ? " (free)" : " (customer " + customer.getId() + ")");
    }
}
