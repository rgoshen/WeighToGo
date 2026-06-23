# GitHub Account Migration Runbook (repository + project board)

> **Scope: one-time, manual operation.** This runbook moves the GitHub
> repository **`rgoshen-snhu/WeighToGo`** and its **Projects v2 board** to the
> personal account **`rgoshen`**, preserving full git history, issues, pull
> requests, releases, and wiki. It is an operational guide, not a capstone
> deliverable.
>
> **Key fact up front:** a repository transfer moves the repo *and* its
> issues/PRs/releases/wiki in one step, but a **Projects v2 board is owned by the
> account, not the repository** — it does **not** travel with the transfer and
> must be copied and repopulated separately (Part 2).
>
> The GitHub repository is named **`WeighToGo`** (one "t"); the local working
> directory is `WeightToGo`. Commands below use the GitHub name.

---

## 1. Prerequisites

| Requirement | Notes |
| --- | --- |
| Admin access to the source repo | You own `rgoshen-snhu/WeighToGo`, so this is satisfied. |
| Target name is free | `rgoshen` must **not** already have a repo named `WeighToGo`, nor a fork in the same network. Delete/rename any collision first. |
| Both accounts on `github.com` | Confirmed — neither is an enterprise-managed user (EMU). GitHub **blocks** transfers into or out of an EMU enterprise, so this only works because you created the `snhu` account yourself. |
| `gh` CLI ≥ 2.95.0 (optional) | Only needed for the CLI alternatives and the board-repopulation script. Verify with `gh --version`. |
| Decide on the final name | You may keep `WeighToGo` or rename to `WeightToGo` **during** the transfer (the transfer dialog offers a rename field). |

---

## 2. What transfers (and what does not)

**Transferred automatically with the repository:**

- All git history — commits, branches, tags, and contribution attribution.
- Issues, pull requests, and their comments.
- Releases and the wiki.
- Stars and watchers.
- Webhooks, services, secrets, and deploy keys (remain associated).
- Fork relationships (upstream and downstream) stay intact.
- The old `github.com/rgoshen-snhu/WeighToGo` URLs **301-redirect** to the new
  location, so existing clones and links keep working.

**NOT transferred — handled manually in Part 2:**

- The **Projects v2 board** (account-owned). The new owner can administer
  repo-linked projects after transfer, but the standalone board itself stays on
  `rgoshen-snhu` until you copy it.
- Per GitHub's docs, **copying a project carries its views and custom fields but
  not its items, collaborators, or repository links** — so the board's cards and
  their column placement must be rebuilt.

> **Note:** After the transfer, `rgoshen-snhu` is automatically added as a
> **collaborator** on the repo under `rgoshen`. Remove it later if you want a
> clean break (repo → Settings → Collaborators).

---

## 3. Part 1 — Transfer the repository

### 3.1 Web UI (recommended)

1. Go to `https://github.com/rgoshen-snhu/WeighToGo` → **Settings** → **General**.
2. Scroll to the **Danger Zone** → click **Transfer**.
3. Type the repository name to confirm, then enter the new owner: **`rgoshen`**.
   (Optionally set a new repository name here if you want `WeightToGo`.)
4. Submit. **`rgoshen` receives a confirmation email and must accept within 24
   hours** (personal→personal transfers expire after one day if not accepted).
5. Accept the transfer from the email (or from the notification on `rgoshen`).

### 3.2 CLI alternative

`gh` 2.95.0 has **no** `repo transfer` subcommand, so use the REST API directly.
The 24-hour email acceptance still applies for a personal-account target.

```bash
gh api --method POST repos/rgoshen-snhu/WeighToGo/transfer \
  -f new_owner=rgoshen
```

### 3.3 Post-transfer: update the local clone

The remote in this clone is named `snhu` and points at the old owner. Repoint it
(the old URL would redirect, but use the canonical one):

```bash
# update the existing 'snhu' remote to the new owner
git remote set-url snhu https://github.com/rgoshen/WeighToGo.git

# optional: rename the remote so it no longer implies the old account
git remote rename snhu origin

# confirm and re-sync
git remote -v
git fetch --all
```

---

## 4. Part 2 — Move the Projects v2 board

Do **Part 1 first** so the issues already live under `rgoshen/WeighToGo` before
you re-link them to the new board.

Find the source project number:

```bash
gh project list --owner rgoshen-snhu
```

### 4.1 Copy the board structure

**Web UI:** open the project → **`...`** menu → **Make a copy** → choose owner
`rgoshen`, give it a name, and (optionally) include draft issues. The copy
reproduces your views and custom fields on a clean board.

**CLI:**

```bash
gh project copy <PROJECT_NUMBER> \
  --source-owner rgoshen-snhu \
  --target-owner rgoshen \
  --title "WeighToGo" \
  --drafts            # omit if you don't want draft issues carried over
```

> ⚠️ **Cross-account caveat.** `gh project copy` needs a single token that can
> read the source **and** write the target. Two separate personal accounts
> usually cannot, so this may `403`. If it does, use the **Web UI** copy above
> (it operates within your authenticated session), or recreate the handful of
> custom fields by hand — then continue to 4.2.

### 4.2 Repopulate items and restore field values

A copy is an empty shell — it has the columns but none of the cards. This script
re-adds every transferred issue/PR and restores its **Status** by matching the
option *name* (the copied board has brand-new field and option IDs, so you cannot
reuse the old ones).

```bash
#!/usr/bin/env bash
set -euo pipefail

SRC_OWNER="rgoshen-snhu"
DST_OWNER="rgoshen"
SRC_NUM="<SOURCE_PROJECT_NUMBER>"
DST_NUM="<COPIED_PROJECT_NUMBER>"

# Export source items and the NEW board's field map.
# -L 1000 is mandatory: item-list/field-list default to only 30 rows and
# silently truncate beyond that.
gh project item-list  "$SRC_NUM" --owner "$SRC_OWNER" -L 1000 --format json > src-items.json
gh project field-list "$DST_NUM" --owner "$DST_OWNER" -L 100  --format json > dst-fields.json

DST_PID=$(gh project view "$DST_NUM" --owner "$DST_OWNER" --format json --jq '.id')
STATUS_FID=$(jq -r '.fields[] | select(.name=="Status") | .id' dst-fields.json)

# Re-add each real issue/PR (skip drafts), then restore its Status column.
jq -c '.items[] | select(.content.type=="Issue" or .content.type=="PullRequest")' src-items.json |
while read -r item; do
  url=$(jq -r '.content.url' <<<"$item")
  status=$(jq -r '.status // empty' <<<"$item")

  iid=$(gh project item-add "$DST_NUM" --owner "$DST_OWNER" --url "$url" --format json --jq '.id')

  if [[ -n "$status" ]]; then
    oid=$(jq -r --arg s "$status" \
      '.fields[] | select(.name=="Status") | .options[] | select(.name==$s) | .id' dst-fields.json)
    [[ -n "$oid" ]] && gh project item-edit \
      --id "$iid" --project-id "$DST_PID" \
      --field-id "$STATUS_FID" --single-select-option-id "$oid"
  fi
  echo "added $url ($status)"
done
```

Notes and constraints:

- **One field per `item-edit` call** is a hard API limit. For each *additional*
  custom field (Priority, Iteration, a text/number field), repeat the
  lookup-and-edit block using the matching flag: `--single-select-option-id`,
  `--text`, `--number`, `--date`, or `--iteration-id`.
- **Auth across accounts.** Run the **export** (`item-list`, `field-list`,
  `view`) while authenticated as the account that can read the source
  (`gh auth switch --user rgoshen-snhu`), then run the **import** loop as
  `rgoshen`. Splitting it avoids the cross-account token problem in 4.1.
- The field-value JSON keys mirror your actual field titles. Eyeball
  `src-items.json` once to confirm the key names before relying on them.

> **KISS check.** For a board with only a handful of cards across a few columns,
> rebuilding it by hand after the transfer is faster and less error-prone than
> debugging cross-account tokens and ID mapping. Script this only when the item
> count is large.

### 4.3 Re-link the board to the repository

A project copy drops repository links, so:

1. New project → **Settings** → link it to `rgoshen/WeighToGo`.
2. Optionally pin it on the repo's **Projects** tab.

---

## 5. Verification

1. **Redirects** — `https://github.com/rgoshen-snhu/WeighToGo` resolves to
   `rgoshen/WeighToGo`.
2. **Local clone** — `git remote -v` shows the new owner; `git fetch --all` and a
   test `git push` succeed.
3. **Platform data** — issues, PRs, and releases are all present under
   `rgoshen/WeighToGo`.
4. **Board** — the new project under `rgoshen` shows every card in its correct
   Status column (and any custom field values you migrated).
5. **Board linkage** — the new board is linked to `rgoshen/WeighToGo`.

If all five pass, the migration is complete.

---

## 6. Notes and rollback

- **Reversible.** A transfer can be undone by transferring the repo back; the
  same 24-hour acceptance window applies each direction.
- **The 24-hour window** is the most common failure point — if the new owner
  doesn't accept in time, the invitation silently expires and you simply
  re-issue it.
- **Cleanup.** After verifying, remove `rgoshen-snhu` as a collaborator on the
  transferred repo (Part 2 note), and archive or delete the now-empty source
  board on `rgoshen-snhu`.

---

## 7. References

- [GitHub Docs — Transferring a repository](https://docs.github.com/en/repositories/creating-and-managing-repositories/transferring-a-repository)
- [GitHub Docs — Copying an existing project](https://docs.github.com/en/issues/planning-and-tracking-with-projects/creating-projects/copying-an-existing-project)
- [GitHub Docs — Using the API to manage projects](https://docs.github.com/en/issues/planning-and-tracking-with-projects/automating-your-project/using-the-api-to-manage-projects)
- [GitHub Docs — Merging multiple personal accounts](https://docs.github.com/en/account-and-profile/setting-up-and-managing-your-personal-account-on-github/managing-user-account-settings/merging-multiple-personal-accounts)
- [GitHub CLI manual — `gh project` commands](https://cli.github.com/manual/gh_project)
