package com.it.attendance.Adapters.CoursesHomePageLecturer;

public class course {
    String cName,cNumber,cSection;

    public course() {
    }

    public course(String cname, String cNumber, String section) {
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
