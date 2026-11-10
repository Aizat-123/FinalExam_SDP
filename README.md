# Pet Care Planner

A local web app for scheduling pet procedures, adding optional services, viewing in-app reminders, completing care, and downloading a summary. The app uses the JDK HTTP server, a small HTML renderer, and the Java Properties format. Maven, Gradle, and external libraries are unnecessary.

## Setup and launch

Install JDK 21 or later and Git. Use a recent browser. Run these commands in the folder containing Main.java.

~~~powershell
java -version
javac -version
javac -encoding UTF-8 --release 21 -d out *.java
java -cp out PlannerTest
java -cp out Main
~~~

Open http://127.0.0.1:8080 in your browser. Stop the server with Ctrl+C. Keep the terminal open while using the app. The app runs with the JDK alone.

## Sample data and demo

The first launch copies sample.properties to planner.properties. Milo has an overdue bath with brushing. Luna has a completed checkup. The deliberately old sample date keeps Milo visible in Reminders whenever the project is demonstrated. Restarting keeps the edits you made.

1. Enter Coco, Bath, Brushing, and Saturday 2024-01-06. Choose Exact date and save. The plan shows January 6 and 40 minutes.
2. Edit Coco's plan and choose Next weekday. Save and check that the due date becomes Monday, January 8.
3. Mark Coco complete. The saved plan changes status and the reminder disappears.
4. Download the care summary and open the text file. Restart the app and confirm that Coco remains completed.
5. Create a disposable plan and delete it. Enter spaces as a pet name to try server-side validation.

For a fresh copy without touching planner.properties, use `java -cp out Main demo-local.properties` and keep that local file out of Git. See TEAM_GUIDE.md for Git and submission, and PROJECT_EXPLAINED.md for the architecture and theory.
