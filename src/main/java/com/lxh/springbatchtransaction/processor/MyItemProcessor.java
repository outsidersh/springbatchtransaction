package com.lxh.springbatchtransaction.processor;

import com.lxh.springbatchtransaction.model.Transaction;
import org.springframework.batch.item.ItemProcessor;


public class MyItemProcessor implements ItemProcessor<Transaction,Transaction> {

    @Override
    public Transaction process(Transaction transaction) throws Exception {
        return transaction;
    }
//    public Transaction setValidator(Transaction item1){
//        return item1;
//    }


}
