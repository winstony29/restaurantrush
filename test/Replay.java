import java.util.*;

// plays a per-turn script through the real Game.runGame(), usage: java Replay test/victory.txt
public class Replay {
    static class ScriptedCLI extends RestaurantCLI {
        final Map<Integer, String[]> script;
        ScriptedCLI(Map<Integer, String[]> script) { this.script = script; }
        String[] line(Game g) { return script.getOrDefault(g.getTurn(), new String[] {"wait", "wait"}); }
        Customer cust(Game g, String id) { return g.getCustomers().stream().filter(c -> ("c" + c.getId()).equals(id)).findFirst().orElse(null); }
        public WaiterChoice readWaiterChoice(Game g) {
            String[] w = line(g)[0].split(" ");
            return switch (w[0]) {
                case "seat" -> new WaiterChoice(WaiterChoice.Kind.SEAT, cust(g, w[1]), g.getTables().get(Integer.parseInt(w[2].substring(1)) - 1));
                case "order" -> new WaiterChoice(WaiterChoice.Kind.TAKE_ORDER, cust(g, w[1]), null);
                case "serve" -> new WaiterChoice(WaiterChoice.Kind.SERVE, cust(g, w[1]), null);
                default -> new WaiterChoice(WaiterChoice.Kind.WAIT, null, null);
            };
        }
        public ChefChoice readChefChoice(Game g) {
            String[] c = line(g)[1].split(" ");
            return new ChefChoice(c[0].equals("cook") ? cust(g, c[1]).getOrder() : null);
        }
        // a scripted move should never be illegal
        public void displayError(String r) { throw new AssertionError("script move rejected: " + r); }
        public void displayTurnEnd(Game g) {
            StringBuilder sb = new StringBuilder(String.format("T%-2d %-14s %-10s |", g.getTurn(), line(g)[0], line(g)[1]));
            for (Customer c : g.getCustomers()) {
                String dish = c.getOrder() == null ? "" : " " + c.getOrder().getItem().getName() + ":" + c.getOrder().getStatus();
                sb.append(String.format(" c%d %d %s%s;", c.getId(), c.getSatisfaction(), c.getStatus(), dish));
            }
            sb.append(String.format(" | $%.2f paid=%d", g.getRevenueCents() / 100.0, g.getPaidCount()));
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
            String[] wc = p[1].split("\\|");
            m.put(Integer.parseInt(p[0].strip().substring(1)), new String[] {wc[0].strip(), wc[1].strip()});
        }
        return m;
    }

    public static void main(String[] a) throws Exception {
        String text = new String(java.nio.file.Files.readAllBytes(java.nio.file.Path.of(a[0])));
        new Game(new ScriptedCLI(parse(text))).runGame();
    }
}
