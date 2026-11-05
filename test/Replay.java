import java.util.*;

// plays a per-turn script through the real Game.runGame(), usage: java Replay test/victory.txt
// line format: T5: [hh |] [host <c> <t> | ]<waiter> | <chef>; two columns is the Stage 1/2 format
public class Replay {
    static class ScriptedCLI extends RestaurantCLI {
        // each turn is {hh or "", host, waiter, chef}
        final Map<Integer, String[]> script;
        // turn-end lines, recoveries and the result, for ReplayCheck
        final List<String> trace = new ArrayList<>();
        ScriptedCLI(Map<Integer, String[]> script) { this.script = script; }
        String[] line(Game g) { return script.getOrDefault(g.getTurn(), new String[] {"", "host wait", "wait", "wait"}); }
        // an id the script names must exist on that turn, never a silent wait
        Customer cust(Game g, String id) {
            return g.getCustomers().stream().filter(c -> ("c" + c.getId()).equals(id)).findFirst()
                    .orElseThrow(() -> new AssertionError("T" + g.getTurn() + ": no customer " + id + " yet"));
        }
        Table table(Game g, String id) {
            return g.getTables().stream().filter(t -> ("t" + t.getTableId()).equals(id)).findFirst()
                    .orElseThrow(() -> new AssertionError("T" + g.getTurn() + ": no table " + id));
        }
        public boolean askActivateHappyHour(Game g) { return line(g)[0].equals("hh"); }
        public HostChoice readHostChoice(Game g) {
            String[] h = line(g)[1].split(" ");
            return h[1].equals("wait") ? HostChoice.waitTurn() : HostChoice.seat(cust(g, h[1]), table(g, h[2]));
        }
        public WaiterChoice readWaiterChoice(Game g) {
            String[] w = line(g)[2].split(" ");
            return switch (w[0]) {
                case "seat" -> new WaiterChoice(WaiterChoice.Kind.SEAT, cust(g, w[1]), table(g, w[2]));
                case "order" -> new WaiterChoice(WaiterChoice.Kind.TAKE_ORDER, cust(g, w[1]), null);
                case "serve" -> new WaiterChoice(WaiterChoice.Kind.SERVE, cust(g, w[1]), null);
                default -> new WaiterChoice(WaiterChoice.Kind.WAIT, null, null);
            };
        }
        public ChefChoice readChefChoice(Game g) {
            String[] c = line(g)[3].split(" ");
            return new ChefChoice(c[0].equals("cook") ? cust(g, c[1]).getOrder() : null);
        }
        // a scripted move should never be illegal
        public void displayError(String r) { throw new AssertionError("script move rejected: " + r); }
        public void displayRecovery(Customer c, int newValue) {
            super.displayRecovery(c, newValue);
            trace.add("  recover c" + c.getId() + " " + newValue);
        }
        public void displayTurnEnd(Game g) {
            String[] l = line(g);
            StringBuilder sb = new StringBuilder(String.format("T%-2d %-2s %-12s %-14s %-10s |", g.getTurn(), l[0], l[1], l[2], l[3]));
            for (Customer c : g.getCustomers()) {
                String dish = c.getOrder() == null ? "" : " " + c.getOrder().getItem().getName() + ":" + c.getOrder().getStatus();
                sb.append(String.format(" c%d %d %s%s;", c.getId(), c.getSatisfaction(), c.getStatus(), dish));
            }
            HappyHour hh = g.getHappyHour();
            String hhState = hh.isActive(g.getTurn()) ? "active" : hh.isUsed() ? "used" : "-";
            sb.append(String.format(" | $%.2f paid=%d rep=%s hh=%s", g.getRevenueCents() / 100.0, g.getPaidCount(), g.getReputation(), hhState));
            trace.add(sb.toString());
            System.out.println(sb);
        }
        public void displayResult(Game g) {
            String result = String.format("RESULT %s: paid=%d revenue=$%.2f avg=%s served=%d left=%d turns=%d",
                    g.isVictory() ? "VICTORY" : "DEFEAT", g.getPaidCount(), g.getRevenueCents() / 100.0,
                    g.averageSatisfaction().isPresent() ? String.format("%.1f", g.averageSatisfaction().getAsDouble()) : "N/A",
                    g.getServedCount(), g.getUnhappyDepartures(), g.getTurn());
            trace.add(result);
            System.out.println(result);
        }
    }

    // a column must be one of these, anything else is a typo in the script
    static final String HOST = "host (wait|c\\d+ t\\d+)";
    static final String WAITER = "wait|seat c\\d+ t\\d+|order c\\d+|serve c\\d+";
    static final String CHEF = "wait|cook c\\d+";

    static Map<Integer, String[]> parse(String text) {
        Map<Integer, String[]> m = new HashMap<>();
        for (String l : text.strip().split("\n")) {
            String[] p = l.split(":", 2);
            List<String> cols = new ArrayList<>();
            for (String col : p[1].split("\\|")) {
                cols.add(col.strip().replaceAll("\\s+", " "));
            }
            String hh = cols.get(0).equals("hh") ? cols.remove(0) : "";
            String host = cols.size() == 3 ? cols.remove(0) : "host wait";
            if (cols.size() != 2 || !host.matches(HOST) || !cols.get(0).matches(WAITER) || !cols.get(1).matches(CHEF)) {
                throw new IllegalArgumentException("bad script line: " + l);
            }
            m.put(Integer.parseInt(p[0].strip().substring(1)), new String[] {hh, host, cols.get(0), cols.get(1)});
        }
        return m;
    }

    // plays one script and returns its trace
    static List<String> run(String path) throws Exception {
        String text = new String(java.nio.file.Files.readAllBytes(java.nio.file.Path.of(path)));
        ScriptedCLI cli = new ScriptedCLI(parse(text));
        new Game(cli).runGame();
        return cli.trace;
    }

    public static void main(String[] a) throws Exception {
        run(a[0]);
    }
}
