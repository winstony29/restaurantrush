
public class HappyHour {
    private int activatedTurn = -1;

    public void activate(int turn) {
        if (isUsed()) {
            throw new IllegalMoveException("Happy Hour can only be activated once.");
        }
        if (turn < 1) {
            throw new IllegalMoveException("Turn must be positive.");
        }
        activatedTurn = turn;
    }

    public boolean isUsed() {
        return activatedTurn != -1;
    }

    public boolean isActive(int turn) {
        return isUsed() && (turn == activatedTurn || turn == activatedTurn + 1);
    }

    public int remainingTurns(int turn) {
        if (turn == activatedTurn && isUsed()) return 2;
        if (turn == activatedTurn + 1 && isUsed()) return 1;
        return 0;
    }

    public boolean wasActiveOn(int turn) {
        return isActive(turn);
    }

    public int discountOn(int placedTurn, int cents) {
        if (wasActiveOn(placedTurn)) {
            return (cents * 80 + 50) / 100;
        }
        return cents;
    }
}
