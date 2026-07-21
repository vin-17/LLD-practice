---
name: lld-interview-notes
description: Writes and updates README.md revision notes for folders in an LLD (low-level design) interview-prep repo — both design-pattern folders (singleton, strategy, observer, factory, etc.) and full LLD problem folders (tic-tac-toe, parking lot, elevator, etc.). Notes are optimized for retention — recall phrase and category tag up top, a vivid analogy, and self-test questions with hidden, collapsible answers. Always use this skill when the user asks to add, write, update, or regenerate notes or README content for a pattern or LLD problem folder, says things like "document this", "add notes for X", "quiz me on this", or shares code from such a folder and asks for interview revision notes. Don't write generic documentation for these folders without this skill — notes must follow this structure and reference the user's actual code and class names, not generic examples.
---

# LLD interview notes writer

Generates and updates README.md revision notes for a single folder in the user's LLD interview-prep repo. The notes are written for spaced-repetition style review the night before an interview, not as general documentation — dense, skimmable, anchored to the user's own code, and structured so the reader has to actively try to recall an answer before seeing it, not just re-read passively.

## Step 1 — Find the target folder and read its code

- If the user names a folder ("strategypattern", "tic-tac-toe"), read every source file inside it (recursively, skipping `.git`, `node_modules`, `build`, `target`, compiled `.class`/`.o` files).
- If the folder has `problem/` and `solution/` subfolders, read both — the notes should contrast them.
- If a `README.md` already exists in the folder, read it first. If it contains a `## My notes` (or similarly named personal) section, preserve that section's content verbatim and re-insert it at the end of the new README. Everything else gets regenerated from scratch — don't try to patch the old version line by line.
- If the folder can't be determined from context, ask which folder before doing anything else.

## Step 2 — Classify the folder

- **Design pattern** — folder name matches a known GoF/behavioral/structural/creational pattern (singleton, strategy, observer, factory, decorator, etc.), usually with a small, focused set of classes illustrating one idea.
- **Full LLD problem** — folder name is a system/problem (tic-tac-toe, parking lot, elevator, splitwise, snake-and-ladder, etc.), usually with several classes/enums working together plus a `Main` driver.

Use judgment — a few classes named `Game`, `Board`, `Player` plus a `Main.java` is a full problem even if it happens to *use* a pattern internally.

## Step 3 — Write the notes

### Retention principles (why the templates look the way they do)

- **Active recall beats re-reading.** Every interview question hides its answer behind a `<details>` toggle (GitHub and most markdown viewers render this as a click-to-reveal section). The reader has to attempt an answer before seeing it — this is the single most effective memory technique there is, and it's the whole reason this format exists. It only visually collapses in renderers that support HTML in markdown (GitHub, VS Code preview); in a plain text editor it just shows as tags, which is still fine — the point is that the answer isn't sitting in the reader's eyeline.
- **Contrast sticks better than flat description.** "Use when / skip when", pattern-vs-pattern, "X not Y" — the brain remembers boundaries and distinctions more easily than plain definitions.
- **A vivid, slightly unusual analogy beats a generic one.** A bland analogy ("it's like a strategy for a game") is forgettable; a specific, slightly odd one is not. Reach past the first comparison that comes to mind.
- **Small chunks, not paragraphs.** Bullets and short lines over dense prose — working memory holds a handful of short items far better than one long block.

Pick the template below that matches the folder's classification. Fill every section using the actual code — reference real class names, method names, and file paths, not generic placeholders. Skip a section only if it's genuinely not applicable; don't pad with filler.

### Template A — Design pattern

```markdown
# {Pattern name}

**Category:** {Creational / Structural / Behavioral}
**Recall phrase:** "{a short, punchy, contrast-shaped mnemonic — e.g. "Vary the behavior, not the object." Must have its own hook (contrast, rhythm, or a vivid verb pairing) — not just a shorter rewording of the TL;DR.}"

> **TL;DR:** {one sentence — what it does and why it exists}. Use it when {one line}. Skip it when {one line}.

## The problem
{2-4 sentences, plain english, no jargon: what goes wrong without this pattern}

## Analogy
{one concrete, vivid, slightly unusual real-world comparison — specific enough to picture clearly, not a generic restatement of the problem}

## Structure
- `{ClassOrInterfaceName}` — {one-line role}
- `{ClassOrInterfaceName}` — {one-line role}
(list every key class/interface actually in the code, using this repo's actual names)

## Code walkthrough
{Walk through the actual flow using the real file(s), e.g. "In `PaymentStrategy.java`, the interface exposes `pay(amount)`. `CardStrategy` and `UpiStrategy` implement it independently, and `PaymentContext` holds a reference it delegates to." Reference real method names. A short snippet (3-6 lines) is fine to anchor a key trick; don't paste the whole file — refer to it by name instead.}

## When to use vs avoid
| Use it when | Avoid when |
|---|---|
| {case} | {case} |

## Test yourself
<details>
<summary><strong>Q: {common question}</strong></summary>

{2-3 sentence model answer}

</details>

<details>
<summary><strong>Q: {common question}</strong></summary>

{2-3 sentence model answer}

</details>

(3-5 questions total — favor ones that actually get asked: "how is this different from X", "what breaks if you skip the interface", "where have you seen this in a real framework". Each question gets its own `<details>` block — never write the answer in plain text next to the question.)

## Common pitfalls
- {mistake people make with this pattern, one line each}

## Related patterns
- {Pattern} vs {this pattern} — {the one-line distinction that actually matters}
```

### Template B — Full LLD problem

```markdown
# {Problem name} — LLD

> **TL;DR:** {one sentence describing the system}. Core idea: {one line}.

## Requirements
**Functional**
- {bullet list, pulled from what the code actually supports}

**Non-functional / constraints** (if applicable)
- {e.g. extensibility, concurrency, scale — only if the code actually addresses it}

## Core classes
- `{ClassName}` — {responsibility, one line}
- `{ClassName}` — {responsibility, one line}
(every class/enum/interface that matters — this is the part interviewers actually probe)

## Key design decisions
- {decision} — {why, and what the alternative would have cost, e.g. "Board stores state as an enum grid, not a bitmask, for readability at this scale."}

## Extensibility
{If asked to add a feature (e.g. a 4th player, a bigger board, a new piece type), what would change and what wouldn't? This is the single most common interview follow-up for LLD problems — always answer it explicitly.}

## Edge cases handled
- {bullet list, from the actual code — e.g. draw detection, invalid move, full board}

## Test yourself
<details>
<summary><strong>Q: {common follow-up, e.g. "how would you make this thread-safe?"}</strong></summary>

{short model answer}

</details>

(3-5 questions total, same hidden-answer format as Template A)

## Related problems
- {other LLD problem} — {what's genuinely reusable between them}
```

## Writing style rules (apply to both templates)

- Write for someone skimming this at 11pm the night before an interview, not someone reading documentation for the first time. Short sentences. No throat-clearing.
- **Bold** the term being defined the first time it's used, not repeatedly after.
- The **recall phrase** (Template A) is a mnemonic, not a summary. Test: if it reads like a compressed version of the TL;DR, rewrite it. Good: "Vary the behavior, not the object." Bad: "Swap the algorithm, not the class." (the bad version is just the TL;DR with fewer words — no independent hook).
- The **analogy** should be specific and a little unusual, not the first generic comparison that comes to mind.
- Every interview question goes inside a `<details><summary>` block: bold question in the summary, answer in the body. Leave a blank line right after `<summary>...</summary>` and right before `</details>` — GitHub's renderer needs that blank line to correctly render bullet lists or bold text inside the block; skip it and the answer can render as raw, broken text.
- Every claim should be traceable to actual code in the folder — don't invent design rationale the code doesn't support.
- Use tables only for genuine contrasts (use vs avoid, pattern vs pattern) — not as a substitute for prose everywhere.
- Interview answers should be the length the user would actually say out loud in an interview (2-3 sentences), not an essay.
- The TL;DR at the top must stand alone — if the user reads nothing else, that line should still be enough to jog full recall of the rest.

## Step 4 — Save it

Write the result to `README.md` inside the target folder, overwriting the old one except for the preserved `## My notes` section (see Step 1). Show the user a short summary of what changed rather than pasting the whole file back into chat, since they can just open the file directly.