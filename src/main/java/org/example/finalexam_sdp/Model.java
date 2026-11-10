package org.example.finalexam_sdp;

import java.time.LocalDate;

record Pet(String name) {}
record Procedure(String name, int minutes) implements Care {
    public int duration() { return minutes; }
}
record Service(String name, int minutes) {}
record Plan(int id, Pet pet, Procedure procedure, Service service, String strategy,
            LocalDate requested, LocalDate due, int minutes, boolean done) {
    Plan withStatus(int id, boolean done) {
        return new Plan(id, pet, procedure, service, strategy, requested, due, minutes, done);
    }
}
record PlanInput(String pet, String procedure, String service, String strategy, String date) {}
