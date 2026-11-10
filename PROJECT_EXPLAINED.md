# Pet Care Planner Architecture and Theory

The planner helps pet owners organize care in one browser page. A user names a pet, chooses a procedure and optional service, and selects how to schedule it. Saving creates a plan, updates in-app reminders, and writes the result to disk. Users can edit pending plans, complete or delete plans, and download the current summary.

## Data and rules

| Entity | Information | Relationship |
| --- | --- | --- |
| Pet | Name | Each plan stores a pet snapshot. A pet name can appear in several plans. |
| Procedure | Name and base minutes | Each plan has one procedure chosen from the catalog. |
| Service | Name and extra minutes | Each plan can have one optional service. |
| Plan | ID, requested and due dates, scheduling method, duration, and completion state | A saved care booking contains its pet, procedure, and optional service. |

The date strategy changes the due date while the requested date stays available for editing. Exact date keeps the selected date. Next weekday moves Saturday or Sunday forward to Monday and leaves weekdays in place. Reminders include pending plans due tomorrow or sooner, based on the server's local date when the page loads. A completion or deletion removes the plan from the reminder board. Completed plans can be exported or deleted.

## Source files

| File | Purpose |
| --- | --- |
| .gitignore | Keeps generated data, temporary write files, and compiled classes out of Git. |
| Model.java | Defines immutable records for pets, procedures, services, plans, and form input. |
| PlanBuilder.java | Collects the pieces of a care plan and checks construction rules. |
| SchedulingStrategy.java | Declares the scheduling interface and both scheduling algorithms. |
| Care.java | Adds extra service minutes to anything that implements Care. |
| PlanStore.java | Converts between typed plans and raw Java properties files through PlanStore. |
| PlanEvents.java | Publishes plan changes and updates the in-app reminder board. |
| Planner.java | Provides the Planner entry points and combines the patterns, validation, and persistence. |
| Main.java | Starts the local server, loads sample data on first launch, and connects HTTP routes to Planner. |
| Dashboard.java | Renders the page from the HTML template and escapes displayed values. |
| index.html | Renders the form, reminder list, errors, saved plans, and export link. |
| sample.properties | Supplies two fictional plans for a first launch. |
| PlannerTest.java | Exercises the business rules, storage, event handling, and HTTP routes. |
| README.md | Gives setup commands, sample data, and the demo. |
| TEAM_GUIDE.md | Shows how to use the two member branches and prepare the submission. |
| PROJECT_EXPLAINED.md | Explains design, files, limitations, and defense exercises. |
| planner.properties | Generated at runtime. Stores the active plan collection between launches. |

## Pattern participants and demonstration

| Pattern and category | Problem and participants | Demonstration | Suggested owner |
| --- | --- | --- | --- |
| Builder, creational | PlanBuilder collects pet, care, and schedule. Its build() method checks the assembled Plan before returning it. Planner is the client. | Create a plan, then try a pet name containing only spaces. | Student A |
| Strategy, behavioral | SchedulingStrategy is the contract. ExactDate and NextWeekday implement different date rules. Planner selects the strategy from the form value. | Save a Saturday using Exact date, then edit it to use Next weekday. | Student A |
| Decorator, structural | Care is the common interface, Procedure is the concrete component, and WithService wraps it. Both expose duration(). | Compare a 30-minute bath with a 40-minute bath plus brushing. | Student A |
| Adapter, structural | Planner expects PlanStore and deals in Plan values. PropertiesFile reads and writes Java Properties. PropertiesStoreAdapter converts values and calls PropertiesFile. | Save a plan, stop the server, and restart it. | Student B |
| Observer, behavioral | PlanEvents is the subject. PlanObserver defines the notification contract. ReminderBoard receives events and updates its pending collection. | Complete an overdue plan and show its reminder disappears while the completed plan remains visible. | Student B |
| Facade, structural | Planner gives the HTTP handlers simple operations and coordinates the catalog, strategies, builder, decorator, store, and events. | Trace a form submission from the save handler into configure() and back to the updated page. | Student B |

## How a save works

1. The form posts to /save. The handler parses inputs and calls Planner.configure.
2. Planner looks up the selected procedure, optional service, and scheduling strategy.
3. The strategy computes the due date. A decorator adds service minutes when applicable.
4. PlanBuilder constructs and validates the Plan. The facade assigns or preserves the ID.
5. Planner saves the proposed collection through PlanStore. PropertiesStoreAdapter converts the collection to properties, and PropertiesFile replaces the data file.
6. After a successful write, Planner updates its in-memory plans and publishes an event. ReminderBoard updates its pending plans.
7. The handler redirects to the dashboard. The browser loads saved plans and current reminders.

## Separation and SOLID

The browser layer manages HTTP and presentation. The planner owns business decisions. The storage adapter owns conversion to Java properties. PlanStore, Care, SchedulingStrategy, and PlanObserver are small interfaces. Planner depends on those interfaces where it needs interchangeable behavior. For example, a different store can be supplied to NewPlanner without changing the HTTP handlers. Adding a strategy requires an implementation plus a catalog registration. Synchronized planner operations keep a single server process from interleaving conflicting saves. Properties-file persistence is suitable for a small local application with one process owning its data file.

## Defense practice and limitations

- Builder groups construction and validation. A smaller object might need only a constructor. Practice adding a care note and passing it through the builder.
- Strategy isolates date algorithms. Available choices still need registration. Practice adding Next day.
- Decorator can be composed through Care. This UI allows one extra per plan. Practice creating a second wrapper in a test and checking the total.
- Adapter isolates storage format. This implementation rewrites the collection per change. Practice adding a temporary in-memory PlanStore for a test.
- Observer separates the reminder update from saving. Its subscribers execute synchronously. Practice registering a second observer that counts events.
- Facade simplifies handler calls. It may grow if unrelated use cases accumulate. Practice adding a count of pending plans without putting business rules in the HTML template.

Future work could add recurring plans, multiple extras per booking, separately managed pet profiles, and shared storage for multiple users.

