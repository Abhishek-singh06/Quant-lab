package com.quantlab.watchlist.service;

import com.quantlab.model.Watchlist;
import com.quantlab.repository.WatchlistRepository;
import com.quantlab.watchlist.dto.WatchlistDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class WatchlistServiceTest {

    private WatchlistRepository repository;
    private WatchlistService service;

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(WatchlistRepository.class);
        service = new WatchlistService(repository);
    }

    @Test
    @DisplayName("Should create watchlist successfully with uppercase symbols")
    void testCreateWatchlist() {
        Watchlist mockSaved = new Watchlist("Tech Portfolio", "user_1", "Tech stocks", List.of("TCS", "INFY"));
        mockSaved.setId(1L);

        when(repository.save(any(Watchlist.class))).thenReturn(mockSaved);

        WatchlistDTO result = service.createWatchlist("Tech Portfolio", "user_1", "Tech stocks", List.of("tcs", "infy"));

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Tech Portfolio", result.name());
        assertEquals("user_1", result.userId());
        assertEquals(2, result.itemCount());
        assertTrue(result.symbols().contains("TCS"));
        assertTrue(result.symbols().contains("INFY"));
    }

    @Test
    @DisplayName("Should reject empty watchlist name")
    void testCreateWatchlistEmptyName() {
        assertThrows(IllegalArgumentException.class, () ->
                service.createWatchlist("", "user_1", null, List.of("TCS")));
    }

    @Test
    @DisplayName("Should seed default Nifty 50 watchlist when user has none")
    void testGetWatchlistsByUserSeedsDefault() {
        when(repository.findByUserId("user_2")).thenReturn(List.of());

        Watchlist defaultWl = new Watchlist("NIFTY 50 Core", "user_2", "Benchmark", List.of("RELIANCE", "TCS"));
        defaultWl.setId(10L);
        when(repository.save(any(Watchlist.class))).thenReturn(defaultWl);

        List<WatchlistDTO> result = service.getWatchlistsByUser("user_2");

        assertEquals(1, result.size());
        assertEquals("NIFTY 50 Core", result.get(0).name());
        verify(repository).save(any(Watchlist.class));
    }

    @Test
    @DisplayName("Should add symbol to watchlist without duplicates")
    void testAddSymbol() {
        Watchlist wl = new Watchlist("My List", "user_1");
        wl.setId(5L);
        wl.setSymbols(new ArrayList<>(List.of("RELIANCE")));

        when(repository.findById(5L)).thenReturn(Optional.of(wl));
        when(repository.save(any(Watchlist.class))).thenReturn(wl);

        Optional<WatchlistDTO> updated = service.addSymbol(5L, "tcs");

        assertTrue(updated.isPresent());
        assertEquals(2, updated.get().itemCount());
        assertTrue(updated.get().symbols().contains("TCS"));

        // Adding duplicate symbol should not create duplicate
        Optional<WatchlistDTO> duplicate = service.addSymbol(5L, "TCS");
        assertTrue(duplicate.isPresent());
        assertEquals(2, duplicate.get().itemCount());
    }

    @Test
    @DisplayName("Should remove symbol from watchlist")
    void testRemoveSymbol() {
        Watchlist wl = new Watchlist("My List", "user_1");
        wl.setId(5L);
        wl.setSymbols(new ArrayList<>(List.of("RELIANCE", "TCS")));

        when(repository.findById(5L)).thenReturn(Optional.of(wl));
        when(repository.save(any(Watchlist.class))).thenReturn(wl);

        Optional<WatchlistDTO> updated = service.removeSymbol(5L, "RELIANCE");

        assertTrue(updated.isPresent());
        assertEquals(1, updated.get().itemCount());
        assertFalse(updated.get().symbols().contains("RELIANCE"));
    }

    @Test
    @DisplayName("Should delete watchlist by ID")
    void testDeleteWatchlist() {
        when(repository.existsById(10L)).thenReturn(true);
        doNothing().when(repository).deleteById(10L);

        boolean deleted = service.deleteWatchlist(10L);
        assertTrue(deleted);
        verify(repository).deleteById(10L);

        when(repository.existsById(999L)).thenReturn(false);
        boolean notDeleted = service.deleteWatchlist(999L);
        assertFalse(notDeleted);
    }
}
