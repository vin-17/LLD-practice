# Singleton Pattern

> ### At a glance
> **Category:** Creational
> **Recall phrase:** "One instance, no back door."
> **In one line:** A private constructor plus a static accessor means the JVM can never hold two of this class at once.

**TL;DR:** Block the normal `new` route with a private constructor, and hand out the one allowed instance through a static `getInstance()` instead. **Use when** exactly one shared instance must exist app-wide (config, connection pool, logger). **Skip when** you actually want a testable, swappable dependency — that's what DI-managed "singleton scope" is for.

## The problem it kills

Without it, nothing stops two parts of the app from each doing `new Logger()` or `new ConnectionPool()` — you end up with multiple objects that were supposed to represent one shared thing, silently drifting out of sync or wasting resources. `Singleton.java` shows four attempts at closing that door, each trading off *when* the instance gets built against *how safely* it survives multiple threads calling `getInstance()` at once.

## Analogy

One shared office coffee machine: everyone who wants coffee goes through the same machine (`getInstance()`), nobody's allowed to bring their own pot in (private constructor). If ten people walk into an empty kitchen at the same instant and each starts wheeling in a new machine because they didn't check whether one already existed, you end up with ten "the one" machines — which defeats the entire point. That race is exactly what `LazySingleton` is exposed to and `DoubleCheckedSingleton` is built to prevent.

## The players

| Class (real name from code) | Its one job / tradeoff |
|---|---|
| `LazySingleton` | Builds the instance on first `getInstance()` call, no synchronization at all — the file literally comments `// not thread safe` above it. |
| `ThreadSafeSingleton` | Same lazy creation, but `getInstance()` is `synchronized` — correct under concurrency, but every call pays a lock-acquisition cost, even the millionth call after the instance already exists. |
| `DoubleCheckedSingleton` | Checks `instance == null` before *and* after entering a `synchronized` block, and marks the field `volatile` — locks only during the rare construction race, not on every call. |
| `EagerSingleton` | Builds the instance at class-load time via a `static final` field — thread safety comes free from the JVM's class-init guarantee, at the cost of always constructing it, used or not. |

## How it actually flows

The subtlest of the four is `DoubleCheckedSingleton` (lines 55-70). The comment above the field spells out *why* `volatile` is load-bearing:

```java
// volatile prevents reordering of instructions in: instance = new Singleton()
// since it involves three steps: allocate memory, call constructor, assign reference
private static volatile DoubleCheckedSingleton instance;

public static DoubleCheckedSingleton getInstance() {
    if (instance == null) {                                  // fast path, no lock
        synchronized (DoubleCheckedSingleton.class) {
            if (instance == null) {                           // re-check inside the lock
                instance = new DoubleCheckedSingleton();
            }
        }
    }
    return instance;
}
```

Without `volatile`, the JVM/CPU is free to reorder those three construction steps, so another thread could see a non-null `instance` pointing at an object whose constructor hasn't actually finished running. `volatile` forbids that reordering, so the outer null-check only ever sees "nothing" or "a fully built object" — never something in between.

Notice `EagerSingleton`'s field is `static final`, not `static volatile`: it's assigned once, right at declaration, so there's no lazy-assignment race to guard against and `final` is enough. `DoubleCheckedSingleton`'s field *can't* be `final` — it's deliberately assigned later, inside `getInstance()` — so it needs `volatile` instead, purely to guarantee visibility and ordering across threads.

## Use it vs avoid it

| Reach for it when | Don't bother when |
|---|---|
| You need exactly one shared instance with a single, well-known global access point (config, connection pool, cache) | You need the dependency to be mockable/swappable in tests — a hand-rolled static accessor makes that hard |
| Construction is cheap or the instance is guaranteed to be used, so `EagerSingleton`-style eager creation is fine | You need per-request or per-thread instances — that's `ThreadLocal` or scoped DI, not a Singleton |
| You control the class and want the JVM itself to enforce "only one" | A DI container already manages single-instance lifecycle for you — a manual Singleton class is redundant ceremony |

## Test yourself

<details>
<summary><strong>Q: Why is `LazySingleton` marked `// not thread safe` even though it does check `if (instance == null)` first?</strong></summary>

Two threads can both evaluate `instance == null` as true at the same moment, before either has assigned it — both then construct their own `new LazySingleton()`. Now there are two "the one" instances in memory, which is the exact failure the pattern exists to prevent.

</details>

<details>
<summary><strong>Q: What does `synchronized` buy `ThreadSafeSingleton` over `LazySingleton`, and what does it cost?</strong></summary>

It makes the race in `LazySingleton` impossible — only one thread can execute `getInstance()`'s body at a time, so two threads can never both pass the null check simultaneously. The cost is that *every* call acquires the lock, even the millionth call long after the instance already exists — wasted overhead on a hot path.

</details>

<details>
<summary><strong>Q: What breaks if you remove `volatile` from `DoubleCheckedSingleton`'s field but keep everything else the same?</strong></summary>

Constructing an object isn't one atomic step — it's allocate memory, run the constructor, then assign the reference — and without `volatile` those steps can be reordered. A second thread could see a non-null `instance` that's actually still mid-construction and start using a broken, half-initialized object. `volatile` is what removes that possibility.

</details>

<details>
<summary><strong>Q: How is `EagerSingleton` fundamentally different from the other three, and when would you actually prefer it?</strong></summary>

It builds the instance at class-load time via a `static final` field, leaning entirely on the JVM's guarantee that class initialization is thread-safe — there's no lazy check or lock anywhere in `getInstance()`. Prefer it when construction is cheap and the singleton is essentially always going to be used; avoid it when construction is expensive and the class might never actually be requested.

</details>

<details>
<summary><strong>Q: Interviewers often bring up the "Bill Pugh" initialization-on-demand holder idiom — how does it compare to `DoubleCheckedSingleton`?</strong></summary>

The holder idiom puts the instance in a private static inner class, so the JVM's own lazy class-loading plus its thread-safe class-init guarantee does the "double-checking" for you — `getInstance()` becomes just `return Holder.instance;`, no `volatile`, no `synchronized` block. Same laziness and thread safety as `DoubleCheckedSingleton`, with far less code that could be gotten subtly wrong.

</details>

## Traps people fall into

- Assuming any `getInstance()` with a null check is automatically "thread safe" — the race in `LazySingleton` is real and only shows up under actual concurrent load, not a quick single-threaded test.
- Forgetting `volatile` on a double-checked-locking field — the bug is invisible in most testing because reordering rarely surfaces on a given JVM/CPU in practice, so it ships silently.
- Reaching for Singleton to hold shared *mutable* state — it turns that state into a hidden global dependency, which makes unit tests order-dependent and hard to isolate. This is also why Singleton is sometimes accused of violating Single Responsibility: the class manages both its own business logic *and* its own lifecycle/access control.

## Don't confuse it with

- **Static utility class vs Singleton** — a static class can't implement interfaces, can't be subclassed or mocked, and can't be constructed lazily or conditionally; a Singleton is a real object (passed by reference, can implement interfaces) that just happens to have exactly one instance.
- **Singleton vs DI-managed "singleton scope"** — a DI container (e.g. Spring's default bean scope) gives you one instance per container without baking a private constructor and static accessor into the class itself, which keeps the class testable and swappable — most modern codebases prefer this over a hand-rolled Singleton.
- **Singleton vs Monostate** — Monostate lets `new` work normally but backs every instance with the same shared *static* state, so callers don't even know they're touching a singleton-like thing; classic Singleton blocks `new` outright via a private constructor.
