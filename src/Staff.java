//Main parent class for staff
public abstract class Staff {
    private boolean actedThisTurn = false;

    public void resetTurn() {
        actedThisTurn = false;
    }

    protected void requireAvailable() {
        if (actedThisTurn) {
            throw new IllegalMoveException("Already acted this turn");
        }
    }

    protected void markActed() {
        actedThisTurn = true;
    }
}






