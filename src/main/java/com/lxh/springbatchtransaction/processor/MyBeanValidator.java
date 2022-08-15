package com.lxh.springbatchtransaction.processor;

import com.lxh.springbatchtransaction.model.Transaction;
import org.springframework.batch.item.validator.ValidatingItemProcessor;
import org.springframework.batch.item.validator.ValidationException;

public class MyBeanValidator extends ValidatingItemProcessor<Transaction> {

    @Override
    public Transaction process(Transaction item) throws ValidationException {
        /**
         * 需要执行super.process(item)才会调用自定义校验器
         */
//        super.process(item);
        /**
         * 对数据进行简单的处理
         */
//        if (item.getTransaction().equals("springboot")) {
//            item.setTransaction("springboot 系列还请看看我Jc");
//        } else {
//            item.setTransaction("未知系列");
//        }
        return item;

    }

}
