# Continuous testing controller

The controller stores authoritative state in `.testing/state.sqlite` and writes a
rebuildable `.testing/state.json` snapshot. Runtime data is ignored by Git.

From the backend repository run:

```powershell
python -m scripts.continuous_testing.cli start
python -m scripts.continuous_testing.cli status
python -m scripts.continuous_testing.cli pause
python -m scripts.continuous_testing.cli resume
python -m scripts.continuous_testing.cli stop
python -m scripts.continuous_testing.cli replay <failure-id>
python -m scripts.continuous_testing.cli explore
python -m scripts.continuous_testing.cli run-baseline
```

The current foundation provides bounded subprocess execution, immutable Git
worktrees, baseline gate definitions, and semantic exploration de-duplication.
It never pushes or merges the main development branches.

Install the Windows logon task with automatic restart after failure:

```powershell
.\scripts\continuous_testing\install-task.ps1 -Action Install
```
