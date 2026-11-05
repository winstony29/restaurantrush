//Shared seating rules for Host and Waiter
public abstract class Seater extends Staff {

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
}
