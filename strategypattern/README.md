# Strategy Pattern

**Category:** Behavioral
**Recall phrase:** "Swap the plug, not the socket."

> **TL;DR:** Pull the part of a class that varies (an algorithm) into its own interface + implementations, and have the class hold a reference to that interface instead of branching on a type/string. Use it when you have multiple interchangeable algorithms for the same job and want to add new ones without touching existing code. Skip it when you only have 1-2 variants that will never grow.

## The problem

`problem/ECommerceAppV1.java` computes shipping cost with `ShippingCostCalculatorNaive.calculateShippingCost(order, strategyType)` — a single method with an if/else-if chain keyed on a `strategyType` string (`"FLAT_RATE"`, `"WEIGHT_BASED"`, `"DISTANCE_BASED"`, `"THIRD_PARTY_API"`). Every new shipping option means editing this method again — the file even leaves a comment calling this out: *"What if we want to try a new 'PremiumZone' strategy? We have to go modify this calculator class again."* It's also stringly-typed (a typo in `strategyType` fails at runtime, not compile time), and you can't unit-test one algorithm without going through the whole method.

## Analogy

A universal travel charger: the brick (context) always calls the same `deliverPower()` contract on whatever's clicked into its base. Click in the US adapter, the UK adapter, or the EU adapter (concrete strategies) depending on which country's socket you're facing, and the brick's internals never get opened up or rewired. Buy a new adapter for a new country and nothing about the existing charger changes.

## Structure

- `ShippingStrategy` (`solution/ShippingStrategy.java`) — the Strategy interface, one method: `calculateCost(Order order)`.
- `FlatRateShippingStrategy`, `WeightBasedShippingStrategy`, `DistanceBasedShippingStrategy`, `ThirdPartyApiShippingStrategy` (all in `solution/ShippingStrategy.java`) — Concrete Strategies, each one algorithm with its own config (`rate`, `ratePerKg`, `ratePerKm`, `baseFee`).
- `ShippingCostService` (`solution/ShippingCostService.java`) — the Context. Holds a `ShippingStrategy strategy` field, delegates via `strategy.calculateCost(order)`, exposes `setStrategy(...)` to swap it at runtime.
- `ECommerceAppV1` (`solution/ECommerceAppV1.java`) — the Client. Constructs concrete strategies, injects one into the context, swaps strategies mid-run.
- `Order` (`solution/Order.java`) — plain data holder (`totalWeight`, `destinationZone`, `orderValue`); intentionally has zero shipping logic of its own.

## Code walkthrough

The context never mentions a concrete strategy class:

```java
// ShippingCostService.java
public double calculateShippingCost(Order order) {
    return strategy.calculateCost(order);   // single dispatch call, no if-else
}
```

The client picks the object, and swapping behavior is just reassigning a field:

```java
// ECommerceAppV1.java
ShippingCostService shippingService = new ShippingCostService(flatRate);
shippingService.calculateShippingCost(order1);
shippingService.setStrategy(weightBased);   // swap algorithm, context code unchanged
shippingService.calculateShippingCost(order1);
```

The naive version re-decides "which branch" from a string on every call. Here, the decision is made once — when a strategy object is constructed and assigned — and every subsequent call is resolved by ordinary virtual dispatch (`strategy.calculateCost(order)` runs whichever `calculateCost` override belongs to `strategy`'s runtime type).

## When to use vs avoid

| Use it when | Avoid when |
|---|---|
| An if-else/switch chain picks behavior by type/enum/string and keeps growing (exactly `ShippingCostCalculatorNaive`'s shape) | Only 1-2 variants that will basically never grow — an interface adds ceremony for nothing |
| You need to pick or swap the algorithm at runtime (config, user tier, A/B test) | The "algorithms" don't share a meaningful contract — forcing an interface just to dodge an if-else is over-engineering |
| You want to unit-test each algorithm in isolation | The variation is in *object creation*, not *behavior* — that's Factory, not Strategy |

## Test yourself

<details>
<summary><strong>Q: How does Strategy achieve the Open/Closed Principle here?</strong></summary>

`ShippingCostService` depends only on the `ShippingStrategy` interface, never on concrete classes. Adding `FreeShippingStrategy` is one new class implementing the interface — zero changes to `ShippingCostService` or any existing strategy.

</details>

<details>
<summary><strong>Q: Why does `strategy.calculateCost(order)` replace a 5-branch if-else with one call?</strong></summary>

Polymorphism — every concrete strategy implements the same interface, so the JVM resolves the correct override based on `strategy`'s actual runtime type. The "branching" already happened once, when the strategy object was chosen and assigned, not on every call.

</details>

<details>
<summary><strong>Q: Why do the concrete strategies each hold their own config (`rate`, `ratePerKg`, ...) instead of putting it all on `Order`?</strong></summary>

That config is specific to *how one algorithm* computes cost, not a property of the order itself. Keeping it on the strategy keeps `Order` a plain data holder and keeps each algorithm's parameters encapsulated with the algorithm that uses them.

</details>

<details>
<summary><strong>Q: How would you pick a strategy dynamically instead of the client hardcoding `new FlatRateShippingStrategy(10.0)`?</strong></summary>

Add a factory/registry (e.g. `Map<String, ShippingStrategy>` or a `ShippingStrategyFactory`) that the client queries by order attributes — this is Strategy + Factory combined, common in real systems.

</details>

<details>
<summary><strong>Q: What's the tradeoff of Strategy vs just keeping the if-else?</strong></summary>

More classes and indirection for a small, stable number of cases; the if-else is genuinely simpler to read for 2 branches that won't grow. Strategy pays off once the number of variants or the churn on them grows.

</details>

## Common pitfalls

- Making the Context know about concrete strategy types (e.g. `instanceof` checks) — defeats the whole point; the Context should only ever call through the interface.
- Putting mutable shared state in a Strategy implementation and reusing the same instance across contexts/threads without thinking about it — strategies are often stateless by design for exactly this reason.
- Forgetting the "closed for modification" test: if adding a new variant still requires editing an existing class (besides the client wiring), it's not really Strategy yet.

## Related patterns

- **State vs Strategy** — nearly identical structure, but a State often transitions itself to the next State, while a Strategy is picked by the client and doesn't know about the others.
- **Template Method vs Strategy** — Template Method varies a step via inheritance/method-override; Strategy varies it via composition (a held object). Composition is generally preferred — it avoids fragile-base-class problems.
- **Factory vs Strategy** — Factory decides *which object to create*; Strategy decides *which behavior to run*. They compose well: a factory that returns the right `ShippingStrategy` for a given order.
