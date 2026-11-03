/**
 * Thrown by a domain guard when a move breaks the game's rules.
 *
 * It is thrown before any state changes (and before Staff.markActed()),
 * so the move is simply rejected: the CLI prints getMessage() and re-prompts.
 */
public class IllegalMoveException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public IllegalMoveException(String reason) {
        super(reason);
    }
}
