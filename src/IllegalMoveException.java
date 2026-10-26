package restaurantrush;

//exception class for illegal moves
class IllegalMoveException extends RuntimeException {
    public IllegalMoveException(String message) {
        super(message);
    }
}