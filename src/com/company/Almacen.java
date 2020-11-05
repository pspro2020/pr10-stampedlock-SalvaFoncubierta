package com.company;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.StampedLock;

public class Almacen {

    private ArrayList<Integer> lista = new ArrayList<>();
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    private final StampedLock stampedLock = new StampedLock();
    public ArrayList<Integer> getLista() {
        return lista;
    }

    public void consultProduct(int consulta) throws InterruptedException {
        long stamp = stampedLock.tryOptimisticRead();
        int contador = 0;
        int tamanho = lista.size();
        for (int i = 0; i < tamanho; i++) {
            if(lista.get(i) == consulta + 1){
                contador++;
            }
        }
        if (!stampedLock.validate(stamp)) {
            stamp = stampedLock.readLock();
            contador = 0;
            try {
                tamanho = lista.size();
                for (int i = 0; i < tamanho; i++) {
                    if(lista.get(i) == consulta + 1){
                        contador++;
                    }
                }
                System.out.printf("%s: el número %d aparece %d veces %s\n",Thread.currentThread().getName(), consulta + 1,
                        contador, LocalTime.now().format(dateTimeFormatter));
            } finally {
                stampedLock.unlockRead(stamp);
            }
        } else {
            System.out.printf("%s: el número %d aparece %d veces %s\n",Thread.currentThread().getName(), consulta + 1,
                    contador, LocalTime.now().format(dateTimeFormatter));
        }

    }

    public void addProduct (int product) throws InterruptedException {
        long stamp = stampedLock.writeLock();
        try {
            lista.add(product);
            System.out.printf("Se ha añadido el producto %d a la lista %s\n",
                    product, LocalTime.now().format(dateTimeFormatter));
        } finally {
            stampedLock.unlockWrite(stamp);
        }
    }

}
