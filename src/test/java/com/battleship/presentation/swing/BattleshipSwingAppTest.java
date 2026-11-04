package com.battleship.presentation.swing;

import com.battleship.application.BattleshipApplicationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class BattleshipSwingAppTest {

    @Test
    @DisplayName("Should instantiate BattleshipSwingApp frame with all panels and menus")
    void shouldInitializeSwingApp() {
        assertDoesNotThrow(() -> {
            BattleshipApplicationService service = new BattleshipApplicationService();
            BattleshipSwingApp app = new BattleshipSwingApp(service);

            assertThat(app.getTitle()).contains("Battleship");
            assertThat(app.getJMenuBar()).isNotNull();
            assertThat(app.getContentPane()).isNotNull();

            app.showMainMenu();
            app.dispose();
        });
    }
}
