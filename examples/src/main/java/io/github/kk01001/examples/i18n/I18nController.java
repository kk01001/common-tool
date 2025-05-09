package io.github.kk01001.examples.i18n;

import io.github.kk01001.i18n.provider.I18nManager;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class I18nController {

    private final I18nManager i18nManager;

    @PostMapping("/i18n/message")
    public void addMessage(@RequestParam String locale,
                           @RequestParam String code,
                           @RequestParam String message) {
        i18nManager.addMessage(locale, code, message);
    }

    @PostMapping("/i18n/messages")
    public void addMessages(@RequestParam String locale,
                            @RequestBody Map<String, String> messages) {
        i18nManager.addMessages(locale, messages);
    }

    @DeleteMapping("/i18n/message")
    public void removeMessage(@RequestParam String locale,
                              @RequestParam String code) {
        i18nManager.removeMessage(locale, code);
    }

    @PostMapping("/i18n/refresh")
    public void refresh() {
        i18nManager.refresh();
    }
}