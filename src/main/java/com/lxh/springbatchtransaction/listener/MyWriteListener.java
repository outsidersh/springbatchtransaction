package com.lxh.springbatchtransaction.listener;

import com.lxh.springbatchtransaction.model.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ItemWriteListener;

import java.util.List;

import static java.lang.String.format;

public class MyWriteListener implements ItemWriteListener<Transaction> {
    private Logger logger = LoggerFactory.getLogger(MyWriteListener.class);

    @Override
    public void beforeWrite(List<? extends Transaction> items) {
    }

    @Override
    public void afterWrite(List<? extends Transaction> items) {
    }

    @Override
    public void onWriteError(Exception exception, List<? extends Transaction> items) {
        try {
            logger.info(format("%s%n", exception.getMessage()));
            for (Transaction message : items) {
                logger.info(format("Failed writing BlogInfo : %s", message.toString()));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
