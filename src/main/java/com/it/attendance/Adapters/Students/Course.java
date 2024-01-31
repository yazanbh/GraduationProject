package com.it.attendance.Adapters.Students;

public class Course {
    String cName,cNumber,cSection;

    public Course() {
    }

    public Course(String cname, String cNumber, String section) {
        this.cName = cname;
        this.cNumber = cNumber;
        this.cSection = section;
    }

    public String getCname() {
        return cName;
    }

    public void setCname(String cname) {
        this.cName = cname;
    }

    public String getcNumber() {
        return cNumber;
    }

    public void setcNumber(String cNumber) {
        this.cNumber = cNumber;
    }

    public String getcSection() {
        return cSection;
    }

    public void setcSection(String section) {
        this.cSection = section;
    }
}
