package com.quantlab.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WatchlistTest {

    @Test
    void createWatchlist() {
        Watchlist watchlist = new Watchlist("My Portfolio", "user-1");

        assertEquals("My Portfolio", watchlist.getName());
        assertEquals("user-1", watchlist.getUserId());
        assertNotNull(watchlist.getCreatedAt());
    }

    @Test
    void updateWatchlistName() {
        Watchlist watchlist = new Watchlist("Old Name", "user-1");
        watchlist.setName("New Name");

        assertEquals("New Name", watchlist.getName());
    }
}
