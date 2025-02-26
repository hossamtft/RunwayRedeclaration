**Group 2 SEG**

Git Workflow Guide

This guide explains the Git workflow for collaborating on this project using GitLab. Follow these steps to create feature branches, keep them updated, and merge them back into the main branch.

**Getting Started**

Before you begin, make sure you have Git installed and have cloned the repository to your local machine.

**Clone the Repository**

If you haven't already cloned the repository, run the following command:

```
git clone https://git.soton.ac.uk/lmt1n22/group-2-seg.git
cd group-2-seg
```

**Creating a Feature Branch**

When working on a new feature or bug fix, create a new branch from the main branch.

```
git checkout main          # Switch to the main branch
git pull origin main       # Ensure you have the latest changes
git checkout -b feature-branch-name  # Create and switch to a new branch
```

Use a descriptive name for your branch, such as feature-login or fix-dashboard-bug.

**Pushing Your Branch to Remote**

Once you have made changes and committed them locally, push your branch to the remote repository:

```
git add .                  # Add your local branch to version control
git commit -m "Describe your changes"  # Commit changes
git push origin feature-branch-name  # Push branch to remote
```

**Keeping Your Branch Updated**

To avoid merge conflicts, regularly update your branch with the latest changes from main:

```
git checkout main          # Switch to the main branch
git pull origin main       # Get latest changes
git checkout feature-branch-name  # Switch back to your feature branch
git merge main             # Merge latest main branch changes
```
Resolve any conflicts if they appear, then commit and push the resolved code.

**Creating a Merge Request (MR)**

Once your feature is complete and tested, create a Merge Request (MR) to merge your branch into main:


- Go to the GitLab project page.
- Navigate to Merge Requests.
- Click New Merge Request.
- Select feature-branch-name as the source branch and main as the target branch.
- Add a title and description for your changes.
- Request a review from a team member.
- Once approved, merge the branch.
- After merging, delete your branch to keep the repository clean:

```
git branch -d feature-branch-name  # Delete local branch
git push origin --delete feature-branch-name  # Delete remote branch
```

For any questions, reach out to the team on the project chat or issue tracker.