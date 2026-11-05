import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.Scanner;
import java.util.Set;
import java.util.StringJoiner;

/**
 * «boundary» All console input and output. Holds no game rules: it turns
 * what the manager types into a WaiterChoice / ChefChoice and prints the
 * game state. Whether a move is legal is decided by Staff, Customer, Table
 * and Order, which throw IllegalMoveException; Game catches it, calls
 * displayError(reason) and asks again.
 *
 * The CLI re-prompts on its own only when input cannot form a choice at all
 * (not a number, unknown table or customer id, customer has no order yet).
 *
 * Uses these Game getters: getTurn, getMaxTurns, getRevenueCents,
 * getWaitingQueue, getTables, getCustomers, getOrders, getFreeTables,
 * getCustomersToOrder, getOrdersToServe, getOrdersToCook, getPaidCount,
 * getServedCount, getUnhappyDepartures, averageSatisfaction, isVictory.
 *
 * Stage 2: customers are shown with their type (c2 VIP, c3 Critic) and
 * READY dishes show the turn they became ready, since that is what the
 * Critic judges. The type label is display only; no rule depends on it.
 *
 * Stage 3: readHostChoice (the Host acts before the Waiter each turn) and
 * askActivateHappyHour (Game only asks while Happy Hour is unused). The
 * Happy Hour status and recovery lines read Game.getHappyHour(), so they
 * live in the Game PR.
 *
 * Layout: each turn screen is split into sections with a summary of what
 * each staff member can do, and the first VIP and the first Critic are
 * explained when they arrive. The explanations are display text only; the
 * rules they describe live in the customer classes and Reputation.
 */
public class RestaurantCLI {

    private static final String RULE = "=".repeat(64);
    private static final String THIN = "-".repeat(64);

    // shown once, the first time a customer of that type arrives
    private static final Map<String, List<String>> INTRODUCTIONS = Map.of(
            "VIP", List.of(
                    "ALERT: A VIP has arrived!",
                    "VIPs are patient: while waiting for a table or food they lose only"
                            + "\n  5 satisfaction per turn, instead of 8 for a Regular.",
                    "VIPs pay 10% less than the menu price, so they bring in a little less money.",
                    "Like any customer, a VIP leaves without paying if satisfaction reaches 0."),
            "Critic", List.of(
                    "WARNING: A food critic has arrived!",
                    "The critic always orders the most expensive dish on the menu, and loses"
                            + "\n  12 satisfaction per turn while waiting for a table or food.",
                    "The critic judges how quickly the dish reaches the table once it is READY:"
                            + "\n  served the next turn = GOOD, after 2 turns = NEUTRAL, 3 or more = BAD.",
                    "A GOOD review gives the restaurant a GOOD reputation: from then on every"
                            + "\n  waiting customer loses 2 less satisfaction per turn.",
                    "A BAD review gives a BAD reputation: every waiting customer loses 2 more"
                            + "\n  per turn. A NEUTRAL review leaves the reputation unchanged.",
                    "Tip: serve the critic's dish on the turn after it becomes READY."));

    private final Scanner in;
    // display preference only: the manager can hide or show the Happy Hour explanation
    private boolean explainHappyHour = true;
    // display memory only: who has been announced, which types have been explained
    private final Set<Integer> announced = new HashSet<>();
    private final Set<String> introduced = new HashSet<>();

    public RestaurantCLI() {
        this(new Scanner(System.in));
    }

    /** Lets tests feed scripted input. */
    public RestaurantCLI(Scanner in) {
        this.in = in;
    }

    // ------------------------------------------------------------------ input

    /** The Host can only seat one waiting customer, or wait. Asked before the Waiter. */
    public HostChoice readHostChoice(Game g) {
        while (true) {
            System.out.println();
            System.out.println("  HOST - choose one task");
            System.out.println("    1) Seat a waiting customer");
            System.out.println("    0) Wait");
            Integer pick = readInt("  > ");
            if (pick == null) {
                return HostChoice.waitTurn(); // input ended
            }
            switch (pick) {
                case 1 -> {
                    System.out.println("    Waiting: " + customerIds(g.getWaitingQueue())
                            + "   |   Free tables: " + tableIds(g.getFreeTables()));
                    Customer c = askCustomer(g, "    Customer id (blank to go back): ");
                    if (c == null) {
                        continue;
                    }
                    Table t = askTable(g, "    Table id (blank to go back): ");
                    if (t == null) {
                        continue;
                    }
                    return HostChoice.seat(c, t);
                }
                case 0 -> {
                    return HostChoice.waitTurn();
                }
                default -> System.out.println("    Please enter 0 or 1.");
            }
        }
    }

    public WaiterChoice readWaiterChoice(Game g) {
        while (true) {
            System.out.println();
            System.out.println("  WAITER - choose one task");
            System.out.println("    1) Seat a waiting customer");
            System.out.println("    2) Take an order");
            System.out.println("    3) Serve a ready order");
            System.out.println("    0) Wait");
            Integer pick = readInt("  > ");
            if (pick == null) {
                return WaiterChoice.waitTurn(); // input ended
            }
            switch (pick) {
                case 1 -> {
                    System.out.println("    Waiting: " + customerIds(g.getWaitingQueue())
                            + "   |   Free tables: " + tableIds(g.getFreeTables()));
                    Customer c = askCustomer(g, "    Customer id (blank to go back): ");
                    if (c == null) {
                        continue;
                    }
                    Table t = askTable(g, "    Table id (blank to go back): ");
                    if (t == null) {
                        continue;
                    }
                    return WaiterChoice.seat(c, t);
                }
                case 2 -> {
                    System.out.println("    Ready to order: " + customerIds(g.getCustomersToOrder()));
                    Customer c = askCustomer(g, "    Customer id (blank to go back): ");
                    if (c == null) {
                        continue;
                    }
                    return WaiterChoice.takeOrder(c);
                }
                case 3 -> {
                    System.out.println("    Ready to serve: " + orderList(g.getOrdersToServe()));
                    Customer c = askCustomer(g, "    Customer id (blank to go back): ");
                    if (c == null) {
                        continue;
                    }
                    return WaiterChoice.serve(c);
                }
                case 0 -> {
                    return WaiterChoice.waitTurn();
                }
                default -> System.out.println("    Please enter 0, 1, 2 or 3.");
            }
        }
    }

    public ChefChoice readChefChoice(Game g) {
        while (true) {
            System.out.println();
            System.out.println("  CHEF - choose one task");
            System.out.println("    1) Cook one unit of an order");
            System.out.println("    0) Wait");
            Integer pick = readInt("  > ");
            if (pick == null) {
                return ChefChoice.waitTurn(); // input ended
            }
            switch (pick) {
                case 1 -> {
                    System.out.println("    To cook: " + orderList(g.getOrdersToCook()));
                    Customer c = askCustomer(g, "    Customer id (blank to go back): ");
                    if (c == null) {
                        continue;
                    }
                    // ChefChoice(null) means "wait" to Game, so catch this here.
                    if (c.getOrder() == null) {
                        System.out.println("    Customer " + c.getId() + " has not ordered yet.");
                        continue;
                    }
                    return ChefChoice.cook(c.getOrder());
                }
                case 0 -> {
                    return ChefChoice.waitTurn();
                }
                default -> System.out.println("    Please enter 0 or 1.");
            }
        }
    }

    /**
     * Asks whether to start Happy Hour this turn. Game only calls this while
     * Happy Hour is unused; whether it really can start is HappyHour's rule.
     * Returns false if input has ended.
     */
    public boolean askActivateHappyHour(Game g) {
        System.out.println();
        if (explainHappyHour) {
            printHappyHourExplanation();
        }
        while (true) {
            String toggle = explainHappyHour ? "h = hide this explanation" : "h = show the explanation";
            System.out.print("  Start Happy Hour on turn " + g.getTurn() + "? (y/n, " + toggle + ") > ");
            if (!in.hasNextLine()) {
                return false;
            }
            String answer = in.nextLine().trim().toLowerCase();
            switch (answer) {
                case "y", "yes" -> {
                    return true;
                }
                case "n", "no" -> {
                    return false;
                }
                case "h" -> {
                    explainHappyHour = !explainHappyHour;
                    if (explainHappyHour) {
                        printHappyHourExplanation();
                    } else {
                        System.out.println("  Explanation hidden. Enter h at this prompt to show it again.");
                    }
                }
                default -> System.out.println("  Please enter y, n or h.");
            }
        }
    }

    private static void printHappyHourExplanation() {
        System.out.println("  HAPPY HOUR is available (once per game). If you start it now it lasts");
        System.out.println("  this turn and the next: orders taken get 20% off, and customers still");
        System.out.println("  waiting for a table or food recover 15 satisfaction at each turn start.");
    }

    /** Called by Game when a staff action throws IllegalMoveException. */
    public void displayError(String reason) {
        System.out.println();
        System.out.println("  ! Not allowed: " + reason);
        System.out.println("    Choose again.");
    }

    // ----------------------------------------------------------------- output

    public void displayTurnStart(Game g) {
        System.out.println();
        System.out.println(RULE);
        System.out.println("  TURN " + g.getTurn() + " of " + g.getMaxTurns());
        System.out.println(RULE);
        announceArrivals(g);

        System.out.println();
        System.out.println("  Revenue " + money(g.getRevenueCents()) + "   |   Paid " + g.getPaidCount()
                + "   |   Avg satisfaction " + average(g));
        System.out.println("  Reputation " + g.getReputation() + "   |   Happy Hour " + happyHourStatus(g));

        System.out.println();
        System.out.println("  WAITING");
        List<Customer> waiting = g.getWaitingQueue();
        if (waiting.isEmpty()) {
            System.out.println("    nobody");
        }
        for (Customer c : waiting) {
            System.out.printf("    %-13s satisfaction %d%n", label(c), c.getSatisfaction());
        }

        System.out.println();
        System.out.println("  TABLES");
        for (Table t : g.getTables()) {
            Customer c = t.getCustomer();
            if (c == null) {
                System.out.println("    Table " + t.getTableId() + "   free");
            } else {
                String dish = c.getOrder() == null ? "-" : c.getOrder().getItem().getName();
                System.out.printf("    Table %d   %-13s %-13s %-21s satisfaction %d%n",
                        t.getTableId(), label(c), c.getStatus(), dish, c.getSatisfaction());
            }
        }

        System.out.println();
        System.out.println("  KITCHEN");
        boolean any = false;
        for (Order o : g.getOrders()) {
            if (o.getStatus() == OrderStatus.TAKEN) {
                System.out.printf("    %-13s %-21s cooking %d/%d%n", label(o.getCustomer()),
                        o.getItem().getName(), o.getUnitsDone(), o.getItem().getPrepUnits());
                any = true;
            } else if (o.getStatus() == OrderStatus.READY) {
                String since = o.getReadyTurn() > 0 ? " (ready since turn " + o.getReadyTurn() + ")" : "";
                System.out.printf("    %-13s %-21s READY to serve%s%n", label(o.getCustomer()),
                        o.getItem().getName(), since);
                any = true;
            }
        }
        if (!any) {
            System.out.println("    no open orders");
        }

        System.out.println();
        System.out.println("  WHAT YOUR STAFF CAN DO");
        System.out.println("    Host     " + hostTasks(g));
        System.out.println("    Waiter   " + waiterTasks(g));
        System.out.println("    Chef     " + chefTasks(g));
        System.out.println(THIN);
    }

    public void displayTurnEnd(Game g) {
        System.out.println();
        System.out.println(THIN);
        System.out.println("  END OF TURN " + g.getTurn() + " of " + g.getMaxTurns());
        System.out.println("  Revenue " + money(g.getRevenueCents()) + "   |   Served " + g.getServedCount()
                + "   |   Paid " + g.getPaidCount() + "   |   Left unhappy " + g.getUnhappyDepartures());
        System.out.println("  Avg satisfaction " + average(g) + "   |   Reputation " + g.getReputation()
                + "   |   Turns used " + g.getTurn() + " of " + g.getMaxTurns());
        System.out.println(THIN);
    }

    /** Game calls it right after Happy Hour starts. */
    public void displayHappyHour(Game g) {
        System.out.println("  Happy Hour: " + happyHourStatus(g));
    }

    /** Called by Game for each customer who recovers during Happy Hour. */
    public void displayRecovery(Customer c, int newValue) {
        System.out.println("  Happy Hour: " + label(c) + " recovers to satisfaction " + newValue);
    }

    private static String happyHourStatus(Game g) {
        HappyHour hh = g.getHappyHour();
        int left = hh.remainingTurns(g.getTurn());
        if (left > 0) {
            return "active, " + left + " turn(s) left";
        }
        return hh.isUsed() ? "used" : "available";
    }

    public void displayResult(Game g) {
        System.out.println();
        System.out.println(RULE);
        System.out.println("  GAME OVER");
        System.out.println(RULE);
        System.out.println("  Revenue:            " + money(g.getRevenueCents()));
        System.out.println("  Customers served:   " + g.getServedCount());
        System.out.println("  Customers paid:     " + g.getPaidCount());
        System.out.println("  Customers who left: " + g.getUnhappyDepartures());
        System.out.println("  Avg satisfaction:   " + average(g));
        System.out.println();
        System.out.println(g.isVictory() ? "  RESULT: VICTORY!" : "  RESULT: DEFEAT");
        System.out.println(RULE);
    }

    // ------------------------------------------------------- arrivals and help

    /** Announces each newcomer once; the first VIP and the first Critic are also explained. */
    private void announceArrivals(Game g) {
        for (Customer c : g.getCustomers()) {
            if (announced.add(c.getId())) {
                System.out.println();
                System.out.println("  New arrival: " + label(c) + " joins the waiting queue.");
                List<String> intro = INTRODUCTIONS.get(typeName(c));
                if (intro != null && introduced.add(typeName(c))) {
                    explain(intro);
                }
            }
        }
    }

    private void explain(List<String> lines) {
        for (String line : lines) {
            System.out.println();
            System.out.println("  " + line);
            pause();
        }
    }

    /** Waits for Enter so the player can read an explanation. Scripted runs override it. */
    protected void pause() {
        System.out.print("    < press Enter to continue >");
        if (in.hasNextLine()) {
            in.nextLine();
        }
    }

    // ---------------------------------------------------------------- helpers

    /** Reads one whole number; re-prompts on bad input. Returns null if input has ended. */
    private Integer readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            if (!in.hasNextLine()) {
                return null;
            }
            String line = in.nextLine().trim();
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("    Please enter a whole number.");
            }
        }
    }

    /** Like readInt, but a blank line means "go back" and returns null. Accepts "c3" or "3". */
    private Integer readIdOrBlank(String prompt) {
        while (true) {
            System.out.print(prompt);
            if (!in.hasNextLine()) {
                return null;
            }
            String line = in.nextLine().trim();
            if (line.isEmpty()) {
                return null;
            }
            if (line.startsWith("c") || line.startsWith("C") || line.startsWith("t") || line.startsWith("T")) {
                line = line.substring(1);
            }
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("    Please enter a number, or leave blank to go back.");
            }
        }
    }

    private Table askTable(Game g, String prompt) {
        while (true) {
            Integer id = readIdOrBlank(prompt);
            if (id == null) {
                return null;
            }
            for (Table t : g.getTables()) {
                if (t.getTableId() == id) {
                    return t;
                }
            }
            System.out.println("    There is no table " + id + ".");
        }
    }

    private Customer askCustomer(Game g, String prompt) {
        while (true) {
            Integer id = readIdOrBlank(prompt);
            if (id == null) {
                return null;
            }
            for (Customer c : g.getCustomers()) {
                if (c.getId() == id) {
                    return c;
                }
            }
            System.out.println("    There is no customer " + id + ".");
        }
    }

    // the lists come from Game, so the CLI only words them
    private static String hostTasks(Game g) {
        if (g.getWaitingQueue().isEmpty() || g.getFreeTables().isEmpty()) {
            return "nothing to do (nobody waiting or no free table)";
        }
        return "seat " + customerIds(g.getWaitingQueue()) + " (free tables: " + tableIds(g.getFreeTables()) + ")";
    }

    private static String waiterTasks(Game g) {
        List<String> tasks = new ArrayList<>();
        if (!g.getWaitingQueue().isEmpty() && !g.getFreeTables().isEmpty()) {
            tasks.add("seat (like the Host)");
        }
        if (!g.getCustomersToOrder().isEmpty()) {
            tasks.add("take order from " + customerIds(g.getCustomersToOrder()));
        }
        if (!g.getOrdersToServe().isEmpty()) {
            tasks.add("serve " + orderList(g.getOrdersToServe()));
        }
        return tasks.isEmpty() ? "nothing to do" : String.join("; ", tasks);
    }

    private static String chefTasks(Game g) {
        return g.getOrdersToCook().isEmpty() ? "nothing to cook" : "cook " + orderList(g.getOrdersToCook());
    }

    private static String average(Game g) {
        OptionalDouble avg = g.averageSatisfaction();
        return avg.isPresent() ? String.format("%.1f", avg.getAsDouble()) : "N/A";
    }

    private static String customerIds(List<Customer> list) {
        StringJoiner sj = new StringJoiner(", ");
        for (Customer c : list) {
            sj.add(label(c));
        }
        return sj.length() == 0 ? "none" : sj.toString();
    }

    /**
     * "c2 VIP", "c3 Critic", "c1 Regular". Display only: the type comes from
     * the subclass name (VIPCustomer -> VIP), so a new customer type shows up
     * here with no change to the CLI. If Customer has no subtype yet, just "c1".
     */
    private static String label(Customer c) {
        String type = typeName(c);
        return type.isEmpty() ? "c" + c.getId() : "c" + c.getId() + " " + type;
    }

    private static String typeName(Customer c) {
        String type = c.getClass().getSimpleName();
        return type.endsWith("Customer") ? type.substring(0, type.length() - "Customer".length()) : type;
    }

    private static String tableIds(List<Table> list) {
        StringJoiner sj = new StringJoiner(", ");
        for (Table t : list) {
            sj.add(String.valueOf(t.getTableId()));
        }
        return sj.length() == 0 ? "none" : sj.toString();
    }

    private static String orderList(List<Order> list) {
        StringJoiner sj = new StringJoiner(", ");
        for (Order o : list) {
            sj.add(label(o.getCustomer()) + " " + o.getItem().getName());
        }
        return sj.length() == 0 ? "none" : sj.toString();
    }

    private static String money(int cents) {
        return String.format("$%d.%02d", cents / 100, cents % 100);
    }
}
