/**
 * The Host task the manager picked this turn. Returned by
 * RestaurantCLI.readHostChoice() and carried out by Game (host.seat(...)).
 * Mirrors WaiterChoice. A null customer means the Host waits.
 */
public record HostChoice(Customer customer, Table table) {

    public static HostChoice seat(Customer c, Table t) {
        return new HostChoice(c, t);
    }

    public static HostChoice waitTurn() {
        return new HostChoice(null, null);
    }
}