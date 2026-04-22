package by.step.enums;

import lombok.Getter;

/**
 * Перечисление поддерживаемых социальных платформ для ссылок в профиле.
 * Определяет префиксы URL для построения полных ссылок.
 *
 * @author Skin Market Team
 * @version 1.0
 */
@Getter
public enum SocialPlatform {

    /**
     * Социальная сеть ВКонтакте.
     */
    VK("ВКонтакте", "https://vk.com/"),

    /**
     * Мессенджер Telegram.
     */
    TELEGRAM("Telegram", "https://t.me/"),

    /**
     * Социальная сеть Instagram.
     */
    INSTAGRAM("Instagram", "https://instagram.com/"),

    /**
     * Платформа Discord.
     */
    DISCORD("Discord", "https://discord.gg/"),

    /**
     * Сайт NameMC для Minecraft профилей.
     */
    NAMEMC("NameMC", "https://namemc.com/profile/");

    /**
     * Отображаемое название платформы.
     */
    private final String displayName;

    /**
     * Префикс URL для построения полной ссылки.
     */
    private final String urlPrefix;

    SocialPlatform(String displayName, String urlPrefix) {
        this.displayName = displayName;
        this.urlPrefix = urlPrefix;
    }
}