# Stage 1 test notes

Base setting, no randomness. Customers pick food by id (`Customer.chooseItem`): c1 Salad, c2 Burger, c3 Pasta, c4 Salad, c5 Burger, c6 Pasta. Arrivals: c1 T1, c2 T4, c3 T7, c4 T10, c5 T13, c6 T16.

Each run is a list of per-turn choices: Waiter task | Chef task. Turns not listed are Wait | Wait.
The same scripts are in `test/victory.txt` and `test/defeat.txt` and can be replayed through `Game.runGame()`:

```
javac -d out src/*.java test/Replay.java
java -cp out Replay test/victory.txt
```

From Stage 2 on, the base setting has a VIP on T4 and a Critic on T7, so these two scripts only reproduce the Stage 1 numbers at the stage-1 tag (`git checkout stage-1`). On Stage 2 code `victory.txt` gives the Stage 2 victory numbers and `defeat.txt` stops at T16, because the Critic has already left.

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

# Stage 2 test notes

Arrivals: c1 Regular T1, c2 VIP T4, c3 Critic T7, c4 Regular T10, c5 Regular T13, c6 Regular T16. Food is still picked by id, except the Critic always picks the most expensive item, so the dishes are the same as Stage 1: c1 Salad, c2 Burger, c3 Pasta, c4 Salad, c5 Burger, c6 Pasta.

- Waiting loss per turn (WAITING, SEATED, ORDERED): Regular 8, VIP 5, Critic 12.
- VIP pays 90% of the base price: Burger 1500 * 90 / 100 = $13.50.
- Critic rating when served: served turn - READY turn = 1 GOOD, 2 NEUTRAL, 3 or more BAD. Reputation starts NEUTRAL; from then on every waiting customer's loss is GOOD -2, BAD +2, never below 1.
- The rating is given when the Waiter serves, so it already applies to that turn's end-of-turn loss.
- Timing is unchanged from Stage 1.

These runs reproduce at the stage-2 tag (`git checkout stage-2`):

```
javac -d out src/*.java test/*.java
java -cp out Replay test/stage2-victory.txt
java -cp out Replay test/stage2-defeat.txt
```

From Stage 3 on, the menu has combos, so c3 orders a 3-unit Burger + Pasta and these scripts are rejected part way through.

## Stage 2 victory run

Same choices as the Stage 1 victory run (`test/stage2-victory.txt` is a copy of `test/victory.txt`).

| Turn | Waiter | Chef | End of turn |
|---|---|---|---|
| 1 | seat c1 at table 1 | wait | c1 92 |
| 2 | take order c1 | cook c1 (Salad READY) | c1 84 |
| 3 | serve c1 | wait | c1 84 SERVED |
| 4 | seat c2 at table 2 | wait | c1 READY_TO_PAY, c2 95 |
| 5 | take order c2 | cook c2 (Burger READY) | c1 pays $9.00 (84), c2 90 |
| 6 | serve c2 | wait | c2 90 SERVED |
| 7 | seat c3 at table 1 | wait | c2 READY_TO_PAY, c3 88 |
| 8 | take order c3 | cook c3 (1/2) | c2 pays $13.50 (90), c3 76 |
| 9 | wait | cook c3 (2/2, READY) | c3 64 |
| 10 | serve c3 | wait | 10 - 9 = 1, GOOD; c4 94 (loss 6) |
| 11 | seat c4 at table 2 | wait | c3 READY_TO_PAY, c4 88 |
| 12 | take order c4 | cook c4 (Salad READY) | c3 pays $18.00 (64), c4 82 |
| 13 | serve c4 | wait | c4 82 SERVED, c5 94 |
| 14 | seat c5 at table 1 | wait | c4 READY_TO_PAY, c5 88 |
| 15 | take order c5 | cook c5 (Burger READY) | c4 pays $9.00 (82), c5 82 |
| 16 | serve c5 | wait | c5 82 SERVED, c6 94 |
| 17 | seat c6 at table 2 | wait | c5 READY_TO_PAY, c6 88 |
| 18 | take order c6 | cook c6 (1/2) | c5 pays $15.00 (82), c6 82 |

Paid satisfactions 84, 90, 64, 82, 82.

**Result: VICTORY**, 5 paid, revenue $64.50, average satisfaction 80.4, 0 departures.

Compared with Stage 1: the VIP pays $1.50 less but loses less while waiting, the Critic loses 12 a turn and pays at 64, and the GOOD rating cuts every later Regular's loss to 6.

## Stage 2 defeat run

The manager ignores c1, then seats c4 instead of serving the Critic's READY Pasta.

| Turn | Waiter | Chef | End of turn |
|---|---|---|---|
| 1-3 | wait | wait | c1 92, 84, 76 (never seated) |
| 4 | seat c2 at table 2 | wait | c1 68, c2 95 |
| 5 | take order c2 | cook c2 (Burger READY) | c1 60, c2 90 |
| 6 | serve c2 | wait | c1 52, c2 90 SERVED |
| 7 | seat c3 at table 1 | wait | c1 44, c2 READY_TO_PAY, c3 88 |
| 8 | take order c3 | cook c3 (1/2) | c2 pays $13.50 (90), c1 36, c3 76 |
| 9 | wait | cook c3 (2/2, READY) | c1 28, c3 64 |
| 10 | seat c4 at table 2 | wait | c1 20, c3 52, c4 92 |
| 11 | take order c4 | cook c4 (Salad READY) | c1 12, c3 40, c4 84 |
| 12 | serve c3 | wait | 12 - 9 = 3, BAD; c1 2, c4 74 (loss 10) |
| 13 | serve c4 | wait | c1 0, leaves; c3 READY_TO_PAY, c5 90 |
| 14 | wait (no free table) | wait | c3 pays $18.00 (40), c4 READY_TO_PAY, c5 80 |
| 15 | seat c5 at table 1 | wait | c4 pays $9.00 (74), c5 70 |
| 16 | take order c5 | cook c5 (Burger READY) | c5 60, c6 90 |
| 17 | serve c5 | wait | c5 60 SERVED, c6 80 |
| 18 | seat c6 at table 2 | wait | c5 READY_TO_PAY, c6 70 |

Paid satisfactions 90, 40, 74. c5 cannot pay in time.

**Result: DEFEAT**, 3 paid, revenue $40.50, average satisfaction 68.0, 1 departure. Fails the paid and revenue targets.

c1 leaves at the end of T13 under either reputation (NEUTRAL would give 4 then 0). No customer can leave only because of a BAD rating in the base setting: the earliest BAD rating is T12 (Pasta READY T9 at the earliest, served 3 turns later), and a Regular arriving T10 loses at most 8 + 8 + 7 * 10 = 86 by T18.

# Stage 3 test notes

Arrivals and customer types are the same as Stage 2. The menu adds two combos after the three base items: Salad + Pasta $24.30 (3 units) and Burger + Pasta $29.70 (3 units), each 90% of the two prices rounded to the cent. Customers still pick by id, now over five items, and the Critic still picks the most expensive: c1 Salad, c2 Burger, c3 Burger + Pasta, c4 Salad + Pasta, c5 Burger + Pasta, c6 Salad.

- Host: seats one waiting customer per turn, before the Waiter. The Waiter can still seat, so the Waiter can take the order of a customer the Host seated that same turn.
- Happy Hour: once per game, asked at the start of a turn. It lasts that turn and the next.
- Orders taken while it is active pay 80% after the VIP 90%, rounded to the cent: Salad + Pasta 2430 * 80 / 100 = $19.44.
- On each active turn, after the start-of-turn display, every customer still waiting for a table or food recovers 15 (max 100).
- Everything else is unchanged from Stage 2.

Script lines can now have two more columns: `T13: hh | host c5 t1 | order c4 | cook c4`. A leading `hh` starts Happy Hour that turn, and `host c<id> t<id>` is the Host's seat. Turns without them, and every Stage 1 and 2 line, mean no Happy Hour and the Host waits. A line Replay does not recognise, or a customer or table that does not exist on that turn, stops the replay with an error instead of becoming a wait.

These runs cannot be replayed until Darren's Host and Cashier branch is merged into main. They were checked on a local merge of all four Stage 3 branches, and against a separate hand model of the rules. Then:

```
javac -d out src/*.java test/*.java
java -cp out Replay test/stage3-victory.txt
java -cp out Replay test/stage3-defeat.txt
java -cp out ReplayCheck
```

`ReplayCheck` compares every turn, not just the final result: each turn-end line, each Happy Hour recovery and the result must match `test/stage3-victory.expected` and `test/stage3-defeat.expected`. Changing the recovery from 15 to 14, for example, leaves both final results unchanged but fails the check at c4's T13 recovery (97 vs 96).

## Stage 3 victory run

| Turn | Host | Waiter | Chef | End of turn |
|---|---|---|---|---|
| 1 | seat c1 at table 1 | take order c1 | cook c1 (Salad READY) | c1 92 |
| 2 | wait | serve c1 | wait | c1 92 SERVED |
| 3 | wait | wait | wait | c1 READY_TO_PAY |
| 4 | seat c2 at table 2 | take order c2 | cook c2 (Burger READY) | c1 pays $9.00 (92), c2 95 |
| 5 | wait | serve c2 | wait | c2 95 SERVED |
| 6 | wait | wait | wait | c2 READY_TO_PAY |
| 7 | seat c3 at table 1 | take order c3 | cook c3 (1/3) | c2 pays $13.50 (95), c3 88 |
| 8 | wait | wait | cook c3 (2/3) | c3 76 |
| 9 | wait | wait | cook c3 (3/3, READY) | c3 64 |
| 10 | seat c4 at table 2 | serve c3 | wait | 10 - 9 = 1, GOOD; c4 94 (loss 6) |
| 11 | wait | wait | wait | c3 READY_TO_PAY, c4 88 |
| 12 | wait | wait | wait | c3 pays $29.70 (64), c4 82 |
| 13 | Happy Hour on; seat c5 at table 1 | take order c4 | cook c4 (1/3) | start: c4 82 -> 97, c5 100; end: c4 91, c5 94 |
| 14 | wait | take order c5 | cook c4 (2/3) | start: c4 91 -> 100, c5 94 -> 100; end: c4 94, c5 94 |
| 15 | wait | wait | cook c4 (3/3, READY) | c4 88, c5 88 |
| 16 | wait | serve c4 | cook c5 (1/3) | c4 88 SERVED, c5 82, c6 94 (no free table) |
| 17 | wait | wait | cook c5 (2/3) | c4 READY_TO_PAY, c5 76, c6 88 |
| 18 | wait | wait | cook c5 (3/3, READY) | c4 pays $19.44 (88), c5 70, c6 82 |

Paid satisfactions 92, 95, 64, 88. c5 and c6 cannot pay in time.

**Result: VICTORY**, 4 paid, revenue $71.64, average satisfaction 84.75 (Replay prints 84.8), 0 departures.

The Host and Waiter work in the same turn on T1, T4 and T7 (seat, then take that customer's order) and on T10 and T13 (Host seats while the Waiter serves or takes another order). The manager holds c4's order on T11 and T12 and starts Happy Hour on T13: c4 recovers a full 15 (82 -> 97) and pays at 88, and Happy Hour takes $4.86 off c4's bill (2430 -> 1944).

## Stage 3 defeat run

The manager seats the Critic and c4 but holds both orders for a late Happy Hour, then the two 3-unit combos share the Chef.

| Turn | Host | Waiter | Chef | End of turn |
|---|---|---|---|---|
| 1 | seat c1 at table 1 | take order c1 | cook c1 (Salad READY) | c1 92 |
| 2 | wait | serve c1 | wait | c1 92 SERVED |
| 3 | wait | wait | wait | c1 READY_TO_PAY |
| 4 | seat c2 at table 2 | take order c2 | cook c2 (Burger READY) | c1 pays $9.00 (92), c2 95 |
| 5 | wait | serve c2 | wait | c2 95 SERVED |
| 6 | wait | wait | wait | c2 READY_TO_PAY |
| 7 | seat c3 at table 1 | wait (holds the order) | wait | c2 pays $13.50 (95), c3 88 |
| 8-9 | wait | wait | wait | c3 76, 64 |
| 10 | seat c4 at table 2 | wait (holds the order) | wait | c3 52, c4 92 |
| 11 | wait | wait | wait | c3 40, c4 84 |
| 12 | Happy Hour on | take order c3 | cook c3 (1/3) | start: c3 40 -> 55, c4 84 -> 99; end: c3 43, c4 91 |
| 13 | wait | take order c4 | cook c4 (1/3) | start: c3 43 -> 58, c4 91 -> 100, c5 100; end: c3 46, c4 92, c5 92 (no free table) |
| 14 | wait | wait | cook c3 (2/3) | c3 34, c4 84, c5 84 |
| 15 | wait | wait | cook c4 (2/3) | c3 22, c4 76, c5 76 |
| 16 | wait | wait | cook c3 (3/3, READY) | c3 10, c4 68, c5 68, c6 92 |
| 17 | wait | serve c3 | cook c4 (3/3, READY) | 17 - 16 = 1, GOOD; c3 10 SERVED, c4 62, c5 62, c6 86 |
| 18 | wait | serve c4 | wait | c3 READY_TO_PAY, c4 62 SERVED, c5 56, c6 80 |

Paid satisfactions 92, 95. c3 and c4 are served but cannot pay in time, so their Happy Hour prices ($23.76 and $19.44) are never collected.

**Result: DEFEAT**, 2 paid, revenue $22.50, average satisfaction 93.5, 0 departures. Fails the paid and revenue targets.

Only two tables means the kitchen never holds more than two orders, so combos only clog it when the manager lets orders pile up. In the base setting every policy that keeps all staff busy wins (checked over every Waiter priority order, Chef order, Happy Hour turn and Host use), so a defeat needs a mistake like holding orders here.
