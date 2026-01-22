package blockchain.tasks;

import java.util.ArrayList;
import java.util.List;

public class HYI_ChainResponse {
    public List<HYI_Block> chain = new ArrayList<>();
    public int length;

    public HYI_ChainResponse(List<HYI_Block> chain, int length) {
        this.chain = chain;
        this.length = length;
    }
}
