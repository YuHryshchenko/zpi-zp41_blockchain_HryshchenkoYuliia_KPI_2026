package blockchain.tasks;

import static spark.Spark.*;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.util.UUID;
import java.util.List;

public class HYI_Controller {
    public static void main(String[] args) {

        // Set port (default 4568, change if running multiple nodes)
        port(4568);

        HYI_Blockchain blockchain = new HYI_Blockchain();
        Gson gson = new Gson();

        // --- Lab 2 Endpoints ---

        // Endpoint to create a new transaction
        post("/transactions/new", (req, res) -> {
            try {
                // Create transaction object from request body
                HYI_Transaction transaction = gson.fromJson(req.body(), HYI_Transaction.class);
                
                // Add new transaction
                int index = blockchain.hyi_newTransaction(transaction.getSender(), transaction.getRecipient(), transaction.getAmount());
                
                res.status(201);
                return "Transaction will be added to Block " + index;
            } catch (JsonSyntaxException e) {
                res.status(400);
                return "Invalid JSON";
            }
        });

        // Endpoint to mine a new block
        get("/mine", (req, res) -> {
            HYI_Block lastBlock = blockchain.hyi_lastBlock();

            // Якщо це перший запуск і блоків ще немає (lastBlock == null),
            // створюємо Генезис-блок автоматично.
            if (lastBlock == null) {
                blockchain.hyi_createGenesisBlock();
                lastBlock = blockchain.hyi_lastBlock();
            }
            int lastProof = lastBlock.getProof();
            
            // Calculate Proof of Work
            int proofOfWork = blockchain.hyi_proofOfWork(lastProof);

            // Reward the miner (sender "0" signifies a mining reward)
            blockchain.hyi_newTransaction("0", UUID.randomUUID().toString().replace("-", ""), 1);

            String lastHash = HYI_Blockchain.hash(lastBlock);
            HYI_Block newBlock = blockchain.hyi_newBlock(proofOfWork, lastHash);

            // String json = gson.toJson(newBlock);
            JsonElement jsonElement = gson.toJsonTree(newBlock);
            JsonObject jsonObject = (JsonObject) jsonElement;

            // Додамо message
            jsonObject.addProperty("message", "New Block Forged");

            // серіалізуємо у String
            String json = jsonObject.toString();

            res.status(200);
            return json;
        });

        // Endpoint to get the full chain
        get("/chain", (req, res) -> {
            HYI_ChainResponse response = new HYI_ChainResponse(blockchain.hyi_getChain(), blockchain.hyi_getChain().size());
            return gson.toJson(response);
        });

        // --- Lab 4 Endpoints (Consensus) ---

        // Endpoint to register new nodes
        post("/nodes/register", (req, res) -> {
            try {
                List<String> nodes = gson.fromJson(req.body(), HYI_NodesResponse.class).nodes;
                for (String node : nodes) {
                    blockchain.hyi_registerNode(node);
                }
                return gson.toJson(blockchain.hyi_getNodes());
            } catch (Exception e) {
                res.status(400);
                return "Incorrect host address";
            }
        });

        // Endpoint to resolve conflicts (Consensus Algorithm)
        get("/nodes/resolve", (req, res) -> {
            boolean replaced = blockchain.resolveConflicts();
            HYI_ChainResponse response = new HYI_ChainResponse(blockchain.hyi_getChain(), blockchain.hyi_getChain().size());
            JsonElement jsonElement = gson.toJsonTree(response);
            JsonObject jsonObject = (JsonObject) jsonElement;
            if (replaced) {
                // Додамо message -  chain was replaced
                jsonObject.addProperty("message", "Chain was replaced");
                // серіалізуємо у String
                String json = jsonObject.toString();
                return json;
            } else {
                // Додамо message -  chain is authoritative
                jsonObject.addProperty("message", "Chain is authoritative");
                // серіалізуємо у String
                String json = jsonObject.toString();
                return json;
            }
        });
    }
}