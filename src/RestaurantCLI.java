import java.util.List;
import java.util.OptionalDouble;
import java.util.Scanner;
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
 */
public class RestaurantCLI {
 
    private final Scanner in;
 
    public RestaurantCLI() {
        this(new Scanner(System.in));
    }
 
    /** Lets tests feed scripted input. */
    public RestaurantCLI(Scanner in) {
        this.in = in;
    }
 
    // ------------------------------------------------------------------ input
 
    public WaiterChoice readWaiterChoice(Game g) {
        while (true) {
            System.out.println();
            System.out.println("WAITER - choose one task:");
            System.out.println("  1) Seat a waiting customer");
            System.out.println("  2) Take an order");
            System.out.println("  3) Serve a ready order");
            System.out.println("  0) Wait");
            Integer pick = readInt("> ");
            if (pick == null) {
                return WaiterChoice.waitTurn(); // input ended
            }
            switch (pick) {
                case 1 -> {
                    System.out.println("Waiting: " + customerIds(g.getWaitingQueue())
                            + "   Free tables: " + tableIds(g.getFreeTables()));
                    Customer c = askCustomer(g, "Customer id (blank to go back): ");
                    if (c == null) {
                        continue;
                    }
                    Table t = askTable(g, "Table id (blank to go back): ");
                    if (t == null) {
                        continue;
                    }
                    return WaiterChoice.seat(c, t);
                }
                case 2 -> {
                    System.out.println("Ready to order: " + customerIds(g.getCustomersToOrder()));
                    Customer c = askCustomer(g, "Customer id (blank to go back): ");
                    if (c == null) {
                        continue;
                    }
                    return WaiterChoice.takeOrder(c);
                }
                case 3 -> {
                    System.out.println("Ready to serve: " + orderList(g.getOrdersToServe()));
                    Customer c = askCustomer(g, "Customer id (blank to go back): ");
                    if (c == null) {
                        continue;
                    }
                    return WaiterChoice.serve(c);
                }
                case 0 -> {
                    return WaiterChoice.waitTurn();
                }
                default -> System.out.println("Please enter 0, 1, 2 or 3.");
            }
        }
    }
 
    public ChefChoice readChefChoice(Game g) {
        while (true) {
            System.out.println();
            System.out.println("CHEF - choose one task:");
            System.out.println("  1) Cook one unit of an order");
            System.out.println("  0) Wait");
            Integer pick = readInt("> ");
            if (pick == null) {
                return ChefChoice.waitTurn(); // input ended
            }
            switch (pick) {
                case 1 -> {
                    System.out.println("To cook: " + orderList(g.getOrdersToCook()));
                    Customer c = askCustomer(g, "Customer id (blank to go back): ");
                    if (c == null) {
                        continue;
                    }
                    // ChefChoice(null) means "wait" to Game, so catch this here.
                    if (c.getOrder() == null) {
                        System.out.println("Customer " + c.getId() + " has not ordered yet.");
                        continue;
                    }
                    return ChefChoice.cook(c.getOrder());
                }
                case 0 -> {
                    return ChefChoice.waitTurn();
                }
                default -> System.out.println("Please enter 0 or 1.");
            }
        }
    }
 
    /** Called by Game when a staff action throws IllegalMoveException. */
    public void displayError(String reason) {
        System.out.println("Not allowed: " + reason);
        System.out.println("Choose again.");
    }
 
    // ----------------------------------------------------------------- output
 
    public void displayTurnStart(Game g) {
        System.out.println();
        System.out.println("=============== Turn " + g.getTurn() + " / " + g.getMaxTurns()
                + " ===============");
        System.out.println("Revenue: " + money(g.getRevenueCents()));
 
        List<Customer> waiting = g.getWaitingQueue();
        if (waiting.isEmpty()) {
            System.out.println("Waiting: nobody");
        } else {
            StringJoiner sj = new StringJoiner(", ");
            for (Customer c : waiting) {
                sj.add(label(c) + " (satisfaction " + c.getSatisfaction() + ")");
            }
            System.out.println("Waiting: " + sj);
        }
 
        System.out.println("Tables:");
        for (Table t : g.getTables()) {
            Customer c = t.getCustomer();
            if (c == null) {
                System.out.println("  Table " + t.getTableId() + ": free");
            } else {
                String dish = c.getOrder() == null ? "" : ", " + c.getOrder().getItem().getName();
                System.out.println("  Table " + t.getTableId() + ": " + label(c) + " "
                        + c.getStatus() + dish + " (satisfaction " + c.getSatisfaction() + ")");
            }
        }
 
        System.out.println("Kitchen:");
        boolean any = false;
        for (Order o : g.getOrders()) {
            if (o.getStatus() == OrderStatus.TAKEN) {
                System.out.println("  " + label(o.getCustomer()) + " " + o.getItem().getName()
                        + " - cooking " + o.getUnitsDone() + "/" + o.getItem().getPrepUnits());
                any = true;
            } else if (o.getStatus() == OrderStatus.READY) {
                String since = o.getReadyTurn() > 0 ? " (ready since turn " + o.getReadyTurn() + ")" : "";
                System.out.println("  " + label(o.getCustomer()) + " " + o.getItem().getName()
                        + " - READY to serve" + since);
                any = true;
            }
        }
        if (!any) {
            System.out.println("  (no open orders)");
        }
    }
 
    public void displayTurnEnd(Game g) {
        System.out.println("--- End of turn " + g.getTurn() + ": revenue " + money(g.getRevenueCents())
                + ", paid " + g.getPaidCount() + ", left " + g.getUnhappyDepartures() + " ---");
    }
 
    public void displayResult(Game g) {
        System.out.println();
        System.out.println("================ GAME OVER ================");
        System.out.println("Revenue:            " + money(g.getRevenueCents()));
        System.out.println("Customers served:   " + g.getServedCount());
        System.out.println("Customers paid:     " + g.getPaidCount());
        System.out.println("Customers who left: " + g.getUnhappyDepartures());
        OptionalDouble avg = g.averageSatisfaction();
        System.out.println("Avg satisfaction:   "
                + (avg.isPresent() ? String.format("%.1f", avg.getAsDouble()) : "n/a (nobody paid)"));
        System.out.println(g.isVictory() ? "RESULT: VICTORY!" : "RESULT: DEFEAT");
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
                System.out.println("Please enter a whole number.");
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
                System.out.println("Please enter a number, or leave blank to go back.");
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
            System.out.println("There is no table " + id + ".");
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
            System.out.println("There is no customer " + id + ".");
        }
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
        String type = c.getClass().getSimpleName();
        if (type.endsWith("Customer")) {
            type = type.substring(0, type.length() - "Customer".length());
        }
        return type.isEmpty() ? "c" + c.getId() : "c" + c.getId() + " " + type;
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