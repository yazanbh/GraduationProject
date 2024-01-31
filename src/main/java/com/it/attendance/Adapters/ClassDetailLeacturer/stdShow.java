package com.it.attendance.Adapters.ClassDetailLeacturer;

public class stdShow {
    String email,name;

    public stdShow(String email, String name) {
        this.email = email;
        this.name = name;
    }
    public stdShow(){}

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
