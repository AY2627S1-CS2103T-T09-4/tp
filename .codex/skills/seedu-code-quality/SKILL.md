---
name: seedu-code-quality
description: "Review and write project code using the CS2103/T code-quality rules for readability, naming, safe constructs, and maintainability."
---

# SE-EDU Code Quality

Code quality primarily supports maintainability: readable code is easier for
other programmers, and your future self, to understand and modify safely.
Apply this skill when writing or reviewing project code, including tests and
support code.

## Authority and interpretation

The five **Source-backed** sections below adapt the official
[Code Quality chapter](https://nus-cs2103-ay2627-s1.github.io/website/se-book-adapted/chapters/codeQuality.html)
to this Java project. They cover its basic, intermediate, and advanced techniques.
Requirements retain their force; recommendations require judgment, and stated
exceptions remain available. Do not treat every recommendation or review trigger
as an unconditional violation. Evaluate whether an exception preserves clarity
and correctness, and record a concrete rationale when it matters to the review.

Additional conventions and workflow appear separately under **Project-specific**.
For detailed Java syntax, formatting, naming forms, and Javadoc rules, use the
companion [seedu-java-coding-standard](../seedu-java-coding-standard/SKILL.md)
skill rather than duplicating its basic and intermediate rules here.

## Source-backed: Maximize readability

### Basic

- **Method length:** Avoid long methods. **More than 30 lines** triggers review
  of whether shortening or splitting by responsibility would help. It is a
  strong guideline, not a mechanical failure; retain a longer method when a
  split would harm clarity or correctness and explain the justification.
- **Nesting:** Avoid deep, arrowhead-shaped control flow. Aim for **three levels
  of nesting or fewer** where practical; **more than three indentation levels**
  triggers review, not automatic rejection. Simplify when it improves clarity.
- **Expressions:** Break complicated expressions into meaningful intermediate
  variables and evaluate them in steps, especially when excessive negations or
  nested parentheses obscure the logic.
- **Magic literals:** Replace unexplained numbers, strings, and other literal
  values with meaningful named constants. A literal whose meaning is obvious
  in context need not be extracted; an index or loop bound can still be magic.
- **Explicit intent:** Make code obvious with explicit Java type conversions
  and grouping using braces or parentheses, even where syntax permits omission.
  Use enums for small finite sets of states instead of unexplained numeric or
  string codes. Follow the companion skill's detailed Java layout rules.

### Intermediate

- **Logical structure:** Make code read like a story. Use classes, methods,
  indentation, and blank lines to group related statements. Where correctness
  permits different operation orders, choose the clearest sequence.
- **Reader surprises:** Remove unnecessary unused parameters; keep equivalent
  things looking consistent and distinct things visibly different. Avoid
  multiple statements on one line and data-flow anomalies, such as assigning
  a value and changing it before that assigned value is used.
- **KISS:** Prefer the simplest correct solution. Demand strong justification
  for clever complexity or premature abstractions, including supposed future
  reuse; a straightforward brute-force solution may be the better choice.
- **Optimization:** Avoid premature optimization. Follow **make it work, make
  it right, make it fast**: normally prioritize correctness and readability.
  Ideally profile first to find real bottlenecks; compilation, minification,
  transpilation, or other transformations can conceal the actual cost.
  Target the bottlenecks identified by profiling rather than optimizing broadly.
  Hand optimization can complicate code and make it harder for the compiler
  to optimize. Optimize when profiling or concrete requirements justify it;
  resource-constrained systems can legitimately give optimization priority.
- **SLAP:** Keep each code fragment or method at one level of abstraction
  (Single Level of Abstraction Principle), using the highest reasonable level.
  Extract lower-level details into meaningful operations when helpful. Mixed
  levels can be justified if readability remains clear: mark higher-level
  steps with useful comments and separate adjacent steps with blank lines.

### Advanced

- **Happy path:** Keep successful execution prominent and as lightly nested as
  practical. Handle unusual and error cases near their detection with guard
  clauses and early returns; use loop `continue` statements where appropriate.
  Where nesting remains necessary, nest unusual or error handling rather than
  the normal execution path.
  Keep exceptional handling out of the reader's way without obscuring it.

## Source-backed: Follow a standard

- The whole team should use one consistent coding standard across the project,
  covering formatting, indentation, braces, and naming so the codebase has a
  coherent style. It should generally align with common industry practice.
- IDE formatting and other features can help enforce mechanical style rules;
  review their output rather than assuming tooling verifies overall quality.
- Apply the companion `seedu-java-coding-standard` skill for both basic and
  intermediate Java rules, including syntax, formatting, and Javadoc.

## Source-backed: Name well

### Basic

- Use nouns for classes, fields, variables, and data; use verbs for methods and
  actions. Clearly distinguish single-valued and multi-valued variables: use
  singular names for individual values and plural names for collections,
  including arrays.
- Use correctly spelled, standard English words. Avoid texting-style spelling,
  unnecessary foreign-language words, slang, private jokes, and names relying
  on time- or location-specific references.

### Intermediate

- Make names accurately explain the entity and its role at a sufficient level
  of detail. Avoid vague names such as `temp`, `flag`, `data`, or `value` unless
  the context makes their meaning precise. Put multi-word names in sensible
  word order.
- Do not distinguish names only by numbers or capitalization. Use meaningful
  qualifiers, such as `originalValue` and `finalValue`.
- Avoid names that are too short or unnecessarily long; prioritize sufficient
  meaning over brevity. If abbreviations or acronyms are necessary, use them
  consistently and explain their full meaning at an obvious location.
- Name related things similarly and unrelated things differently. Avoid
  misleading, ambiguous, similar-sounding, hard-to-pronounce, and almost-identical
  names, including confusing letters with digits, such as `l`/`I`/`1` or `O`/`0`.

## Source-backed: Avoid unsafe shortcuts

### Basic

- **Fallbacks:** Every `switch`/`case` statement must have a `default` branch.
  Use it for the actual fallback action or error detection, never merely for
  the final enumerated option. When there is no fallback action, use `default`
  to signal an unexpected state with a suitable error. Apply the same meaning
  to a final `else`: it represents everything else, rather than the last
  expected option. Avoid `else` when its condition can be stated explicitly,
  **unless there is absolutely no other possibility**. Use `else if` for an
  identifiable option and reserve a final `else` for a genuine fallback or a
  suitable error.
- **Single purpose:** Use one variable for one purpose, even when another use
  has the same type. Do not reuse formal parameters as local variables.
- **Exceptions:** Avoid empty `catch` blocks, which silently ignore errors.
  If an empty block is unavoidable, include a comment explaining why.
- **Dead code:** Delete dead, unused, and obsolete commented-out code when it
  becomes redundant. Version control makes deletion safe by preserving earlier
  implementations; do not retain code merely because it might be needed later.

### Intermediate

- **Scope:** Minimize global variables, including shared mutable static state
  in Java, because they create implicit links between code segments. Declare
  locals near their first use in the smallest useful scope; a variable used
  only inside an `if` belongs inside that block.
- **Duplication:** Minimize duplicated code (DRY), particularly copy-paste-modify
  patterns. Think carefully before duplicating; a small helper or domain method
  may express a shared rule better. Zero duplication is not always possible;
  weigh extraction against clarity and KISS rather than forcing an abstraction
  for coincidental similarity.

## Source-backed: Comment minimally but sufficiently

### Basic

- Improve unclear code before adding comments to explain it. Do not repeat
  information that is already obvious from self-explanatory code.
- Write for other programmers reading the code, not as private notes. Use a
  class or operation header to explain its purpose when useful; apply the
  companion skill's Javadoc requirements and exceptions.

### Intermediate

- Explain **WHAT** the code should do so a maintainer can compare the intended
  behavior with the implementation, and **WHY** a non-obvious implementation,
  decision, or workaround exists. Do not explain **HOW** self-explanatory code
  works. Keep comments accurate and useful to a future maintainer.

## Project-specific conventions and review workflow

These are retained project practices, not requirements attributed to the Code
Quality chapter.

- **Behavior and APIs:** Preserve behavior and public APIs during quality-only
  refactoring unless the task authorizes changing them. Prefer small, focused
  improvements. If an interface or override requires an unused parameter,
  preserve that contract and clarify when needed; this is a project
  accommodation, not an exception stated by the chapter.
- **Boolean names:** Retain this skill's existing project convention: boolean
  fields, locals, parameters, and record components must use a predicate prefix
  such as `is`, `has`, `can`, `should`, or `was`. Boolean methods must read as
  predicates; action methods may return a status when documentation explains
  it. The Code Quality chapter does not impose this prefix requirement.
- **Review context:** Read the full affected class, including relevant tests,
  before editing. Review nearby naming, nesting, duplication, scope, and
  abstraction levels without expanding the task into unrelated cleanup.
- **Verification after Java changes:** Review relevant tests and update or add
  focused tests when behavior changes warrant them. Use Java 25, as configured
  in `build.gradle` and CI. Run `./gradlew.bat checkstyleMain checkstyleTest` on
  Windows and the relevant automated tests described in `docs/DevOps.md`.
  For documentation-only edits, review the document and diff instead.
- **Final review:** Inspect the diff and full affected classes against all five
  guideline sections and the companion Java standard where applicable.
  Checkstyle verifies only part of code quality. Distinguish explicit rule
  violations from recommendations and review triggers; report unresolved
  concerns and justified exceptions rather than silently accepting them.
