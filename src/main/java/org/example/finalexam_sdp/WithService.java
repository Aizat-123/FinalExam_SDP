package org.example.finalexam_sdp;

import java.security.Provider;

interface Care {
    int duration();
}

record WithService(Care base, Provider.Service extra) implements Care {
    public int duration() { return base.duration() + extra.minutes(); }
}

