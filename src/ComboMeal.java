/**
 * A menu item made of two existing items.
 *
 * Unlike a plain MenuItem, which is given its price and prep units, a combo
 * computes them from its two parts:
 *   name  = "<first> + <second> combo"
 *   price = 90% of the two prices combined, rounded to the nearest cent
 *   units = the two prep units added together
 * Everything is passed to MenuItem's constructor, so MenuItem itself does not
 * change and a combo can go anywhere a MenuItem can (Menu, Order, Cashier).
 */
public class ComboMeal extends MenuItem {

    private static final int COMBO_PERCENT = 90;

    private final MenuItem first;
    private final MenuItem second;

    public ComboMeal(MenuItem first, MenuItem second) {
        super(comboName(first, second), comboPrice(first, second), comboUnits(first, second));
        this.first = first;
        this.second = second;
    }

    public MenuItem getFirst() {
        return first;
    }

    public MenuItem getSecond() {
        return second;
    }

    // Static helpers run before super(...), so the null check happens before
    // MenuItem is built. comboName is called first, so it does the check.

    private static String comboName(MenuItem first, MenuItem second) {
        if (first == null || second == null) {
            throw new IllegalArgumentException("A combo needs two menu items");
        }
        return first.getName() + " + " + second.getName() + " combo";
    }

    /** (sum * 90 + 50) / 100: 90% of the combined price, rounded half up to the cent. */
    private static int comboPrice(MenuItem first, MenuItem second) {
        int sum = first.getPriceCents() + second.getPriceCents();
        return (sum * COMBO_PERCENT + 50) / 100;
    }

    private static int comboUnits(MenuItem first, MenuItem second) {
        return first.getPrepUnits() + second.getPrepUnits();
    }
}