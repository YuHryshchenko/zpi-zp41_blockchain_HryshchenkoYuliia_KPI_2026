package blockchain.tasks;

/**
 * Виконує пункти: Coinbase, зменшення нагороди,
 * цикл майнінгу ([DD]+1)%13, транзакції [DD] монет, баланси.
 */
public class HYI_App 
{
    public static void main( String[] args )
    {
        System.out.println("=== START TASK 2 (" + HYI_Config.PIP + ") ===");
        
        HYI_Blockchain blockchain = new HYI_Blockchain();
        blockchain.hyi_createGenesisBlock(); // Block 1 (Genesis)

        String myWallet = "MyWallet";
        String friendWallet = "FriendWallet";

        // Розрахунок кількості блоків для майнінгу: ([DD]+1) mod 13
        int blocksToMine = (HYI_Config.DD + 1) % 13;
        System.out.println("Blocks to mine: " + blocksToMine);
        
        // 2.3 Майнінг блоків з отриманням винагороди
        for (int i = 0; i < blocksToMine; i++) {
            System.out.println("\nMining block #" + (i + 2) + "..."); // +2 бо генезис це #1
            blockchain.hyi_mineBlock(myWallet);
            
            // Після кожного блоку виводимо баланс, щоб бачити зменшення нагороди
            blockchain.hyi_printBalances();
        }

        // 2.4 Додавання транзакцій у Мемпул
        System.out.println("\n--- Creating Transactions ---");
        // Передаємо [DD] монет
        int transferAmount = HYI_Config.DD;
        
        // Перевіряємо, чи є гроші (ми їх намайнили вище)
        blockchain.hyi_addTransaction(new HYI_Transaction(myWallet, friendWallet, transferAmount));
        
        // Вивід стану мемпулу
        blockchain.hyi_printMempool();

        // 2.5 Намайнити блок із транзакціями
        System.out.println("\n--- Mining Block with Transactions ---");
        // Я знову майню, отримую нагороду + проводжу транзакцію
        blockchain.hyi_mineBlock(myWallet);

        // 2.6 Фінальний вивід
        blockchain.hyi_printChain();
        blockchain.hyi_printBalances();
        blockchain.hyi_printMempool(); // Має бути пустим
    }
}
