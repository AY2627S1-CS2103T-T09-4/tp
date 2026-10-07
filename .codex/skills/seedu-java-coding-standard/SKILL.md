---
name: seedu-java-coding-standard
description: "Review and write Java code in this project using the SE-EDU basic-plus-intermediate coding standard."
---

# Seedu Java Coding Standard

## Authority and scope

Use this skill when writing or reviewing new or modified Java source in this
repository, including tests. The authoritative source is the
[SE-EDU basic + intermediate Java guide](https://se-education.org/guides/conventions/java/intermediate.html).
The sections below explicitly cover its normative rules. Preserve the distinction
between requirements, recommendations, and permitted exceptions.

Use the [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
only for topics that SE-EDU does not cover, including its linked definition of a
constant. Google guidance must not replace SE-EDU's naming, indentation, wrapping,
or other covered rules. Additional repository conventions are explicitly labeled
in **Project-specific conventions** and must not conflict with SE-EDU.

## Naming

### Package names

- Package names must be lowercase.
- A school project's package root must use its project or group name, followed
  by logical package names. For this repository, see **Package root** below.
- Do not use NUS-owned roots such as `edu.nus.comp.*` or similar names that imply
  NUS produced the project.

### Identifier forms

- Use English for all names.
- Classes and enums must have PascalCase noun names, such as `Person` and
  `VisitStatus`.
- Variables must use camelCase, such as `visitDate`.
- Methods must have camelCase verb names, such as `addPerson` and `computeTotal`.
- Constants must use SCREAMING_SNAKE_CASE, such as `MAX_VISITS`.
- Acronyms and abbreviations in mixed-case identifiers must not contain
  consecutive uppercase letters: use `XmlParser` and `loadJson`, rather than
  `XMLParser` and `loadJSON`. Constants still use the required uppercase form.
- Test methods may use `featureUnderTest_testScenario_expectedBehavior`, with
  camelCase components separated by underscores. For example,
  `addPerson_duplicatePerson_throwsException()`.
- Omit the third test-name component, or both the second and third components,
  when appropriate: omit the third for a test covering the named scenario's
  variations, as in `addPerson_duplicatePerson()`; omit both the second and
  third for a test covering all scenarios of the feature, as in `addPerson()`.
  The guide does not permit omitting only the second component while retaining
  the third.

### Scope and scratch variables

- Give variables with wider scope longer, more descriptive names.
- Small-scope scratch variables used within a few lines may have short names.
  Appropriate scratch names include `i`, `j`, `k`, `m`, and `n` for integers or
  indices, and `c` and `d` for characters.
- Iterator names such as `i`, `j`, and `k` are allowed. Reserve `j`, `k`, and
  similar iterator names for nested loops; use `i` for the outer loop or choose
  descriptive names.

### Boolean names

- Boolean variables and methods should sound like predicates. Prefer prefixes
  such as `is`, `has`, `was`, `can`, or `should` wherever possible, for example
  `isVisible`, `hasVisits`, and `canAddPerson()`.
- Apply the boolean naming convention to boolean setter parameters, for example
  `void setVisible(boolean isVisible)`. The setter itself remains an action verb.

### Collections and related constants

- Collection names should be plural, including arrays: `persons`, `tags`, and
  `visitDates`. Use one space between a variable's type and name.
- Related constants should share a common prefix, such as `MESSAGE_SUCCESS`
  and `MESSAGE_DUPLICATE_PERSON`.

## Layout

### Indentation and line length

- Use four spaces for each indentation level. Never use tabs.
- Keep every line at or below 120 characters; aim for 110 characters or fewer
  as the soft target. Wrap lines that exceed the hard limit.
- Indent a wrapped continuation line by eight additional spaces relative to
  its parent line. Apply this relative to the current nesting level as well.

### Wrapping

- Choose line breaks that improve readability. Inspect IDE auto-formatting
  instead of blindly accepting it.
- Prefer breaks after commas and before operators. Treat a member-access dot
  (`.`), a type-bound ampersand (`<T extends First & Second>`), and a multi-catch
  pipe (`catch (FirstException | SecondException exception)`) as operator-like
  symbols: place a break before the symbol when wrapping there.
- Keep method and constructor names attached to their opening `(`; never put
  whitespace or a line break between the name and that parenthesis.
- Prefer higher-level breaks outside nested expressions to breaks inside them.
  For example, break before an outer addition rather than inside its operand's
  parenthesized calculation.
- Both ternary layouts below are acceptable. Keep the single-line form within
  the line limit; in the wrapped form put `?` and `:` before their operands:

```java
displayName = (isNamePresent) ? suppliedName : defaultName;
displayName = (isNamePresent)
        ? suppliedName
        : defaultName;
```

### Braces and statement formats

Use K&R braces: put the opening `{` on the declaration or control-header line,
including the last continuation line of a wrapped header. Put the closing `}`
on its own line, except when followed by `else`, `catch`, `finally`, or the
`while` part of a do-while statement. Indent block contents by four spaces.

Use these forms for methods, constructors, if/else (including a lone `if`),
for, enhanced for, while, do-while, and try/catch/finally. A `finally` clause
may be omitted when not needed. These snippets illustrate layout:

```java
/**
 * Saves the scheduled visits.
 *
 * @throws IOException If writing the visits fails.
 */
public void saveVisits() throws IOException {
    writeVisits();
}

/**
 * Creates a visit record for the given visit date.
 */
public VisitRecord(LocalDate visitDate) {
    this.visitDate = visitDate;
}

if (isNewPerson) {
    addPerson();
} else if (hasChanges) {
    updatePerson();
} else {
    showPerson();
}

for (int i = 0; i < persons.size(); i++) {
    showPerson(persons.get(i));
}

for (Person person : persons) {
    showPerson(person);
}

while (hasNextVisit()) {
    readVisit();
}

do {
    readVisit();
} while (hasNextVisit());

try {
    saveVisits();
} catch (IOException exception) {
    reportFailure(exception);
} finally {
    closeStorage();
}
```

### Switch formats

Indent `case` and `default` labels four spaces inside the switch, and indent
traditional case bodies another four spaces. Traditional cases should normally
end with `break`. The guide requires the exact comment `// Fallthrough` whenever
a traditional `case` lacks a `break`; for intentional fall-through into another
case, put the comment immediately before the next label. Do not silently omit
a break or add exceptions to this rule that the guide does not state. Arrow
cases use the guide's illustrated `->` forms and do not need breaks or that comment.

Use these forms for traditional switches, arrow switches, and switch expressions:

```java
switch (visitStatus) {
    case PLANNED:
        prepareReminder();
        // Fallthrough
    case CONFIRMED:
        sendReminder();
        break;
    default:
        clearReminder();
        break;
}

switch (visitStatus) {
    case PLANNED -> prepareReminder();
    case CONFIRMED -> sendReminder();
    default -> clearReminder();
}

int reminderCount = switch (visitStatus) {
    case PLANNED -> 1;
    case CONFIRMED -> 2;
    default -> 0;
};
```

### Whitespace and blank lines

- Surround operators with spaces, as in `total = (count + extra) * factor`.
  Unary operators stay attached to their operand, as in `!isReady` and `i++`.
  The operator-like symbols mentioned under **Wrapping** concern line breaks,
  not spaces around member-access dots.
- Put whitespace after Java reserved words where followed by another token:
  `if (isReady)`, `while (hasNext)`, `return value`, and `catch (IOException exception)`.
- Put whitespace after commas: `addVisit(person, visitDate)`.
- Surround a binary or ternary colon with spaces, including the enhanced-for
  colon: `Person person : persons` and `isReady ? first : second`.
- Do not apply binary-colon spacing to switch labels: write `case PLANNED:`
  and `default:`, with no space before the colon.
- Put a space after each semicolon in a for header:
  `for (int i = 0; i < count; i++)`.
- Separate logical units within a block with one blank line.

## Statements

### Packages and imports

- Every class must belong to a package; do not use the unnamed/default package.
- Keep import ordering consistent. The team should use the same IDE or the same
  import-ordering configuration, since IDE defaults can differ.
- Imports must be explicit, minimal, and up to date. Remove unused, duplicate,
  and redundant imports; configure IDE import management accordingly.
- Wildcard imports are forbidden, including static wildcard imports. Import
  each required class or static member explicitly.
- Apply this repository's labeled **Import ordering** convention below.

### Types and variables

- Attach array brackets to the type: `String[] names`, never `String names[]`.
- Initialize variables at declaration whenever possible.
- Declare each variable in the smallest possible scope, including loop indices
  in their loop header when possible.
- If valid initialization at declaration is impossible, leave the variable
  uninitialized and assign it before use. Do not assign a fake placeholder value
  such as `null` or `0` merely to supply an initializer.

### Field visibility

Class fields must not be public unless the class is a data class with no
behavior. Constants are exempt from this restriction. Otherwise use non-public
fields and access methods to preserve encapsulation.

### Loop and conditional bodies

- Every loop body must use braces, regardless of its length or statement count.
- Every conditional body must use braces, including a single-statement body.
- Put conditional bodies on separate lines from their header and closing brace.
  Do not write `if (isReady) proceed();` or `if (isReady) { proceed(); }`.
- Use the forms in **Braces and statement formats** and **Switch formats**.

## Comments and Javadoc

### Language and placement

- Write all comments in English, using American spelling. Avoid local slang.
- Indent comments with the code they describe so that they preserve the block's
  visual structure.
- Trailing comments are allowed when readable and within the line limit, for
  example `saveVisits(); // Persist the updated schedule.`

### Required documentation

- Add a descriptive Javadoc header comment to every class, regardless of its
  visibility, and descriptive Javadoc to every public method.
- Getters and setters may omit Javadoc.
- An overriding method may omit Javadoc only when the parent method's Javadoc
  applies exactly as written. `@Override` alone does not establish this exception.
- Test classes and test methods may omit Javadoc.
- When documenting an override, use `{@inheritDoc}` where appropriate to reuse
  the parent documentation and explain differences. Changed behavior needs its
  own explanation rather than relying solely on unchanged inherited text.

### Javadoc format and tags

- Begin a multiline Javadoc comment with `/**` on its own line.
- Make the first sentence a short summary. A method summary should start with
  a present-tense form such as "Returns ...", "Adds ...", or "Sends ...",
  rather than "Return ..." or "Returning ...".
- Align the leading asterisks with the first asterisk in the opening delimiter.
  Put a space after each leading `*` before text; use a bare `*` for a blank line.
- Put a blank Javadoc line between the description and the tag section.
- End every parameter description with punctuation.
- Put no blank line between the Javadoc block and its declaration. Declaration
  annotations, when present, remain directly between the comment and declaration.
- Use `@param` for parameters, `@return` for return values, and `@throws` for
  relevant exceptions and the conditions that cause them.
- `@return` may be omitted for a void method or when the return value is already
  obvious from the description.
- Include `@param` for all parameters or omit all of them. Omit them all only
  when every parameter name is self-explanatory or every parameter is already
  explained in the description, so that the tags would add no information.
- Simple class members may use concise single-line Javadoc, for example
  `/** Number of scheduled visits. */`. This is the exception to the multiline
  opening-delimiter rule.

Example of a multiline method header:

```java
/**
 * Returns the number of visits scheduled for the given person.
 * Includes both planned and confirmed visits.
 *
 * @param person Person whose scheduled visits are counted.
 * @return Number of scheduled visits.
 * @throws IllegalArgumentException If the person is unknown.
 */
public int countVisits(Person person) {
    return findScheduledVisits(person).size();
}
```

## Project-specific conventions

All rules in this section are project-specific additions or applications of
the SE-EDU rules; they do not override the guide.

### Package root

**Project-specific:** The actual package root is `seedu.address`. Use logical
subpackages such as `seedu.address.logic.parser`, `seedu.address.model.person`,
and `seedu.address.storage`. Keep tests in the corresponding package hierarchy.

### Import ordering

**Project-specific:** Follow `CustomImportOrder` in
`config/checkstyle/checkstyle.xml`: static imports first, then standard
`java`/`javax` imports, then `org` imports, then `com` imports. Sort imports
alphabetically within each configured group. Remaining imports, such as
`javafx` and `seedu.address`, follow the configured groups and remain ordered
consistently with neighboring source files; do not invent a different IDE order.
Use blank lines to separate import groups consistently with the repository.

### Maintenance and verification

- **Project-specific:** Preserve behavior and public APIs during style-only
  work unless the user has authorized a behavioral or API change.
- **Project-specific:** Add concise Javadoc to non-obvious class members.
- **Project-specific:** For complementary CS2103 code-quality guidance, consult
  `.codex/skills/seedu-code-quality/SKILL.md`, especially method length, class
  size, and keeping a method at one level of abstraction. It supplements this
  skill and must not contradict the authoritative style rules.
- **Project-specific:** After Java changes, review relevant tests and run
  appropriate project checks using Java 25, as configured in `build.gradle` and
  CI. On Windows, Checkstyle runs via
  `.\gradlew.bat checkstyleMain checkstyleTest`; run relevant tests as appropriate.
- **Project-specific:** Do not weaken Checkstyle configuration or add
  suppressions merely to hide a violation. Passing Checkstyle is only partial
  verification: manually review semantic naming, documentation applicability,
  scope, readable wrapping, and the other rules it cannot fully establish.
  If a configuration allowance conflicts with SE-EDU, follow SE-EDU and report
  the discrepancy.

## Skill maintenance and verification

When refining this skill, read the existing file, inspect the repository's
packages and relevant conventions, and consult the authoritative guide before
editing. For an update scoped to this skill, modify only
`.codex/skills/seedu-java-coding-standard/SKILL.md`; do not modify Java files,
tests, build files, Checkstyle configuration, or companion skill metadata.

After editing, create or update the coverage checklist below, mapping every
requested rule to an explicit section. Finish only when each rule is covered
or clearly identified as intentionally unsupported. Report the files changed
and limitations of automated verification. Validate frontmatter and Markdown
structure, review coverage against the guide, and verify the allowed file scope.
Java compilation, Java tests, and Checkstyle do not validate this Markdown skill
and need not run for a skill-only edit.

## Coverage checklist

Completed after the rewrite and reviewed against the authoritative guide and
the requested requirements. Each checked row maps one requested rule to the
section that explicitly covers it. All 84 rules are covered; none is intentionally
unsupported. This checklist records coverage, not automated proof of Java conformance.

The follow-up guide audit also checked the rule wording, permitted omissions,
and examples. Test-name omissions are limited to the third component or both
the second and third; the traditional-switch rule preserves the guide's no-break
comment requirement without an added return/throw exemption; public-method
examples include the required Javadoc.

### Naming coverage

| Covered | Rule | Section |
| --- | --- | --- |
| [x] N01 | Lowercase package names. | [Package names](#package-names) |
| [x] N02 | Project/group package root followed by logical packages. | [Package names](#package-names) |
| [x] N03 | No NUS-owned package roots. | [Package names](#package-names) |
| [x] N04 | Classes and enums use PascalCase nouns. | [Identifier forms](#identifier-forms) |
| [x] N05 | Variables use camelCase. | [Identifier forms](#identifier-forms) |
| [x] N06 | Methods use camelCase verbs. | [Identifier forms](#identifier-forms) |
| [x] N07 | Constants use SCREAMING_SNAKE_CASE. | [Identifier forms](#identifier-forms) |
| [x] N08 | Tests may use feature/scenario/behavior names. | [Identifier forms](#identifier-forms) |
| [x] N09 | Omit the third test component, or both second and third. | [Identifier forms](#identifier-forms) |
| [x] N10 | Names are in English. | [Identifier forms](#identifier-forms) |
| [x] N11 | No consecutive acronym/abbreviation capitals in mixed case. | [Identifier forms](#identifier-forms) |
| [x] N12 | Wider-scope variables have more descriptive names. | [Scope and scratch variables](#scope-and-scratch-variables) |
| [x] N13 | Short names are allowed for small-scope scratch variables. | [Scope and scratch variables](#scope-and-scratch-variables) |
| [x] N14 | Appropriate scratch names include i, j, k, m, n, c, d. | [Scope and scratch variables](#scope-and-scratch-variables) |
| [x] N15 | Reserve j, k, and similar iterator names for nested loops. | [Scope and scratch variables](#scope-and-scratch-variables) |
| [x] N16 | Boolean predicates; prefer is/has/was/can/should. | [Boolean names](#boolean-names) |
| [x] N17 | Boolean setter parameters follow boolean naming. | [Boolean names](#boolean-names) |
| [x] N18 | Plural collection names. | [Collections and related constants](#collections-and-related-constants) |
| [x] N19 | Related constants share a prefix. | [Collections and related constants](#collections-and-related-constants) |

### Layout coverage

| Covered | Rule | Section |
| --- | --- | --- |
| [x] L01 | Four spaces; never tabs. | [Indentation and line length](#indentation-and-line-length) |
| [x] L02 | 120-character hard limit; 110-character soft target. | [Indentation and line length](#indentation-and-line-length) |
| [x] L03 | Eight additional spaces for continuations. | [Indentation and line length](#indentation-and-line-length) |
| [x] L04 | Readable wrapping; inspect IDE auto-formatting. | [Wrapping](#wrapping) |
| [x] L05 | Prefer breaks after commas and before operators. | [Wrapping](#wrapping) |
| [x] L06 | Dots, type-bound ampersands, and multi-catch pipes wrap like operators. | [Wrapping](#wrapping) |
| [x] L07 | Method/constructor names stay attached to opening parentheses. | [Wrapping](#wrapping) |
| [x] L08 | Prefer higher-level breaks over nested-expression breaks. | [Wrapping](#wrapping) |
| [x] L09 | Both permitted ternary formats. | [Wrapping](#wrapping) |
| [x] L10 | K&R braces. | [Braces and statement formats](#braces-and-statement-formats) |
| [x] L11 | Standard method/control forms, including switch expressions. | [Statement forms][forms], [Switches][switches] |
| [x] L12 | Traditional cases normally end with break. | [Switch formats](#switch-formats) |
| [x] L13 | Explicit // Fallthrough whenever a traditional case lacks break. | [Switch formats](#switch-formats) |
| [x] L14 | Spaces around operators. | [Whitespace and blank lines](#whitespace-and-blank-lines) |
| [x] L15 | Whitespace after Java reserved words. | [Whitespace and blank lines](#whitespace-and-blank-lines) |
| [x] L16 | Whitespace after commas. | [Whitespace and blank lines](#whitespace-and-blank-lines) |
| [x] L17 | Spaces around binary/ternary colons. | [Whitespace and blank lines](#whitespace-and-blank-lines) |
| [x] L18 | Binary-colon spacing excludes switch labels. | [Whitespace and blank lines](#whitespace-and-blank-lines) |
| [x] L19 | Spaces after semicolons in for headers. | [Whitespace and blank lines](#whitespace-and-blank-lines) |
| [x] L20 | One blank line between logical units in a block. | [Whitespace and blank lines](#whitespace-and-blank-lines) |

### Statements coverage

| Covered | Rule | Section |
| --- | --- | --- |
| [x] S01 | Every class belongs to a package. | [Packages and imports](#packages-and-imports) |
| [x] S02 | Consistent import ordering. | [Packages and imports](#packages-and-imports) |
| [x] S03 | Same team IDE or import-ordering configuration. | [Packages and imports](#packages-and-imports) |
| [x] S04 | Explicit, minimal imports. | [Packages and imports](#packages-and-imports) |
| [x] S05 | No wildcard imports. | [Packages and imports](#packages-and-imports) |
| [x] S06 | Array brackets attached to the type. | [Types and variables](#types-and-variables) |
| [x] S07 | Initialize at declaration whenever possible. | [Types and variables](#types-and-variables) |
| [x] S08 | Declare variables in the smallest possible scope. | [Types and variables](#types-and-variables) |
| [x] S09 | Leave uninitialized instead of assigning fake placeholders. | [Types and variables](#types-and-variables) |
| [x] S10 | Non-public fields except in behavior-free data classes. | [Field visibility](#field-visibility) |
| [x] S11 | Constants exempt from the field-visibility restriction. | [Field visibility](#field-visibility) |
| [x] S12 | Braces for every loop body. | [Loop and conditional bodies](#loop-and-conditional-bodies) |
| [x] S13 | Braces for every conditional body. | [Loop and conditional bodies](#loop-and-conditional-bodies) |
| [x] S14 | Conditional bodies appear on separate lines. | [Loop and conditional bodies](#loop-and-conditional-bodies) |

### Comments and Javadoc coverage

| Covered | Rule | Section |
| --- | --- | --- |
| [x] C01 | English comments. | [Language and placement](#language-and-placement) |
| [x] C02 | American spelling. | [Language and placement](#language-and-placement) |
| [x] C03 | No local slang. | [Language and placement](#language-and-placement) |
| [x] C04 | Descriptive headers for every class. | [Required documentation](#required-documentation) |
| [x] C05 | Descriptive Javadoc for every public method. | [Required documentation](#required-documentation) |
| [x] C06 | Getters/setters may omit Javadoc. | [Required documentation](#required-documentation) |
| [x] C07 | Override omission only if parent Javadoc applies exactly. | [Required documentation](#required-documentation) |
| [x] C08 | Test classes/methods may omit Javadoc. | [Required documentation](#required-documentation) |
| [x] C09 | Opening /** on its own line, with the member exception. | [Javadoc format and tags](#javadoc-format-and-tags) |
| [x] C10 | Short first-sentence summary. | [Javadoc format and tags](#javadoc-format-and-tags) |
| [x] C11 | Method summaries start with Returns/Adds/Sends forms. | [Javadoc format and tags](#javadoc-format-and-tags) |
| [x] C12 | Aligned leading asterisks and spaces after them. | [Javadoc format and tags](#javadoc-format-and-tags) |
| [x] C13 | Blank line between description and tags. | [Javadoc format and tags](#javadoc-format-and-tags) |
| [x] C14 | Punctuation ends parameter descriptions. | [Javadoc format and tags](#javadoc-format-and-tags) |
| [x] C15 | No blank line between Javadoc and declaration. | [Javadoc format and tags](#javadoc-format-and-tags) |
| [x] C16 | Appropriate @param, @return, and @throws. | [Javadoc format and tags](#javadoc-format-and-tags) |
| [x] C17 | @return optional for void or an obvious return value. | [Javadoc format and tags](#javadoc-format-and-tags) |
| [x] C18 | All @param tags or none under the stated conditions. | [Javadoc format and tags](#javadoc-format-and-tags) |
| [x] C19 | Appropriate @inheritDoc use for overrides. | [Required documentation](#required-documentation) |
| [x] C20 | Concise single-line Javadoc for simple members. | [Javadoc format and tags](#javadoc-format-and-tags) |
| [x] C21 | Comments indented with their code. | [Language and placement](#language-and-placement) |
| [x] C22 | Readable trailing comments allowed. | [Language and placement](#language-and-placement) |

### Project-specific requirements coverage

| Covered | Rule | Section |
| --- | --- | --- |
| [x] P01 | Actual seedu.address root throughout project examples. | [Package root](#package-root) |
| [x] P02 | Preserve useful, non-conflicting project rules. | [Project-specific conventions](#project-specific-conventions) |
| [x] P03 | Clearly label additional project-specific rules. | [Project-specific conventions](#project-specific-conventions) |
| [x] P04 | Google only for topics absent from SE-EDU. | [Authority and scope](#authority-and-scope) |

### Editing and verification coverage

| Covered | Rule | Section |
| --- | --- | --- |
| [x] E01 | Modify only this SKILL.md after reading it and inspecting the repository. | [Skill maintenance and verification](#skill-maintenance-and-verification) |
| [x] E02 | No Java, test, build, or Checkstyle configuration changes. | [Skill maintenance and verification](#skill-maintenance-and-verification) |
| [x] E03 | Create a rule-to-section checklist after editing. | [Skill maintenance and verification](#skill-maintenance-and-verification) |
| [x] E04 | Finish only with full coverage or explicit unsupported rules. | [Skill maintenance and verification](#skill-maintenance-and-verification) |
| [x] E05 | Report changed files and automated-verification limits. | [Skill maintenance and verification](#skill-maintenance-and-verification) |

[forms]: #braces-and-statement-formats
[switches]: #switch-formats
