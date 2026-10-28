# Stage 1 test notes

Base setting, no randomness. Customers pick food by id (`Customer.chooseItem`): c1 Salad, c2 Burger, c3 Pasta, c4 Salad, c5 Burger, c6 Pasta. Arrivals: c1 T1, c2 T4, c3 T7, c4 T10, c5 T13, c6 T16.

Each run is a list of per-turn choices: Waiter task | Chef task. Turns not listed are Wait | Wait.
The same scripts are in `test/victory.txt` and `test/defeat.txt` and can be replayed through `Game.runGame()`:

```
javac -d out src/*.java test/Replay.java
java -cp out Replay test/victory.txt
```

## Worked example check (brief 3.3)

c3 orders Pasta and gets full attention in the victory run, turns 7 to 12:

| Turn | Choices | c3 at turn end | Brief |
|---|---|---|---|
| 7 | seat c3 at table 1 | 92 SEATED | 92 |
| 8 | take c3's order, chef unit 1 of 2 | 84 ORDERED, Pasta TAKEN | 84 |
| 9 | waiter waits, chef unit 2 of 2 | 76, Pasta READY (not servable this turn) | 76 |
| 10 | serve c3 | 76 SERVED | 76 |
| 11 | - | 76 READY_TO_PAY | 76 |
| 12 | - | PAID, $18.00 collected, table 1 freed, 76 recorded | 76 recorded |

## Victory run

| Turn | Waiter | Chef |
|---|---|---|
| 1 | seat c1 at table 1 | wait |
| 2 | take order c1 | cook c1 (Salad READY) |
| 3 | serve c1 | wait |
| 4 | seat c2 at table 2 | wait |
| 5 | take order c2 | cook c2 (Burger READY) |
| 6 | serve c2 | wait |
| 7 | seat c3 at table 1 | wait |
| 8 | take order c3 | cook c3 (1/2) |
| 9 | wait | cook c3 (2/2, READY) |
| 10 | serve c3 | wait |
| 11 | seat c4 at table 2 | wait |
| 12 | take order c4 | cook c4 (Salad READY) |
| 13 | serve c4 | wait |
| 14 | seat c5 at table 1 | wait |
| 15 | take order c5 | cook c5 (Burger READY) |
| 16 | serve c5 | wait |
| 17 | seat c6 at table 2 | wait |
| 18 | take order c6 | cook c6 (1/2) |

Payments: c1 T5, c2 T8, c3 T12, c4 T15, c5 T18. c6 cannot pay in time (intended).
Paid satisfactions 84, 84, 76, 76, 76.

**Result: VICTORY**, 5 paid, revenue $66.00, average satisfaction 79.2, 0 departures.

Decision with constrained staff: T10, c4 arrives while c3's Pasta is READY. The Waiter serves c3 first and c4 waits a turn (ends T10 at 92), because a READY dish served late costs the seated customer satisfaction every turn.

## Defeat run

Same arrivals, but the manager leaves the Chef idle until turn 9.

| Turn | Waiter | Chef |
|---|---|---|
| 1 | seat c1 at table 1 | wait |
| 2 | take order c1 | wait |
| 4 | seat c2 at table 2 | wait |
| 5 | take order c2 | wait |
| 9 | wait | cook c1 (Salad READY) |
| 10 | serve c1 | cook c2 (Burger READY) |
| 11 | serve c2 | wait |
| 13 | seat c3 at table 1 | wait |
| 14 | seat c4 at table 2 | wait |
| 15 | take order c3 | cook c3 (1/2) |
| 16 | take order c4 | cook c3 (2/2, READY) |
| 17 | serve c3 | cook c4 (Salad READY) |
| 18 | serve c4 | wait |

Payments: c1 T12 (28), c2 T13 (44). c3 and c4 are served but still eating or waiting to pay at T18.

**Result: DEFEAT**, 2 paid, revenue $24.00, average satisfaction 36.0. Fails all three targets.
