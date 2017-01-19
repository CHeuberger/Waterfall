package cfh.waterfall;

import static java.awt.GridBagConstraints.*;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.BitSet;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;


public class Main {
    
    private static final int MAX_BITS = 4;
    private static final int MAX_WIDTH = 129;
    private static final int MAX_LENGTH = 1024;

    private static final int CELL_SIZE = 10;

    
    public static void main(String[] args) {
        new Main();
    }


    private final JFrame frame;
    
    private final SpinnerNumberModel bitsModel;
    private final JPanel rulePanel;
    private final SpinnerNumberModel widthModel;
    private final SpinnerNumberModel lengthModel;
    private final Waterfall waterfall;
    
    private int bits = 3;
    private int size = (int) Math.pow(2, bits);
    private BitSet rule = new BitSet(MAX_BITS);
    private BitSet start = new BitSet(MAX_WIDTH);


    private Main() {
        Insets insets = new Insets(4, 4, 4, 4);
        
        bitsModel = new SpinnerNumberModel(1, 1, MAX_BITS, 1);
        JSpinner bitsSpinner = new JSpinner(bitsModel);
        bitsSpinner.addChangeListener(this::bitsChanged);
        
        rulePanel = new JPanel(new GridLayout(1, 0, 6, 4));
        
        widthModel = new SpinnerNumberModel(32, 1, MAX_WIDTH, 1);
        JSpinner widthSpinner = new JSpinner(widthModel);
        widthSpinner.addChangeListener(this::resize);
        
        lengthModel = new SpinnerNumberModel(32, 1, MAX_LENGTH, 1);
        JSpinner lengthSpinner = new JSpinner(lengthModel);
        lengthSpinner.addChangeListener(this::resize);
        
        waterfall = new Waterfall();
        JScrollPane waterfallPane = new JScrollPane(waterfall);
        waterfallPane.setBorder(new TitledBorder("Waterfall"));
        
        JPanel input = new JPanel();
        input.setLayout(new GridBagLayout());
        input.setBorder(new TitledBorder("Input"));
        input.add(new JLabel("bits:"), new GridBagConstraints(0, RELATIVE, 1, 1, 0.0, 0.0, BASELINE_LEADING, NONE, insets, 0, 0));
        input.add(bitsSpinner, new GridBagConstraints(RELATIVE, RELATIVE, REMAINDER, 1, 0.0, 0.0, BASELINE_LEADING, NONE, insets, 0, 0));
        input.add(new JLabel("rule:"), new GridBagConstraints(0, RELATIVE, 1, 1, 0.0, 0.0, BASELINE_LEADING, NONE, insets, 0, 0));
        input.add(rulePanel, new GridBagConstraints(RELATIVE, RELATIVE, REMAINDER, 1, 1.0, 0.0, LINE_START, NONE, insets, 0, 0));
        input.add(new JLabel("width:"), new GridBagConstraints(0, RELATIVE, 1, 1, 0.0, 0.0, BASELINE_LEADING, NONE, insets, 0, 0));
        input.add(widthSpinner, new GridBagConstraints(RELATIVE, RELATIVE, 1, 1, 0.0, 0.0, BASELINE_LEADING, NONE, insets, 0, 0));
        input.add(new JLabel("length:"), new GridBagConstraints(RELATIVE, RELATIVE, 1, 1, 0.0, 0.0, BASELINE_LEADING, NONE, insets, 0, 0));
        input.add(lengthSpinner, new GridBagConstraints(RELATIVE, RELATIVE, REMAINDER, 1, 0.0, 0.0, LINE_START, NONE, insets, 0, 0));
        
        frame = new JFrame("Waterfall");
        frame.setLayout(new BorderLayout());
        frame.add(input, BorderLayout.BEFORE_FIRST_LINE);
        frame.add(waterfallPane, BorderLayout.CENTER);
        
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(1020, 1000);
        frame.validate();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        
        bitsModel.setValue(bits);
    }
    
    private void bitsChanged(ChangeEvent ev) {
        bits = bitsModel.getNumber().intValue();
        size  = (int) Math.pow(2, bits);
        rulePanel.removeAll();
        for (int i = 0; i < size; i += 1) {
            rulePanel.add(new RuleButton(i));
        }
        rulePanel.revalidate();
    }
    
    private void resize(ChangeEvent ev) {
        int width = widthModel.getNumber().intValue();
        int length = lengthModel.getNumber().intValue();
        waterfall.changeSize(width, length);
    }
    
    
    @SuppressWarnings("serial")
    private class RuleButton extends JToggleButton {
        private static final int GAP = 4;
        private final int number;
        private final BitSet bitset;
        RuleButton(int number) {
            assert 0 <= number && number < size;
            this.number = number;
            this.bitset = BitSet.valueOf(new byte[] { (byte) (number & 0xFF) });
            
            setPreferredSize(new Dimension(bits*(CELL_SIZE+GAP)+1, 2*(CELL_SIZE+GAP)+1));
            setSelected(rule.get(number));
            addActionListener(this::action);
        }

        private void action(ActionEvent ev) {
            rule.set(number, isSelected());
            waterfall.repaint();
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            for (int i = 0; i < bits; i += 1) {
                int x = GAP/2 + (CELL_SIZE + GAP) * (bits - i - 1);
                int y = GAP/2;
                if (bitset.get(i)) {
                    g.fillRect(x, y, CELL_SIZE+1, CELL_SIZE+1);
                } else {
                    g.drawRect(x, y, CELL_SIZE, CELL_SIZE);
                }
            }
            int x = (getWidth() - CELL_SIZE) / 2;
            int y = GAP/2 + CELL_SIZE + GAP;
            if (isSelected()) {
                g.fillRect(x, y, CELL_SIZE+1, CELL_SIZE+1);
            } else {
                g.drawRect(x, y, CELL_SIZE, CELL_SIZE);
            }
        }
    }
    
    
    private class Waterfall extends Component {
        
        int width;
        int length;
        private boolean[][] data;
        
        Waterfall() {
            width = widthModel.getNumber().intValue();
            length = lengthModel.getNumber().intValue();
            data = new boolean[width][length];
            
            setPreferredSize(new Dimension(width*CELL_SIZE, length*CELL_SIZE));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent ev) {
                    clicked(ev);
                }
            });
        }
        
        void changeSize(int width, int length) {
            assert width > 0 : width;
            assert length > 0 : length;
            if (width != this.width || length != this.length) {
                this.width = width;
                this.length = length;
                data = new boolean[width][length];
                for (int i = 0; i < width; i += 1) {
                    data[i][0] = start.get(i);
                }
                setPreferredSize(new Dimension(width*CELL_SIZE, length*CELL_SIZE));
                revalidate();
            }
        }
        
        private void clicked(MouseEvent ev) {
            if (ev.getY() <= CELL_SIZE) {
                int x = ev.getX()/CELL_SIZE;
                if (x < width && ev.getX() != x*CELL_SIZE) {
                    data[x][0] ^= true;
                    start.set(x, data[x][0]);
                    repaint();
                }
            }
        }
        
        @Override
        public void repaint() {
            int d = (bits-1) / 2;
            for (int i = 1; i < length; i += 1) {
                for (int j = 0; j < width; j += 1) {
                    int index = 0;
                    int mask = 1;
                    for (int k = j-d+bits-1; k >= j-d; k -= 1) {
                        if (0 <= k && k < width && data[k][i-1]) {
                            index |= mask;
                        }
                        mask <<= 1;
                    }
                    data[j][i] = rule.get(index);
                }
            }
            
            super.repaint();
        }
        
        @Override
        public void paint(Graphics g) {
            for (int i = 0; i < length; i += 1) {
                int y = i*CELL_SIZE;
                for (int j = 0; j < width; j += 1) {
                    int x = j*CELL_SIZE;
                    if (data[j][i]) {
                        g.fillRect(x, y, CELL_SIZE+1, CELL_SIZE+1);
                    } else {
                        g.drawRect(x, y, CELL_SIZE, CELL_SIZE);
                    }
                }
            }
        }
    }
}
