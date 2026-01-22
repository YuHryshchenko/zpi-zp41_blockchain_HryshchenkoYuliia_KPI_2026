package blockchain.tasks;

import com.google.common.hash.Hashing;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class HYI_Blockchain {
    private List<HYI_Block> chain = new ArrayList<>();
    private Set<String> nodes = new HashSet<>();
    // "Мемпул" транзакцій
    private List<HYI_Transaction> currentTransactions = new ArrayList<>();
    
    // Для Завдання 2: Баланси (гаманець -> сума)
    private Map<String, Integer> balances = new HashMap<>();

    public HYI_Blockchain() {
        // Генезис блок не майниться за правилами PoW, а створюється вручну згідно Завдання 1.2
        // Але для коректності структури створимо його "порожнім"
    }

    // === Завдання 1.1: Префікс [PIP] (hyi_) у методах ===
    
    // Створення Генезис блоку (Завдання 1.2)
    public void hyi_createGenesisBlock() {
        // Попередній хеш = [SN]
        HYI_Block genesis = new HYI_Block(1, 0, HYI_Config.SN, new ArrayList<>());
        // Для генезису хеш може бути довільним або обчисленим
        genesis.setHash(hyi_calculateHash(genesis));
        chain.add(genesis);
        
        // Ініціалізація балансу "системи" або майнера, якщо потрібно
        balances.put("miner", 0);
    }

    // Створення нового блоку (Mining)
    public HYI_Block hyi_mineBlock(String minerAddress) {
        // === Завдання 2.1: Coinbase транзакція ===
        // Додаємо винагороду перед створенням блоку
        int reward = hyi_calculateReward(chain.size() + 1);
        if (reward > 0) {
            // Транзакція від "0" до майнера
            hyi_addTransaction(new HYI_Transaction("0", minerAddress, reward));
        }

        // Беремо транзакції з мемпулу
        List<HYI_Transaction> txs = new ArrayList<>(currentTransactions);
        
        HYI_Block lastBlock = chain.get(chain.size() - 1);
        String prevHash = lastBlock.getHash();

        // === Завдання 1.3, 1.4: PoW (Консенсус) ===
        int nonce = hyi_proofOfWork(prevHash, txs);
        
        // Створюємо блок
        HYI_Block newBlock = new HYI_Block(chain.size() + 1, nonce, prevHash, txs);
        String newHash = hyi_calculateHash(newBlock.getIndex(), newBlock.getTimestamp(), nonce, prevHash);
        newBlock.setHash(newHash);

        // Очищаємо мемпул
        currentTransactions.clear();
        // Додаємо в ланцюг
        chain.add(newBlock);
        
        // Оновлюємо баланси (Завдання 2)
        hyi_updateBalances(txs);

        return newBlock;
    }

    // === Завдання 1.3: Консенсус PoW ===
    private int hyi_proofOfWork(String prevHash, List<HYI_Transaction> txs) {
        int nonce = HYI_Config.START_NONCE; // [DDMM]
        int iterations = 0;
        
        System.out.print("Mining block... ");
        
        while (true) {
            // Формуємо хеш для перевірки
            // Для спрощення використовуємо поточний час (у реальності час змінюється, тут фіксуємо для ітерації)
            // Або просто хешуємо (prevHash + nonce) як у прикладі з PDF, але краще додати дані
            String input = prevHash + nonce + txs.toString(); 
            String hash = Hashing.sha256().hashString(input, StandardCharsets.UTF_8).toString();

            // Умова: наявність в кінці хешу [MM] (наприклад "03")
            if (hash.endsWith(HYI_Config.MM_STR)) {
                System.out.println("Success! Nonce: " + nonce + ", Iterations: " + iterations);
                System.out.println("Hash: " + hash);
                return nonce;
            }

            // === Завдання 1.4: Стратегія зміни Nonce ===
            if (HYI_Config.IS_SURNAME_EVEN) {
                // Лінійно-зростаючий
                nonce++;
            } else {
                // Випадковим чином (в межах до MAX_NONCE для безпеки, або просто рандом)
                nonce = ThreadLocalRandom.current().nextInt(HYI_Config.START_NONCE, HYI_Config.MAX_NONCE + 100000);
            }

            // Захист від нескінченного циклу (опціонально, згідно умови MAX_NONCE)
            if (HYI_Config.IS_SURNAME_EVEN && nonce > HYI_Config.MAX_NONCE) {
                 // Якщо дійшли до межі і не знайшли - скидаємо або продовжуємо (тут почнемо спочатку)
                 nonce = HYI_Config.START_NONCE;
            }
            
            iterations++;
        }
    }

    // Хешування
    private String hyi_calculateHash(int index, long time, long nonce, String prevHash) {
        String input = index + "" + time + nonce + prevHash;
        return Hashing.sha256().hashString(input, StandardCharsets.UTF_8).toString();
    }

    public List<HYI_Block> hyi_getChain() {
        return this.chain;
    }

    public Set<String> hyi_getNodes() {
        return this.nodes;
    }
    
    private String hyi_calculateHash(HYI_Block block) {
        return hyi_calculateHash(block.getIndex(), block.getTimestamp(), block.getNonce(), block.getPreviousHash());
    }

    public void hyi_registerNode(String netloc) {
        nodes.add(netloc);
    }

    // === Завдання 2.2: Розрахунок винагороди ===
    private int hyi_calculateReward(int blockHeight) {
        // Початкова винагорода [YYYY]
        double reward = HYI_Config.YYYY;
        int divisor = HYI_Config.MM + 1;
        
        // Кожні два блоки зменшується
        // Генезис - блок 1 (індекс 1), тому (blockHeight - 1) / 2
        int halvings = (blockHeight - 1) / 2;

        for (int i = 0; i < halvings; i++) {
            reward = reward / divisor;
        }
        
        return (int) reward;
    }

    // === Завдання 2: Мемпул та Транзакції ===
    public void hyi_addTransaction(HYI_Transaction tx) {
        // Перевірка балансу для користувацьких транзакцій (крім Coinbase "0")
        if (!tx.getSender().equals("0")) {
            int senderBalance = balances.getOrDefault(tx.getSender(), 0);
            if (senderBalance < tx.getAmount()) {
                System.out.println("Error: Not enough funds for " + tx.getSender());
                return;
            }
        }
        this.currentTransactions.add(tx);
        System.out.println("Added to Mempool: " + tx);
    }
    
    /**
     * Направляє нову транзакцію в наступний блок
     *
     * @param sender Адреса відправника
     * @param recipient Адреса отримувача
     * @param amount Сума
     * @return Індекса блока, що буде зберігати цю транзакцію
     */
    public int hyi_newTransaction(String sender, String recipient, int amount) {
        this.currentTransactions.add(new HYI_Transaction(sender, recipient, amount));
        return this.chain.size();
    }

    /**
     *
     * @param proof  Докази проведенної роботи
     * @param previousHash Хеш попереднього блока
     * @return Новий блок
     */
    public HYI_Block hyi_newBlock(int proof, String previousHash) {

        // створюмо копію списка
        List<HYI_Transaction> transactions = this.currentTransactions.stream().collect(Collectors.toList());

        // створюємо новий об'єкт блока
        HYI_Block newBlock = new HYI_Block(this.chain.size(), proof, previousHash, transactions);

        // очищаємо список транзакцій
        this.currentTransactions.clear();

        // додаємо новий блок у цепочку
        this.chain.add(newBlock);

        // повертаємо новий блок
        return newBlock;
    }

    /**
     *
     * @param block Блок
     * @return Хеш блока
     */
    public static String hash(HYI_Block block) {
        StringBuilder hashingInputBuilder = new StringBuilder();
        // додаємо параметри блока у змінну в певному незмінному по-
        hashingInputBuilder.append(block.getIndex())
                .append(block.getTimestamp()).append(block.getProof())
                .append(block.getPreviousHash());

        String hashingInput = hashingInputBuilder.toString();
        // генеруємо хеш блока на основі її полів за допомогою змінної
        return Hashing.sha256().hashString(hashingInput, StandardCharsets.UTF_8).toString();
    }

    public HYI_Block hyi_lastBlock() {
        return this.chain.size() > 0 ? this.chain.get(this.chain.size()-1) : null;
    }

    /**
     * Проста перевірка алгоритму: Пошук числа p`, так як hash(pp`)
     * містить 4 заголовних нуля, де p — попередній p є попереднім
     * доказом, а p`— новим
     *
     * @param lastProofOfWork
     * @return int
     */
    public int hyi_proofOfWork(int lastProofOfWork) {
        int proof = 0;
        while (!hyi_isProofValid(lastProofOfWork, proof)) {
            proof++;
        }
        return proof;
    }

    /**
     * Підтвердження доказу: Чи містить hash(lastProof, proof) 4 заголовних нуля
     *
     * @param lastProof
     * @param proof
     * @return
     */

    private boolean hyi_isProofValid(int lastProof, int proof) {
        String guessString = Integer.toString(lastProof) + Integer.toString(proof);
        String guessHash = Hashing.sha256().hashString(guessString,
                StandardCharsets.UTF_8).toString();
        return guessHash.endsWith("0000");
    }

    private void hyi_updateBalances(List<HYI_Transaction> txs) {
        for (HYI_Transaction tx : txs) {
            // Віднімаємо у відправника (якщо не Coinbase)
            if (!tx.getSender().equals("0")) {
                balances.put(tx.getSender(), balances.get(tx.getSender()) - tx.getAmount());
            }
            // Додаємо отримувачу
            balances.put(tx.getRecipient(), balances.getOrDefault(tx.getRecipient(), 0) + tx.getAmount());
        }
    }

    public void hyi_printChain() {
        System.out.println("\n=== BLOCKCHAIN STATE ===");
        for (HYI_Block block : chain) {
            System.out.println(block);
        }
    }

    public void hyi_printBalances() {
        System.out.println("\n=== BALANCES ===");
        balances.forEach((k, v) -> System.out.println("Wallet " + k + ": " + v));
    }
    
    public void hyi_printMempool() {
        System.out.println("\n=== MEMPOOL ===");
        if (currentTransactions.isEmpty()) System.out.println("Empty");
        else currentTransactions.forEach(System.out::println);
    }

    public boolean validChain(List<HYI_Block> chain) {
        for (int i = 1; i < chain.size(); i++) {
            HYI_Block lastBlock = chain.get(i - 1);
            HYI_Block currentBlock = chain.get(i);
            if (!currentBlock.getPreviousHash().equals(hash(lastBlock))) {
                System.out.println("Hash don't match");
                return false;
            }
            if (!hyi_isProofValid(lastBlock.getProof(), currentBlock.getProof())) {
                System.out.println("Proof is not valid");
                return false;
            }
        }
        return true;
    }

    public boolean resolveConflicts() {
        Gson gson = new Gson();
        int maxLen = this.chain.size();
        List<HYI_Block> newChain = this.chain;
        try {
            for (String host : this.nodes) {
                URL url;
                url = new URL(host + "/chain");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                int status = con.getResponseCode();
                if (status == 200) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    String inputLine;
                    StringBuffer content = new StringBuffer();
                    while ((inputLine = in.readLine()) != null) {
                        content.append(inputLine);
                    }
                    in.close();
                    con.disconnect();
                    HYI_ChainResponse response = gson.fromJson(content.toString(), HYI_ChainResponse.class);
                    if (response.length > maxLen && validChain(response.chain)) {
                        maxLen = response.length;
                        newChain = response.chain;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (newChain != this.chain) {
            this.chain = newChain;
        }
        return newChain == this.chain;
    }
}