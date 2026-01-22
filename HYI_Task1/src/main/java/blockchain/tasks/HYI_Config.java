package blockchain.tasks;

public class HYI_Config {
    // === ВАШІ ВИХІДНІ ДАНІ (ЗМІНІТЬ ТУТ) ===
    public static final String SN = "Hryshchenko"; // Прізвище
    public static final String PIP = "HYI";        // Ініціали
    public static final int DD = 12;               // День
    public static final int MM = 3;                // Місяць
    public static final int YYYY = 1981;           // Рік
    
    // Автоматично обчислювані параметри
    public static final String MM_STR = String.format("%02d", MM); // "03"
    public static final int START_NONCE = Integer.parseInt(String.format("%d%02d", DD, MM)); // 1203
    public static final int MAX_NONCE = Integer.parseInt(String.format("%02d%d", MM, YYYY)); // 31981
    
    // Перевірка парності прізвища для Завдання 1.4
    public static final boolean IS_SURNAME_EVEN = SN.length() % 2 == 0;
}
