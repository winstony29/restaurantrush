/**
 * One dish on the menu. Immutable: every field is final and there are no
 * setters, so a price cannot change after an order has been taken.
 */
public class MenuItem {

    private final String name;
    private final int priceCents;
    private final int prepUnits;

    public MenuItem(String name, int priceCents, int prepUnits) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Menu item needs a name");
        }
        if (priceCents < 0) {
            throw new IllegalArgumentException("Price cannot be negative: " + priceCents);
        }
        if (prepUnits < 1) {
            throw new IllegalArgumentException("Prep units must be at least 1: " + prepUnits);
        }
        this.name = name;
        this.priceCents = priceCents;
        this.prepUnits = prepUnits;
    }

    public String getName() {
        return name;
    }

    public int getPriceCents() {
        return priceCents;
    }

    public int getPrepUnits() {
        return prepUnits;
    }

    @Override
    public String toString() {
        return String.format("%s ($%d.%02d, %d prep)",
                name, priceCents / 100, priceCents % 100, prepUnits);
    }
}
