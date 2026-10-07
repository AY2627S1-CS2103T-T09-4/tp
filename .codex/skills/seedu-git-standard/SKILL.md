---
name: seedu-git-standard
description: "Draft, review, amend, and validate commit messages and branch names in this project using the SE-EDU Git conventions."
---

# SE-EDU Git Standard

Apply this skill when drafting, reviewing, suggesting, amending, or validating
commit messages and branch names in this project, including preparation for an
authorized commit.

Authoritative source: [SE-EDU Git conventions](https://se-education.org/guides/conventions/git.html).
The SE-EDU sections below distinguish mandatory rules, recommendations, optional
advice, examples, and rationale. Repository workflow requirements are separate.
The source's basic/intermediate/advanced legend classifies rules by level; it
does not introduce additional Git operations or tooling requirements.

## SE-EDU: Commit subject

### Mandatory rules

- Every commit must have a clear, well-written subject line.
- Treat 72 characters as the hard maximum, including any prefix and spaces.
- Use the imperative mood: describe the action as a command.
- Capitalize the first letter of the subject, subject to the optional prefix
  examples below.
- Do not end the subject with a period.

### Recommendation and rationale

Try to keep the entire subject within 50 characters. This is a target, not a
second hard limit. Short subjects help when tools truncate their display.

### Examples

| Check | Good | Bad |
| --- | --- | --- |
| Imperative mood | `Add README.md` | `Added README.md`; `Adding README.md` |
| Initial capital | `Move index.html file to root` | `move index.html file to root` |
| No final period | `Update sample data` | `Update sample data.` |

### Optional prefixes and other conventions

Allow a relevant `<scope>:` or `<category>:` prefix when applicable. Apply the
imperative mood to the action after the prefix. The official examples include
lowercase categories. Accept the illustrated forms alongside the capitalization
rule; the page does not specify a general prefix-casing policy:

- `Person class: Remove static imports`
- `Main.java: Remove blank lines`
- `bug fix: Add space after name`
- `chore: Update release date`

Other conventions, such as [Conventional Commits](https://www.conventionalcommits.org/),
may also be used. Their richer format can provide additional benefits, but this
skill does not require it or impose its types and syntax. Honor an explicitly
requested convention and identify any conflict with these rules in a review.

## SE-EDU: Commit body

### Recommendation and formatting rules

Non-trivial commits should have a body giving details of the change. A trivial
commit may use only a subject. When a body is present:

- Separate the subject and body with one blank line.
- Wrap body lines at 72 characters, including bullet markers and indentation.
- Separate paragraphs with blank lines.

### Content guidance

- Explain WHAT changed and WHY it was needed or chosen. Leave HOW the code
  implements it to the diff.
- Give enough context for a reviewer to judge whether the change is worthwhile
  without first reading the diff; the diff then shows whether the implementation
  delivers that change.
- Minimize repetition of information already present in the commit's code
  comments.
- Use bullet points or other useful constructs when they improve readability.
- If the description becomes excessively long, consider splitting the commit
  into smaller coherent commits rather than merely shortening the explanation.

Prefer this body structure, including the parts relevant to the change:

1. Describe the current situation in present tense.
2. Explain why it needs to change.
3. Describe what is being done in the imperative mood.
4. Explain why that approach was chosen.
5. Include other relevant information, such as the next PR step or a useful
   reference.

Avoid unnecessary words such as `currently` and `originally` when describing
the situation; that timing is implied. `Let's` may introduce the section that
describes the change.

### Representative good body examples

These illustrative messages cover each example category on the official page.
Use them as writing models, with facts appropriate to the actual diff.

**A step in a multi-commit PR:** Explain the incremental goal and its relationship
to the later work.

```text
Extract patient record formatting helpers

Several screens format patient records inside their display methods.

Embedded formatting makes a consistent display harder to introduce.

Extract each screen's record formatting into a separate helper.

Keeping the display unchanged makes this preparation easy to review.

The next commit in this PR will align the displayed fields.
```

**A bug fix:** Use `Let's` and bullets when listing related changes helps.

```text
Reject duplicate patient records

Adding a patient can create another record for an existing patient.

Duplicate records split visit history across entries and make the
patient's complete history harder to find.

Let's:
- reject additions that match an existing patient
- explain the conflict in the command's error message

Use the identity policy applied to record updates so both commands agree
on which records refer to the same patient.
```

**A code quality refactoring:** Explain the duplication, the change, and the
reason for the design choice. Add a real supporting reference when useful.

```text
Extract a shared visit date validator

Commands that add or reschedule a visit repeat date validation.

Copies of the rule can diverge when the date policy changes.

Extract the rule into a shared VisitDateValidator.

Keep the validator separate because unrelated commands use the policy.
Composition avoids an artificial command inheritance hierarchy.
```

Examples illustrate style, not additional requirements or claims about this
repository. Follow the stated formatting rules rather than copying incidental
formatting inconsistencies in the source's examples. Do not copy sample links
as real supporting evidence.

## SE-EDU: Branch names

- Use meaningful, relevant keywords in kebab-case (lowercase words separated
  by hyphens), such as `refactor-ui-tests`.
- When a branch is tied to an issue, use
  `issueNumber-some-keywords-from-issue-title`, such as `1234-ui-freeze-error`.

## Optional tooling advice: SourceTree

If using SourceTree, these settings can help compose messages; they are optional
tooling advice, not mandatory Git rules or prerequisites:

1. Open `Tools -> Options -> General -> Commit settings`.
2. Enable the commit-message column guide and set it to 72 characters.
3. If the guide option is disabled, enable fixed-width fonts for commit messages
   first. A column guide is meaningful with a fixed-width font.
4. Enable spell checking for commit messages in the same settings section.

## External references

The official page links to these sources for context or further advice. Their
additional rules do not become mandatory through this skill:

- [Conventional Commits](https://www.conventionalcommits.org/): an optional
  alternative subject convention with a richer format.
- [Git project's SubmittingPatches guidance](https://github.com/git/git/blob/e05806da9ec4aff8adfed142ab2a2b3b02e33c8c/Documentation/SubmittingPatches):
  attribution for the advice to split commits with overly long descriptions.
- [How to Write a Git Commit Message](https://chris.beams.io/posts/git-commit/):
  further commit-message writing advice.

The refactoring example's sample Stack Overflow URL is a placeholder. Use a real,
relevant reference when needed; the placeholder is not an operational dependency.

## Repository-specific workflow

These requirements govern work in this project; they are not SE-EDU rules:

- Inspect the staged diff before drafting a message for an actual commit and
  again before committing if the staging area has changed. Keep unrelated
  changes out of the commit. Check the intended diff when staging is not yet
  complete; never assume all working-tree changes belong together.
- Validate the final subject, body, and branch name using the rules above.
  During review, distinguish mandatory-rule violations from recommendations
  and optional style choices, and propose concrete corrections.
- Do not commit, amend an existing commit, or push without explicit user
  authorization for that action. A request to draft or review a message does not
  authorize running those operations.
- Inspect repository instructions and actual configuration before relying on
  local checks. Do not invent hooks, scripts, directories, commands, or
  validation tools, and do not install or reconfigure them through this skill.
- Do not require `.githooks` unless it exists and is actually configured in the
  repository. If an existing check is configured, inspect its real behavior and
  follow applicable repository instructions rather than assuming what it does.

## Official-page coverage checklist

Use this mapping when maintaining the skill. Checked items indicate coverage,
not new mandatory behavior; their classification remains as described above.

- [x] **Legend (basic/intermediate/advanced):** introduction; contextual rule
  levels rather than additional workflow requirements.
- [x] **Commit message: Subject:** mandatory rules (well-written subject,
  imperative mood, initial capital, no final period, 72-character maximum);
  recommendation and rationale (50-character target and tool truncation).
- [x] **Subject examples:** examples table covers all good/bad mood, case, and
  punctuation examples; optional prefixes cover class, file, bug-fix, and chore
  categories, including the lowercase categories shown by the source.
- [x] **Other subject conventions:** optional prefixes and other conventions;
  Conventional Commits remains optional and linked under external references.
- [x] **Commit message: Body:** recommendation and formatting rules cover
  non-trivial versus trivial commits, the subject/body blank line, 72-character
  wrapping, and paragraph spacing.
- [x] **Multi-commit PR example:** first representative body illustrates an
  incremental change and its place in the PR.
- [x] **SourceTree tips:** optional tooling advice covers the settings path,
  72-character guide, fixed-width-font prerequisite and rationale, and spelling.
- [x] **Bullet points and bug-fix example:** content guidance and second
  representative body illustrate bullets and `Let's`.
- [x] **WHAT/WHY, not HOW:** content guidance covers reviewer context, judging
  the change before the diff, minimizing comment repetition, and splitting
  overly large commits; external references retain the Git-project attribution.
- [x] **Body structure and wording tips:** five-part preferred structure,
  present-tense situation, imperative change, implied timing, and optional
  `Let's` introduction.
- [x] **Code quality refactoring example:** third representative body explains
  the design choice; content guidance allows other relevant information and
  real references; external references identify the sample URL as a placeholder.
- [x] **Further reading:** external references retain the commit-message
  article as optional advice.
- [x] **Branch names:** branch rules cover meaningful keywords, kebab-case,
  issue-number format, and both official examples.

The page's examples, rationale, legend, and further-reading links provide
context; they cannot establish facts about a diff or require a particular code
change. SourceTree tips depend on that application's UI. Preserve these as
illustration, explanation, references, or optional manual advice rather than
inventing repository automation. Site navigation, build metadata, and branding
do not define Git conventions.
