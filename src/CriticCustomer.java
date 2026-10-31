public class CriticCustomer extends Customer {

    public CriticCustomer(int id) {
        super(id);
    }

    @Override
    protected int waitingLoss() {
        return 12;
    }

    @Override
    public MenuItem chooseItem(Menu menu) {
        if (menu == null || menu.getItems() == null || menu.getItems().isEmpty()) {
            throw new IllegalArgumentException("Menu must contain at least one item.");
        }

        MenuItem mostExpensive = menu.getItems().get(0);

        for (MenuItem item : menu.getItems()) {
            if (item.getPriceCents() > mostExpensive.getPriceCents()) {
                mostExpensive = item;
            }
        }

        return mostExpensive;
    }

    @Override
    public Rating rateService(Order o, int servedTurn) {
        int delay = servedTurn - o.getReadyTurn();

        if (delay <= 1) {
            return Rating.GOOD;
        } else if (delay == 2) {
            return Rating.NEUTRAL;
        } else {
            return Rating.BAD;
        }
    }
}