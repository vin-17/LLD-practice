# Factory Pattern

> ### At a glance
> **Category:** Creational
> **Recall phrase:** "Order by destination, not by vehicle."
> **In one line:** The client says *what* it needs (a mode string); the factory decides *which class* to instantiate for it.

**TL;DR:** Move the if/else that picks a concrete class out of the caller and into one dedicated method, so callers depend only on the interface plus a mode value. **Use when** object-creation logic branches on a type/mode and that branch would otherwise be duplicated across callers. **Skip when** only one implementation exists, or when what actually varies is *behavior*, not *which class to build* — that's Strategy.

## The problem it kills

`problem/LogisticsV1.java`'s `LogisticsService.send(mode)` embeds the decision directly: `if (mode.equals("Air")) { ... new Air() ... } else if (mode.equals("Road")) { ... new Road() ... }`. That's a straight Open/Closed Principle violation — adding a new mode (`"Ship"`, `"Drone"`) means reopening `send()` every time, and `LogisticsService` ends up **tightly coupled** to the concrete `Air`/`Road` classes instead of just the interface, which also makes it harder to test in isolation (you can't substitute a fake logistics implementation without going through the real classes). It's fragile in smaller ways too: `mode.equals("Air")` is case-sensitive and throws `NullPointerException` if `mode` is `null`, and an unrecognized mode falls through both branches and silently does nothing — no error, no feedback, just a no-op.

## Analogy

Ordering at a restaurant: you tell the waiter "the salmon," not which pan, which knife, or which prep station to use. The kitchen (factory) decides internally which station handles it and hands you the finished plate — you'd never expect to walk into the kitchen and pick the cookware yourself.

## The players

| Class / interface (real name from code) | Its one job |
|---|---|
| `LogisticsV2` | The Product interface — one method, `send()`. |
| `Road`, `Air` | Concrete Products — each implements `send()` with its own delivery logic. |
| `LogisticsFactory` | The Factory — static `getLogistics(String mode)` maps a mode string to a concrete product, and throws `IllegalArgumentException` for anything unrecognized. |
| `LogisticsService` | The client's business logic — asks the factory for a `LogisticsV2` and calls `.send()`; never names `Road` or `Air` directly. |
| `Main` | Driver — calls `service.send("Air")` / `service.send("Road")`. |

## How it actually flows

```java
// LogisticsFactory.java — the branch lives in exactly one place
public static LogisticsV2 getLogistics(String mode) {
    if (mode.equalsIgnoreCase("Air")) { return new Air(); }
    else if (mode.equalsIgnoreCase("Road")) { return new Road(); }
    throw new IllegalArgumentException("Unknown logistics mode: " + mode);
}

// LogisticsService.java — no concrete class name appears here at all
public void send(String mode) {
    LogisticsV2 logistics = LogisticsFactory.getLogistics(mode);
    logistics.send();
}
```

Compare that to the problem version, where `LogisticsService` itself contained the `new Air()` / `new Road()` branch. The solution also fixed the two silent bugs along the way: `equalsIgnoreCase` instead of `equals`, and a thrown exception instead of a silent no-op for an unknown mode — centralizing the decision made those edge cases obvious enough to actually handle.

## Use it vs avoid it

| Reach for it when | Don't bother when |
|---|---|
| Creation logic branches on a type/mode string, and more than one caller would otherwise duplicate that branch | Only one concrete implementation exists — there's nothing to select between yet |
| You want callers to depend only on the interface, never on concrete class names (`LogisticsService` never mentions `Road`/`Air`) | The varying part is *behavior for the same object*, not *which class to construct* — that's Strategy |
| You want the business-logic class to be testable/mockable without pulling in every concrete implementation | Construction needs caller-supplied arguments the factory can't know ahead of time — plain constructors are more direct |

## Test yourself

<details>
<summary><strong>Q: In `LogisticsV1`'s `LogisticsService.send(mode)`, what has to change every time a new mode (e.g. `"Sea"`) is added?</strong></summary>

`LogisticsService` itself — you'd add another `else if` branch and a `new Sea()` call directly inside the business-logic class, so the class that's just supposed to "send" also has to know about every shipping mode that exists.

</details>

<details>
<summary><strong>Q: What concrete class name (`Road` or `Air`) appears anywhere inside `LogisticsService` in the solution version?</strong></summary>

None — it references only the `LogisticsV2` interface and calls `LogisticsFactory.getLogistics(mode)`. `Road` and `Air` are never named inside it, which is exactly what makes adding a third mode a change to `LogisticsFactory` alone, and what makes `LogisticsService` easy to test with a fake implementation.

</details>

<details>
<summary><strong>Q: What breaks if `LogisticsFactory.getLogistics()` didn't throw on an unrecognized mode, and instead fell through and returned `null` like the problem version's silent no-op?</strong></summary>

`LogisticsService.send()` would call `.send()` on a `null` reference for any typo'd mode string, producing a `NullPointerException` at a call site far from where the actual typo happened. The explicit `IllegalArgumentException` fails fast, at the source of the bad input, with a message naming the invalid mode.

</details>

<details>
<summary><strong>Q: Is `LogisticsFactory` a textbook GoF Factory Method, or something simpler?</strong></summary>

It's the "Simple Factory" idiom — one static method with an if/else picking a concrete class. True Factory Method has an abstract creation step overridden by Creator subclasses, so a subclass decides what to instantiate via polymorphism rather than a string-keyed branch. People colloquially call both "the factory pattern," but this one is more precisely Simple Factory.

</details>

<details>
<summary><strong>Q: How is Factory different from Strategy, given both hide an if/else behind an interface?</strong></summary>

Factory decides *which object to construct* — the caller ends up holding a `Road` or `Air` instance and calls the same `send()` on it once. Strategy assumes you already have the object and instead swaps *which algorithm* runs for a method called repeatedly, typically via a `setStrategy(...)` call — see `strategypattern/README.md`'s shipping-cost example for the same distinction from the other side.

</details>

## Traps people fall into

- Putting real business logic inside the factory method itself instead of just construction — a factory should decide *which class*, not *what happens next*.
- Forgetting a default/error case in the mode branch — `LogisticsV1`'s version silently does nothing for an unrecognized mode instead of failing loudly, exactly the bug the solution's `IllegalArgumentException` fixes.
- Calling any static method that returns different subtypes "the Factory pattern" without checking whether it's actually Simple Factory, Factory Method, or Abstract Factory — interviewers do probe this distinction.

## Don't confuse it with

- **Simple Factory vs Factory Method** — Simple Factory (what `LogisticsFactory` is) is one static method with a branch; Factory Method makes construction a polymorphic step overridden by Creator subclasses, with no string branch at all.
- **Factory vs Abstract Factory** — Abstract Factory produces *families* of related objects per call (e.g. a `RoadKitFactory` making a matching shipper *and* invoice type), not just one product.
- **Factory vs Strategy** — construction vs behavior-swapping; see the test-yourself question above.
