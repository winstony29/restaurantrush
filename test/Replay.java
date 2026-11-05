import java.util.*;

// plays a per-turn script through the real Game.runGame(), usage: java Replay test/victory.txt
// line format: T5: [hh |] [host <c> <t> | ]<waiter> | <chef>; two columns is the Stage 1/2 format
public class Replay {
    static class ScriptedCLI extends RestaurantCLI {
        // each turn is {hh or "", host, waiter, chef}
        final Map<Integer, String[]> script;
        ScriptedCLI(Map<Integer, String[]> script) { this.script = script; }
        String[] line(Game g) { return script.getOrDefault(g.getTurn(), new String[] {"", "host wait", "wait", "wait"}); }
        Customer cust(Game g, String id) { return g.getCustomers().stream().filter(c -> ("c" + c.getId()).equals(id)).findFirst().orElse(null); }
        Table table(Game g, String id) { return g.getTables().get(Integer.parseInt(id.substring(1)) - 1); }
        public boolean askActivateHappyHour(Game g) { return line(g)[0].equals("hh"); }
        public HostChoice readHostChoice(Game g) {
            String[] h = line(g)[1].split(" ");
            return h.length == 3 ? HostChoice.seat(cust(g, h[1]), table(g, h[2])) : HostChoice.waitTurn();
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
            System.out.println(sb);
        }
        public void displayResult(Game g) {
            System.out.printf("RESULT %s: paid=%d revenue=$%.2f avg=%s served=%d left=%d turns=%d%n",
                    g.isVictory() ? "VICTORY" : "DEFEAT", g.getPaidCount(), g.getRevenueCents() / 100.0,
                    g.averageSatisfaction().isPresent() ? String.format("%.1f", g.averageSatisfaction().getAsDouble()) : "N/A",
                    g.getServedCount(), g.getUnhappyDepartures(), g.getTurn());
        }
    }

    static Map<Integer, String[]> parse(String text) {
        Map<Integer, String[]> m = new HashMap<>();
        for (String l : text.strip().split("\n")) {
            String[] p = l.split(":", 2);
            List<String> cols = new ArrayList<>();
            for (String col : p[1].split("\\|")) {
                cols.add(col.strip());
            }
            String hh = cols.get(0).equals("hh") ? cols.remove(0) : "";
            String host = cols.size() == 3 ? cols.remove(0) : "host wait";
            m.put(Integer.parseInt(p[0].strip().substring(1)), new String[] {hh, host, cols.get(0), cols.get(1)});
        }
        return m;
    }

    public static void main(String[] a) throws Exception {
        String text = new String(java.nio.file.Files.readAllBytes(java.nio.file.Path.of(a[0])));
        new Game(new ScriptedCLI(parse(text))).runGame();
    }
}
