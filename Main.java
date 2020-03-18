package com.marcschwaiger.astar;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;

public class Main implements ActionListener, KeyEventDispatcher {

    //GUI variables
    private final String TITLE_BASE = "A* algorithm visualization";
    private final int WIDTH = 20, HEIGHT = 20;
    private JFrame jFrame;
    private JButton[][] buttons = new JButton[WIDTH][HEIGHT];
    private FieldType[][] fieldTypes = new FieldType[WIDTH][HEIGHT];
    private int clickMode; // 0 is place blocks; 1 is to clear blocks; 2 is place start; 3 is place end

    //algorithm variables
    private boolean algorithmRunning, foundEnd;
    private int currentStep;
    private int[] start, end, bestBackPos;

    public Main() {
        //Add Keyboard events
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(this);

        //Setup GUI
        jFrame = new JFrame(TITLE_BASE);
        LayoutManager mgr = new GridLayout(WIDTH, HEIGHT);
        jFrame.setLayout(mgr);
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                JButton button = new JButton("");
                button.addActionListener(this);
                jFrame.add(button);
                buttons[x][y] = button;
                fieldTypes[x][y] = FieldType.UNCHECKED;
            }
        }
        jFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        jFrame.setSize(WIDTH * 50, HEIGHT * 50);
        jFrame.setResizable(false);
        jFrame.setAlwaysOnTop(true);
        jFrame.setVisible(true);
    }

    public static void main(String[] args) throws InterruptedException {
        new Main().run();
    }

    //Main loop
    private void run() throws InterruptedException {
        while (true) {
            if (algorithmRunning) {
                runAlgorithm();
            }
            updateColors();
            updateStatusBar();
            Thread.sleep(50L);
        }
    }

    private void runAlgorithm() {
        if (foundEnd) {
            if (currentStep != 0) {
                currentStep--;
                ArrayList<int[]> poss = new ArrayList<>();

                if (bestBackPos == null) {
                    poss.addAll(Arrays.asList(getReachablePoss(end[0], end[1], fieldTypes, true)));
                } else {
                    fieldTypes[bestBackPos[0]][bestBackPos[1]] = FieldType.USED;
                    poss.addAll(Arrays.asList(getReachablePoss(bestBackPos[0], bestBackPos[1], fieldTypes, true)));
                }

                int lowest = Integer.MAX_VALUE;
                for (int[] pos : poss) {
                    JButton button = this.buttons[pos[0]][pos[1]];
                    if (button.getText().equals("S")) {
                        break;
                    }
                    int current = Integer.parseInt(button.getText());
                    if (current < lowest) {
                        lowest = current;
                        bestBackPos = pos;
                    }
                }

            }
        } else {
            ArrayList<int[]> poss = new ArrayList<>();
            if (currentStep == 0) {
                poss.addAll(Arrays.asList(getReachablePoss(start[0], start[1], fieldTypes, false)));
            } else {
                for (int x = 0; x < WIDTH; x++) {
                    for (int y = 0; y < HEIGHT; y++) {
                        JButton button = buttons[x][y];
                        if (button.getText().equals(String.valueOf(currentStep))) {
                            poss.addAll(Arrays.asList(getReachablePoss(x, y, fieldTypes, false)));
                        }
                    }
                }
            }
            currentStep++;
            for (int[] pos : poss) {
                if (fieldTypes[pos[0]][pos[1]] == FieldType.END) {
                    foundEnd = true;
                    break;
                }
                JButton button = buttons[pos[0]][pos[1]];
                button.setText(String.valueOf(currentStep));
                fieldTypes[pos[0]][pos[1]] = FieldType.CHECKED;
            }
        }
    }

    private void updateColors() {
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                JButton button = buttons[x][y];
                switch (fieldTypes[x][y]) {
                    case UNCHECKED:
                        button.setBackground(Color.GRAY);
                        break;
                    case CHECKED:
                        button.setBackground(Color.GREEN);
                        break;
                    case USED:
                        button.setBackground(Color.RED);
                        break;
                    case BLOCKED:
                        button.setBackground(Color.BLACK);
                        break;
                    case START:
                    case END:
                        button.setBackground(Color.CYAN);
                        break;
                }
            }
        }
    }

    private void updateStatusBar() {
        if (this.bestBackPos != null && currentStep == 0) {
            jFrame.setTitle(TITLE_BASE + " | finished");
        } else if (this.algorithmRunning) {
            jFrame.setTitle(TITLE_BASE + " | running");
        } else {
            switch (clickMode) {
                case 0: //place blocks
                    jFrame.setTitle(TITLE_BASE + " | place blocks");
                    break;
                case 1: //clear blocks
                    jFrame.setTitle(TITLE_BASE + " | clear blocks");
                    break;
                case 2: //place start
                    jFrame.setTitle(TITLE_BASE + " | place start");
                    break;
                case 3: //place end
                    jFrame.setTitle(TITLE_BASE + " | place end");
                    break;
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!(this.bestBackPos != null && currentStep == 0) && !this.algorithmRunning && e.getSource() instanceof JButton) {
            JButton button = (JButton) e.getSource();
            int[] pos = this.getPosFromButton(button);
            if (pos != null) {
                boolean isStart = buttons[pos[0]][pos[1]].getText().equals("S");
                boolean isEnd = buttons[pos[0]][pos[1]].getText().equals("E");
                if (isStart || isEnd) {
                    buttons[pos[0]][pos[1]].setText("");
                    if (isStart)
                        this.start = null;
                    if (isEnd)
                        this.end = null;
                }
                switch (clickMode) {
                    case 0: //place blocks
                        fieldTypes[pos[0]][pos[1]] = FieldType.BLOCKED;
                        break;
                    case 1: //clear blocks
                        if (buttons[pos[0]][pos[1]].getText().equals("S") || buttons[pos[0]][pos[1]].getText().equals("E")) {
                            buttons[pos[0]][pos[1]].setText("");
                        }
                        fieldTypes[pos[0]][pos[1]] = FieldType.UNCHECKED;
                        break;
                    case 2: //place start
                        for (int x = 0; x < WIDTH; x++) {
                            for (int y = 0; y < HEIGHT; y++) {
                                if (buttons[x][y].getText().equals("S")) {
                                    buttons[x][y].setText("");
                                    fieldTypes[x][y] = FieldType.UNCHECKED;
                                }
                            }
                        }
                        buttons[pos[0]][pos[1]].setText("S");
                        fieldTypes[pos[0]][pos[1]] = FieldType.START;
                        start = pos;
                        break;
                    case 3: //place end
                        for (int x = 0; x < WIDTH; x++) {
                            for (int y = 0; y < HEIGHT; y++) {
                                if (buttons[x][y].getText().equals("E")) {
                                    buttons[x][y].setText("");
                                    fieldTypes[x][y] = FieldType.UNCHECKED;
                                }
                            }
                        }
                        buttons[pos[0]][pos[1]].setText("E");
                        fieldTypes[pos[0]][pos[1]] = FieldType.END;
                        end = pos;
                        break;
                }
            }
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {
        if (e.getID() == KeyEvent.KEY_PRESSED) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_S: //start
                    if (start != null && end != null)
                        this.algorithmRunning = true;
                    break;
                case KeyEvent.VK_R: //reset
                    reset();
                    break;
            }
            switch (e.getKeyChar()) {
                case KeyEvent.VK_0: //place blocks
                    clickMode = 0;
                    break;
                case KeyEvent.VK_1: //clear blocks
                    clickMode = 1;
                    break;
                case KeyEvent.VK_2: //place start
                    clickMode = 2;
                    break;
                case KeyEvent.VK_3: //place end
                    clickMode = 3;
                    break;
            }
        }
        return false;
    }

    public void reset() {
        this.start = null;
        this.end = null;
        this.bestBackPos = null;
        this.currentStep = 0;
        this.algorithmRunning = false;
        this.foundEnd = false;
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                buttons[x][y].setText("");
                fieldTypes[x][y] = FieldType.UNCHECKED;
            }
        }
    }

    private int[] getPosFromButton(JButton button) {
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                if (button == buttons[x][y]) {
                    return new int[]{ x, y };
                }
            }
        }
        return null;
    }

    private int[][] getReachablePoss(int x, int y, FieldType[][] fieldTypes, boolean backwards) {
        ArrayList<int[]> poss = new ArrayList<>();

        if (backwards) {
            //move left
            if (x - 1 >= 0 && (fieldTypes[x - 1][y] == FieldType.CHECKED || fieldTypes[x - 1][y] == FieldType.START)) {
                poss.add(new int[]{ x - 1, y });
            }

            //move right
            if (x + 1 < WIDTH && (fieldTypes[x + 1][y] == FieldType.CHECKED || fieldTypes[x + 1][y] == FieldType.START)) {
                poss.add(new int[]{ x + 1, y });
            }

            //move up
            if (y - 1 >= 0 && (fieldTypes[x][y - 1] == FieldType.CHECKED || fieldTypes[x][y - 1] == FieldType.START)) {
                poss.add(new int[]{ x, y - 1 });
            }

            //move down
            if (y + 1 < HEIGHT && (fieldTypes[x][y + 1] == FieldType.CHECKED || fieldTypes[x][y + 1] == FieldType.START)) {
                poss.add(new int[]{ x, y + 1 });
            }
        } else {
            //move left
            if (x - 1 >= 0 && (fieldTypes[x - 1][y] == FieldType.UNCHECKED || fieldTypes[x - 1][y] == FieldType.END)) {
                poss.add(new int[]{ x - 1, y });
            }

            //move right
            if (x + 1 < WIDTH && (fieldTypes[x + 1][y] == FieldType.UNCHECKED || fieldTypes[x + 1][y] == FieldType.END)) {
                poss.add(new int[]{ x + 1, y });
            }

            //move up
            if (y - 1 >= 0 && (fieldTypes[x][y - 1] == FieldType.UNCHECKED || fieldTypes[x][y - 1] == FieldType.END)) {
                poss.add(new int[]{ x, y - 1 });
            }

            //move down
            if (y + 1 < HEIGHT && (fieldTypes[x][y + 1] == FieldType.UNCHECKED || fieldTypes[x][y + 1] == FieldType.END)) {
                poss.add(new int[]{ x, y + 1 });
            }
        }


        int[][] out = new int[poss.size()][2];
        for (int i = 0; i < poss.size(); i++) {
            out[i] = poss.get(i);
        }
        return out;
    }

    enum FieldType {
        UNCHECKED, CHECKED, USED, BLOCKED, START, END
    }

}
