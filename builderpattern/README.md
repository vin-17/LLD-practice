# Builder Pattern

> ### At a glance
> **Category:** Creational
> **Recall phrase:** "Assemble first, freeze after."
> **In one line:** A mutable builder collects optional pieces one fluent call at a time, then hands them to a private constructor that locks them into an immutable product.

**TL;DR:** Replace a constructor that tries to take every possible parameter with a builder object that only collects the fields a caller actually sets, one chained call at a time. **Use when** an object has several optional fields and must end up immutable. **Skip when** most fields are required or there are only a couple of them — a plain constructor is simpler.

## The problem it kills

`problem/BurgerMealV1.java`'s `BurgerMeal` has one constructor taking every field — `bun, patty, sides, toppings, cheese` — so a caller who only wants the required parts still has to pass `null, null, false` positionally: `new BurgerMeal("wheat", "veg", null, null, false)`. Nothing about that call site says *which* optional fields were skipped versus deliberately set. The file's own commented-out alternative (lines 26-32) shows where this leads if you try to "fix" it with overloads instead — a `BurgerMeal` constructor per combination of optional args, which explodes combinatorially and still doesn't self-document.

## Analogy

A custom PC build at a computer store: you don't hand the cashier one giant order form up front. You walk the counter picking a CPU and motherboard (required), then optionally add RAM, then optionally a GPU, then optionally RGB fans — and only when you say "that's everything" does the store lock the spec and assemble the exact machine you configured. Walk away mid-build and nothing has been soldered together yet.

## The players

| Class (real name from code) | Its one job |
|---|---|
| `BurgerMeal` (`problem/BurgerMealV1.java`) | The naive product — one constructor for every field, optional ones passed as `null`/`false` when unused. |
| `BurgerMealV2` (`solution/BurgerMealV2.java`) | The Product — `final` fields, a **private** constructor that only `BurgerBuilder` can call. |
| `BurgerMealV2.BurgerBuilder` (static nested class) | The Builder — takes required fields (`bunType`, `patty`) in its own constructor, exposes fluent `withCheese`/`withToppings`/`withSide`/`withDrink`, each returning `this`. |
| `Main` (`solution/BurgerMealV2.java`) | The client — and since this is fluent-builder style, it also plays the Director's role by chaining calls in whatever order it needs; no separate Director class exists. |

## How it actually flows

```java
// BurgerBuilder — required fields up front, optional ones fluent
public BurgerBuilder(String bunType, String patty) { this.bunType = bunType; this.patty = patty; }
public BurgerBuilder withCheese(boolean hasCheese) { this.hasCheese = hasCheese; return this; }
public BurgerMealV2 build() { return new BurgerMealV2(this); }   // hands itself to the private ctor
```

The client chains exactly the calls it needs, nothing more:

```java
BurgerMealV2 plainBurger = new BurgerMealV2.BurgerBuilder("wheat", "veg").build();

BurgerMealV2 loadedBurger = new BurgerMealV2.BurgerBuilder("multigrain", "chicken")
                                .withCheese(true)
                                .withToppings(toppings)
                                .withSide("fries")
                                .withDrink("coke")
                                .build();
```

`BurgerMealV2`'s constructor is private and only reads from a `BurgerBuilder`, so there is no path to a `BurgerMealV2` instance that skipped configuration — `build()` is the sole gate. The same shape shows up outside this repo as Lombok's `@Builder` annotation (which generates exactly this boilerplate for you) and in real systems like Amazon's cart, where quantity, size, gift-wrap, and delivery option are each set incrementally before checkout "builds" the final order.

## Use it vs avoid it

| Reach for it when | Don't bother when |
|---|---|
| Several optional fields exist and most callers only need a subset — `BurgerMealV2` has 2 required vs 4 optional | Most fields are required, or there are only 1-2 total — a plain constructor is less ceremony than a nested builder class |
| The product should end up immutable but construction still needs to be flexible | The object needs to keep changing after creation anyway — immutability isn't the goal, so a simple setter-based bean is fine |
| You want each call site to self-document (`withCheese(true)` vs a mystery positional `false`) | You're only ever constructing the object one fixed way — there's nothing to configure |

## Test yourself

<details>
<summary><strong>Q: In `BurgerMealV1`, what does `new BurgerMeal("wheat", "veg", null, null, false)` actually tell a reader about which optional fields were intentionally skipped?</strong></summary>

Almost nothing without opening the constructor signature — the `null, null, false` are only interpretable by position. A caller could swap two of them by mistake and the code would still compile.

</details>

<details>
<summary><strong>Q: Why is `BurgerMealV2`'s constructor private, and what actually enforces that every instance went through `BurgerBuilder`?</strong></summary>

Because it's private, the only code allowed to call `new BurgerMealV2(builder)` is code inside `BurgerMealV2` itself — which is exactly `BurgerBuilder.build()`. There's no other path to construct one, so every instance is guaranteed to have gone through the builder's fluent configuration.

</details>

<details>
<summary><strong>Q: What breaks if `withCheese()` returned `void` instead of `BurgerBuilder`?</strong></summary>

The fluent chain in `Main` — `new BurgerMealV2.BurgerBuilder(...).withCheese(true).withToppings(...).build()` — stops compiling right after the `void` call, because there's nothing to call `.withToppings(...)` on anymore. Every builder step has to return `this` for chaining to keep working.

</details>

<details>
<summary><strong>Q: Why doesn't this codebase have a separate `Director` class?</strong></summary>

It uses fluent-builder style, where the client chains builder calls directly in whatever order it needs — `Main` effectively plays the Director's role itself. A dedicated `Director` only earns its keep when you want to encapsulate a *reusable, standard* sequence (e.g. a `standardMeal()` method) so callers stop repeating the same chain everywhere.

</details>

<details>
<summary><strong>Q: How is Builder different from just using a mutable object with setters (JavaBean style)?</strong></summary>

A bean stays mutable forever — any caller with a reference can call a setter at any time. `BurgerMealV2` becomes immutable the instant `build()` runs, since its fields are `final` and set exactly once via the private constructor. Builder can also enforce required fields up front — `BurgerBuilder`'s own constructor demands `bunType` and `patty` — while a bean has no such gate.

</details>

## Traps people fall into

- Forgetting to `return this;` from a builder setter — silently breaks fluent chaining the moment someone tries to call another method on the result.
- Giving the Product public setters or non-`final` fields after construction — defeats the immutability the pattern is usually adopted for in the first place.
- Treating the commented-out telescoping-constructor block in `BurgerMealV1.java` (lines 26-32) as a real alternative rather than the exact anti-pattern Builder replaces.

## Don't confuse it with

- **Builder vs telescoping constructors** — telescoping adds one overloaded constructor per combination of optional args, which explodes combinatorially; Builder uses one object with named fluent methods instead.
- **Builder vs Factory** — Factory decides *which class* to instantiate; Builder decides *how to assemble* one already-known, complex object step by step. They combine well: a factory that hands back a pre-configured builder.
- **Builder vs Prototype** — Prototype creates a new object by cloning an existing configured instance; Builder creates a new object by assembling it field-by-field from scratch every time.
