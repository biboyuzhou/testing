package com.drcnet.highway.service.check;

import java.util.concurrent.locks.ReentrantLock;

/**
 * @Author jack
 * @Date: 2019/10/23 16:00
 * @Desc:
 **/
public class IntLock implements Runnable {
    public static ReentrantLock lock1 = new ReentrantLock();
    public static ReentrantLock lock2 = new ReentrantLock();

    int lock;

    public IntLock(int lock) {
        this.lock = lock;
    }

    @Override
    public void run() {
        try {
            if (lock == 1) {
                lock1.lockInterruptibly();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                lock2.lockInterruptibly();
                System.out.println(Thread.currentThread().getName()+", 执行完毕！");
            } else {
                lock2.lockInterruptibly();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                lock1.lockInterruptibly();
                System.out.println(Thread.currentThread().getName()+", 执行完毕！");
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            //查询当前线程是否保持此锁
            if (lock1.isHeldByCurrentThread()) {
                lock1.unlock();
            }
            if (lock2.isHeldByCurrentThread()) {
                lock2.unlock();
            }
            System.out.println(Thread.currentThread().getName()+", 退出。");
        }
    }

    public static void main(String[] args) throws InterruptedException {
        IntLock intLock1 = new IntLock(1);
        IntLock intLock2 = new IntLock(2);
        Thread thread1 = new Thread(intLock1, "线程1");
        Thread thread2 = new Thread(intLock2, "线程2");
        thread1.start();
        thread2.start();
        Thread.sleep(1000);
        thread2.interrupt();
    }
}
