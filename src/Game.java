import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;
import java.util.Queue;

// Turn controller, sequences the turns and keeps score only, no output.
public class Game {
    private final RestaurantCLI cli;

    private int turn = 0;
    private final int maxTurns = 18;
    private final int[] arrivalTurns = {1, 4, 7, 10, 13, 16};

    private final List<Table> tables = new ArrayList<>();
    private final Queue<Customer> waitingQueue = new ArrayDeque<>();
    private final List<Customer> customers = new ArrayList<>();
    private final List<Order> orders = new ArrayList<>();
    private Menu menu;

    private final Waiter waiter = new Waiter();
    private final Chef chef = new Chef();
    private final Cashier cashier = new Cashier();
    private final List<Staff> staff = List.of(waiter, chef, cashier);

    private int revenueCents = 0;
    private final List<Integer> paidSatisfactions = new ArrayList<>();

    public Game(RestaurantCLI cli) {
        this.cli = cli;
        // tables, menu
    }

    public void runGame() {
    }

    public void runTurn() {
    }

    private void processArrival() {
    }

    private void applyManagerChoices() {
    }

    private void processEatingAndPayment() {
    }

    private Customer nextCustomerToPay() {
        return null;
    }

    private void processWaitingAndAbandonment() {
    }

    public boolean isOver() {
        return false;
    }

    public boolean isVictory() {
        return false;
    }

    public OptionalDouble averageSatisfaction() {
        return OptionalDouble.empty();
    }

    public int getRevenueCents() { return revenueCents; }
    public int getTurn() { return turn; }
    public int getMaxTurns() { return maxTurns; }
    public List<Customer> getWaitingQueue() { return null; }
    public List<Table> getTables() { return null; }
    public List<Customer> getCustomers() { return null; }
    public List<Order> getOrders() { return null; }
    public Menu getMenu() { return menu; }
    public int getServedCount() { return 0; }
    public int getUnhappyDepartures() { return 0; }
}
