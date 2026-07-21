---
name: lld-interview-notes
description: Writes and updates README.md revision notes for folders in an LLD (low-level design) interview-prep repo — both design-pattern folders (singleton, strategy, observer, factory, etc.) and full LLD problem folders (tic-tac-toe, parking lot, elevator, etc.). Notes are built for memory retention, not documentation — a one-glance recall card up top, a vivid analogy, and self-test questions with hidden, click-to-reveal answers for active recall. Always use this skill when the user asks to add, write, update, or regenerate notes or README content for a pattern or LLD problem folder, says things like "document this", "add notes for X", "make revision notes", "quiz me on this", or shares code from such a folder and asks for interview notes. Don't write generic documentation for these folders without this skill — notes must follow this retention structure and reference the user's actual code and class names, not textbook examples.
---

# LLD interview notes writer

Generates and updates `README.md` revision notes for a single folder in the user's LLD interview-prep repo. These are **memory notes, not documentation**. The reader will skim them the night before an interview and again on the commute in — the entire format is engineered so that each pass strengthens recall instead of being a passive re-read. Anchor everything to the user's own code (real class names, real file paths), because people remember *their* code far better than a textbook's.

## Step 1 — Find the target folder and read its code

- If the user names a folder ("strategypattern", "tic-tac-toe"), read every source file inside it recursively, skipping `.git`, `node_modules`, `build`, `target`, and compiled artifacts (`.class`, `.o`).
- If the folder has `problem/` and `solution/` subfolders, read both — the notes should contrast the naive version against the fixed one.
- If a `README.md` already exists, read it first. If it has a `## My notes` (or similar personal) section, preserve that block **verbatim** and re-insert it at the end of the new file. Regenerate everything else from scratch rather than patching line by line.
- If the target folder is unclear, ask which one before doing anything else.

## Step 2 — Classify the folder

- **Design pattern** → Template A. Folder name matches a known GoF pattern (singleton, strategy, observer, factory, decorator, adapter, etc.); usually a small set of classes illustrating one idea.
- **Full LLD problem** → Template B. Folder name is a system (tic-tac-toe, parking lot, elevator, splitwise, snake-and-ladder); usually several classes/enums plus a `Main` driver.

Judgment call: `Game` + `Board` + `Player` + `Main.java` is a full problem (Template B) even if it uses a pattern internally. A folder literally named after the pattern it teaches is Template A.

## Step 3 — The seven retention levers

Every design choice in the templates traces back to one of these. When filling a template, consciously pull each lever — don't just fill blanks.

1. **Active recall > re-reading.** This is the big one. Every self-test question hides its answer behind a `<details>` toggle so the reader is forced to *attempt* recall before revealing. Retrieval practice is the most evidence-backed memory technique there is. A note the reader can passively skim teaches far less than one that makes them stop and try to answer.
2. **One-glance recall card.** The top of every note is a tight block (category, recall phrase, one-line essence) that *is* the whole topic compressed. On the 5th review the reader should be able to read only this and reconstruct the rest mentally.
3. **A vivid, slightly weird analogy.** Bland comparisons evaporate. Reach past the first obvious one to something specific and a little odd — oddness is what makes it stick.
4. **Contrast and boundaries.** "Use when / skip when", "X not Y", pattern-vs-pattern. The brain encodes distinctions more durably than flat definitions. Every note should sharpen at least one boundary.
5. **Elaboration — always the "why".** Never state a design decision without the reason and the cost of the alternative. Facts tied to a rationale are recalled; orphan facts are forgotten.
6. **Small chunks.** Short lines and bullets over paragraphs. Working memory holds ~4 short items far better than one dense block. If a section runs long, it's doing too much.
7. **Anchor to real code.** Reference the user's actual class and method names throughout. Self-reference is a genuine memory boost — their `PaymentContext` is stickier than a generic `Context`.

Fill every section from the actual code. Skip a section only if genuinely N/A — never pad.

### Template A — Design pattern

```markdown
# {Pattern name}

> ### At a glance
> **Category:** {Creational / Structural / Behavioral}
> **Recall phrase:** "{a punchy, contrast-shaped mnemonic with its own hook — rhythm, opposition, or a vivid verb pair. e.g. "Vary the behavior, not the object." NOT just the TL;DR with fewer words.}"
> **In one line:** {the single sentence you'd want to survive if everything else were erased}

**TL;DR:** {2 sentences max — what it does and why it exists}. **Use when** {one line}. **Skip when** {one line}.

## The problem it kills
{2-4 plain-English sentences: what actually goes wrong in code without this pattern. Name the specific pain — the growing if-else ladder, the duplicated logic, the class you have to reopen every time requirements change.}

## Analogy
{One concrete, vivid, slightly unusual real-world comparison. Specific enough to picture in one frame. Not a restatement of the problem in different words.}

## The players
| Class / interface (real name from code) | Its one job |
|---|---|
| `{RealName}` | {one line} |
| `{RealName}` | {one line} |

## How it actually flows
{Walk the real code path using real names: "In `PaymentStrategy.java` the interface exposes `pay(amount)`. `CardStrategy` and `UpiStrategy` each implement it, and `PaymentContext` holds one reference and delegates to it — swapping strategies at runtime means calling `setStrategy(...)`, no branching." A 3-6 line snippet is fine to anchor the one key trick; don't paste whole files — cite them by name.}

## Use it vs avoid it
| Reach for it when | Don't bother when |
|---|---|
| {case} | {case} |
| {case} | {case} |

## Test yourself
{3-5 questions, hidden answers. Order them easy → hard. At least one must be a "how is this different from {related pattern}" question and at least one a "what breaks if you remove {key piece}" question — those two framings are the most commonly asked and the most revealing.}

<details>
<summary><strong>Q: {question}</strong></summary>

{2-3 sentence answer — the length you'd actually say out loud}

</details>

<details>
<summary><strong>Q: {question}</strong></summary>

{answer}

</details>

## Traps people fall into
- {a real mistake made when applying this pattern — one line}
- {another}

## Don't confuse it with
- **{Pattern} vs {this}** — {the single distinction that actually separates them}
```

### Template B — Full LLD problem

```markdown
# {Problem name} — LLD

> ### At a glance
> **Core idea:** {the one sentence that captures the whole design}
> **Central abstraction:** {the class or two everything revolves around, and why}

**TL;DR:** {2 sentences on what the system does and how the pieces fit}.

## Requirements
**Functional** (what it does)
- {bullets, pulled from what the code actually supports}

**Non-functional** (only if the code addresses it)
- {extensibility / concurrency / scale — skip entirely if not applicable}

## The classes — cover the right column and test yourself
| Class / enum (real name) | Responsibility |
|---|---|
| `{RealName}` | {one line} |
| `{RealName}` | {one line} |

{Note to reader can be implicit: this table doubles as a flashcard — read a class name, recall its job before reading across.}

## Design decisions (the "why", not just the "what")
- **{decision}** — {why this way, and what the alternative would have cost. e.g. "`Board` stores state as an enum grid, not a bitmask — readability wins at 3x3 scale, and bitmask cleverness would only pay off at much larger boards."}
- **{decision}** — {why + cost of alternative}

## Extensibility — the #1 follow-up
{Interviewers almost always ask "now add X". Answer preemptively for the likely asks (a 4th player, a bigger board, a new move type, undo): what changes, what stays untouched, and which class absorbs the change. If the design makes an extension painful, say so honestly — knowing the weakness is itself interview gold.}

## Edge cases the code handles
- {from the actual code — draw detection, invalid move, full board, etc.}

## Test yourself
{3-5 questions, hidden answers, easy → hard. Include at least one "how would you make this {thread-safe / scale / support feature X}" question — that's the standard LLD escalation.}

<details>
<summary><strong>Q: {question}</strong></summary>

{answer}

</details>

<details>
<summary><strong>Q: {question}</strong></summary>

{answer}

</details>

## Reuses ideas from
- **{other LLD problem}** — {what genuinely transfers between them}
```

## Writing style rules (both templates)

- Write for someone skimming at 11pm before an interview. Short sentences. Zero throat-clearing, zero "in this document we will".
- **Recall phrase** is a mnemonic, not a summary. If it reads like a shortened TL;DR, rewrite it. Good: "Vary the behavior, not the object." Bad: "Swap the algorithm, not the class" (that's just the TL;DR compressed — no hook).
- **Analogy** must be specific and a little unusual — never the first generic comparison.
- **Every design decision carries its "why" and the cost of the alternative.** A decision stated without rationale is a decision the reader won't remember or be able to defend.
- **Every self-test question lives in a `<details><summary>` block** — bold question in the summary, answer in the body. Never put an answer in plain text beside its question; that defeats active recall. Order questions easy → hard so early wins build momentum.
- **Critical rendering rule:** leave one blank line immediately after `</summary>` and immediately before `</details>`. GitHub's markdown renderer needs those blank lines to render bold text, bullets, or code inside the block — omit them and the answer renders as broken raw text.
- Tables only for genuine contrasts or flashcard-style lists (players, use-vs-avoid, class responsibilities) — not as a prose substitute everywhere.
- Every claim traces to real code in the folder. Never invent rationale the code doesn't support; if a design choice looks accidental rather than deliberate, don't dress it up as intentional.
- Answers are interview-length: 2-3 sentences you could say out loud, not essays.
- The "At a glance" card must stand fully alone — if the reader reads only that block, it should still trigger recall of the rest.

## Step 4 — Save it

Write to `README.md` inside the target folder, overwriting the old one except for any preserved `## My notes` section (Step 1). Then give the user a short summary of what the note covers and how many self-test questions it has — don't paste the whole file back into chat, since they can open it directly. If useful, remind them the hidden answers render as click-to-reveal on GitHub and in VS Code's markdown preview.