# Observer Pattern

> ### At a glance
> **Category:** Behavioral
> **Recall phrase:** "Shout once, let anyone listen in."
> **In one line:** The subject loops over a dynamic observer list and calls the same `update()` on each, without knowing or caring who's on it.

**TL;DR:** Let a subject broadcast state changes to a dynamic list of observers it knows only through one interface, instead of hardcoding a call to each dependent class by name. **Use when** multiple, independently-changing consumers need to react to the same state change. **Skip when** there's exactly one fixed consumer — a direct method call is simpler than the registration machinery.

## The problem it kills

`problem/FitnessDataV1.java` hardcodes three consumer fields — `liveDisplay`, `progressLogger`, `notificationService` — directly on `FitnessDataV1`, and calls each by name inside `newFitnessDataPushed()`. Adding a fourth consumer means reopening `FitnessDataV1` itself to add another field and another manual call, and there's no way to add or remove a consumer at runtime — the set of listeners is frozen at compile time. `FitnessDataV1` also has to know the concrete `...Naive` class names, coupling it to every consumer's implementation instead of just an interface.

## Analogy

A stadium PA announcer: they don't walk over and personally tell every fan the score. They announce once, and anyone tuned in — fans, the scoreboard crew, the concession stands — reacts however they individually need to. A fan can put in earbuds (register) or take them out (unregister) mid-game without the announcer changing anything about how they announce, and the announcer never learns what any listener actually does with the information.

## The players

| Class / interface (real name from code) | Its one job |
|---|---|
| `FitnessDataObserver` | The Observer interface — one method, `update(FitnessData data)`. |
| `FitnessDataSubject` | The Subject interface — `registerObserver`, `removeObserver`, `notifyObservers`. |
| `FitnessData` | Concrete Subject — holds `List<FitnessDataObserver> observers`, calls `observer.update(this)` on each inside `notifyObservers()`. |
| `LiveActivityDisplay`, `ProgressLogger`, `GoalNotifier` | Concrete Observers — each reacts differently to the same push; `GoalNotifier` even carries its own state (`goalReached`) independent of the subject. |
| `FitnessAppClientV2` | Wires observers at runtime, including `removeObserver(logger)` mid-run — exactly the flexibility the naive version couldn't offer. |

## How it actually flows

```java
// FitnessData.java — the subject only ever talks to the interface
public void notifyObservers() {
    for (FitnessDataObserver observer : observers) {
        observer.update(this);
    }
}

// FitnessAppClientV2.java — observers come and go at runtime
fitnessData.registerObserver(logger);
...
fitnessData.removeObserver(logger);   // no change to FitnessData needed for this
```

This folder also has a second, independent example — `solution/StockTickerDemo.java` — showing the same shape (`StockExchange` as Subject, `PriceDisplay`/`AlertService`/`TradingBot` as Observers) applied to a totally different domain, worth noticing precisely *because* it's the same four moving pieces every time. It also has one refinement `FitnessData` doesn't: `StockExchange.notifyObservers()` iterates a defensive copy, `new ArrayList<>(observers)`, instead of the live list — see the trap below for why that matters.

## Use it vs avoid it

| Reach for it when | Don't bother when |
|---|---|
| Multiple, independently-changing consumers must react to the same state change (`FitnessData` feeds live display, logging, and goal-notification at once) | Exactly one fixed consumer will ever exist — a direct method call is simpler than interface + registration overhead |
| Consumers need to be added or removed without touching the subject's code | Observers need to send a computed result *back* to the caller — Observer's `void update()` is push-and-forget only |
| Observers shouldn't know about each other (`GoalNotifier` has no idea `ProgressLogger` exists) | You need a guaranteed notification order or transactional all-or-nothing delivery — plain Observer gives you neither |

## Test yourself

<details>
<summary><strong>Q: In `FitnessDataV1`, what would you need to change to add a 4th consumer, e.g. a `WeeklySummaryEmailer`?</strong></summary>

Add a new field for it inside `FitnessDataV1`, construct it, and add a new manual call inside `newFitnessDataPushed()`. Every new consumer means reopening and editing the subject class itself.

</details>

<details>
<summary><strong>Q: In `FitnessData.notifyObservers()`, what does the subject actually know about `LiveActivityDisplay`, `ProgressLogger`, or `GoalNotifier`?</strong></summary>

Nothing beyond the fact that each implements `FitnessDataObserver` and exposes `update(FitnessData data)`. It loops over `List<FitnessDataObserver> observers` and calls `update(this)`, with zero knowledge of what any concrete observer does with that call.

</details>

<details>
<summary><strong>Q: `StockExchange.notifyObservers()` iterates `new ArrayList<>(observers)`, a defensive copy, while `FitnessData.notifyObservers()` iterates the live list directly. What could go wrong with `FitnessData`'s version if an observer's `update()` called `removeObserver(this)` on itself mid-notification?</strong></summary>

Removing from a `List` while a `for`-each loop is actively iterating that same list throws a `ConcurrentModificationException`. `StockExchange` sidesteps this by notifying against a snapshot copy, so an observer can safely unregister itself during its own callback — `FitnessData` can't.

</details>

<details>
<summary><strong>Q: Why does `GoalNotifier` keep its own `goalReached` boolean instead of asking `FitnessData` whether the goal was already announced?</strong></summary>

State about *one observer's* reaction belongs on that observer, not on the shared subject. If `FitnessData` tracked it instead, the subject would have to carry bookkeeping for every observer's internal concerns — exactly the coupling the pattern exists to avoid.

</details>

<details>
<summary><strong>Q: Observer and Strategy both let a class hold a reference to an interface instead of a concrete type — how are they actually different?</strong></summary>

A subject holds a *list* of observers and calls the same one-way `update()` on all of them, broadcasting outward with no return value expected. A context holds *one* strategy at a time and calls it to get a computed result back. Observer is one-to-many notification; Strategy is one-to-one delegation for an answer.

</details>

## Traps people fall into

- Iterating the live observer list while an observer might mutate it (register/remove itself) during notification — `FitnessData.notifyObservers()` doesn't guard against this; `StockExchange.notifyObservers()` does, via a defensive copy.
- Putting per-observer state on the shared subject instead of the observer itself — see `GoalNotifier`'s `goalReached` question above.
- Assuming observers are notified in a guaranteed order, or that one failing observer won't block the rest — both implementations here loop synchronously, so the first exception an observer throws propagates and stops the remaining ones from being notified.

## Don't confuse it with

- **Observer vs Strategy** — one-to-many broadcast with no return value vs one-to-one delegation for a computed result.
- **Observer vs Pub/Sub** — classic Observer has the subject hold direct references to its observers (simple but coupled to the interface); Pub/Sub adds a broker/event bus between publishers and subscribers so neither side holds a reference to the other — what most real event-bus/message-queue systems actually use at scale.
- **Push vs pull observer style** — `FitnessData` is push-style: it hands the full `FitnessData` object straight to `update()`. A pull-style observer would only be told *something* changed and then call getters on the subject itself to fetch what it needs. Both terms come up in interviews, so know which one your code is doing.
