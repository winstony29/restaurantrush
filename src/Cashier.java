//Cashier subclass
public class Cashier extends Staff {

    public int collect(Customer c) {

        // Check whether Cashier has already acted
        requireAvailable();

        // Check whether customer exists
        if (c == null) {
            throw new IllegalMoveException("No customer selected");
        }

        // Check whether customer is ready to pay
        if (c.getStatus() != CustomerStatus.READY_TO_PAY) {
            throw new IllegalMoveException("Customer is not ready to pay");
        }

        // Retrieve customer's order
        Order order = c.getOrder();

        // Check whether order exists
        if (order == null) {
            throw new IllegalMoveException("Customer has no order");
        }

        // Check whether order has been served
        if (order.getStatus() != OrderStatus.SERVED) {
            throw new IllegalMoveException("Order has not been served");
        }

        // STAGE 2 CHANGE: Customer-specific discount
        int bill = c.applyDiscount(order.basePriceCents());

        // Save the table reference before updating customer status
        Table table = c.getTable();

        // Update order and customer statuses
        order.markPaid();
        c.markPaid();

        // Free the table after successful payment (changed after pull request comment)
        if (table != null) {
            table.clear();
        }

        // Mark Cashier as having acted
        markActed();

        // Return payment amount
        return bill;
    }
}
