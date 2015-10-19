package de.unisaarland.UniApp.staff;

import java.io.Serializable;

public class StaffInfo implements Serializable {
    private final String name;
    private final String gender;
    private final String academicDegree;
    private final String building;
    private final String room;
    private final String phone;
    private final String fax;
    private final String email;

    public StaffInfo(String name, String gender, String academicDegree,
                     String building, String room, String phone, String fax,
                     String email) {
        this.name = name;
        this.gender = gender;
        this.academicDegree = academicDegree;
        this.building = building;
        this.room = room;
        this.phone = phone;
        this.fax = fax;
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public String getGender() {
        return gender;
    }

    public String getAcademicDegree() {
        return academicDegree;
    }

    public String getBuilding() {
        return building;
    }

    public String getRoom() {
        return room;
    }

    public String getPhone() {
        return phone;
    }

    public String getFax() {
        return fax;
    }

    public String getEmail() {
        return email;
    }
}
