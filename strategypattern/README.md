# Strategy Pattern

**Category:** Behavioral
**Intent:** Define a family of algorithms, encapsulate each one, and make them interchangeable at runtime — the client can swap the algorithm a class uses without changing the class itself.

One-line recall: **"Vary the behavior, not the object."** Pull the part of a class that changes into its own interface + implementations, and let the class hold a reference to that interface instead of an if-else ladder.

---

## 1. The Problem (`problem/`)

[`ECommerceAppV1.java`](problem/ECommerceAppV1.java) has `ShippingCostCalculatorNaive.calculateShippingCost()` — a single method with an if/else-if chain keyed on a `strategyType` string:

```java
if ("FLAT_RATE".equalsIgnoreCase(strategyType)) { cost = 10.0; }
else if ("WEIGHT_BASED".equalsIgnoreCase(strategyType)) { cost = order.getTotalWeight() * 2.5; }
else if ("DISTANCE_BASED".equalsIgnoreCase(strategyType)) { ... }
else if ("THIRD_PARTY_API".equalsIgnoreCase(strategyType)) { ... }
else { throw new IllegalArgumentException(...); }
```

**Why this is bad (name the smells during revision):**
- **Violates Open/Closed Principle** — adding a `PremiumZone` strategy means editing this method directly (see the comment left at the bottom of the file as a reminder of this pain).
- **Stringly-typed dispatch** — `"FLAT_RATE"` typos fail at runtime, not compile time.
- **One class does everything** — `ShippingCostCalculatorNaive` has a reason to change for every single algorithm, not just one.
- **Can't unit-test one algorithm in isolation** — every test has to go through the same fat method.
- **Can't swap algorithm at runtime cleanly** — no object represents "the current strategy"; it's re-selected by string on every call.

---

## 2. The Solution (`solution/`)

### Participants → actual classes

| Role | Class | Responsibility |
|---|---|---|
| **Strategy** (interface) | [`ShippingStrategy`](solution/ShippingStrategy.java) | Declares `calculateCost(Order order)` — the one method every algorithm must provide. |
| **Concrete Strategies** | `FlatRateShippingStrategy`, `WeightBasedShippingStrategy`, `DistanceBasedShippingStrategy`, `ThirdPartyApiShippingStrategy` (all in `ShippingStrategy.java`) | Each is *one* algorithm, isolated, independently testable, with its own constructor params (e.g. `rate`, `ratePerKg`). |
| **Context** | [`ShippingCostService`](solution/ShippingCostService.java) | Holds a `ShippingStrategy strategy` field. Delegates via `strategy.calculateCost(order)`. Doesn't know or care *which* concrete strategy it holds. |
| **Client** | [`ECommerceAppV1`](solution/ECommerceAppV1.java) | Picks a concrete strategy, injects it into the context, can call `setStrategy(...)` to swap it at runtime. |

### The key mechanic: composition over conditionals

The naive version asks *"which branch do I run?"* inside one method.
The strategy version asks *"which object do I hold?"* — and polymorphism (`strategy.calculateCost(order)`) picks the right behavior for you.

```java
// Context (ShippingCostService.java) — never touches concrete strategy classes
public double calculateShippingCost(Order order) {
    return strategy.calculateCost(order);   // single dispatch call, no if-else
}

// Client (ECommerceAppV1.java) — swaps behavior at runtime
ShippingCostService shippingService = new ShippingCostService(flatRate);
shippingService.calculateShippingCost(order1);
shippingService.setStrategy(weightBased);   // <-- swap algorithm, context code unchanged
shippingService.calculateShippingCost(order1);
```

### What adding a new strategy looks like now

Per the comment at the bottom of [`solution/ECommerceAppV1.java`](solution/ECommerceAppV1.java): to add `FreeShippingStrategy`, you write **one new class** implementing `ShippingStrategy` and wire it in at the client. **Zero changes** to `ShippingCostService` or any existing strategy class — that's the Open/Closed Principle in action: open for extension, closed for modification.

---

## 3. Structure (generic UML shape)

```
Client ---> Context ---has-a---> «interface» Strategy
                                        ^
                     ┌──────────────────┼──────────────────┐
              ConcreteStrategyA   ConcreteStrategyB   ConcreteStrategyC
```

- Context holds a **reference to the interface**, not to any concrete class.
- Concrete strategies are **swappable** because they share one contract.
- The relationship Context → Strategy is **composition**, set via constructor or setter (both present here: constructor injection + `setStrategy`).

---

## 4. When to reach for it

- You have a family of algorithms that do the *same conceptual job* differently (shipping cost, sorting, discount calculation, compression, validation rules).
- You see an if-else or switch chain that picks behavior based on a type/enum/string, and that chain keeps growing.
- You want to pick the algorithm **at runtime** (config, user tier, A/B test) rather than at compile time.
- You want to unit-test each algorithm in isolation.

## 5. When NOT to bother

- Only 1-2 variants that will basically never grow — an interface + DI container wiring is overhead for nothing.
- The "algorithms" don't actually share a meaningful contract (forcing an interface just to avoid an if-else is over-engineering).
- If the variation is in *object creation* rather than *behavior/algorithm*, you probably want **Factory**, not Strategy.

## 6. Related patterns (don't confuse them)

- **State pattern** — nearly identical structure, but strategies don't usually know about each other, while States often transition themselves to the next State. Strategy = client picks; State = object switches itself.
- **Template Method** — also varies an algorithm step, but via inheritance/method-override, not composition. Strategy favors composition, which is why it's generally preferred (avoids fragile base class issues).
- **Factory** — creates *objects*; Strategy chooses *behavior*. They're often used together (a factory that returns the right `ShippingStrategy` for an order).

---

## 7. Self-test (cover the code, answer these)

1. In `ShippingCostService`, what type is the `strategy` field declared as — the interface or a concrete class? Why does that matter?
2. Where does the runtime decide *which* `calculateCost()` implementation actually runs?
3. Name the 3 constructor params across the concrete strategies. Why does each strategy own its own config instead of `Order` holding all of it?
4. What has to change in `ShippingCostService.java` to add a 5th shipping strategy? (Trick question — answer should be "nothing.")
5. Why is `strategy.calculateCost(order)` a *single dispatch* call able to replace a 5-branch if-else? (Answer: polymorphism — the JVM resolves the correct override based on the object's runtime type.)

<details>
<summary>Answers (try to answer first, then check)</summary>

1. **The interface (`ShippingStrategy`)**, not any concrete class — see [`ShippingCostService.java:4`](solution/ShippingCostService.java). This matters because it's what makes the field swappable: the context can hold *any* current or future implementation without knowing its concrete type. Program to an interface, not an implementation.

2. **At the call site `strategy.calculateCost(order)`**, resolved by the JVM via dynamic (virtual) dispatch based on the actual runtime type of the object stored in `strategy` — decided the moment `setStrategy(...)` or the constructor assigned that field, not when `calculateShippingCost()` is called. Compare to the naive version, where the branch is re-decided from a string on *every single call*.

3. `rate` (`FlatRateShippingStrategy`), `ratePerKg` (`WeightBasedShippingStrategy`), `ratePerKm` (`DistanceBasedShippingStrategy`) — plus `baseFee` (`ThirdPartyApiShippingStrategy`), so 4 total. Each strategy owns its own config because that config is *specific to how that one algorithm computes cost* — it's not a property of the `Order` itself. Keeping it on the strategy keeps `Order` a plain data holder and keeps each algorithm's parameters encapsulated with the algorithm that uses them (single responsibility).

4. **Nothing.** Write a new class implementing `ShippingStrategy`, instantiate it at the client, call `setStrategy(...)`. `ShippingCostService` never references any concrete strategy class, so it's closed for modification — exactly the Open/Closed Principle payoff called out in section 2.

5. Because all concrete strategies implement the same `ShippingStrategy` interface, `strategy.calculateCost(order)` doesn't need to know *which* one it's holding — the JVM looks up the correct override on the object's actual runtime class (vtable dispatch) and calls it directly. One polymorphic call site replaces N branches because the "branching" already happened once, earlier, when the strategy object was chosen and assigned.

</details>
