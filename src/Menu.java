import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The restaurant's menu: one or more {@link MenuItem}s (multiplicity 1..*).
 * Owned by Game. The list is copied in and handed out read-only, so nothing
 * outside Menu can add, remove or reorder items.
 */
public class Menu {

    private final List<MenuItem> items;

    public Menu(List<MenuItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("A menu needs at least one item");
        }
        for (MenuItem item : items) {
            if (item == null) {
                throw new IllegalArgumentException("Menu items cannot be null");
            }
        }
        this.items = new ArrayList<>(items);
    }

    /** Read-only view of the items, in menu order. */
    public List<MenuItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    /** Item at a 0-based position (e.g. for Customer.chooseItem picking at random). */
    public MenuItem getItem(int index) {
        if (index < 0 || index >= items.size()) {
            throw new IndexOutOfBoundsException(
                    "No menu item at index " + index + " (menu has " + items.size() + ")");
        }
        return items.get(index);
    }
}
