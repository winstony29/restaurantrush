//Waiter subclass
public class Waiter extends Staff {

    public void seat(Customer c, Table t) {

        requireAvailable();

        if (c == null || t == null) {
            throw new IllegalMoveException("Invalid customer or table");
        }

        if (c.getStatus() != CustomerStatus.WAITING) {
            throw new IllegalMoveException("Customer is not waiting");
        }

        if (!t.isFree()) {
            throw new IllegalMoveException("Table is occupied");
        }
        
        //Swapped the order after pull request comment
        c.seatAt(t);
        t.seat(c);
        

        markActed();
    }

    public Order takeOrder(Customer c, Menu m) {

        requireAvailable();

        if (c == null || m == null) {
            throw new IllegalMoveException("Invalid customer or menu");
        }

        if (c.getStatus() != CustomerStatus.SEATED) {
            throw new IllegalMoveException("Customer is not seated");
        }
        if (c.getOrder() != null) {
            throw new IllegalMoveException("Customer already has an order");
        }

        MenuItem item = c.chooseItem(m);

        Order order = new Order(item, c);

        c.placeOrder(order);

        markActed();

        return order;
    }

    public void serve(Order o, int turn) {

        requireAvailable();

        if (o == null) {
            throw new IllegalMoveException("No order selected");
        }

        if (o.getStatus() != OrderStatus.READY) {
            throw new IllegalMoveException("Order is not ready");
        }

        Customer c = o.getCustomer();

        if (c.getStatus() != CustomerStatus.ORDERED) {
            throw new IllegalMoveException("Customer cannot be served");
        }

        o.markServed();
        c.markServed(turn);

        markActed();
    }
}