package blockchain.tasks;

/**
 * Виконує пункти: Генезис з [SN], PoW, вивід ланцюга.
 * 
 */
public class HYI_App 
{
    public static void main( String[] args )
    {
        System.out.println("=== START TASK 1 (" + HYI_Config.PIP + ") ===");
        System.out.println("Surname: " + HYI_Config.SN + " (Even length: " + HYI_Config.IS_SURNAME_EVEN + ")");
        System.out.println("Start Nonce: " + HYI_Config.START_NONCE + ", Target ends with: " + HYI_Config.MM_STR);

        HYI_Blockchain blockchain = new HYI_Blockchain();

        // 1.2 Генезис блок з попереднім хешем [SN]
        blockchain.hyi_createGenesisBlock();

        // Створимо кілька блоків для демонстрації PoW
        // (Транзакції поки порожні, бо це Завдання 1)
        blockchain.hyi_mineBlock("Miner1");
        blockchain.hyi_mineBlock("Miner1");
        blockchain.hyi_mineBlock("Miner1");

        // 1.5 Вивести ланцюг
        blockchain.hyi_printChain();
    }
}
