package com.drcnet.highway.service.check;

import java.util.concurrent.locks.ReentrantLock;

/**
 * @Author jack
 * @Date: 2019/10/23 16:13
 * @Desc:
 **/
public class FairLock implements Runnable {
    public static ReentrantLock fairLock = new ReentrantLock(false);

    @Override
    public void run() {
        while (true) {
            try {
                fairLock.lock();
                System.out.println(Thread.currentThread().getName()+"，获得锁!");
            } finally {
                fairLock.unlock();
            }
        }
    }

    public static void main(String[] args) {
        FairLock fairLock = new FairLock();
        Thread t1 = new Thread(fairLock, "线程1");
        Thread t2 = new Thread(fairLock, "线程2");
        t1.start();
        t2.start();
    }
}
