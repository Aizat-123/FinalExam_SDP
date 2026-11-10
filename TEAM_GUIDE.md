# Two Person Git and Submission Guide

Use this workflow with your existing GitHub or GitLab repository. The code package is a tested reference. The assignment requires both students to independently implement their patterns, show genuine development history, and disclose AI use. Recreate or develop your own work in the order below. Record the names and commits for the work each partner actually implements.

## 1 Connect to the existing repository

Invite your partner as a collaborator or project member and have them accept. Each partner works in a separate local clone using their own hosting account and Git identity. Open PowerShell in a parent folder outside the ZIP and copy the clone URL from your repository.

~~~powershell
$repo = 'https://github.com/OWNER/REPOSITORY.git'
git clone $repo pet-care-team
cd pet-care-team
git config user.name "Your Real Name"
git config user.email "Your verified email or hosting noreply address"
git remote -v
git fetch origin
git remote show origin
git status
~~~

Replace the URL with yours. For GitLab, use your GitLab clone URL. Use the default branch shown in the output. The commands below use main. If you already cloned the repository, open that checkout and start with git remote -v. If origin exists but points elsewhere, correct it with `git remote set-url origin $repo`. If origin is absent, use `git remote add origin $repo`.

If you already committed the Go version, remove its tracked source files in a separate migration commit on a member branch before merging the Java work. First run git ls-files and review what belongs to the previous project. Use git rm -- followed by the reviewed file names, then commit the removals and open a pull request. Keep unrelated files and existing history.

If the repository is completely empty, Student A initializes main once. Skip this block if main or another default branch already exists.

~~~powershell
git switch -c main
'# Pet Care Planner' | Set-Content README.md
git add README.md
git commit -m "Start Pet Care Planner repository"
git push -u origin main
~~~

Confirm main is the default branch on the hosting site. Student B can clone or fetch after this initial push. Use the actual existing member branch names in place of student-a and student-b. Keep both branches through the defense.

## 2 Open or connect your member branches

Student A runs the following with their own branch name. Student B uses student-b in place of student-a.

~~~powershell
git fetch origin
git branch -a
git switch student-a
~~~

If you see the branch only under remotes/origin and Git does not automatically track it, run `git switch --track origin/student-a`. If the branch does not yet exist, run `git switch -c student-a origin/main` and `git push -u origin student-a`. If an existing branch needs main, run `git merge origin/main` with a clean working tree. Use `git status -sb` to confirm the current branch before editing.

## 3 Week 1 architecture and Student A patterns

Agree on the Plan, PlanInput, and PlanStore shapes before developing separately. Sketch the form, reminders, and saved plans. Use the report class diagram as a reference and draw the initial version yourselves. To make a runnable skeleton early, Student A can create a temporary Main.java that prints a startup message or serves a single page. In the initial Care.java, define only the Care interface with int duration(). The model uses this interface. In the Decorator commit, add WithService to Care.java. Student B replaces the temporary Main.java with the real routes during integration.

Work through the groups below in Student A's clone. Each row gives the files to add and the command. Copy only the relevant reference files for comparison and stage the files you actually authored. Each commit should reflect real work and be tested at the stage where it is created.

| Files to add | Git command | Commit command |
| --- | --- | --- |
| .gitignore, Model.java, starter Care.java, initial Main.java | `git add -- .gitignore Model.java Care.java Main.java` | `git commit -m "Set up Java project and care entities"` |
| PlanBuilder.java | `git add -- PlanBuilder.java` | `git commit -m "Build and validate care plans in stages"` |
| SchedulingStrategy.java | `git add -- SchedulingStrategy.java` | `git commit -m "Add exact date and weekday scheduling"` |
| Care.java | `git add -- Care.java` | `git commit -m "Wrap procedures with optional services"` |

Before publishing, inspect your changes.

~~~powershell
git status
git diff --stat origin/main
javac -encoding UTF-8 --release 21 -d out *.java
git push -u origin student-a
~~~

Student A opens a pull request or merge request from student-a into main. Student B reviews the interfaces and verifies the skeleton. Merge with a normal merge commit where the host permits it so the individual commits remain visible. Keep the source branch. Show the instructor the scenarios, sketch, pattern assignments, UML, and runnable skeleton.

## 4 Week 2 Student B patterns and integration

After Student A's merge, Student B updates their own branch.

~~~powershell
git switch student-b
git fetch origin
git merge origin/main
~~~

Student B stages these groups in order. The planner facade uses Student A's three patterns.

| Files to add | Git command | Commit command |
| --- | --- | --- |
| PlanStore.java, sample.properties | `git add -- PlanStore.java sample.properties` | `git commit -m "Adapt properties files to the planner store"` |
| PlanEvents.java | `git add -- PlanEvents.java` | `git commit -m "Refresh reminders from plan events"` |
| Planner.java | `git add -- Planner.java` | `git commit -m "Coordinate scheduling and persistence in planner facade"` |
| Main.java, Dashboard.java, index.html | `git add -- Main.java Dashboard.java index.html` | `git commit -m "Connect care planner to the browser interface"` |

~~~powershell
javac -encoding UTF-8 --release 21 -d out *.java
java -cp out Main
~~~

Open the app and demonstrate both scheduling choices, save and reload, and remove a reminder by completing a plan. Stop the server with Ctrl+C.

~~~powershell
git status
git push -u origin student-b
~~~

Student B opens a pull request into main. Student A reviews it and tries the full journey. Merge and keep student-b. Show the instructor the integrated version and persistence.

## 5 Week 3 tests and documentation

Student A syncs, creates PlannerTest.java and updates README.md, then runs these commands.

~~~powershell
git switch student-a
git fetch origin
git merge origin/main
javac -encoding UTF-8 --release 21 -d out *.java
java -cp out PlannerTest
git add -- PlannerTest.java README.md
git commit -m "Verify care planning scenarios and document setup"
git push
~~~

Open and merge a second Student A pull request after review. Student B then syncs and finishes the theory guide and team guide.

~~~powershell
git switch student-b
git fetch origin
git merge origin/main
git add -- PROJECT_EXPLAINED.md TEAM_GUIDE.md
git commit -m "Document project architecture and team workflow"
git push
~~~

Open and merge a second Student B pull request. The report is submitted separately through Moodle. Before submission, both partners replace the draft's role labels with actual names and contributions, refresh screenshots and results after their own changes, and record any sources or tools actually used.

## 6 Conflict recovery

If a merge stops, run `git status` to find the affected files. Agree which content to preserve, edit the file to remove conflict markers, stage the resolved file by name, and run `git commit` to complete the merge. Test before pushing. Use `git merge --abort` if you need to return to the clean pre-merge state. Use normal pushes to preserve each partner's history. Keep planner.properties and local demo files out of commits because they contain generated local state.

## 7 Verify and package from main

~~~powershell
git switch main
git pull --ff-only origin main
git status -sb
git log --all --graph --oneline --decorate
git shortlog -sn --all
javac -encoding UTF-8 --release 21 -d out *.java
java -cp out PlannerTest
git archive --format=zip --output=../pet-care-planner-java-source.zip HEAD
~~~

Unzip that archive to a fresh folder and run the README instructions. Confirm the repository link opens for the instructor. Upload the source ZIP, repository link, and completed PDF report through Moodle. The assignment specifies final materials by the end of course Week 9 and the defense in Week 10.

## 8 Prepare for the defense

Run the demo in README.md as a team. Each partner then points to their three pattern files, identifies the interface and concrete participants, traces the normal flow, and explains one limitation. Practice the changes in PROJECT_EXPLAINED.md on a temporary branch. The instructor awards 30 of the 100 points individually for understanding, contribution, and live modification. Keep the commits and review history that show who wrote each part.
