package com.moon.nugasam.data;

import java.util.List;

public class UndoData {

    public String date;

    public User me;

    public List<User> who;

    public UndoData(){}

    public UndoData(String date, User me, List<User> who) {
        this.date = date;
        this.me = me;
        this.who = who;
    }
}
