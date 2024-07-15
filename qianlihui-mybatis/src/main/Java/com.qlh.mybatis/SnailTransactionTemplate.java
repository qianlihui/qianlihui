package com.qlh.mybatis;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.support.TransactionTemplate;

@Slf4j
@Data
public class SnailTransactionTemplate {

    private TransactionTemplate transactionTemplate;

    public void doTransaction(Runnable runnable) {
        doTransaction(runnable, 1);
    }

    public void doTransaction(Runnable runnable, int retry) {
        while (retry-- > 0) {
            try {
                transactionTemplate.execute((st) -> {
                    runnable.run();
                    return st;
                });
                return;
            } catch (RuntimeException e) {
                if (retry > 0) {
                    log.error("执行事务失败", e);
                    continue;
                } else {
                    throw e;
                }
            }
        }
    }
}
