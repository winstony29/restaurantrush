import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
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
    private final Menu menu;

    private final Waiter waiter = new Waiter();
    private final Chef chef = new Chef();
    private final Cashier cashier = new Cashier();
    private final List<Staff> staff = List.of(waiter, chef, cashier);

    private int revenueCents = 0;
    private final List<Integer> paidSatisfactions = new ArrayList<>();

    public Game(RestaurantCLI cli) {
        this.cli = cli;
        // tables, menu
        tables.add(new Table(1));
        tables.add(new Table(2));
        // order matters, customers pick by index
        menu = new Menu(List.of(
                new MenuItem("Salad", 900, 1),
                new MenuItem("Burger", 1500, 1),
                new MenuItem("Pasta", 1800, 2)));
    }

    public void runGame() {
    }

    public void runTurn() {
    }

    // ids 1..6 in arrival order
    private void processArrival() {
        for (int t : arrivalTurns) {
            if (t == turn) {
                Customer c = new Customer(customers.size() + 1);
                customers.add(c);
                waitingQueue.add(c);
            }
        }
    }

    // waiter before chef, a rejected choice is asked again without using up the task
    private void applyManagerChoices() {
        while (true) {
            try {
                applyWaiterChoice(cli.readWaiterChoice(this));
                break;
            } catch (IllegalMoveException e) {
                cli.displayError(e.getMessage());
            }
        }
        while (true) {
            try {
                applyChefChoice(cli.readChefChoice(this));
                break;
            } catch (IllegalMoveException e) {
                cli.displayError(e.getMessage());
            }
        }
    }

    private void applyWaiterChoice(WaiterChoice choice) {
        Customer c = choice.customer();
        switch (choice.kind()) {
            case SEAT -> {
                waiter.seat(c, choice.table());
                waitingQueue.remove(c);
            }
            case TAKE_ORDER -> orders.add(waiter.takeOrder(c, menu));
            case SERVE -> waiter.serve(c == null ? null : c.getOrder(), turn);
            case WAIT -> { }
        }
    }

    // null order means wait
    private void applyChefChoice(ChefChoice choice) {
        if (choice.order() != null) {
            chef.cook(choice.order());
        }
    }

    // served on an earlier turn finishes eating, then the cashier takes one payment
    private void processEatingAndPayment() {
        for (Customer c : customers) {
            if (c.getStatus() == CustomerStatus.SERVED && c.getServedTurn() < turn) {
                c.markReadyToPay(turn);
            }
        }
        Customer payer = nextCustomerToPay();
        if (payer != null) {
            revenueCents += cashier.collect(payer);
            paidSatisfactions.add(payer.getSatisfaction());
        }
    }

    // oldest ready to pay before this turn, tie-break by id
    private Customer nextCustomerToPay() {
        return customers.stream()
                .filter(c -> c.getStatus() == CustomerStatus.READY_TO_PAY && c.getReadyToPayTurn() < turn)
                .min(Comparator.comparingInt(Customer::getReadyToPayTurn).thenComparingInt(Customer::getId))
                .orElse(null);
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
