package org.example.finalexam_sdp;

import java.time.LocalDate;

class PlanBuilder {
    private Pet pet;
    private Procedure procedure;
    private Service service;
    private int minutes;
    private String strategy;
    private LocalDate requested;
    private LocalDate due;

    PlanBuilder forPet(String name) {
        pet = new Pet(name.strip());
        return this;
    }

    PlanBuilder withCare(Procedure procedure, Service service, int minutes) {
        this.procedure = procedure;
        this.service = service;
        this.minutes = minutes;
        return this;
    }

    PlanBuilder scheduled(String strategy, LocalDate requested, LocalDate due) {
        this.strategy = strategy;
        this.requested = requested;
        this.due = due;
        return this;
    }

    Plan build() {
        if (pet == null || pet.name().isBlank() || pet.name().length() > 60)
            throw new IllegalArgumentException("Pet name must have 1 to 60 characters.");
        if (procedure == null || minutes <= 0 || strategy == null || requested == null
                || due == null || due.isBefore(requested) || due.getYear() > 2099)
            throw new IllegalArgumentException("Choose valid care and a due date through 2099.");
        return new Plan(0, pet, procedure, service, strategy, requested, due, minutes, false);
    }
}
