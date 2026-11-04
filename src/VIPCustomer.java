public class VIPCustomer extends Customer {

    public VIPCustomer(int id) {
        super(id);
    }

    @Override
    protected int waitingLoss() {
        return 5;
    }

    @Override
    public int applyDiscount(int cents) {
        return cents * 90 / 100;
    }
}