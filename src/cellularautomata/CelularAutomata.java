/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cellularautomata;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 *
 * @author Thomas & Kevin
 */
public class CelularAutomata extends JFrame{
    private final String OS;    
    private final JPanel worldPanel;
    private int[][] land;
    private final int width = 1000;
    private final int height = 15;
    private int blockSize = 10;
    
    public CelularAutomata()
    {
        OS = System.getProperty("os.name").toLowerCase();
        
        this.land = new int[this.height][this.width];
        worldPanel = new JPanel() {
            @Override
            public void paint(Graphics g){
                if(land != null){
                    for(int i = 0; i < land.length; i++){
                        for(int j = 0; j < land[0].length; j++){
                            if(land[i][j] == 1){
                                g.setColor(Color.BLUE);
                            } else if(land[i][j] == 0){
                                g.setColor(Color.WHITE);
                            } else if(land[i][j] == 2){
                                g.setColor(Color.YELLOW);
                            }
                            g.fillRect(blockSize*j, blockSize*i, blockSize, blockSize);
                        }
                    }
                }
            }
        };
        
        if(OS.contains("win"))
        {
            this.setSize(1020 + 16, height * this.blockSize + 58);
        }else if(OS.contains("mac"))
        {
            this.setSize(1020, height * this.blockSize + 41);
        }
        
        worldPanel.setPreferredSize(new Dimension(width * this.blockSize, height * this.blockSize));
        JScrollPane scrollPane = new JScrollPane(worldPanel);
        worldPanel.setAutoscrolls(true);
        scrollPane.setPreferredSize(new Dimension(1000, height * this.blockSize));
        
        this.add(scrollPane);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
        
        this.generateLand();
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        CelularAutomata test = new CelularAutomata();
        test.processN(10);
        test.placeCoins();
        test.landCleanUp();
        test.repaint();
    }
    
    public void processN(int n) {
        
        //*
        for(int i = 0;i < n; i++){
                //land = process(land);
            //generateLand();    
            land = updateLand(land);
            
            try {
                    Thread.sleep(750);
            } catch (InterruptedException e) {
                    e.printStackTrace();
            }
            repaint();
        }
        //*/
    }
    
    public void placeCoins() {
        int[][] topBottom = betweenLand(land, 1);
        for(int i = 0; i < land.length - 1; i++){
            for(int j = 0; j < land[0].length -1; j++){
                if(land[i][j] == 1 && topBottom[i][j] == 0){
                    if(i < land.length - 2 && land[i+2][j] == 1){
                         land[i][j] = 2;
                    }
                }
            }
        }
    }
    
    private int[][] nextToLand(int[][] land, int tile){
        int[][] temp = new int[height][width];
        for(int i = 0; i < land.length; i++){
            for(int j = 0; j < land[0].length; j++){
                temp[i][j] = 0;
                if(j>0){
                    temp[i][j] += (land[i][j-1] == tile) ? 1 : 0; // left
                                    }
                if(j < land[0].length - 1){
                    temp[i][j] += (land[i][j+1] == tile) ? 1 : 0; // right
                }
            }
        }
        return temp;
    }
    
    private int[][] betweenLand(int[][] land, int tile){
        int[][] temp = new int[height][width];
        for(int i = 0; i < land.length; i++){
            for(int j = 0; j < land[0].length; j++){
                temp[i][j] = 0;
                if(i>0){
                    temp[i][j] += (land[i-1][j] == tile) ? 1 : 0; // top
                }
                if(i < land.length - 1){
                    temp[i][j] += (land[i+1][j] == tile) ? 1 : 0; // bottom
                }
            }
        }
        return temp;
    }
    
    private int[][] neighboorsLand(int[][] land){
        int[][] temp = new int[width][height];
        for(int i = 0; i < land.length; i++){
            for(int j = 0; j < land[0].length; j++){
                temp[i][j] = 0;
                if(j>0){
                    temp[i][j] += land[i][j-1]; // left
                    if(i>0){
                        temp[i][j] += land[i-1][j-1]; // top-left
                    }
                    if(i<land.length - 1){
                        temp[i][j] += land[i+1][j-1]; // bottom-left
                    }
                }
                if(j < land[0].length - 1){
                    temp[i][j] += land[i][j+1]; // right
                    if(i>0){
                        temp[i][j] += land[i-1][j+1]; // top-right
                    }
                    if(i<land.length - 1){
                        temp[i][j] += land[i+1][j+1]; // bottom-right
                    }
                }
                if(i>0){
                    temp[i][j] += land[i-1][j]; // top
                }
                if(i<land.length - 1){
                    temp[i][j] += land[i+1][j]; // bottom
                }
            }
        }
        return temp;
    }
    
    private int[][] updateLand(int[][] land){
        //int[][] neighboors = neighboorsLand(land);
        int[][] neighboors = nextToLand(land, 1);
        int[][] topBottom = betweenLand(land, 1);
        int[][] temp = land;
        for(int i = 0; i < land.length; i++){
            for(int j = 0; j < land[0].length; j++){
                //rule one (no neighboors become air)
                if(neighboors[i][j] == 0){
                    temp[i][j] = 0;
                } else if(neighboors[i][j] == 2){ //rule two (block needs to be at least 3)
                        temp[i][j] = 1;
                } else {
                    //*
                    if(land[i][j] == 1){
                        if(neighboors[i][j] == 1){
                            if(j > 0 && neighboors[i][j-1] == 2){
                                temp[i][j] = 1;
                            } else if(j < land[0].length-1 && neighboors[i][j+1] == 2){
                                temp[i][j] = 1;
                            } else {
                                temp[i][j] = 0;
                            }
                        }
                    }
                    //*/
                }
                
                // rule tree (when a bock has no block above and below look at block above it that one has two topbottom neighboors.
                // if it has remove the origional block)
                if(land[i][j] == 1 && topBottom[i][j] == 0){
                    if(i > 0 && topBottom[i-1][j] == 2){
                         temp[i][j] = 0;
                    }
                }
                
                // rule five (if air has 1 neighboor below with a solid neighboor then remove this first neighboor below the air)
                if(land[i][j] == 0 && topBottom[i][j] == 1){
                    if(i > land.length-1 && land[i+1][j] == 1 && topBottom[i+1][j] == 1){
                        temp[i+1][j] = 0;
                    }
                }
            }
        }
        return temp;
        
    }
    
    private int[][] landCleanUp(){
        int[][] temp = land;
        int[][] topBottom = betweenLand(land,1);
        int[][] neighboors = nextToLand(land,1);
        
        for(int i = 0; i < land.length-1; i++){
            for(int j = 0; j < land[0].length-1; j++){
                //*
                // rule foor (when air is enclosed in blocks fill the air)
                if(land[i][j] == 0 && topBottom[i][j] == 2 ){//&& ((i > 0 && topBottom[i-1][j] == 1) || (i < (land.length - 2) && topBottom[i+1][j] == 1))){
                    temp[i-1][j] = 1;
                    temp[i][j] = 1;
                    temp[i+1][j] = 1;
                }
                //*/
                
                //*
                if(land[i][j] == 1 && neighboors[i][j] == 0){
                    temp[i][j] = 0;
                }
                //*/
            }
        }
        
        return temp;
    }
    
    private void generateLand(){
        if(land != null){
            for(int i = 0; i < land.length; i++){
                for(int j = 0; j < land[0].length; j++){
                    land[i][j] = ((Math.random() + ((double)i/(land.length*1.7))) > 0.95 ? 1 : 0);
                }
            }
        }
    }
}