import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.IntFunction;
import java.util.List;
import java.util.OptionalDouble;
import java.util.Queue;

// Turn controller, sequences the turns and keeps score only, no output.
public class Game {
    private final RestaurantCLI cli;

    private int turn = 0;
    private final int maxTurns = 18;
    // who arrives when: the base run replaces two Regulars with a VIP (turn 4) and a Critic (turn 7)
    private final Map<Integer, IntFunction<Customer>> arrivals = new LinkedHashMap<>();
    private Reputation reputation = Reputation.NEUTRAL;
    private final HappyHour happyHour = new HappyHour();

    private final List<Table> tables = new ArrayList<>();
    private final Queue<Customer> waitingQueue = new ArrayDeque<>();
    private final List<Customer> customers = new ArrayList<>();
    private final List<Order> orders = new ArrayList<>();
    private final Menu menu;

    private final Host host = new Host();
    private final Waiter waiter = new Waiter();
    private final Chef chef = new Chef();
    private final Cashier cashier = new Cashier();
    private final List<Staff> staff = List.of(host, waiter, chef, cashier);

    private int revenueCents = 0;
    private final List<Integer> paidSatisfactions = new ArrayList<>();

    public Game(RestaurantCLI cli) {
        this.cli = cli;
        arrivals.put(1, RegularCustomer::new);
        arrivals.put(4, VIPCustomer::new);
        arrivals.put(7, CriticCustomer::new);
        arrivals.put(10, RegularCustomer::new);
        arrivals.put(13, RegularCustomer::new);
        arrivals.put(16, RegularCustomer::new);
        // tables, menu
        tables.add(new Table(1));
        tables.add(new Table(2));
        // order matters, customers pick by index; combos go after the three base items
        MenuItem salad = new MenuItem("Salad", 900, 1);
        MenuItem burger = new MenuItem("Burger", 1500, 1);
        MenuItem pasta = new MenuItem("Pasta", 1800, 2);
        menu = new Menu(List.of(salad, burger, pasta,
                new ComboMeal(salad, pasta),
                new ComboMeal(burger, pasta)));
    }

    // always all 18 turns, no early end
    public void runGame() {
        while (!isOver()) {
            runTurn();
        }
        cli.displayResult(this);
    }

    // refuse to run past the last turn, same shape as the staff guards
    // display after arrival so the manager sees who just came in
    public void runTurn() {
        if (isOver()) {
            throw new IllegalStateException("Game is over after turn " + maxTurns);
        }
        turn++;
        staff.forEach(Staff::resetTurn);
        processArrival();
        cli.displayTurnStart(this);
        offerHappyHour();
        applyHappyHourRecovery();
        applyManagerChoices();
        processEatingAndPayment();
        processWaitingAndAbandonment();
        cli.displayTurnEnd(this);
    }

    // ids 1..6 in arrival order
    private void processArrival() {
        IntFunction<Customer> type = arrivals.get(turn);
        if (type != null) {
            Customer c = type.apply(customers.size() + 1);
            customers.add(c);
            waitingQueue.add(c);
        }
    }

    // once per game, so only ask while it is unused
    private void offerHappyHour() {
        if (!happyHour.isUsed() && cli.askActivateHappyHour(this)) {
            happyHour.activate(turn);
        }
    }

    // on each active turn, anyone still waiting for a table or food recovers 15
    private void applyHappyHourRecovery() {
        if (happyHour.isActive(turn)) {
            for (Customer c : customers) {
                if (isWaitingForService(c)) {
                    c.recoverSatisfaction(15);
                    cli.displayRecovery(c, c.getSatisfaction());
                }
            }
        }
    }

    // host, then waiter, then chef
    private void applyManagerChoices() {
        untilLegal(() -> applyHostChoice(cli.readHostChoice(this)));
        untilLegal(() -> applyWaiterChoice(cli.readWaiterChoice(this)));
        untilLegal(() -> applyChefChoice(cli.readChefChoice(this)));
    }

    // a rejected choice is asked again without using up the task
    private void untilLegal(Runnable choice) {
        while (true) {
            try {
                choice.run();
                return;
            } catch (IllegalMoveException e) {
                cli.displayError(e.getMessage());
            }
        }
    }

    // null customer means wait
    private void applyHostChoice(HostChoice choice) {
        Customer c = choice.customer();
        if (c != null) {
            host.seat(c, choice.table());
            waitingQueue.remove(c);
        }
    }

    private void applyWaiterChoice(WaiterChoice choice) {
        Customer c = choice.customer();
        switch (choice.kind()) {
            case SEAT -> {
                waiter.seat(c, choice.table());
                waitingQueue.remove(c);
            }
            case TAKE_ORDER -> orders.add(waiter.takeOrder(c, menu, turn));
            case SERVE -> {
                waiter.serve(c == null ? null : c.getOrder(), turn);
                // the served customer may have an opinion; only a Critic ever does
                Rating r = c.rateService(c.getOrder(), turn);
                if (r != Rating.NONE) {
                    reputation = Reputation.valueOf(r.name());
                }
            }
            case WAIT -> { }
        }
    }

    // null order means wait
    private void applyChefChoice(ChefChoice choice) {
        if (choice.order() != null) {
            chef.cook(choice.order(), turn);
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
            revenueCents += cashier.collect(payer, happyHour);
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

    // anyone not yet served loses satisfaction, at 0 they leave without paying
    private void processWaitingAndAbandonment() {
        for (Customer c : customers) {
            if (isWaitingForService(c)) {
                c.loseSatisfactionForWaiting(reputation);
                if (c.hasGivenUp()) {
                    abandon(c);
                }
            }
        }
    }

    private boolean isWaitingForService(Customer c) {
        CustomerStatus s = c.getStatus();
        return s == CustomerStatus.WAITING || s == CustomerStatus.SEATED || s == CustomerStatus.ORDERED;
    }

    private void abandon(Customer c) {
        c.leave();
        waitingQueue.remove(c);
        if (c.getOrder() != null) {
            c.getOrder().cancel();
        }
        if (c.getTable() != null) {
            c.getTable().clear();
        }
    }

    public boolean isOver() {
        return turn >= maxTurns;
    }

    // at least 4 paid, $45.00 revenue, average satisfaction 60
    public boolean isVictory() {
        return paidSatisfactions.size() >= 4 && revenueCents >= 4500 && averageSatisfaction().orElse(0) >= 60;
    }

    // satisfaction recorded at payment, empty if nobody has paid
    public OptionalDouble averageSatisfaction() {
        return paidSatisfactions.stream().mapToInt(Integer::intValue).average();
    }

    public int getRevenueCents() { return revenueCents; }
    public Reputation getReputation() { return reputation; }
    public Host getHost() { return host; }
    public HappyHour getHappyHour() { return happyHour; }
    public int getTurn() { return turn; }
    public int getMaxTurns() { return maxTurns; }
    public List<Customer> getWaitingQueue() { return List.copyOf(waitingQueue); }
    public List<Table> getTables() { return List.copyOf(tables); }
    public List<Customer> getCustomers() { return List.copyOf(customers); }
    public List<Order> getOrders() { return List.copyOf(orders); }
    public Menu getMenu() { return menu; }
    public int getPaidCount() { return paidSatisfactions.size(); }

    // served means the dish reached the table, paid or not
    public int getServedCount() {
        return countCustomers(CustomerStatus.SERVED) + countCustomers(CustomerStatus.READY_TO_PAY)
                + countCustomers(CustomerStatus.PAID);
    }

    public int getUnhappyDepartures() { return countCustomers(CustomerStatus.LEFT); }

    private int countCustomers(CustomerStatus s) {
        return (int) customers.stream().filter(c -> c.getStatus() == s).count();
    }

    // available tasks for the display, the staff still do the real checks
    public List<Table> getFreeTables() {
        return tables.stream().filter(Table::isFree).toList();
    }

    public List<Customer> getCustomersToOrder() {
        return customers.stream().filter(c -> c.getStatus() == CustomerStatus.SEATED).toList();
    }

    public List<Order> getOrdersToServe() {
        return orders.stream().filter(o -> o.getStatus() == OrderStatus.READY).toList();
    }

    public List<Order> getOrdersToCook() {
        return orders.stream().filter(o -> o.getStatus() == OrderStatus.TAKEN).toList();
    }
}
