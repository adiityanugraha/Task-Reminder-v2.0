package com.example.taskreminder2.data.model;

/**
 * Model Team (Firestore, Team Mode). Bukan Room Entity — Team Mode tidak
 * memakai Room (lihat blueprint Bagian 3.3).
 */
public class Team {

    public String id;
    public String name;
    public String inviteCode;
    public String ownerId;

    public Team() {
    }

    public Team(String id, String name, String inviteCode, String ownerId) {
        this.id = id;
        this.name = name;
        this.inviteCode = inviteCode;
        this.ownerId = ownerId;
    }
}
