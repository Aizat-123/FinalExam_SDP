package org.example.finalexam_sdp;
interface Care {
    int duration();
}

record WithService(Care base, Service extra) implements Care {
    public int duration() { return base.duration() + extra.minutes(); }
}
