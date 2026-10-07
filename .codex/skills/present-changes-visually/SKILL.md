---
name: present-changes-visually
description: Generate an HTML split-view Git diff before handing off changes to application code, tests, support scripts, or UI resources in this project, and for requested visual comparisons of commits, branches, tags, or the worktree.
---

# Present Changes Visually

Generate one interactive HTML page containing every changed file as a side-by-side
before/after diff. The page folds long unchanged runs, highlights changed words
within modified lines, supports file filtering, and includes collapsed panels for
unchanged files.

## When to run

Run this skill before handing off completed changes to application code, tests,
support scripts, or UI resources, as required by the repository's `AGENTS.md`.
The user need not request a visual diff separately. Run it after the final edits
and checks, and regenerate the page if relevant changes follow generation.
Honor an explicit user waiver for the current task. Pure documentation or
agent-instruction changes and read-only tasks are exempt unless a visual diff
is requested. Explicit visual comparisons always use the requested scope.

## Generate a visual diff

1. Treat the repository root as the target unless the user identifies another
   repository.
2. Use `HEAD` as the before point and `WORKTREE` as the after point unless the
   user specifies comparison points. `WORKTREE` includes staged, unstaged, and
   untracked files, but excludes ignored files.
3. Use `_temp/visual-diff.html` as the output path unless the user supplies one.
4. Run the bundled generator from the repository root. Use `python` on Windows
   or `python3` on macOS/Linux:

   ```powershell
   python .codex/skills/present-changes-visually/scripts/generate-split-view-diff.py `
     . HEAD WORKTREE _temp/visual-diff.html
   ```

   ```bash
   python3 .codex/skills/present-changes-visually/scripts/generate-split-view-diff.py \
     . HEAD WORKTREE _temp/visual-diff.html
   ```

   Replace `HEAD`, `WORKTREE`, and the output path with the requested values.
   Valid comparison points include commit SHAs, tags, branches, and expressions
   such as `HEAD~2`. Use `WORKTREE` for the current files.
5. Confirm that the command succeeds and that the output file exists and is
   non-empty. Provide a clickable link to its absolute path together with the
   changed-file count printed by the generator. If the default comparison
   includes pre-existing changes from other tasks, identify that scope; do not
   claim to have authored or verified all included changes.

Do not open a browser automatically. Open or inspect the generated page only when
the user asks for a visual review. Pass `--no-unchanged` when the user wants only
changed files; pass `--open` only when the user explicitly asks to open the page.

## Resource

Use `scripts/generate-split-view-diff.py` for the generation step. It uses only
Python's standard library; syntax highlighting is loaded by the generated page
from a CDN when network access is available, and the page remains usable without
it.
