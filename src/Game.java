import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;
import java.util.Queue;

/** Turn controller: sequences each turn and keeps the score. Holds no printing or parsing. */
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
        // step 2: tables, menu
    }

    public void runGame() {
        // step 7
    }

    public void runTurn() {
        // step 7
    }

    // step 2
    private void processArrival() {
    }

    // step 3
    private void applyManagerChoices() {
    }

    // step 4
    private void processEatingAndPayment() {
    }

    // step 4
    private Customer nextCustomerToPay() {
        return null;
    }

    // step 5
    private void processWaitingAndAbandonment() {
    }

    // step 6
    public boolean isOver() {
        return false;
    }

    // step 6
    public boolean isVictory() {
        return false;
    }

    // step 6
    public OptionalDouble averageSatisfaction() {
        return OptionalDouble.empty();
    }

    // step 6: read-only getters for the CLI
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
