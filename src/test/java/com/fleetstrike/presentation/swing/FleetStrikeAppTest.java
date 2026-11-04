package com.fleetstrike.presentation.swing;

import com.fleetstrike.application.FleetStrikeApplicationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class FleetStrikeAppTest {

    @Test
    @DisplayName("Should instantiate FleetStrikeApp frame with all panels and menus")
    void shouldInitializeSwingApp() {
        assertDoesNotThrow(() -> {
            FleetStrikeApplicationService service = new FleetStrikeApplicationService();
            FleetStrikeApp app = new FleetStrikeApp(service);

            assertThat(app.getTitle()).contains("Fleet Strike");
            assertThat(app.getJMenuBar()).isNotNull();
            assertThat(app.getContentPane()).isNotNull();

            app.showMainMenu();
            app.dispose();
        });
    }
}
