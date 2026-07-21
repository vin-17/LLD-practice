# Strategy Pattern

> ### At a glance
> **Category:** Behavioral
> **Recall phrase:** "Swap the plug, not the socket."
> **In one line:** The Context always calls the same interface method; only the object plugged into it changes.

**TL;DR:** Pull the part of a class that varies (an algorithm) into its own interface, and let the class hold a reference it delegates to instead of branching on a type/string. Adding a new variant becomes a new class, not an edit to existing code. **Use when** multiple interchangeable algorithms exist for the same job. **Skip when** there are only 1-2 variants that will never grow.

## The problem it kills

`problem/ECommerceAppV1.java` computes shipping cost with `ShippingCostCalculatorNaive.calculateShippingCost(order, strategyType)` — a single method with an if/else-if chain keyed on a `strategyType` string (`"FLAT_RATE"`, `"WEIGHT_BASED"`, `"DISTANCE_BASED"`, `"THIRD_PARTY_API"`). Every new shipping option means reopening this same method — the file even leaves a comment admitting it: *"What if we want to try a new 'PremiumZone' strategy? We have to go modify this calculator class again."* It's stringly-typed (a typo in `strategyType` fails at runtime, not compile time), and no single algorithm can be unit-tested without driving the whole method.

## Analogy

A universal travel charger: the brick (context) always calls the same `deliverPower()` contract on whatever's clicked into its base. Click in the US adapter, the UK adapter, or the EU adapter (concrete strategies) depending on which country's socket you're facing, and the brick's internals never get opened up or rewired. Buy a new adapter for a new country and nothing about the existing charger changes.

## The players

| Class / interface (real name from code) | Its one job |
|---|---|
| `ShippingStrategy` (`solution/ShippingStrategy.java`) | The Strategy interface — one method, `calculateCost(Order order)`. |
| `FlatRateShippingStrategy`, `WeightBasedShippingStrategy`, `DistanceBasedShippingStrategy`, `ThirdPartyApiShippingStrategy` (`solution/ShippingStrategy.java`) | Concrete Strategies — each one algorithm, each owning its own config (`rate`, `ratePerKg`, `ratePerKm`, `baseFee`). |
| `ShippingCostService` (`solution/ShippingCostService.java`) | The Context — holds a `ShippingStrategy strategy` field, delegates via `strategy.calculateCost(order)`, exposes `setStrategy(...)` to swap at runtime. |
| `ECommerceAppV1` (`solution/ECommerceAppV1.java`) | The Client — constructs concrete strategies, injects one into the context, swaps mid-run. |
| `Order` (`solution/Order.java`) | Plain data holder (`totalWeight`, `destinationZone`, `orderValue`) — deliberately has zero shipping logic. |

## How it actually flows

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

The naive version re-decides "which branch" from a string on every call. Here the decision is made once — when a strategy object is constructed and assigned — and every later call is resolved by ordinary virtual dispatch: `strategy.calculateCost(order)` runs whichever override belongs to `strategy`'s runtime type.

## Use it vs avoid it

| Reach for it when | Don't bother when |
|---|---|
| An if-else/switch chain picks behavior by type/enum/string and keeps growing — exactly `ShippingCostCalculatorNaive`'s shape | Only 1-2 variants that will basically never grow — an interface adds ceremony for nothing |
| You need to pick or swap the algorithm at runtime (config, user tier, A/B test) | The "algorithms" don't share a meaningful contract — forcing an interface just to dodge an if-else is over-engineering |
| You want to unit-test each algorithm in isolation | The variation is in *object creation*, not *behavior* — that's Factory, not Strategy |

## Test yourself

<details>
<summary><strong>Q: In `ShippingCostService`, what type is the `strategy` field declared as — the interface or a concrete class? Why does that matter?</strong></summary>

The interface (`ShippingStrategy`), never a concrete class. That's what makes the field swappable: the context can hold *any* current or future implementation without knowing its concrete type — program to an interface, not an implementation.

</details>

<details>
<summary><strong>Q: How does adding a `FreeShippingStrategy` avoid touching `ShippingCostService`?</strong></summary>

`ShippingCostService` only ever calls `strategy.calculateCost(order)` through the interface. A new strategy is one new class implementing `ShippingStrategy`, wired in at the client via `setStrategy(...)` — zero edits to `ShippingCostService` or any existing strategy. That's Open/Closed in action.

</details>

<details>
<summary><strong>Q: What breaks if you remove the `ShippingStrategy` interface and have `ShippingCostService` hold a `FlatRateShippingStrategy` field directly?</strong></summary>

`setStrategy(weightBased)` would no longer compile — the field's type would only accept `FlatRateShippingStrategy` instances. You'd be back to either one hardcoded algorithm or an if-else picking between concrete types inside the context, which is exactly the naive version's problem relocated one level up.

</details>

<details>
<summary><strong>Q: Strategy and State have nearly identical class diagrams. How are they actually different?</strong></summary>

Who decides the current object. In Strategy, the *client* picks the strategy and strategies are typically unaware of each other (`ECommerceAppV1` chooses `flatRate` or `weightBased`). In State, the *state object itself* often triggers the transition to the next state as part of handling a request — the states know about and hand off to each other.

</details>

<details>
<summary><strong>Q: What's the tradeoff of introducing Strategy here vs just keeping the if-else chain?</strong></summary>

More classes and a layer of indirection for what might be a small, stable number of cases — for 2 branches that will never grow, the if-else is genuinely easier to read. Strategy pays for itself once the number of variants, or the rate of change on them, actually grows.

</details>

## Traps people fall into

- Letting the Context learn concrete strategy types (`instanceof` checks) — defeats the whole point; it should only ever call through the interface.
- Reusing one Strategy instance with mutable state across contexts/threads without thinking about it — strategies are usually designed stateless for exactly this reason.
- Forgetting the "closed for modification" test: if adding a new variant still means editing an existing class (beyond client wiring), it isn't really Strategy yet.

## Don't confuse it with

- **State vs Strategy** — same shape, but a State often transitions itself to the next State, while a Strategy is picked by the client and stays unaware of its siblings.
- **Template Method vs Strategy** — Template Method varies a step via inheritance/override; Strategy varies it via composition (a held object). Composition is generally favored — it sidesteps fragile-base-class issues.
- **Factory vs Strategy** — Factory decides *which object to create*; Strategy decides *which behavior to run*. They compose well together: a factory that returns the right `ShippingStrategy` for a given order.
