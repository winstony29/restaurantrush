public class RegularCustomer extends Customer {

    public RegularCustomer(int id) {
        super(id);
    }

    @Override
    protected int waitingLoss() {
        return 8;
    }
}