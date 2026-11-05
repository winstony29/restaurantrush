# Restaurant Rush — SC2002 Group Assignment

A Java command-line, turn-based restaurant management game developed using object-oriented programming principles.

## Build and Run

Compile:
```bash
javac -d out src/*.java
```

Run:
```bash
java -cp out Main
```

## Replay a Scripted Run

Compile:
```bash
javac -d out src/*.java test/Replay.java
```

Run:
```bash
java -cp out Replay test/victory.txt
```

## Stage 1 — Basic Restaurant Operations

**Git tag:** `stage-1`

Stage 1 implements the core restaurant management system.

### Code Structure

| Class | Responsibility |
|---|---|
| `Game` | Controls turn progression, customer arrivals, staff assignments and victory conditions. |
| `RestaurantCLI` | Handles user input and displays game information. |
| `Customer` | Manages customer status, satisfaction and orders. |
| `Order` | Tracks food preparation, order status and billing. |
| `Menu` / `MenuItem` | Stores available food items, prices and preparation times. |
| `Table` | Manages seating and table availability. |
| `Staff` | Abstract superclass that enforces one action per staff member per turn. |
| `Waiter` | Seats customers, takes orders and serves food. |
| `Chef` | Advances food preparation. |
| `Cashier` | Collects payments and releases tables. |
| `IllegalMoveException` | Handles invalid actions without consuming staff turns. |

### OOP Design

- **Encapsulation:** Customer and Order manage their own states through methods rather than direct field access.
- **Inheritance:** Waiter, Chef and Cashier extend the abstract Staff class.
- **Polymorphism:** Game manages staff through `List<Staff>` and calls `resetTurn()` on each object.
- **Composition:** Game owns its tables, staff and menu.

## Stage 2 — Customer Behaviour Extension

**Git tag:** `stage-2`

Stage 2 introduces VIP and Critic customers with different satisfaction, pricing and service-rating behaviours.

### New and Modified Files

| File | Change | Reason |
|---|---|---|
| `RegularCustomer.java` | New | Preserves Stage 1 customer behaviour. |
| `VIPCustomer.java` | New | Implements reduced satisfaction loss and a 10% discount. |
| `CriticCustomer.java` | New | Implements increased satisfaction loss, most-expensive-item preference and service ratings. |
| `Rating.java` | New | Defines Critic service ratings. |
| `Reputation.java` | New | Adjusts satisfaction loss based on restaurant reputation. |
| `Customer.java` | Modified | Becomes abstract and defines methods for customer-specific behaviour. |
| `Order.java` | Modified | Records the turn an order becomes READY and exposes its base price. |
| `Chef.java` | Modified | Passes the current turn to `Order.advancePrep(turn)`. |
| `Cashier.java` | Modified | Calls `Customer.applyDiscount()` to calculate the final bill. |
| `Game.java` | Modified | Creates different customer types and manages restaurant reputation. |
| `RestaurantCLI.java` | Modified | Displays customer types and restaurant reputation. |
| `TEST_NOTES.md` / `test/` | Modified | Adds reproducible Stage 2 victory and defeat tests. |

### Customer Behaviour

| Customer Type | Satisfaction Loss per Turn | Discount | Special Behaviour |
|---|---|---|---|
| Regular | 8 | None | Standard customer behaviour |
| VIP | 5 | 10% | Reduced waiting penalty |
| Critic | 12 | None | Prefers the most expensive item and rates service |

The Critic evaluates service based on the number of turns between an order becoming READY and being served:

- **GOOD:** 1 turn — reduces satisfaction loss by 2.
- **NEUTRAL:** 2 turns — no adjustment.
- **BAD:** 3 or more turns — increases satisfaction loss by 2.

The latest Critic rating determines the restaurant's reputation, which affects the waiting satisfaction loss of all customers.

### OOP Design Changes

**Inheritance and Polymorphism**

`Customer` becomes an abstract superclass extended by `RegularCustomer`, `VIPCustomer` and `CriticCustomer`.

Each subclass implements its own behaviour through method overriding:

- `waitingLoss()` determines satisfaction loss.
- `applyDiscount()` calculates customer-specific discounts.
- `rateService()` allows Critics to evaluate service.
- `chooseItem()` allows Critics to select their preferred food.

The `Game` and `Cashier` classes interact with customers through the common `Customer` superclass without checking their specific types.

**Encapsulation**

Customer-specific behaviour remains inside the respective customer subclasses. Order preparation and READY-turn tracking remain within `Order`.

**Extensibility**

New customer types can be introduced without modifying the existing seating, cooking and payment workflow.

### GitHub Comparison

Stage 1 → Stage 2: [stage-1...stage-2](https://github.com/winstony29/restaurantrush/compare/stage-1...stage-2)

## Stage 2 → Stage 3: Host, Combos and Happy Hour

**Git tag:** `stage-3` (pending)

The Host acts before the Waiter and Chef. Host and Waiter share seating rules
through `Seater`, with one action per staff member per turn. The Cashier applies
the customer discount first, then Happy Hour according to the order's placed turn.
For example, a VIP Burger during Happy Hour costs 1500 × 90% × 80% = 1080 cents.

| File | New / Changed | Purpose |
|---|---|---|
| `Seater.java` | New | Shares the existing seating guards and state transitions between Host and Waiter. |
| `Host.java` | New | Inherits seating with its own staff action availability. |
| `Waiter.java` | Changed | Inherits seating; passes the current turn when taking an order. Serving is unchanged. |
| `Cashier.java` | Changed | Applies Happy Hour after the customer discount, preserving payment and table clearing. |
| `ComboMeal.java` | New | Computes combo price and preparation units from two menu items. |
| `HappyHour.java` | New | Tracks the once-per-game two-turn period and discounts orders placed during it. |
| `HostChoice.java` | New | Represents the manager's Host action. |
| `Order.java` | Changed | Records and exposes the turn the order was placed. |
| `Customer.java` | Changed | Supports capped satisfaction recovery for eligible customers. |
| `Game.java` | Changed | Coordinates Host actions, combos, Happy Hour activation and recovery, and updated staff calls. |
| `RestaurantCLI.java` | Changed | Prompts for Host and Happy Hour choices and displays Happy Hour status. |
| `TEST_NOTES.md` / `test/` | Changed | Documents Stage 3 runs and supports Host and Happy Hour replay choices. |

The table describes the complete Stage 3 transition. This branch implements only
`Seater`, `Host`, `Waiter`, `Cashier` and this README; the remaining rows depend on
teammate branches. Full compilation requires `HappyHour`, the updated `Order`
constructor/accessor and Game's updated staff calls.

Stage 2 → Stage 3 GitHub comparison: pending until `stage-3` is tagged.
