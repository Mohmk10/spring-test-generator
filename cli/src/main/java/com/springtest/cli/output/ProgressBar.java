package com.springtest.cli.output;

public class ProgressBar {

    private int total;
    private int current;
    private long startTime;

    public void start(int total) {
        this.total = total;
        this.current = 0;
        this.startTime = System.currentTimeMillis();
        render();
    }

    public void update(int current) {
        this.current = current;
        render();
    }

    public void finish() {
        this.current = this.total;
        render();
        System.out.println();

        long elapsed = System.currentTimeMillis() - startTime;
        System.out.printf("Completed in %.2f seconds%n", elapsed / 1000.0);
    }

    private void render() {
        int percentage = total > 0 ? (current * 100) / total : 0;
        int barLength = 40;
        int filled = (percentage * barLength) / 100;

        StringBuilder bar = new StringBuilder("[");
        for (int i = 0; i < barLength; i++) {
            bar.append(i < filled ? "=" : " ");
        }
        bar.append("]");

        System.out.print("\r" + bar + " " + percentage + "% (" + current + "/" + total + ")");
        System.out.flush();
    }
}