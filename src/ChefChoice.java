/**
 * The chef task the manager picked this turn. Returned by
 * RestaurantCLI.readChefChoice() and carried out by Game.applyChefChoice().
 * A null order means the chef waits.
 */
public record ChefChoice(Order order) {

    public static ChefChoice cook(Order o) {
        return new ChefChoice(o);
    }

    public static ChefChoice waitTurn() {
        return new ChefChoice(null);
    }
}
