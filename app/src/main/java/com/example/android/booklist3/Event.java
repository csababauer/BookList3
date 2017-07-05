package com.example.android.booklist3;

/**
 * Created by Csaba on 04/07/2017.
 */

public class Event {

    /** Title of the earthquake event */
    public final String title;

    /** Time that the earthquake happened (in milliseconds) */
    public final String authors;


    /**
     * Constructs a new {@link Event}.
     *
     * @param eventTitle is the title of the earthquake event
     */
    public Event(String eventTitle, String eventAuthors) {
        title = eventTitle;
        authors = eventAuthors;
    }
}